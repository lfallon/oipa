@REM ----------------------------------------------------------------------------
@REM OIPA Bootstrap Command Line Interface Batch script
@REM
@REM Optional ENV vars
@REM OIPA_BOOTSTRAP_CLI_HOME - location of Yet Another Admin System Command Line Interface installed home dir
@REM OIPA_BOOTSTRAP_CLI_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@REM OIPA_BOOTSTRAP_CLI_BATCH_PAUSE - set to 'on' to wait for a key stroke before ending
@REM OIPA_BOOTSTRAP_CLI_OPTS - parameters passed to the Java VM when running Yet Another Admin System Command Line Interface
@REM     e.g. to debug Yet Another Admin System Command Line Interface itself, use
@REM set OIPA_BOOTSTRAP_CLI_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@' in case OIPA_BOOTSTRAP_CLI_BATCH_ECHO is 'on'
@echo off
@REM enable echoing my setting OIPA_BOOTSTRAP_CLI_BATCH_ECHO to 'on'
@if "%OIPA_BOOTSTRAP_CLI_BATCH_ECHO%" == "on"  echo %OIPA_BOOTSTRAP_CLI_BATCH_ECHO%

@REM set %HOME% to equivalent of $HOME
if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

set ERROR_CODE=0

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

@REM ==== START VALIDATION ====
:chkMHome
if not "%OIPA_BOOTSTRAP_CLI_HOME%"=="" goto valMHome

if "%OS%"=="Windows_NT" SET "OIPA_BOOTSTRAP_CLI_HOME=%~dp0.."
if "%OS%"=="WINNT" SET "OIPA_BOOTSTRAP_CLI_HOME=%~dp0.."
if not "%OIPA_BOOTSTRAP_CLI_HOME%"=="" goto valMHome

echo.
echo ERROR: OIPA_BOOTSTRAP_CLI_HOME not found in your environment.
echo Please set the OIPA_BOOTSTRAP_CLI_HOME variable in your environment to match the
echo location of the OIPA Bootstrap Command Line Interface installation
echo.
goto error

:valMHome

:stripMHome
if not "_%OIPA_BOOTSTRAP_CLI_HOME:~-1%"=="_\" goto checkMBat
set "OIPA_BOOTSTRAP_CLI_HOME=%OIPA_BOOTSTRAP_CLI_HOME:~0,-1%"
goto stripMHome

:checkMBat
if exist "%OIPA_BOOTSTRAP_CLI_HOME%\bin\oipa-bootstrap.bat" goto chkJHome

echo.
echo ERROR: OIPA_BOOTSTRAP_CLI_HOME is set to an invalid directory.
echo OIPA_BOOTSTRAP_CLI_HOME = "%OIPA_BOOTSTRAP_CLI_HOME%"
echo Please set the OIPA_BOOTSTRAP_CLI_HOME variable in your environment to match the
echo location of the OIPA Bootstrap Command Line Interface installation
echo.
goto error

:chkJHome
if not "%JAVA_HOME%" == "" goto OkJHome

@REM ==== EMBEDDED JAVA_HOME ====
set JAVA_HOME=%OIPA_BOOTSTRAP_CLI_HOME%\jdk1.7

if not "%JAVA_HOME%" == "" goto OkJHome


@REM ==== FIND JAVA_HOME ====
where java > __var.tmp
set /p JAVA_HOME=<__var.tmp
set JAVA_HOME=%JAVA_HOME:bin\java.exe=%
del __var.tmp

if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo ERROR: JAVA_HOME not found in your environment.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error
@REM ==== END VALIDATION ====

:init
@REM Decide how to startup depending on the version of windows

@REM -- Windows NT with Novell Login
if "%OS%"=="WINNT" goto WinNTNovell

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

:WinNTNovell

@REM -- 4NT shell
if "%@eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set OIPA_BOOTSTRAP_CLI_CMD_LINE_ARGS=%*
goto endInit

@REM The 4NT Shell from jp software
:4NTArgs
set OIPA_BOOTSTRAP_CLI_CMD_LINE_ARGS=%$
goto endInit

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of agruments (up to the command line limit, anyway).
set OIPA_BOOTSTRAP_CLI_CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto endInit
set OIPA_BOOTSTRAP_CLI_CMD_LINE_ARGS=%OIPA_BOOTSTRAP_CLI_CMD_LINE_ARGS% %1
shift
goto Win9xApp

@REM Reaching here means variables are defined and arguments have been captured
:endInit
SET OIPA_BOOTSTRAP_CLI_JAVA_EXE="%JAVA_HOME%\bin\java.exe"

@REM -- 4NT shell
if "%@eval[2+2]" == "4" goto 4NTCWJars

@REM -- Regular WinNT shell
for %%i in ("%OIPA_BOOTSTRAP_CLI_HOME%"\boot\plexus-classworlds-*) do set CLASSWORLDS_JAR="%%i"
goto runm2

@REM The 4NT Shell from jp software
:4NTCWJars
for %%i in ("%OIPA_BOOTSTRAP_CLI_HOME%\boot\plexus-classworlds-*") do set CLASSWORLDS_JAR="%%i"
goto runm2

@REM Start OIPA Bootstrap Command Line Interface
:runm2
set CLASSPATH="%OIPA_BOOTSTRAP_CLI_HOME%"
set dir=%OIPA_BOOTSTRAP_CLI_HOME%\libs
for %%i in ("%dir%\*.jar") do call :setone "%%i"
set LAUNCHER=com.pennassurancesoftware.oipa.bootstrap.CLI
%OIPA_BOOTSTRAP_CLI_JAVA_EXE% %OIPA_BOOTSTRAP_CLI_OPTS% -classpath %CLASSPATH% "-Dyaas.sourceDir=%OIPA_BOOTSTRAP_CLI_HOME%" %LAUNCHER% %OIPA_BOOTSTRAP_CLI_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:setone
set file=%1
set file=%file:"=%
set CLASSPATH=%CLASSPATH%;"%file%"
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
set ERROR_CODE=1

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT
if "%OS%"=="WINNT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set OIPA_BOOTSTRAP_CLI_JAVA_EXE=
set OIPA_BOOTSTRAP_CLI_CMD_LINE_ARGS=
goto postExec

:endNT
@endlocal & set ERROR_CODE=%ERROR_CODE%

:postExec

@REM pause the batch file if OIPA_BOOTSTRAP_CLI_BATCH_PAUSE is set to 'on'
if "%OIPA_BOOTSTRAP_CLI_BATCH_PAUSE%" == "on" pause

if "%OIPA_BOOTSTRAP_CLI_TERMINATE_CMD%" == "on" exit %ERROR_CODE%

cmd /C exit /B %ERROR_CODE%