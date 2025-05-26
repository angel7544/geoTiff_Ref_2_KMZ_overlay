@echo off
echo Converting Documentation to PDF...

REM Check if pandoc is installed
where pandoc >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Pandoc is not installed. Please install it from https://pandoc.org/installing.html
    exit /b 1
)

REM Check if Node.js is installed
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Node.js is not installed. Please install it from https://nodejs.org/
    exit /b 1
)

REM Run the conversion script
node convert_docs.js

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Conversion failed. Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo Output files are organized in the 'documentation_output' directory:
echo - PDF file: documentation_output\pdf\Documentation.pdf
echo - Diagrams: documentation_output\diagrams\
echo - Summary:  documentation_output\conversion_summary.txt
echo.
echo Please check the conversion_summary.txt file for details.
pause 