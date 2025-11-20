# PowerShell script to download all PixelLab assets and wait for processing ones
# This script will continuously check and download until all assets are complete

$baseDir = "assets\pixellab"
$maxWaitTime = 600  # Maximum wait time in seconds (10 minutes)
$checkInterval = 10  # Check every 10 seconds
$startTime = Get-Date
$errors = @()
$downloaded = @()

# Function to download with retry
function Download-Asset {
    param($url, $outputPath, $name)
    $maxRetries = 3
    $retryCount = 0
    
    while ($retryCount -lt $maxRetries) {
        try {
            $response = Invoke-WebRequest -Uri $url -OutFile $outputPath -ErrorAction Stop
            return $true
        } catch {
            $retryCount++
            if ($retryCount -lt $maxRetries) {
                Start-Sleep -Seconds 2
            } else {
                $script:errors += "$name : $($_.Exception.Message)"
                return $false
            }
        }
    }
}

# Function to check if asset is ready
function Test-AssetReady {
    param($url)
    try {
        $response = Invoke-WebRequest -Uri $url -Method Head -ErrorAction Stop
        return $response.StatusCode -eq 200
    } catch {
        return $false
    }
}

# Create directories
$dirs = @("rooms", "icons", "weapons", "effects", "projectiles", "ui", "misc", "characters")
foreach ($dir in $dirs) {
    New-Item -ItemType Directory -Force -Path "$baseDir\$dir" | Out-Null
}

Write-Host "=== PixelLab Asset Downloader ===" -ForegroundColor Cyan
Write-Host "This script will download all assets and wait for processing ones to complete.`n" -ForegroundColor Yellow

# Room tiles
$rooms = @(
    @{id="fb276fa6-c2e2-4dc2-8318-10c15225414d"; name="bridge.png"; status="unknown"},
    @{id="8dd2fe16-66bc-4847-9c4f-20a70fc62704"; name="weapons.png"; status="unknown"},
    @{id="bbd965ec-8ec7-476c-87aa-9861e6511697"; name="shields.png"; status="unknown"},
    @{id="47eed372-0c51-4920-97d9-afb6e3fd8cc6"; name="engines.png"; status="unknown"},
    @{id="05c9d32c-c5fb-4e5b-926e-01b592771893"; name="medbay.png"; status="unknown"},
    @{id="73c7306d-fe78-4354-92f8-6b21ab91df2a"; name="oxygen.png"; status="unknown"},
    @{id="476fab76-de97-4fd1-900b-56ddb6ddee17"; name="sensors.png"; status="unknown"},
    @{id="7ff19012-e3f1-4fe4-a9d6-a457e9e8161a"; name="doors.png"; status="unknown"}
)

# Character assets
$characters = @(
    @{id="23342955-63df-49a9-b2ab-66d4a748e6c1"; name="captain"; status="unknown"},
    @{id="301f7893-4edb-4da0-8878-ddf7a2dc9d8f"; name="engineer"; status="unknown"},
    @{id="dbc89bcd-faad-4a5d-8ea1-43045b00d7bd"; name="medic"; status="unknown"},
    @{id="3fcfb823-f587-4fd3-80b5-8b2c738e3938"; name="pilot"; status="unknown"},
    @{id="8473de00-7a09-4849-86d9-54a5bb443d49"; name="soldier"; status="unknown"},
    @{id="bae24e55-9503-4c5d-a15d-194b9fd43921"; name="scientist"; status="unknown"}
)

