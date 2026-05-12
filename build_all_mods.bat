set "JDK17="-Dorg.gradle.java.home=C:\Program Files\Java\jdk-17.0.12""
set "JDK21="-Dorg.gradle.java.home=C:\Users\Dmytro\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2""
set "JDK25="-Dorg.gradle.java.home=C:\Users\Dmytro\.jdks\temurin-25""

if not exist "build" mkdir "build"
if not exist "build\jars" mkdir "build\jars"
del /q /s "build\jars"

:: fabric 1.16.5
cmd /c gradlew.bat %JDK17% build
copy "mod\1.16.5-fabric\build\libs\glintwein-1.0-SNAPSHOT.jar" "build\jars\glintwein-1.16.5-fabric.jar"

:: forge 1.16.5
cmd /d /c "cd /d mod\1.16.5-forge && gradlew.bat %JDK17% build"
copy "mod\1.16.5-forge\build\libs\1.16.5-forge.jar" "build\jars\glintwein-1.16.5-forge.jar"

:: fabric 1.21.4
cmd /d /c "cd /d mod\1.21.4-fabric && gradlew.bat %JDK21% build"
copy "mod\1.21.4-fabric\build\libs\1.21.4-fabric.jar" "build\jars\glintwein-1.21.4-fabric.jar"

:: fabric 26.1.2
cmd /d /c "cd /d mod\26.1.2-fabric && gradlew.bat %JDK25% build"
copy "mod\26.1.2-fabric\build\libs\26.1.2-fabric.jar" "build\jars\glintwein-26.1.2-fabric.jar"
