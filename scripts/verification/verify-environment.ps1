[CmdletBinding()]
param()

$ErrorActionPreference = 'Continue'

$tools = @(
    @{ Name = 'Java'; Command = 'java'; VersionArgs = @('-version'); Install = 'Install a supported JDK, then add its bin directory to PATH.' },
    @{ Name = 'Maven'; Command = 'mvn'; VersionArgs = @('-version'); Install = 'Install Apache Maven, configure JAVA_HOME, and add Maven bin to PATH.' },
    @{ Name = 'Node.js'; Command = 'node'; VersionArgs = @('--version'); Install = 'Install a supported Node.js LTS release and add it to PATH.' },
    @{ Name = 'npm'; Command = 'npm'; VersionArgs = @('--version'); Install = 'Install npm (normally bundled with Node.js) and add it to PATH.' },
    @{ Name = 'Git'; Command = 'git'; VersionArgs = @('--version'); Install = 'Install Git for Windows and ensure git is available on PATH.' },
    @{ Name = 'Docker'; Command = 'docker'; VersionArgs = @('--version'); Install = 'Install Docker Desktop or Docker Engine, start it if needed, and add docker to PATH.' }
)

$passed = 0
$failed = 0

Write-Host 'MuniAI environment verification (read-only)'
Write-Host '-------------------------------------------'

foreach ($tool in $tools) {
    $resolved = Get-Command $tool.Command -ErrorAction SilentlyContinue

    if ($null -eq $resolved) {
        $failed++
        Write-Host ("FAIL {0}" -f $tool.Name) -ForegroundColor Red
        Write-Host '  Version: not available'
        Write-Host ("  Remediation: {0}" -f $tool.Install)
        continue
    }

    try {
        $versionOutput = & $tool.Command @($tool.VersionArgs) 2>&1 | Out-String
        $outputLines = @($versionOutput -split "`r?`n" | Where-Object { $_.Trim() })
        $meaningfulLines = $outputLines | Where-Object {
            $_ -notmatch '^\s*(\+|At .+ char:|CategoryInfo|FullyQualifiedErrorId)' -and
            $_ -notmatch '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
        }
        $versionLine = $meaningfulLines |
            Where-Object { $_ -match '(?i)(version|apache maven|^\s*v?\d+\.)' } |
            Select-Object -First 1
        $version = if ($null -ne $versionLine) { $versionLine.Trim() } else { '' }

        if ([string]::IsNullOrWhiteSpace($version)) {
            $version = 'installed; version output unavailable'
        }

        $passed++
        Write-Host ("PASS {0}" -f $tool.Name) -ForegroundColor Green
        Write-Host ("  Version: {0}" -f $version)
        Write-Host '  Remediation: none required.'

        $diagnostics = $meaningfulLines | Where-Object {
            $_ -ne $versionLine -and $_ -match '(?i)(warning|error)'
        }
        foreach ($diagnostic in $diagnostics) {
            Write-Host ("  Detail: {0}" -f $diagnostic.Trim())
        }
    }
    catch {
        $failed++
        Write-Host ("FAIL {0}" -f $tool.Name) -ForegroundColor Red
        Write-Host '  Version: command found, but version detection failed'
        Write-Host ("  Remediation: Verify the installation and PATH. {0}" -f $tool.Install)
        Write-Host ("  Detail: {0}" -f $_.Exception.Message)
    }
}

# Check Ollama separately because its CLI, API, and required models are
# independent readiness signals. The first lookup intentionally uses PATH.
$ollamaCommand = Get-Command ollama -ErrorAction SilentlyContinue

# The Windows installer may register Ollama for the interactive user without
# updating the PATH inherited by an already-running shell. Resolve the standard
# per-user install location without changing PATH or the Ollama installation.
if ($null -eq $ollamaCommand -and -not [string]::IsNullOrWhiteSpace($env:LOCALAPPDATA)) {
    $ollamaCandidate = Join-Path $env:LOCALAPPDATA 'Programs\Ollama\ollama.exe'
    if (Test-Path -LiteralPath $ollamaCandidate -PathType Leaf) {
        $ollamaCommand = Get-Command $ollamaCandidate -ErrorAction SilentlyContinue
    }
}

