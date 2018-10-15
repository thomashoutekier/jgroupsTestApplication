@echo off
set PORT_OFFSET=0
:loop

set counter=0
:check
wmic path win32_process where "caption like 'java.exe' and CommandLine like '%%jgroups%%7777%%'" get caption | find "java.exe"
if ERRORLEVEL 1 (
echo Not running!
set /a counter=%counter%+1
if %counter% LSS 2 (
  echo counter: %counter%
  timeout /T 3 /NOBREAK
  goto check
)
set counter=0
echo Starting 
start call run.bat
) else (
echo script is running
)
timeout /T 10 /NOBREAK
goto loop


