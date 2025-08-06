@echo off
echo ========================================
echo     CudaGame CI/CD Test Runner
echo ========================================
echo.

REM Check if Maven is available
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please run setup-maven-env.ps1 first
    exit /b 1
)

echo Select test option:
echo 1. Run all tests
echo 2. Run JUnit tests only
echo 3. Run TestNG tests only
echo 4. Run integration tests only
echo 5. Generate test report
echo 6. Clean and rebuild
echo.
set /p choice="Enter your choice (1-6): "

if "%choice%"=="1" (
    echo Running all tests...
    mvn clean test
) else if "%choice%"=="2" (
    echo Running JUnit tests only...
    mvn test -Pjunit-only
) else if "%choice%"=="3" (
    echo Running TestNG tests only...
    mvn test -Ptestng-only
) else if "%choice%"=="4" (
    echo Running integration tests...
    mvn verify
) else if "%choice%"=="5" (
    echo Generating Allure report...
    mvn allure:serve
) else if "%choice%"=="6" (
    echo Cleaning and rebuilding project...
    mvn clean compile
) else (
    echo Invalid choice!
    exit /b 1
)

echo.
echo ========================================
echo Tests completed!
echo ========================================
pause
