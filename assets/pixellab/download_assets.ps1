# PowerShell script to download all PixelLab assets
# Run this script to download all generated assets

$baseDir = "assets\pixellab"
$errors = @()

# Function to download with retry
function Download-Asset {
    param($url, $outputPath, $name)
    $maxRetries = 3
    $retryCount = 0
    
    while ($retryCount -lt $maxRetries) {
        try {
            Write-Host "Downloading $name..." -NoNewline
            Invoke-WebRequest -Uri $url -OutFile $outputPath -ErrorAction Stop | Out-Null
            Write-Host " ✓" -ForegroundColor Green
            return $true
        } catch {
            $retryCount++
            if ($retryCount -lt $maxRetries) {
                Write-Host " (retry $retryCount/$maxRetries)" -ForegroundColor Yellow
                Start-Sleep -Seconds 2
            } else {
                Write-Host " ✗ Failed" -ForegroundColor Red
                $errors += "$name : $($_.Exception.Message)"
                return $false
            }
        }
    }
}

# Create directories
New-Item -ItemType Directory -Force -Path "$baseDir\rooms" | Out-Null
New-Item -ItemType Directory -Force -Path "$baseDir\icons" | Out-Null
New-Item -ItemType Directory -Force -Path "$baseDir\weapons" | Out-Null
New-Item -ItemType Directory -Force -Path "$baseDir\effects" | Out-Null
New-Item -ItemType Directory -Force -Path "$baseDir\projectiles" | Out-Null
New-Item -ItemType Directory -Force -Path "$baseDir\ui" | Out-Null
New-Item -ItemType Directory -Force -Path "$baseDir\misc" | Out-Null

Write-Host "`n=== Downloading Isometric Room Tiles ===" -ForegroundColor Cyan

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

foreach ($room in $rooms) {
    $url = "https://api.pixellab.ai/mcp/isometric-tile/$($room.id)/download"
    $output = "$baseDir\rooms\$($room.name)"
    Download-Asset -url $url -outputPath $output -name "Room: $($room.name)"
}

Write-Host "`n=== Downloading Character Assets ===" -ForegroundColor Cyan

# Character assets
$characters = @(
    @{id="23342955-63df-49a9-b2ab-66d4a748e6c1"; name="captain"},
    @{id="301f7893-4edb-4da0-8878-ddf7a2dc9d8f"; name="engineer"},
    @{id="dbc89bcd-faad-4a5d-8ea1-43045b00d7bd"; name="medic"},
    @{id="3fcfb823-f587-4fd3-80b5-8b2c738e3938"; name="pilot"},
    @{id="8473de00-7a09-4849-86d9-54a5bb443d49"; name="soldier"},
    @{id="bae24e55-9503-4c5d-a15d-194b9fd43921"; name="scientist"}
)

foreach ($char in $characters) {
    $url = "https://api.pixellab.ai/mcp/characters/$($char.id)/download"
    $output = "$baseDir\characters\$($char.name).zip"
    if (Download-Asset -url $url -outputPath $output -name "Character: $($char.name)") {
        # Extract if downloaded successfully
        if (Test-Path $output) {
            $extractPath = "$baseDir\characters\$($char.name)"
            Write-Host "  Extracting $($char.name)..." -NoNewline
            try {
                Expand-Archive -Path $output -DestinationPath $extractPath -Force -ErrorAction Stop
                Write-Host " ✓" -ForegroundColor Green
            } catch {
                Write-Host " ✗ Extract failed" -ForegroundColor Red
                $errors += "Extract $($char.name) : $($_.Exception.Message)"
            }
        }
    }
}

Write-Host "`n=== Summary ===" -ForegroundColor Cyan
if ($errors.Count -eq 0) {
    Write-Host "All assets downloaded successfully!" -ForegroundColor Green
} else {
    Write-Host "Some assets failed to download:" -ForegroundColor Yellow
    foreach ($error in $errors) {
        Write-Host "  - $error" -ForegroundColor Red
    }
}

Write-Host "`nDone! Check assets\pixellab_assets.csv for asset status." -ForegroundColor Cyan
