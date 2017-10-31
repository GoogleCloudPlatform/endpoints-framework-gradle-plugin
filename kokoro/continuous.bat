@echo on

cd github/endpoints-framework-gradle-plugin

rem skip format check, because it fails for some line ending weirdness
rem and it's anyway checked on ubuntu
call gradlew.bat check -x verifyGoogleJavaFormat

exit /b %ERRORLEVEL%
