# Migrating your pipeline from Jenkins to Azure DevOps

Your pipeline moves over easily. The PowerShell logic stays almost the same, because it only uses SAS tokens, HTTP calls and JMeter. What changes is the wrapper around it.

## 1. What changes

| Jenkins | Azure DevOps |
|---|---|
| `Jenkinsfile` | `azure-pipelines.yml` |
| `agent any` | `pool:` (which machine runs it) |
| `parameters { string(...) }` | `parameters:` (runtime parameters) |
| `environment { ... }` | `variables:` |
| Credentials (Secret text) | Variable group with secret variables |
| `withCredentials` | Secrets must be mapped explicitly with `env:` on each step |
| `post { always { } }` | A step with `condition: succeededOrFailed()` |
| `env.BUILD_NUMBER` | `$(Build.BuildId)` |
| `BUILD_URL` | `$(System.CollectionUri)$(System.TeamProject)/_build/results?buildId=$(Build.BuildId)` |
| `timeout(...)` | `timeoutInMinutes:` on the job |
| `disableConcurrentBuilds()` | A single self-hosted agent runs one job at a time, so you get this for free |
| Build with Parameters | Run pipeline → Parameters |

## 2. Choose where it runs

Your JMeter is installed at `D:/apache-jmeter-5.6.3/...` on your PC, so the simplest option is a **self-hosted agent on that same PC**.

| | Self-hosted agent (your PC) | Microsoft-hosted agent |
|---|---|---|
| JMeter | Already installed | You'd download it in every run |
| Reaches internal apps | Yes, if you're on VPN | No, public URLs only |
| Setup | Install the agent once | None, but new orgs may need to request free parallel jobs |

I'd start with self-hosted, which matches what you have now. You can move to Microsoft-hosted later by changing the `pool` and adding a JMeter download step.

## 3. Setup, in order

**A. Create a token for the agent**
ADO → your avatar (top right) → **User settings → Personal access tokens → New Token** → Scopes: **Custom defined → Show all scopes → Agent Pools (Read & manage)** → Create → copy it.

**B. Install the agent**
1. **Project settings → Pipelines → Agent pools → Default → New agent → Windows → Download**. You need admin rights on the pool. If the button isn't there, ask a project admin.
2. Extract the zip to a short path such as `C:\agent`.
3. Open PowerShell **as administrator** in that folder and run `.\config.cmd`. This is the only place a command prompt is needed, and it's local setup, not Azure. Answer the prompts: server URL `https://dev.azure.com/<your-org>`, authentication PAT, paste the token, pool `Default`, any agent name, work folder `_work`, and **yes** to "run as a service".
4. Check **Agent pools → Default → Agents** shows your agent as **Online**.

If JMeter fails later with a permissions error on `D:\`, change the service's "Log on as" account to your own user in the Windows Services app.

**C. Create the secrets**
**Pipelines → Library → + Variable group** → name `aitestiq-secrets`. Add two variables, `SAS_TEST_SCRIPTS` and `SAS_REPORTS`, with your existing SAS token values. Click the **lock icon** next to each one to make them secret, then **Save**.

**D. Add the YAML**
Put the file below in an Azure Repos repository as `azure-pipelines.yml` (**Repos → Files → New file**). Then **Pipelines → New pipeline → Azure Repos Git → your repo → Existing Azure Pipelines YAML file → Save**.

**E. First run**
The first run stops and says the pipeline needs permission for the agent pool and variable group. Click **View → Permit** for each, and it continues.

## 4. The pipeline

```yaml
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
  - name: JMETER_BAT
    value: D:/apache-jmeter-5.6.3/apache-jmeter-5.6.3/bin/jmeter.bat

pool:
  name: Default

jobs:
  - job: nft_run
    displayName: Run NFT (JMeter)
    timeoutInMinutes: 60
    steps:
      - checkout: none

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
          $jmeterExe = $env:JMETER_BAT
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
            runId           = $env:RUN_ID
            prId            = $env:PR_ID
            commitSha       = $env:COMMIT_SHA
            status          = $overallStatus
            jenkinsBuildUrl = $env:BUILD_URL
            totalJmxFiles   = $runs.Count
            runs            = $runs
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

      # Optional: keep the raw JMeter results in ADO for debugging
      - publish: $(System.DefaultWorkingDirectory)/work/results
        artifact: jmeter-results
        condition: succeededOrFailed()
        continueOnError: true
```

## 5. What I changed in your scripts

- **Secrets are mapped by hand.** Unlike Jenkins' `withCredentials`, ADO never exposes secret variables to scripts automatically. Each step lists `env:` entries for the secrets it needs. The upload step maps `SAS_REPORTS` to `SAS_REPORTS_TOKEN` because a secret with the same name as its own environment variable can cause confusion.
- **I removed `$( ... )` from the PowerShell.** ADO replaces `$(name)` text in YAML before the script runs. Your `$($jmx.Name)` style expressions are usually left alone because the names aren't defined variables, but it's safer to avoid them, so I used plain variables and `${env:NAME}`.
- **`Write-Error` became `Write-Host` plus `exit 1`.** The ADO PowerShell task stops on any error, and this makes the messages and failure behaviour predictable.
- **The upload runs on `succeededOrFailed()`.** Your `post { always }` also ran on aborts, and this version doesn't upload when someone cancels the run.
- **File writing:** `WriteAllText` creates UTF-8 without a BOM. `Set-Content` in Windows PowerShell 5.1 writes the system's ANSI encoding, which can corrupt non-ASCII characters.

## 6. Things to know

- **`jenkinsBuildUrl` and the run ids.** The summary key is still called `jenkinsBuildUrl`, but it now holds the ADO run URL. Rename it to `buildUrl` if Pranshu's Ag NFT contract allows (agree this with him). Run ids are now `Build.BuildId`, which are large numbers shared across all pipelines in the project, so `run-8412` in blob won't be sequential.
- **Your script reruns every `.jmx` in the container each time.** That works for now, but once Ag Write adds files regularly, you'll want an `incoming/` folder that gets cleaned after each run, as in my earlier watcher idea. Otherwise old tests rerun forever.
- **Only `run-summary.json` goes to blob.** If Ag NFT later needs the raw `.jtl` or the HTML report, upload those too. The `publish` step above keeps them in the ADO run as an artifact, but that's not in your storage account.
- **Run it by hand first.** **Pipelines → your pipeline → Run pipeline**, set the parameters (or keep the defaults), and open the run log. Then check `reports/PR-manual/run-<id>/run-summary.json` in the portal.

## 7. The advantage you get from ADO

With Jenkins on your PC, Azure couldn't call it, so I suggested polling. ADO has a public REST API, so an **Azure Function** (started by Event Grid when a `.jmx` is uploaded) can start your pipeline directly. Your self-hosted agent only makes outbound connections to ADO, so nothing needs to reach your PC. The call looks like this:

```
POST https://dev.azure.com/<org>/<project>/_apis/pipelines/<pipelineId>/runs?api-version=7.1
Body: { "templateParameters": { "prId": "482", "commitSha": "a1b2c3d" } }
```

It authenticates with a PAT or a service identity. That is the natural way to trigger it from Ag Write, or from an orchestrator.

## 8. Check with your team

1. Is there an ADO project and repo where you can add the YAML and create variable groups?
2. Can you create a self-hosted agent in that organisation, or does a Microsoft-hosted agent have to be used?
3. Does Pranshu's Ag NFT want `jenkinsBuildUrl` renamed?

If you send me the first run's log (or any red step), I can fix it. When this works, I can write the Azure Function that starts the pipeline whenever a `.jmx` lands in the container.