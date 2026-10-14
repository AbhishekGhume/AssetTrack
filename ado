name: aitestiq-nft-$(Date:yyyyMMdd)$(Rev:.r)

trigger: none
pr: none

parameters:
  - name: prId
    displayName: Pull Request ID
    type: string
    default: manual
  - name: commitSha
    displayName: Source commit SHA
    type: string
    default: none

variables:
  - group: aitestiq-secrets
  - name: STORAGE_ACCOUNT
    value: aitestagentstorage

pool:
  vmImage: windows-latest

jobs:
  - job: nft_run
    displayName: Run NFT (JMeter)
    timeoutInMinutes: 60
    steps:
      - checkout: none

      # 0. Java (pre-installed on the hosted image) and JMeter (downloaded)
      - task: JavaToolInstaller@0
        displayName: Select Java 17
        inputs:
          versionSpec: '17'
          jdkArchitectureOption: 'x64'
          jdkSourceOption: 'PreInstalled'

      - powershell: |
          [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
          $version = "5.6.3"
          $root = $env:JMETER_ROOT
          New-Item -ItemType Directory -Path $root -Force | Out-Null
          $zip = Join-Path $root "jmeter.zip"
          $url = "https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-$version.zip"

          $ok = $false
          for ($i = 1; $i -le 3 -and -not $ok; $i++) {
            try {
              Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
              $ok = $true
            }
            catch {
              Write-Host "Download attempt $i failed."
              Start-Sleep -Seconds 5
            }
          }
          if (-not $ok) { Write-Host "Could not download JMeter."; exit 1 }

          Expand-Archive -Path $zip -DestinationPath $root -Force
          $jmDir = Join-Path $root "apache-jmeter-$version"
          Write-Host "##vso[task.setvariable variable=JMETER_DIR]$jmDir"
          & (Join-Path $jmDir "bin\jmeter.bat") -v
        displayName: Install JMeter
        env:
          JMETER_ROOT: $(Agent.TempDirectory)\jmeter

      # 1. Prepare
      - powershell: |
          if (Test-Path "work") { Remove-Item "work" -Recurse -Force }
          New-Item -ItemType Directory -Path "work/scripts" -Force | Out-Null
          New-Item -ItemType Directory -Path "work/results" -Force | Out-Null
          Write-Host "Working directory prepared."
        displayName: Prepare

      # 2. Download all .jmx files
      - powershell: |
          [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
          $sas     = $env:SAS_SCRIPTS.TrimStart('?')
          $base    = "https://${env:STORAGE_ACCOUNT}.blob.core.windows.net/test-scripts"
          $listUri = "${base}?restype=container&comp=list&${sas}"
          try {
            $resp  = Invoke-WebRequest -Uri $listUri -UseBasicParsing
            $text  = $resp.Content
            $xml   = [xml]($text.Substring($text.IndexOf('<')))
            $blobs = @($xml.EnumerationResults.Blobs.Blob | Where-Object { $_.Name -like '*.jmx' } | ForEach-Object { $_.Name })
            $count = $blobs.Count
            if ($count -eq 0) { throw "No .jmx files found in the test-scripts container." }
            Write-Host "JMX files found: $count"
            foreach ($blob in $blobs) {
              $fileName = [IO.Path]::GetFileName($blob)
              $encoded  = ($blob -split '/' | ForEach-Object { [Uri]::EscapeDataString($_) }) -join '/'
              Invoke-WebRequest -Uri "${base}/${encoded}?${sas}" -OutFile "work/scripts/$fileName" -UseBasicParsing
              Write-Host "Downloaded: $blob"
            }
          }
          catch {
            Write-Host "Failed to download JMX files: $($_.Exception.Message)"
            exit 1
          }
        displayName: Download JMX files
        env:
          SAS_SCRIPTS: $(SAS_TEST_SCRIPTS)

      # 3. Run JMeter for every JMX and build run-summary.json
      - powershell: |
          $jmeterExe = Join-Path $env:JMETER_DIR "bin\jmeter.bat"
          if (-not (Test-Path $jmeterExe)) { Write-Host "JMeter not found at $jmeterExe"; exit 1 }

          $runs   = @()
          $failed = $false
          $jmxFiles = Get-ChildItem "work/scripts" -Filter "*.jmx"

          foreach ($jmx in $jmxFiles) {
            $name    = $jmx.BaseName
            $jmxName = $jmx.Name
            $outDir  = "work/results/$name"
            New-Item -ItemType Directory -Path $outDir -Force | Out-Null

            Write-Host ""
            Write-Host "===== Running: $jmxName ====="

            & $jmeterExe -n -t $jmx.FullName -l "$outDir/results.jtl" -j "$outDir/jmeter.log" -e -o "$outDir/html-report"
            $exitCode = $LASTEXITCODE
            Write-Host "JMeter Exit Code: $exitCode"

            $stats     = $null
            $statsPath = "$outDir/html-report/statistics.json"
            if (Test-Path $statsPath) {
              try { $stats = Get-Content $statsPath -Raw | ConvertFrom-Json }
              catch { Write-Host "Could not read statistics.json for ${name}: $($_.Exception.Message)" }
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
              status       = $status
              exitCode     = $exitCode
              overall      = $overall
              transactions = $transactions
            }
          }

          $overallStatus = "COMPLETED"
          if ($failed) { $overallStatus = "ERROR" }

          $summary = [ordered]@{
            runId         = $env:RUN_ID
            prId          = $env:PR_ID
            commitSha     = $env:COMMIT_SHA
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
          PR_ID: ${{ parameters.prId }}
          COMMIT_SHA: ${{ parameters.commitSha }}
          RUN_ID: $(Build.BuildId)
          BUILD_URL: $(System.CollectionUri)$(System.TeamProject)/_build/results?buildId=$(Build.BuildId)

      # 4. Upload run-summary.json (also when the run step failed)
      - powershell: |
          [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
          $summaryPath = "work/run-summary.json"
          if (-not (Test-Path $summaryPath)) { Write-Host "run-summary.json not found. Nothing to upload."; exit 0 }

          $prId = $env:PR_ID
          if ([string]::IsNullOrWhiteSpace($prId)) { $prId = 'manual' }
          $sas = $env:SAS_REPORTS_TOKEN.TrimStart('?')
          $uri = "https://${env:STORAGE_ACCOUNT}.blob.core.windows.net/reports/PR-${prId}/run-${env:RUN_ID}/run-summary.json?${sas}"
          try {
            Invoke-WebRequest -Uri $uri -Method Put -InFile $summaryPath -Headers @{ 'x-ms-blob-type' = 'BlockBlob' } -ContentType 'application/json' -UseBasicParsing | Out-Null
            Write-Host "Uploaded: PR-${prId}/run-${env:RUN_ID}/run-summary.json"
          }
          catch {
            Write-Host "Failed to upload run-summary.json: $($_.Exception.Message)"
            exit 1
          }
        displayName: Upload run-summary.json
        condition: succeededOrFailed()
        env:
          PR_ID: ${{ parameters.prId }}
          RUN_ID: $(Build.BuildId)
          SAS_REPORTS_TOKEN: $(SAS_REPORTS)

      # Optional: keep raw JMeter results in the ADO run for debugging
      - publish: $(System.DefaultWorkingDirectory)/work/results
        artifact: jmeter-results
        condition: succeededOrFailed()
        continueOnError: true