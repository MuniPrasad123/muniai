[CmdletBinding()]
param(
    [switch]$RestartAppProcesses,
    [switch]$CheckOnly,
    [ValidateRange(30, 600)]
    [int]$StartupTimeoutSeconds = 120
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"
$script:RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$script:RuntimeDirectory = Join-Path $script:RepoRoot "local-data\runtime"
$script:Statuses = [System.Collections.Generic.List[object]]::new()

function Write-Step([string]$Message) {
    Write-Host "`n==> $Message" -ForegroundColor Cyan
}

function Add-Status([string]$Component, [string]$State, [string]$Details) {
    $script:Statuses.Add([pscustomobject]@{
        Component = $Component
        State = $State
        Details = $Details
    })
}

function Test-HttpEndpoint([string]$Uri, [int]$TimeoutSeconds = 3) {
    try {
        $response = Invoke-WebRequest -Uri $Uri -UseBasicParsing -TimeoutSec $TimeoutSeconds
        return $response.StatusCode -ge 200 -and $response.StatusCode -lt 300
    }
    catch {
        return $false
    }
}

function Wait-ForEndpoint([string]$Name, [string]$Uri) {
    $deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
    do {
        if (Test-HttpEndpoint -Uri $Uri) { return }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)
    throw "$Name did not become healthy within $StartupTimeoutSeconds seconds. Expected: $Uri"
}

function Get-ListeningProcess([int]$Port) {
    $connection = Get-NetTCPConnection -LocalAddress "127.0.0.1" -LocalPort $Port `
        -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $connection) {
        $connection = Get-NetTCPConnection -LocalPort $Port -State Listen `
            -ErrorAction SilentlyContinue | Select-Object -First 1
    }
    if (-not $connection) { return $null }
    return Get-Process -Id $connection.OwningProcess -ErrorAction SilentlyContinue
}

function Stop-ExpectedListener(
    [int]$Port,
    [string[]]$ExpectedProcessNames,
    [string]$Component
) {
    $process = Get-ListeningProcess -Port $Port
    if (-not $process) { return }
    if ($process.ProcessName -notin $ExpectedProcessNames) {
        throw "Port $Port is owned by unexpected process '$($process.ProcessName)' (PID $($process.Id)). Refusing to stop it."
    }
    Write-Host "Stopping $Component process $($process.ProcessName) (PID $($process.Id))..."
    $processId = $process.Id
    try {
        Stop-Process -Id $processId
    }
    catch {
        & taskkill.exe /PID $processId /T /F | Out-Host
        if ($LASTEXITCODE -ne 0) {
            throw "Could not stop $Component process $processId. $($_.Exception.Message)"
        }
    }
    Wait-Process -Id $processId -Timeout 15 -ErrorAction SilentlyContinue
    if (Get-Process -Id $processId -ErrorAction SilentlyContinue) {
        throw "$Component process $processId did not stop within 15 seconds."
    }
}

function Get-Executable([string]$Name, [string[]]$Candidates = @()) {
    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    foreach ($candidate in $Candidates) {
        if ($candidate -and (Test-Path -LiteralPath $candidate)) { return $candidate }
    }
    throw "$Name is not installed or is not available on PATH."
}

function Start-LoggedProcess(
    [string]$FilePath,
    [string[]]$ArgumentList,
    [string]$WorkingDirectory,
    [string]$LogName
) {
    New-Item -ItemType Directory -Path $script:RuntimeDirectory -Force | Out-Null
    Start-Process -FilePath $FilePath -ArgumentList $ArgumentList `
        -WorkingDirectory $WorkingDirectory -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $script:RuntimeDirectory "$LogName.stdout.log") `
        -RedirectStandardError (Join-Path $script:RuntimeDirectory "$LogName.stderr.log") | Out-Null
}

