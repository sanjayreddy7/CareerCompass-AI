$ErrorActionPreference = 'Stop'

$backendRoot = Split-Path -Parent $PSScriptRoot
$workspaceRoot = Split-Path -Parent $backendRoot
$envFile = Join-Path $backendRoot '.env'
$toolsRoot = Join-Path $env:LOCALAPPDATA 'CareerCompass\tools'
$mavenVersion = '3.9.11'
$mavenHome = Join-Path $toolsRoot "apache-maven-$mavenVersion"
$mavenZip = Join-Path $toolsRoot "apache-maven-$mavenVersion-bin.zip"

if (Test-Path -LiteralPath $envFile) {
    foreach ($line in Get-Content -LiteralPath $envFile) {
        if ($line -match '^\s*([^#=\s]+)\s*=\s*(.*?)\s*$') {
            $name = $Matches[1]
            $value = $Matches[2]
            if ($value.Length -ge 2 -and $value[0] -eq '"' -and $value[$value.Length - 1] -eq '"') {
                $value = $value.Substring(1, $value.Length - 2)
            }
            if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) {
                [Environment]::SetEnvironmentVariable($name, $value, 'Process')
            }
        }
    }
}

if ([string]::IsNullOrWhiteSpace($env:APP_JWT_SECRET)) {
    $secretBytes = [byte[]]::new(32)
    $random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $random.GetBytes($secretBytes)
    $random.Dispose()
    $env:APP_JWT_SECRET = [Convert]::ToBase64String($secretBytes)
}
if ([string]::IsNullOrWhiteSpace($env:APP_DEMO_DATA)) {
    $databaseUrl = $env:DB_URL
    $isLocalDatabase = [string]::IsNullOrWhiteSpace($databaseUrl) -or $databaseUrl -match '^jdbc:mysql://(localhost|127\.0\.0\.1)(:\d+)?/'
    $env:APP_DEMO_DATA = if ($isLocalDatabase) { 'true' } else { 'false' }
}

$maven = (Get-Command mvn.cmd -ErrorAction SilentlyContinue).Source
if (-not $maven) {
    New-Item -ItemType Directory -Force -Path $toolsRoot | Out-Null
    $bundledMaven = Join-Path $mavenHome 'bin\mvn.cmd'
    if (-not (Test-Path -LiteralPath $bundledMaven)) {
        if (-not (Test-Path -LiteralPath $mavenZip)) {
            & curl.exe --fail --location --retry 2 --continue-at - --output $mavenZip "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
            if ($LASTEXITCODE -ne 0) { throw 'Could not download the Apache Maven archive.' }
        }
        Expand-Archive -LiteralPath $mavenZip -DestinationPath $toolsRoot -Force
    }
    $maven = $bundledMaven
}

Write-Host 'Starting Spring Boot with MySQL on http://localhost:8080'
& $maven -f (Join-Path $workspaceRoot 'backend\pom.xml') spring-boot:run
exit $LASTEXITCODE
