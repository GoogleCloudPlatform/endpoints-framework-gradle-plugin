@echo on

cd github/endpoints-framework-gradle-plugin

rem skip format check, because it fails for some line ending weirdness
rem and it's anyway checked on ubuntu
call gradlew.bat check -x verifyGoogleJavaFormat
REM curl -s https://codecov.io/bash | bash

exit /b %ERRORLEVEL%
