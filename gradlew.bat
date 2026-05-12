@rem Gradle startup script for Windows
@rem

@if "%DEBUG%"=="" @echo off

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

set CLASSPATH=%~dp0gradle\wrapper\gradle-wrapper.jar

set JAVACMD=java
%JAVACMD% %DEFAULT_JVM_OPTS% %JAVA_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*