      - powershell: |
          $ProgressPreference = 'SilentlyContinue'
          [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
          $root = $env:JMETER_ROOT
          New-Item -ItemType Directory -Path $root -Force | Out-Null
          $zip = Join-Path $root "jmeter.zip"

          # 1) Fast path: your own storage account
          $sas     = $env:SAS_SCRIPTS.TrimStart('?')
          $blobUrl = "https://${env:STORAGE_ACCOUNT}.blob.core.windows.net/test-scripts/tools/jmeter.zip?${sas}"
          $ok = $false
          try {
            Invoke-WebRequest -Uri $blobUrl -OutFile $zip -UseBasicParsing -TimeoutSec 300
            $ok = $true
            Write-Host "JMeter downloaded from storage."
          }
          catch { Write-Host "Storage download failed: $($_.Exception.Message)" }

          # 2) Slow fallback: Apache archive
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