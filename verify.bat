@echo off
setlocal
set JAVA_HOME=C:\Android\jdk17tmp\jdk-17.0.13+11
cd /d D:\puneet\Projects\chronos
call gradlew.bat test :app:assembleDebug --console=plain --no-daemon > verify.log 2>&1
echo EXITCODE=%ERRORLEVEL% >> verify.log
endlocal
