@echo off

REM  modified from ant.bat
REM  May need to add some license stuff here?

if "%OS%"=="Windows_NT" @setlocal

rem %~dp0 is expanded pathname of the current script
set DEFAULT_CALLISTO_HOME=%~dp0..

if "%CALLISTO_HOME%"=="" set CALLISTO_HOME=%DEFAULT_CALLISTO_HOME%
set DEFAULT_CALLISTO_HOME=

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set CALLISTO_CMD_LINE_ARGS=%1
if ""%1""=="""" goto doneStart
shift
:setupArgs
if ""%1""=="""" goto doneStart
set CALLISTO_CMD_LINE_ARGS=%CALLISTO_CMD_LINE_ARGS% %1
shift
goto setupArgs
rem This label provides a place for the argument list loop to break out
rem and for NT handling to skip to.

:doneStart 
rem find CALLISTO_HOME if it does not exist due to either
rem an invalid value passed by the user or the %0 problem 
rem on Windows 9x
set CALLISTO_JAR=Callisto.jar
if exist "%CALLISTO_HOME%\%CALLISTO_JAR%" goto checkJava

rem check for Callisto in Program Files
if not exist "%ProgramFiles%\Callisto" goto checkSystemDrive
set CALLISTO_HOME=%ProgramFiles%\Callisto
if exist "%CALLISTO_HOME%\%CALLISTO_JAR%" goto checkJava

:checkSystemDrive
rem check for Callisto in root directory of system drive
if not exist %SystemDrive%\Callisto\%CALLISTO_JAR% goto checkCDrive
set CALLISTO_HOME=%SystemDrive%\Callisto\
goto checkJava

:checkCDrive
rem check for Callisto in C:\Callisto for Win9X users
if not exist C:\Callisto\%CALLISTO_JAR% goto noCallistoHome
set CALLISTO_HOME=C:\Callisto\
goto checkJava

:noCallistoHome
echo CALLISTO_HOME is set incorrectly or Callisto could not be located. Please set CALLISTO_HOME.
goto end

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto setClasspath

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

:setClasspath
set LOCALCLASSPATH=%CALLISTO_HOME%\Callisto.jar;%CLASSPATH%
for %%i in ("%CALLISTO_HOME%\lib\*.jar") do call "%CALLISTO_HOME%\bin\lcp.bat" %%i

set CALLISTO_OPTS=-Djava.awt.headless=true

:runCallisto
set MAIN_CLASS=org.mitre.jawb.tasks.BatchExport

"%_JAVACMD%" %CALLISTO_OPTS% -classpath "%LOCALCLASSPATH%" %MAIN_CLASS% %CALLISTO_CMD_LINE_ARGS%
goto end

:end
set _JAVACMD=
set CALLISTO_CMD_LINE_ARGS=
set LOCALCLASSPATH=
set MAIN_CLASS=

if "%OS%"=="Windows_NT" @endlocal

:mainEnd