# Download function for rooms
function Download-Rooms {
    $allDone = $true
    foreach ($room in $rooms) {
        if ($room.status -ne "downloaded") {
            $url = "https://api.pixellab.ai/mcp/isometric-tile/$($room.id)/download"
            $output = "$baseDir\rooms\$($room.name)"
            
            # Check if already downloaded
            if (Test-Path $output) {
                $room.status = "downloaded"
                continue
            }
            
            # Try to download
            if (Test-AssetReady $url) {
                Write-Host "  Downloading Room: $($room.name)..." -NoNewline
                if (Download-Asset -url $url -outputPath $output -name "Room: $($room.name)") {
                    Write-Host " ✓" -ForegroundColor Green
                    $room.status = "downloaded"
                    $script:downloaded += "Room: $($room.name)"
                } else {
                    Write-Host " ✗" -ForegroundColor Red
                    $allDone = $false
                }
            } else {
                Write-Host "  Waiting for Room: $($room.name)..." -ForegroundColor Yellow
                $allDone = $false
            }
        }
    }
    return $allDone
}

# Download function for characters
function Download-Characters {
    $allDone = $true
    foreach ($char in $characters) {
        if ($char.status -ne "downloaded") {
            $url = "https://api.pixellab.ai/mcp/characters/$($char.id)/download"
            $output = "$baseDir\characters\$($char.name).zip"
            $extractPath = "$baseDir\characters\$($char.name)"
            
            # Check if already extracted
            if (Test-Path "$extractPath\rotations\south.png") {
                $char.status = "downloaded"
                continue
            }
            
            # Try to download
            if (Test-AssetReady $url) {
                Write-Host "  Downloading Character: $($char.name)..." -NoNewline
                if (Download-Asset -url $url -outputPath $output -name "Character: $($char.name)") {
                    Write-Host " ✓" -ForegroundColor Green
                    # Extract
                    Write-Host "    Extracting..." -NoNewline
                    try {
                        Expand-Archive -Path $output -DestinationPath $extractPath -Force -ErrorAction Stop
                        Write-Host " ✓" -ForegroundColor Green
                        $char.status = "downloaded"
                        $script:downloaded += "Character: $($char.name)"
                    } catch {
                        Write-Host " ✗ Extract failed" -ForegroundColor Red
                        $script:errors += "Extract $($char.name) : $($_.Exception.Message)"
                        $allDone = $false
                    }
                } else {
                    Write-Host " ✗" -ForegroundColor Red
                    $allDone = $false
                }
            } else {
                Write-Host "  Waiting for Character: $($char.name)..." -ForegroundColor Yellow
                $allDone = $false
            }
        }
    }
    return $allDone
}

# Main download loop
$iteration = 0
while ($true) {
    $iteration++
    $elapsed = (Get-Date) - $startTime
    
    if ($elapsed.TotalSeconds -gt $maxWaitTime) {
        $waitTimeStr = "$maxWaitTime seconds"
        Write-Host "`nMaximum wait time reached ($waitTimeStr). Stopping." -ForegroundColor Yellow
        break
    }
    
    Write-Host "`n=== Iteration $iteration (Elapsed: $([math]::Round($elapsed.TotalSeconds))s) ===" -ForegroundColor Cyan
    
    $roomsDone = Download-Rooms
    $charsDone = Download-Characters
    
    if ($roomsDone -and $charsDone) {
        Write-Host "`n✓ All assets downloaded successfully!" -ForegroundColor Green
        break
    }
    
    Write-Host "`nWaiting $checkInterval seconds before next check..." -ForegroundColor Gray
    Start-Sleep -Seconds $checkInterval
}

# Summary
Write-Host "`n=== Download Summary ===" -ForegroundColor Cyan
Write-Host "Downloaded: $($downloaded.Count) assets" -ForegroundColor Green
if ($downloaded.Count -gt 0) {
    foreach ($item in $downloaded) {
        Write-Host "  ✓ $item" -ForegroundColor Green
    }
}

if ($errors.Count -gt 0) {
    Write-Host "`nErrors: $($errors.Count)" -ForegroundColor Red
    foreach ($error in $errors) {
        Write-Host "  ✗ $error" -ForegroundColor Red
    }
}

Write-Host "`nDone! Check assets\pixellab_assets.csv for asset status." -ForegroundColor Cyan
