set "JDK17="-Dorg.gradle.java.home=C:\Program Files\Java\jdk-17.0.12""
set "JDK21="-Dorg.gradle.java.home=C:\Users\Dmytro\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2""
set "JDK25="-Dorg.gradle.java.home=C:\Users\Dmytro\.jdks\temurin-25""

set "GRADLE_COMMAND=%~1"
if "%GRADLE_COMMAND%"=="" set "GRADLE_COMMAND=build"

:: fabric 1.16.5
cmd /c gradlew.bat %JDK17% %GRADLE_COMMAND%

:: forge 1.16.5
cmd /d /c "cd /d mod\1.16.5-forge && gradlew.bat %JDK17% %GRADLE_COMMAND%"

:: fabric 1.21.4
cmd /d /c "cd /d mod\1.21.4-fabric && gradlew.bat %JDK21% %GRADLE_COMMAND%"

:: fabric 1.21.11
cmd /d /c "cd /d mod\1.21.11-fabric && gradlew.bat %JDK21% %GRADLE_COMMAND%"

:: fabric 26.1.2
cmd /d /c "cd /d mod\26.1.2-fabric && gradlew.bat %JDK25% %GRADLE_COMMAND%"
