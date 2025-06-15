@echo off
echo Compilando el proyecto con Gradle...

REM Compilar el proyecto completo con Gradle
call gradlew.bat build --no-daemon

REM Crear el directorio de test si no existe
if not exist sistemaVotacion\build\classes\java\test mkdir sistemaVotacion\build\classes\java\test

REM Compilar el archivo de test usando las dependencias de Gradle
echo Compilando el archivo de test...
javac -cp "sistemaVotacion\build\classes\java\main;sistemaVotacion\build\libs\*;common\build\libs\*;build\libs\*" -d sistemaVotacion\build\classes\java\test sistemaVotacion\src\test\java\TestVoteStation.java

REM Ejecutar los tests
echo Ejecutando tests...
java -cp "sistemaVotacion\build\classes\java\main;sistemaVotacion\build\classes\java\test;sistemaVotacion\build\libs\*;common\build\libs\*;build\libs\*" test.TestVoteStation

echo Proceso completado. 