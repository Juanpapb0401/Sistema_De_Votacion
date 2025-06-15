@echo off

echo Compilando el proyecto (incluye DispositivoPersonal)...
call gradlew.bat build --no-daemon

REM Directorio de clases de test
if not exist DispositivoPersonal\build\classes\java\test mkdir DispositivoPersonal\build\classes\java\test

echo Compilando los archivos de test de consulta...
javac -cp "DispositivoPersonal\bin\main;DispositivoPersonal\build\classes\java\main;DispositivoPersonal\build\libs\*;build\libs\*" -d DispositivoPersonal\build\classes\java\test DispositivoPersonal\src\test\java\test\*.java

echo Ejecutando tests de consulta...
java -cp "DispositivoPersonal\bin\main;DispositivoPersonal\build\classes\java\main;DispositivoPersonal\build\classes\java\test;DispositivoPersonal\build\libs\*;build\libs\*" test.TestQueryStation

echo Pruebas de consulta completadas. 