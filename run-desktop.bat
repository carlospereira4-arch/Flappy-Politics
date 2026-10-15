@echo off
setlocal
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-desktop.ps1"
if errorlevel 1 (
    echo.
    echo Nao foi possivel iniciar o Flappy Lula. Confira a mensagem de erro acima.
    pause
)
endlocal
