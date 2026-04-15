@echo off
setlocal

set APP_HOME=%~dp0
set GRADLE_WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if not exist "%GRADLE_WRAPPER_JAR%" (
  echo ERROR: gradle wrapper jar manquant: %GRADLE_WRAPPER_JAR%
  exit /b 1
)

if defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXE=java.exe
)

"%JAVA_EXE%" -Dorg.gradle.appname=gradlew -classpath "%GRADLE_WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain --gradle-user-home "%APP_HOME%.gradle" --no-daemon %*

endlocal

