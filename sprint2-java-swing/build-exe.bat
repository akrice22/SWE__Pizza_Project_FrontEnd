@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

if not exist build\classes mkdir build\classes
if not exist build\jar mkdir build\jar
if not exist dist mkdir dist

echo [1/4] Gathering Java source files...
dir /s /B src\*.java > build\sources.txt

echo [2/4] Compiling with javac...
javac -Xlint:all -d build\classes @build\sources.txt
if errorlevel 1 (
    echo Compilation failed.
    exit /b 1
)

echo [3/4] Creating runnable MomAndPopPizza.jar...
jar --create --file build\jar\MomAndPopPizza.jar --main-class com.momandpop.pizza.MomAndPopPizzaApp -C build\classes .
if errorlevel 1 (
    echo JAR creation failed.
    exit /b 1
)

echo [4/4] Building standalone Windows .exe installer with jpackage...
jpackage ^
  --type exe ^
  --name MomAndPopPizza ^
  --app-version 1.0.0 ^
  --vendor "SWE 3313 Group 5" ^
  --input build\jar ^
  --main-jar MomAndPopPizza.jar ^
  --main-class com.momandpop.pizza.MomAndPopPizzaApp ^
  --dest dist ^
  --win-shortcut ^
  --win-menu ^
  --win-dir-chooser

if errorlevel 1 (
    echo jpackage failed. Ensure JDK 17+ and WiX Toolset are installed on Windows.
    exit /b 1
)

echo Standalone Windows installer generated inside dist\
endlocal
