# Script to continuously check and download all PixelLab assets until complete

$baseDir = "assets\pixellab"
$maxIterations = 60
$checkInterval = 10

# Create directories
$dirs = @("rooms", "icons", "weapons", "effects", "projectiles", "ui", "misc", "characters")
foreach ($dir in $dirs) {
    New-Item -ItemType Directory -Force -Path "$baseDir\$dir" | Out-Null
}

# Room tiles
$rooms = @(
    @{id="fb276fa6-c2e2-4dc2-8318-10c15225414d"; name="bridge.png"},
    @{id="8dd2fe16-66bc-4847-9c4f-20a70fc62704"; name="weapons.png"},
    @{id="bbd965ec-8ec7-476c-87aa-9861e6511697"; name="shields.png"},
    @{id="47eed372-0c51-4920-97d9-afb6e3fd8cc6"; name="engines.png"},
    @{id="05c9d32c-c5fb-4e5b-926e-01b592771893"; name="medbay.png"},
    @{id="73c7306d-fe78-4354-92f8-6b21ab91df2a"; name="oxygen.png"},
    @{id="476fab76-de97-4fd1-900b-56ddb6ddee17"; name="sensors.png"},
    @{id="7ff19012-e3f1-4fe4-a9d6-a457e9e8161a"; name="doors.png"}
)

# Character assets
$characters = @(
    @{id="23342955-63df-49a9-b2ab-66d4a748e6c1"; name="captain"},
    @{id="301f7893-4edb-4da0-8878-ddf7a2dc9d8f"; name="engineer"},
    @{id="dbc89bcd-faad-4a5d-8ea1-43045b00d7bd"; name="medic"},
    @{id="3fcfb823-f587-4fd3-80b5-8b2c738e3938"; name="pilot"},
    @{id="8473de00-7a09-4849-86d9-54a5bb443d49"; name="soldier"},
    @{id="bae24e55-9503-4c5d-a15d-194b9fd43921"; name="scientist"}
)

Write-Host "=== PixelLab Asset Extraction ===" -ForegroundColor Cyan
Write-Host "This will check and download assets until all are complete." -ForegroundColor Yellow
Write-Host ""

$iteration = 0
while ($iteration -lt $maxIterations) {
    $iteration++
    $allDone = $true
    $downloadedThisRound = 0
    
    Write-Host "--- Iteration $iteration ---" -ForegroundColor Cyan
    
    # Download room tiles
    foreach ($room in $rooms) {
        $output = "$baseDir\rooms\$($room.name)"
        if (-not (Test-Path $output)) {
            $url = "https://api.pixellab.ai/mcp/isometric-tile/$($room.id)/download"
            Write-Host "  Room: $($room.name)..." -NoNewline
            try {
                Invoke-WebRequest -Uri $url -OutFile $output -ErrorAction Stop | Out-Null
                Write-Host " OK" -ForegroundColor Green
                $downloadedThisRound++
            } catch {
                Write-Host " FAILED" -ForegroundColor Red
                $allDone = $false
            }
        }
    }
    
    # Download and extract characters
    foreach ($char in $characters) {
        $extractPath = "$baseDir\characters\$($char.name)"
        $output = "$baseDir\characters\$($char.name).zip"
        $profilePath = Join-Path $extractPath "rotations\south.png"
        
        if (Test-Path $profilePath) {
            continue
        } elseif (Test-Path $output) {
            Write-Host "  Character: $($char.name) (extracting)..." -NoNewline
            try {
                Expand-Archive -Path $output -DestinationPath $extractPath -Force -ErrorAction Stop
                Write-Host " OK" -ForegroundColor Green
                $downloadedThisRound++
            } catch {
                Write-Host " FAILED" -ForegroundColor Red
                $allDone = $false
            }
        } else {
            $url = "https://api.pixellab.ai/mcp/characters/$($char.id)/download"
            Write-Host "  Character: $($char.name)..." -NoNewline
            try {
                Invoke-WebRequest -Uri $url -OutFile $output -ErrorAction Stop | Out-Null
                Write-Host " OK" -ForegroundColor Green
                Write-Host "    Extracting..." -NoNewline
                Expand-Archive -Path $output -DestinationPath $extractPath -Force -ErrorAction Stop
                Write-Host " OK" -ForegroundColor Green
                $downloadedThisRound++
            } catch {
                $statusCode = $_.Exception.Response.StatusCode.value__
                if ($statusCode -eq 423) {
                    Write-Host " PROCESSING" -ForegroundColor Yellow
                    $allDone = $false
                } else {
                    Write-Host " FAILED" -ForegroundColor Red
                    $allDone = $false
                }
            }
        }
    }
    
    if ($allDone) {
        Write-Host ""
        Write-Host "All assets downloaded and extracted!" -ForegroundColor Green
        break
    }
    
    if ($downloadedThisRound -eq 0) {
        Write-Host "  Waiting for assets to finish processing..." -ForegroundColor Gray
    }
    
    Write-Host "  Waiting $checkInterval seconds..." -ForegroundColor Gray
    Start-Sleep -Seconds $checkInterval
}

# Final summary
Write-Host ""
Write-Host "=== Final Status ===" -ForegroundColor Cyan
$roomCount = (Get-ChildItem -Path "$baseDir\rooms" -Filter *.png -ErrorAction SilentlyContinue).Count
$charDirs = Get-ChildItem -Path "$baseDir\characters" -Directory -ErrorAction SilentlyContinue
$charCount = 0
foreach ($dir in $charDirs) {
    if (Test-Path (Join-Path $dir.FullName "rotations\south.png")) {
        $charCount++
    }
}
$total = $roomCount + $charCount
Write-Host "Total assets ready: $total" -ForegroundColor Green
Write-Host "  - Room tiles: $roomCount/8" -ForegroundColor Green
Write-Host "  - Characters: $charCount/6" -ForegroundColor Green
