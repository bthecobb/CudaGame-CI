# Script to set Maven environment variables permanently
# Run this script as Administrator for system-wide changes

$mavenHome = "C:\Users\Brandon\tools\apache-maven-3.9.5"

Write-Host "Setting up Maven environment variables..." -ForegroundColor Green

# Set MAVEN_HOME for current user
[System.Environment]::SetEnvironmentVariable("MAVEN_HOME", $mavenHome, [System.EnvironmentVariableTarget]::User)

# Get current PATH for user
$currentPath = [System.Environment]::GetEnvironmentVariable("PATH", [System.EnvironmentVariableTarget]::User)

# Add Maven bin to PATH if not already present
$mavenBin = "$mavenHome\bin"
if ($currentPath -notlike "*$mavenBin*") {
    $newPath = "$mavenBin;$currentPath"
    [System.Environment]::SetEnvironmentVariable("PATH", $newPath, [System.EnvironmentVariableTarget]::User)
    Write-Host "Added Maven to PATH" -ForegroundColor Green
} else {
    Write-Host "Maven already in PATH" -ForegroundColor Yellow
}

# Set JAVA_HOME if not set (Maven needs this)
$javaHome = [System.Environment]::GetEnvironmentVariable("JAVA_HOME", [System.EnvironmentVariableTarget]::User)
if (-not $javaHome) {
    # Try to detect Java installation
    $javaPath = "C:\Program Files\Java\jdk-21"
    if (Test-Path $javaPath) {
        [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaPath, [System.EnvironmentVariableTarget]::User)
        Write-Host "Set JAVA_HOME to $javaPath" -ForegroundColor Green
    } else {
        Write-Host "Warning: Could not detect Java installation. Please set JAVA_HOME manually." -ForegroundColor Yellow
    }
}

Write-Host "`nEnvironment variables set successfully!" -ForegroundColor Green
Write-Host "Please restart your terminal or run the following command to use Maven immediately:" -ForegroundColor Cyan
Write-Host '  $env:MAVEN_HOME = "C:\Users\Brandon\tools\apache-maven-3.9.5"' -ForegroundColor White
Write-Host '  $env:PATH = "$env:MAVEN_HOME\bin;" + $env:PATH' -ForegroundColor White
Write-Host "`nVerify installation with: mvn -version" -ForegroundColor Cyan
