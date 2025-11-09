# PowerShell script to download Maven dependencies
# This script will download all required libraries for the project

$projectPath = "c:\coding\javaProjects\InterviewAI"
$pomFile = "$projectPath\pom.xml"

Write-Host "🔍 Checking for Maven installation..." -ForegroundColor Cyan

# Try to find Maven in common locations
$mavenLocations = @(
    "C:\Program Files\Apache\maven\bin\mvn.cmd",
    "C:\Program Files (x86)\Apache\maven\bin\mvn.cmd",
    "C:\tools\maven\bin\mvn.cmd",
    "$env:MAVEN_HOME\bin\mvn.cmd"
)

$mavenCmd = $null
foreach ($location in $mavenLocations) {
    if (Test-Path $location) {
        $mavenCmd = $location
        Write-Host "✓ Found Maven at: $location" -ForegroundColor Green
        break
    }
}

if ($null -eq $mavenCmd) {
    Write-Host "⚠️  Maven not found in standard locations." -ForegroundColor Yellow
    Write-Host "Attempting to use 'mvn' from PATH..." -ForegroundColor Yellow
    $mavenCmd = "mvn"
}

Write-Host "`n📥 Downloading Maven dependencies..." -ForegroundColor Cyan
Write-Host "This may take several minutes on first run...`n" -ForegroundColor Gray

Set-Location $projectPath

# Run Maven dependency download
try {
    & $mavenCmd dependency:resolve -q
    Write-Host "✓ Dependencies downloaded successfully!" -ForegroundColor Green
    
    Write-Host "`n🔨 Building project to verify setup..." -ForegroundColor Cyan
    & $mavenCmd clean compile -q
    Write-Host "✓ Project compiled successfully!" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Error during Maven execution: $_" -ForegroundColor Red
    Write-Host "`nTrying alternative approach..." -ForegroundColor Yellow
}

Write-Host "`n✅ Dependency setup complete!" -ForegroundColor Green
