@echo on

REM Java 9 does not work with our stuff yet, force java 8
set JAVA_HOME=c:\program files\java\jdk1.8.0_152
set PATH=%JAVA_HOME%\bin;%PATH%

cd github/endpoints-framework-gradle-plugin

rem skip format check, because it fails for some line ending weirdness
rem and it's anyway checked on ubuntu
call gradlew.bat check -x verifyGoogleJavaFormat

exit /b %ERRORLEVEL%
