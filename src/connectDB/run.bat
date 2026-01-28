@echo off
REM ======================================
REM Script chay ung dung Quan Ly Nha Hang
REM ======================================

echo.
echo [1/3] Compiling Java source files...
javac -d bin -cp "libs\*;src" -encoding UTF-8 src\GUI\*.java src\Entity\*.java src\DAO\*.java src\connectDB\*.java

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compile failed! Please check your code.
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.
echo [2/3] Checking database connection...
echo Make sure SQL Server is running and database 'QuanLyNhaHang' exists.
echo.
echo [3/3] Starting application...
echo.

java -cp "bin;libs\*" GUI.TrangChu

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application crashed!
    pause
)

echo.
echo Application closed.
pause
