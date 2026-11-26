@echo off
cd /d "%~dp0"
if not exist out mkdir out
javac -d out src\editor\UIEditor.java
if %errorlevel% neq 0 (
    echo Compile failed. Make sure JDK is installed and on PATH.
    pause
    exit /b 1
)
java -cp out editor.UIEditor
pause
