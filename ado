name: aitestiq-nft-$(Date:yyyyMMdd)$(Rev:.r)

trigger: none
pr: none

parameters:
  - name: docId
    displayName: Cosmos document id (from the pr_runs container)
    type: string

variables:
  - group: aitestiq-secrets
  - name: STORAGE_ACCOUNT
    value: aitestagentstorage
  - name: COSMOS_ENDPOINT
    value: https://REPLACE-WITH-YOUR-ACCOUNT.documents.azure.com:443/
  - name: COSMOS_DB
    value: REPLACE-WITH-YOUR-DATABASE-NAME
  - name: COSMOS_CONTAINER
    value: pr_runs

pool:
  vmImage: windows-latest

jobs:
  - job: nft_run
    displayName: Run NFT (JMeter)
    timeoutInMinutes: 60
    steps:
      - checkout: none

      # 0. Java and JMeter
      - task: JavaToolInstaller@0
        displayName: Select Java 17
        inputs:
          versionSpec: '17'
          jdkArchitectureOption: 'x64'
          jdkSourceOption: 'PreInstalled'

      - powershell: |
          $ProgressPreference = 'SilentlyContinue'
          [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
          $root = $env:JMETER_ROOT
          New-Item -ItemType Directory -Path $root -Force | Out-Null
          $zip = Join-Path $root "jmeter.zip"

          $sas     = $env:SAS_SCRIPTS.TrimStart('?')
          $blobUrl = "https://${env:STORAGE_ACCOUNT}.blob.core.windows.net/test-scripts/tools/jmeter.zip?${sas}"
          $ok = $false
          try {
            Invoke-WebRequest -Uri $blobUrl -OutFile $zip -UseBasicParsing -TimeoutSec 300
            $ok = $true
            Write-Host "JMeter downloaded from storage."
          }
          catch { Write-Host "Storage download failed: $($_.Exception.Message)" }

          if (-not $ok) {
            Write-Host "Falling back to the Apache archive (slow)..."
            Invoke-WebRequest -Uri "https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.zip" -OutFile $zip -UseBasicParsing -TimeoutSec 600
          }

          Add-Type -AssemblyName System.IO.Compression.FileSystem
          [System.IO.Compression.ZipFile]::ExtractToDirectory($zip, (Join-Path $root "x"))

          $bat = Get-ChildItem -Path (Join-Path $root "x") -Recurse -Filter jmeter.bat | Select-Object -First 1
          if (-not $bat) { Write-Host "jmeter.bat not found after extracting."; exit 1 }
          $jmDir = Split-Path -Parent (Split-Path -Parent $bat.FullName)
          Write-Host "##vso[task.setvariable variable=JMETER_DIR]$jmDir"
          Write-Host "JMeter ready at $jmDir"
        displayName: Install JMeter
        timeoutInMinutes: 10
        env:
          JMETER_ROOT: $(Agent.TempDirectory)\jmeter
          SAS_SCRIPTS: $(SAS_TEST_SCRIPTS)

      # 1. Prepare
      - powershell: |
          if (Test-Path "work") { Remove-Item "work" -Recurse -Force }
          New-Item -ItemType Directory -Path "work/scripts" -Force | Out-Null
          New-Item -ItemType Directory -Path "work/results" -Force | Out-Null
          Write-Host "Working directory prepared."
        displayName: Prepare

      # 2. Read the Cosmos document and build the list of JMX files to run
      - powershell: |
          [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
          $ProgressPreference = 'SilentlyContinue'

          $docId = $env:DOC_ID
          if ([string]::IsNullOrWhiteSpace($docId)) { Write-Host "docId parameter is empty."; exit 1 }

          $endpoint     = $env:COSMOS_ENDPOINT.TrimEnd('/')
          $resourceLink = "dbs/" + $env:COSMOS_DB + "/colls/" + $env:COSMOS_CONTAINER
          $date         = [DateTime]::UtcNow.ToString("r")
          $payload      = "post`ndocs`n" + $resourceLink + "`n" + $date.ToLowerInvariant() + "`n`n"

          $hmac     = New-Object System.Security.Cryptography.HMACSHA256
          $hmac.Key = [Convert]::FromBase64String($env:COSMOS_KEY.Trim())
          $sig      = [Convert]::ToBase64String($hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($payload)))
          $auth     = [Uri]::EscapeDataString("type=master&ver=1.0&sig=" + $sig)

          $headers = @{
            'Authorization'                              = $auth
            'x-ms-date'                                  = $date
            'x-ms-version'                               = '2018-12-31'
            'x-ms-documentdb-isquery'                    = 'True'
            'x-ms-documentdb-query-enablecrosspartition' = 'True'
          }
          $idJson = ConvertTo-Json -InputObject $docId
          $body   = '{"query":"SELECT * FROM c WHERE c.id = @id","parameters":[{"name":"@id","value":' + $idJson + '}]}'

          try {
            $resp = Invoke-RestMethod -Uri ($endpoint + "/" + $resourceLink + "/docs") -Method Post -Headers $headers -Body $body -ContentType 'application/query+json'
          }
          catch {
            $r = $_.Exception.Response
            if ($r) {
              Write-Host ("Cosmos query failed: HTTP " + [int]$r.StatusCode)
              try { $sr = New-Object IO.StreamReader($r.GetResponseStream()); Write-Host $sr.ReadToEnd() } catch { }
            }
            else { Write-Host ("Cosmos query failed: " + $_.Exception.Message) }
            exit 1
          }

          $docs = @($resp.Documents)
          if ($docs.Count -eq 0) {
            Write-Host ("No document with id '" + $docId + "' found in " + $env:COSMOS_CONTAINER)
            exit 1
          }
          $doc = $docs[0]

          $files = @($doc.test_files | Where-Object { $_.type -eq 'jmeter' -and $_.path -like '*.jmx' -and $_.action -ne 'DELETE' })
          $count = $files.Count
          Write-Host ("JMeter files to run: " + $count)
          foreach ($f in $files) { Write-Host (" - " + $f.path + " (action: " + $f.action + ")") }

          $planJson = ConvertTo-Json -InputObject @($files) -Depth 6
          [System.IO.File]::WriteAllText((Join-Path (Get-Location) "work/plan.json"), $planJson)
        displayName: Read test plan from Cosmos
        env:
          DOC_ID: ${{ parameters.docId }}
          COSMOS_KEY: $(COSMOS_READONLY_KEY)

      # 3. Download only the listed JMX files
      - powershell: |
          [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
          $ProgressPreference = 'SilentlyContinue'

          $plan = @(Get-Content "work/plan.json" -Raw | ConvertFrom-Json | Where-Object { $_ -ne $null })
          if ($plan.Count -eq 0) { Write-Host "Nothing to download."; exit 0 }

          $sas = $env:SAS_SCRIPTS.TrimStart('?')
          $allowedPrefix = "https://" + $env:STORAGE_ACCOUNT + ".blob.core.windows.net/test-scripts/"

          foreach ($f in $plan) {
            $url = $f.blob_url
            if ([string]::IsNullOrWhiteSpace($url)) { $url = $allowedPrefix + $f.path }
            $url = $url.Split('?')[0]

            # never send our SAS token to a location we do not own
            if (-not $url.StartsWith($allowedPrefix, [StringComparison]::OrdinalIgnoreCase)) {
              Write-Host ("Refusing to download from an unexpected location: " + $url)
              exit 1
            }

            $name = [IO.Path]::GetFileName($f.path)
            try {
              Invoke-WebRequest -Uri ($url + "?" + $sas) -OutFile ("work/scripts/" + $name) -UseBasicParsing
              Write-Host ("Downloaded: " + $name)
            }
            catch {
              Write-Host ("Failed to download " + $name + ": " + $_.Exception.Message)
              exit 1
            }
          }
        displayName: Download JMX files
        env:
          SAS_SCRIPTS: $(SAS_TEST_SCRIPTS)

      # 4. Run JMeter for each listed file and build run-summary.json
      - powershell: |
          $jmeterExe = Join-Path $env:JMETER_DIR "bin\jmeter.bat"
          if (-not (Test-Path $jmeterExe)) { Write-Host ("JMeter not found at " + $jmeterExe); exit 1 }

          $plan   = @(Get-Content "work/plan.json" -Raw | ConvertFrom-Json | Where-Object { $_ -ne $null })
          $runs   = @()
          $failed = $false

          foreach ($f in $plan) {
            $jmxName = [IO.Path]::GetFileName($f.path)
            $name    = [IO.Path]::GetFileNameWithoutExtension($jmxName)
            $jmxPath = (Resolve-Path ("work/scripts/" + $jmxName)).Path
            $outDir  = "work/results/" + $name
            New-Item -ItemType Directory -Path $outDir -Force | Out-Null

            Write-Host ""
            Write-Host ("===== Running: " + $jmxName + " =====")

            $jmArgs = @('-n', '-t', $jmxPath, '-l', ($outDir + '/results.jtl'), '-j', ($outDir + '/jmeter.log'), '-e', '-o', ($outDir + '/html-report'))
            if ($f.threads)          { $jmArgs += ('-Jthreads=' + $f.threads) }
            if ($f.ramp_up_seconds)  { $jmArgs += ('-Jrampup=' + $f.ramp_up_seconds) }
            if ($f.duration_seconds) { $jmArgs += ('-Jduration=' + $f.duration_seconds) }

            & $jmeterExe @jmArgs
            $exitCode = $LASTEXITCODE
            Write-Host ("JMeter Exit Code: " + $exitCode)

            $stats     = $null
            $statsPath = $outDir + "/html-report/statistics.json"
            if (Test-Path $statsPath) {
              try { $stats = Get-Content $statsPath -Raw | ConvertFrom-Json }
              catch { Write-Host ("Could not read statistics.json for " + $name + ": " + $_.Exception.Message) }
            }

            $transactions = [ordered]@{}
            $overall = $null
            if ($null -ne $stats) {
              $overall = $stats.Total
              $stats.PSObject.Properties | Where-Object { $_.Name -ne "Total" } | ForEach-Object { $transactions[$_.Name] = $_.Value }
            }

            if ($exitCode -eq 0 -and $null -ne $stats) { $status = "COMPLETED" }
            else { $status = "ERROR"; $failed = $true }

            $runs += [ordered]@{
              jmx          = $jmxName
              action       = $f.action
              endpoint     = $f.endpoint
              method       = $f.method
              load         = [ordered]@{ threads = $f.threads; rampUpSeconds = $f.ramp_up_seconds; durationSeconds = $f.duration_seconds }
              status       = $status
              exitCode     = $exitCode
              overall      = $overall
              transactions = $transactions
            }
          }

          $overallStatus = "COMPLETED"
          if ($plan.Count -eq 0) { $overallStatus = "NO_TESTS" }
          if ($failed)           { $overallStatus = "ERROR" }

          $summary = [ordered]@{
            runId         = $env:RUN_ID
            docId         = $env:DOC_ID
            status        = $overallStatus
            buildUrl      = $env:BUILD_URL
            totalJmxFiles = $runs.Count
            runs          = $runs
          }
          $json = ConvertTo-Json -InputObject $summary -Depth 10
          [System.IO.File]::WriteAllText((Join-Path (Get-Location) "work/run-summary.json"), $json)
          Write-Host "run-summary.json created."

          if ($failed) { Write-Host "One or more JMeter runs failed."; exit 1 }
        displayName: Run JMeter
        env:
          DOC_ID: ${{ parameters.docId }}
          RUN_ID: $(Build.BuildId)
          BUILD_URL: $(System.CollectionUri)$(System.TeamProject)/_build/results?buildId=$(Build.BuildId)

      # 5. Upload run-summary.json (also when an earlier step failed)
      - powershell: |
          [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
          New-Item -ItemType Directory -Path "work" -Force | Out-Null
          $summaryPath = "work/run-summary.json"

          if (-not (Test-Path $summaryPath)) {
            $fallback = [ordered]@{
              runId         = $env:RUN_ID
              docId         = $env:DOC_ID
              status        = "ERROR"
              buildUrl      = $env:BUILD_URL
              error         = "Pipeline failed before JMeter produced a summary. See the pipeline run log."
              totalJmxFiles = 0
              runs          = @()
            }
            [System.IO.File]::WriteAllText((Join-Path (Get-Location) $summaryPath), (ConvertTo-Json -InputObject $fallback -Depth 5))
            Write-Host "No summary was produced, wrote an ERROR summary instead."
          }

          $docId = $env:DOC_ID
          if ([string]::IsNullOrWhiteSpace($docId)) { $docId = 'unknown' }
          $safeId = [Uri]::EscapeDataString($docId)
          $sas    = $env:SAS_REPORTS_TOKEN.TrimStart('?')
          $uri    = "https://" + $env:STORAGE_ACCOUNT + ".blob.core.windows.net/reports/" + $safeId + "/run-" + $env:RUN_ID + "/run-summary.json?" + $sas
          try {
            Invoke-WebRequest -Uri $uri -Method Put -InFile $summaryPath -Headers @{ 'x-ms-blob-type' = 'BlockBlob' } -ContentType 'application/json' -UseBasicParsing | Out-Null
            Write-Host ("Uploaded: " + $docId + "/run-" + $env:RUN_ID + "/run-summary.json")
          }
          catch {
            Write-Host ("Failed to upload run-summary.json: " + $_.Exception.Message)
            exit 1
          }
        displayName: Upload run-summary.json
        condition: succeededOrFailed()
        env:
          DOC_ID: ${{ parameters.docId }}
          RUN_ID: $(Build.BuildId)
          BUILD_URL: $(System.CollectionUri)$(System.TeamProject)/_build/results?buildId=$(Build.BuildId)
          SAS_REPORTS_TOKEN: $(SAS_REPORTS)

      # Optional: keep raw JMeter results in the ADO run for debugging
      - publish: $(System.DefaultWorkingDirectory)/work/results
        artifact: jmeter-results
        condition: succeededOrFailed()
        continueOnError: true