function Ensure-PostgreSql {
    Write-Step "Checking PostgreSQL"
    $services = @(Get-Service -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -like "postgresql*" })
    if ($services.Count -eq 0) {
        throw "No PostgreSQL Windows service was found. Configure PostgreSQL once, then rerun this script."
    }
    foreach ($service in $services) {
        if ($service.Status -ne "Running") {
            if ($CheckOnly) { throw "PostgreSQL service '$($service.Name)' is stopped." }
            try {
                Start-Service -Name $service.Name
                $service.WaitForStatus("Running", [TimeSpan]::FromSeconds(30))
            }
            catch {
                throw "Could not start '$($service.Name)'. Run this script as Administrator. $($_.Exception.Message)"
            }
        }
    }
    Add-Status "PostgreSQL" "READY" (($services.Name) -join ", ")
}

function Ensure-DockerAndQdrant {
    Write-Step "Checking Docker and Qdrant"
    $docker = Get-Executable "docker.exe"
    $dockerReady = $false
    try {
        & $docker version --format "{{.Server.Version}}" 2>$null | Out-Null
        $dockerReady = $LASTEXITCODE -eq 0
    }
    catch { $dockerReady = $false }

    if (-not $dockerReady) {
        if ($CheckOnly) { throw "Docker Desktop is not running." }
        $dockerDesktop = Join-Path $env:ProgramFiles "Docker\Docker\Docker Desktop.exe"
        if (-not (Test-Path -LiteralPath $dockerDesktop)) {
            throw "Docker Desktop is not running and its executable was not found."
        }
        Start-Process -FilePath $dockerDesktop -ArgumentList "--minimized" -WindowStyle Hidden
        $deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
        do {
            Start-Sleep -Seconds 3
            & $docker version --format "{{.Server.Version}}" 2>$null | Out-Null
            $dockerReady = $LASTEXITCODE -eq 0
        } while (-not $dockerReady -and (Get-Date) -lt $deadline)
        if (-not $dockerReady) {
            throw "Docker Desktop did not become ready within $StartupTimeoutSeconds seconds."
        }
    }

    if (-not (Test-HttpEndpoint "http://127.0.0.1:6333/readyz")) {
        if ($CheckOnly) { throw "Qdrant is not ready." }
        $composeFile = Join-Path $script:RepoRoot "infrastructure\qdrant-compose.yml"
        & $docker compose -f $composeFile up -d --pull never
        if ($LASTEXITCODE -ne 0) {
            throw "Qdrant could not start. Its existing qdrant/qdrant:v1.15.4 image must be available locally."
        }
        Wait-ForEndpoint "Qdrant" "http://127.0.0.1:6333/readyz"
    }
    Add-Status "Qdrant" "READY" "http://127.0.0.1:6333"
}

function Ensure-Ollama {
    Write-Step "Checking Ollama"
    if ($RestartAppProcesses -and -not $CheckOnly) {
        Stop-ExpectedListener 11434 @("ollama") "Ollama"
    }
    if (-not (Test-HttpEndpoint "http://127.0.0.1:11434/api/tags")) {
        if ($CheckOnly) { throw "Ollama is not ready." }
        $ollama = Get-Executable "ollama.exe" @(
            (Join-Path $env:LOCALAPPDATA "Programs\Ollama\ollama.exe")
        )
        Start-LoggedProcess $ollama @("serve") $script:RepoRoot "ollama"
        Wait-ForEndpoint "Ollama" "http://127.0.0.1:11434/api/tags"
    }

    $models = (Invoke-RestMethod -Uri "http://127.0.0.1:11434/api/tags" -TimeoutSec 5).models.name
    $requiredModels = @("llama3.2:3b", "nomic-embed-text:latest")
    $missingModels = @($requiredModels | Where-Object { $_ -notin $models })
    if ($missingModels.Count -gt 0) {
        throw "Ollama is running, but required model(s) are missing: $($missingModels -join ', '). No models were downloaded."
    }
    Add-Status "Ollama" "READY" ($requiredModels -join ", ")
}

function Test-BackendBuildNeeded([string]$JarPath) {
    if (-not (Test-Path -LiteralPath $JarPath)) { return $true }
    $jarTime = (Get-Item -LiteralPath $JarPath).LastWriteTimeUtc
    $backendDirectory = Join-Path $script:RepoRoot "backend\muniai-api"
    $inputs = @(Get-ChildItem (Join-Path $backendDirectory "src\main") -File -Recurse)
    $inputs += Get-Item (Join-Path $backendDirectory "pom.xml")
    return $null -ne ($inputs | Where-Object {
        $_.LastWriteTimeUtc -gt $jarTime
    } | Select-Object -First 1)
}

