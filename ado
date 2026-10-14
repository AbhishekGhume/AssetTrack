      - powershell: |
          [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
          $raw = $env:SAS_SCRIPTS
          if ([string]::IsNullOrWhiteSpace($raw)) { Write-Host "TOKEN IS EMPTY"; exit 0 }
          if ($raw.StartsWith('$' + '(')) { Write-Host "VARIABLE NOT SUBSTITUTED (name mismatch or group not linked)"; exit 0 }
          if ($raw.StartsWith('http')) { Write-Host "You pasted a full URL. Use only the token that starts with sv=" }
          $sas = $raw.TrimStart('?')
          Write-Host ("Token length: " + $sas.Length)
          foreach ($pair in $sas.Split('&')) {
            $k = $pair.Split('=')[0]
            if (@('sv','sp','sr','se','st','spr','sip','srt','ss') -contains $k) { Write-Host $pair }
          }
          try { Write-Host ("Agent IP: " + (Invoke-RestMethod -Uri 'https://api.ipify.org' -TimeoutSec 15)) } catch { }

          $acct = $env:STORAGE_ACCOUNT
          $tests = @(
            @{ name = 'container list'; method = 'Get';  url = "https://${acct}.blob.core.windows.net/test-scripts?restype=container&comp=list&maxresults=5&${sas}" },
            @{ name = 'tools/jmeter.zip'; method = 'Head'; url = "https://${acct}.blob.core.windows.net/test-scripts/tools/jmeter.zip?${sas}" }
          )
          foreach ($t in $tests) {
            try {
              $r = Invoke-WebRequest -Uri $t.url -Method $t.method -UseBasicParsing -TimeoutSec 60
              Write-Host ("OK   " + $t.name + " -> HTTP " + [int]$r.StatusCode)
            }
            catch {
              $resp = $_.Exception.Response
              if ($resp) {
                Write-Host ("FAIL " + $t.name + " -> HTTP " + [int]$resp.StatusCode + "  error-code: " + $resp.Headers['x-ms-error-code'])
                try { $reader = New-Object IO.StreamReader($resp.GetResponseStream()); Write-Host $reader.ReadToEnd() } catch { }
              }
              else { Write-Host ("FAIL " + $t.name + " -> " + $_.Exception.Message) }
            }
          }
        displayName: Check SAS (temporary)
        env:
          SAS_SCRIPTS: $(SAS_TEST_SCRIPTS)