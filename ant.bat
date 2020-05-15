@echo off
set ANT_HOME=%~dp0.\lib\ant

if "%JAVA_HOME%" == "" goto noJavaHome

set PATH=%JAVA_HOME%\bin;%PATH%
"%JAVA_HOME%\bin\java.exe" -showversion -classpath "%ANT_HOME%\lib\ant-launcher.jar" "-Dant.home=%ANT_HOME%" org.apache.tools.ant.launch.Launcher %*
goto :EOF

:noJavaHome
echo Go to Control Panel / System / Advanced System Settings / Environment Variables and set JAVA_HOME to your Java 7 JDK
echo You will have to close this command prompt and restart the GitHub app for this to take effect