function Ensure-Backend {
    Write-Step "Checking backend"
    $backendDirectory = Join-Path $script:RepoRoot "backend\muniai-api"
    $jar = Join-Path $backendDirectory "target\muniai-api-0.0.1-SNAPSHOT.jar"
    if ($RestartAppProcesses -and -not $CheckOnly) {
        Stop-ExpectedListener 8080 @("java", "javaw") "backend"
    }
    if (Test-HttpEndpoint "http://127.0.0.1:8080/actuator/health") {
        Add-Status "Backend" "READY" "http://127.0.0.1:8080"
        return
    }
    if ($CheckOnly) { throw "Backend is not ready." }

    if (Test-BackendBuildNeeded $jar) {
        Write-Host "Building the backend from local Maven dependencies..."
        $maven = Get-Executable "mvn.cmd"
        & $maven -o -DskipTests package -f (Join-Path $backendDirectory "pom.xml")
        if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $jar)) {
            throw "Offline backend build failed. Run 'mvn -DskipTests package' once while online, then rerun."
        }
    }

    $java = Get-Executable "java.exe"
    Start-LoggedProcess $java @("-jar", $jar) $backendDirectory "backend"
    Wait-ForEndpoint "Backend" "http://127.0.0.1:8080/actuator/health"
    Add-Status "Backend" "READY" "http://127.0.0.1:8080"
}

function Ensure-Frontend {
    Write-Step "Checking frontend"
    $frontendDirectory = Join-Path $script:RepoRoot "frontend\muniai-web"
    if ($RestartAppProcesses -and -not $CheckOnly) {
        Stop-ExpectedListener 5173 @("node") "frontend"
    }
    if (Test-HttpEndpoint "http://127.0.0.1:5173") {
        Add-Status "Frontend" "READY" "http://127.0.0.1:5173"
        return
    }
    if ($CheckOnly) { throw "Frontend is not ready." }
    if (-not (Test-Path -LiteralPath (Join-Path $frontendDirectory "node_modules"))) {
        throw "Frontend dependencies are missing. Run 'npm install' once in '$frontendDirectory'."
    }
    $npm = Get-Executable "npm.cmd"
    Start-LoggedProcess $npm @("run", "dev") $frontendDirectory "frontend"
    Wait-ForEndpoint "Frontend" "http://127.0.0.1:5173"
    Add-Status "Frontend" "READY" "http://127.0.0.1:5173"
}

$originalPassword = [Environment]::GetEnvironmentVariable("MUNIAI_DB_PASSWORD", "Process")
$passwordWasTemporarilySet = $false
try {
    if (-not $CheckOnly -and [string]::IsNullOrWhiteSpace($originalPassword)) {
        $securePassword = Read-Host "PostgreSQL password for the 'muniai' user" -AsSecureString
        $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        try {
            $plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
            [Environment]::SetEnvironmentVariable("MUNIAI_DB_PASSWORD", $plainPassword, "Process")
            $passwordWasTemporarilySet = $true
        }
        finally {
            if ($passwordPointer -ne [IntPtr]::Zero) {
                [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
            }
            $plainPassword = $null
        }
    }

    Ensure-PostgreSql
    Ensure-DockerAndQdrant
    Ensure-Ollama
    Ensure-Backend
    Ensure-Frontend

    Write-Host "`nMuniAI is ready." -ForegroundColor Green
    $script:Statuses | Format-Table -AutoSize
    Write-Host "Open http://127.0.0.1:5173" -ForegroundColor Green
    Write-Host "Logs: $script:RuntimeDirectory"
}
catch {
    Write-Host "`nMuniAI startup failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($script:Statuses.Count -gt 0) {
        $script:Statuses | Format-Table -AutoSize
    }
    Write-Host "Logs (if created): $script:RuntimeDirectory"
    exit 1
}
finally {
    if ($passwordWasTemporarilySet) {
        [Environment]::SetEnvironmentVariable("MUNIAI_DB_PASSWORD", $null, "Process")
    }
}