$ollamaExecutable = $null
$installedModels = @()

if ($null -eq $ollamaCommand) {
    $failed++
    Write-Host 'FAIL Ollama CLI' -ForegroundColor Red
    Write-Host '  Version: not available'
    Write-Host '  Remediation: Ensure Ollama is installed and restart the terminal so its installation directory is available on PATH.'
}
else {
    $ollamaExecutable = $ollamaCommand.Source
    if ([string]::IsNullOrWhiteSpace($ollamaExecutable)) {
        $ollamaExecutable = $ollamaCommand.Path
    }

    try {
        # Capture the output only after command resolution. Do not assume an
        # exact vendor-specific version string; preserve non-empty output.
        $ollamaVersionOutput = & $ollamaExecutable --version 2>&1 | Out-String
        $ollamaVersion = ($ollamaVersionOutput -replace "`r?`n", ' ').Trim()

        if ($LASTEXITCODE -ne 0) {
            throw "ollama --version exited with code $LASTEXITCODE."
        }
        if ([string]::IsNullOrWhiteSpace($ollamaVersion)) {
            $ollamaVersion = 'installed; version output unavailable'
        }

        $passed++
        Write-Host 'PASS Ollama CLI' -ForegroundColor Green
        Write-Host ("  Version: {0}" -f $ollamaVersion)
        Write-Host ("  Executable: {0}" -f $ollamaExecutable)
        Write-Host '  Remediation: none required.'

        $ollamaListOutput = @(& $ollamaExecutable list 2>&1)
        if ($LASTEXITCODE -eq 0) {
            $installedModels = @(
                $ollamaListOutput |
                    Select-Object -Skip 1 |
                    ForEach-Object { (($_.ToString().Trim()) -split '\s+')[0] } |
                    Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
            )
        }
    }
    catch {
        $failed++
        Write-Host 'FAIL Ollama CLI' -ForegroundColor Red
        Write-Host '  Version: command found, but version detection failed'
        Write-Host '  Remediation: Run ollama --version manually and verify that the executable is operational.'
        Write-Host ("  Detail: {0}" -f $_.Exception.Message)
        $ollamaExecutable = $null
    }
}

try {
    $null = Invoke-RestMethod -Method Get -Uri 'http://localhost:11434/api/tags' -TimeoutSec 5 -ErrorAction Stop
    $passed++
    Write-Host 'PASS Ollama API' -ForegroundColor Green
    Write-Host '  Endpoint: http://localhost:11434/api/tags'
    Write-Host '  Remediation: none required.'
}
catch {
    $failed++
    Write-Host 'FAIL Ollama API' -ForegroundColor Red
    Write-Host '  Endpoint: http://localhost:11434/api/tags'
    Write-Host '  Remediation: Start Ollama and confirm its local API is listening on port 11434.'
    Write-Host ("  Detail: {0}" -f $_.Exception.Message)
}

$requiredModels = @(
    @{ ResultName = 'Chat model'; ModelName = 'llama3.2:3b' },
    @{ ResultName = 'Embedding model'; ModelName = 'nomic-embed-text:latest' }
)

foreach ($requiredModel in $requiredModels) {
    if ($installedModels -contains $requiredModel.ModelName) {
        $passed++
        Write-Host ("PASS {0}" -f $requiredModel.ResultName) -ForegroundColor Green
        Write-Host ("  Model: {0}" -f $requiredModel.ModelName)
        Write-Host '  Remediation: none required.'
    }
    else {
        $failed++
        Write-Host ("FAIL {0}" -f $requiredModel.ResultName) -ForegroundColor Red
        Write-Host ("  Model: {0}" -f $requiredModel.ModelName)
        if ($null -eq $ollamaExecutable) {
            Write-Host '  Remediation: Restore Ollama CLI access, then verify the installed model list.'
        }
        else {
            Write-Host ("  Remediation: The required model is not listed. Review it with: ollama list")
        }
    }
}

Write-Host '-------------------------------------------'
Write-Host ("Summary: {0} PASS, {1} FAIL" -f $passed, $failed)

if ($failed -gt 0) {
    exit 1
}

exit 0
