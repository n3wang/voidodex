# Simple script to download all ready PixelLab assets

$baseDir = "assets\pixellab"
$downloaded = 0
$errors = @()

# Create directories
$dirs = @("rooms", "icons", "weapons", "effects", "projectiles", "ui", "misc", "characters")
foreach ($dir in $dirs) {
    New-Item -ItemType Directory -Force -Path "$baseDir\$dir" | Out-Null
}

Write-Host "=== Downloading Ready PixelLab Assets ===`n" -ForegroundColor Cyan

# Room tiles - all ready
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

Write-Host "Room Tiles:" -ForegroundColor Yellow
foreach ($room in $rooms) {
    $output = "$baseDir\rooms\$($room.name)"
    if (Test-Path $output) {
        Write-Host "  ✓ $($room.name) (already exists)" -ForegroundColor Green
        $downloaded++
    } else {
        $url = "https://api.pixellab.ai/mcp/isometric-tile/$($room.id)/download"
        Write-Host "  Downloading $($room.name)..." -NoNewline
        try {
            Invoke-WebRequest -Uri $url -OutFile $output -ErrorAction Stop | Out-Null
            Write-Host " ✓" -ForegroundColor Green
            $downloaded++
        } catch {
            Write-Host " ✗" -ForegroundColor Red
            $errors += "Room $($room.name): $($_.Exception.Message)"
        }
    }
}

# Character assets
$characters = @(
    @{id="23342955-63df-49a9-b2ab-66d4a748e6c1"; name="captain"},
    @{id="301f7893-4edb-4da0-8878-ddf7a2dc9d8f"; name="engineer"},
    @{id="dbc89bcd-faad-4a5d-8ea1-43045b00d7bd"; name="medic"},
    @{id="3fcfb823-f587-4fd3-80b5-8b2c738e3938"; name="pilot"},
    @{id="8473de00-7a09-4849-86d9-54a5bb443d49"; name="soldier"},
    @{id="bae24e55-9503-4c5d-a15d-194b9fd43921"; name="scientist"}
)

Write-Host "`nCharacters:" -ForegroundColor Yellow
foreach ($char in $characters) {
    $extractPath = "$baseDir\characters\$($char.name)"
    $output = "$baseDir\characters\$($char.name).zip"
    
    if (Test-Path (Join-Path $extractPath "rotations\south.png")) {
        Write-Host "  ✓ $($char.name) (already extracted)" -ForegroundColor Green
        $downloaded++
    } elseif (Test-Path $output) {
        Write-Host "  Extracting $($char.name)..." -NoNewline
        try {
            Expand-Archive -Path $output -DestinationPath $extractPath -Force -ErrorAction Stop
            Write-Host " ✓" -ForegroundColor Green
            $downloaded++
        } catch {
            Write-Host " ✗" -ForegroundColor Red
            $errors += "Extract $($char.name): $($_.Exception.Message)"
        }
    } else {
        $url = "https://api.pixellab.ai/mcp/characters/$($char.id)/download"
        Write-Host "  Downloading $($char.name)..." -NoNewline
        try {
            Invoke-WebRequest -Uri $url -OutFile $output -ErrorAction Stop | Out-Null
            Write-Host " ✓" -ForegroundColor Green
            Write-Host "    Extracting..." -NoNewline
            Expand-Archive -Path $output -DestinationPath $extractPath -Force -ErrorAction Stop
            Write-Host " ✓" -ForegroundColor Green
            $downloaded++
        } catch {
            if ($_.Exception.Response.StatusCode.value__ -eq 423) {
                Write-Host " ⏳ (still processing)" -ForegroundColor Yellow
            } else {
                Write-Host " ✗" -ForegroundColor Red
                $errors += "Character $($char.name): $($_.Exception.Message)"
            }
        }
    }
}

Write-Host "`n=== Summary ===" -ForegroundColor Cyan
Write-Host "Downloaded/Ready: $downloaded assets" -ForegroundColor Green
if ($errors.Count -gt 0) {
    Write-Host "Errors: $($errors.Count)" -ForegroundColor Red
    foreach ($error in $errors) {
        Write-Host "  - $error" -ForegroundColor Red
    }
}
Write-Host "`nDone!" -ForegroundColor Cyan
