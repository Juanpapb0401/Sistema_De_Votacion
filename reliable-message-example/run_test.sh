#!/bin/bash
echo "Compilando el proyecto con Gradle..."

# Compilar el proyecto completo con Gradle
./gradlew build --no-daemon

# Crear el directorio de test si no existe
mkdir -p sistemaVotacion/build/classes/java/test

# Compilar el archivo de test usando las dependencias de Gradle
echo "Compilando el archivo de test..."
javac -cp "sistemaVotacion/build/classes/java/main:sistemaVotacion/build/libs/*:common/build/libs/*:build/libs/*" -d sistemaVotacion/build/classes/java/test sistemaVotacion/src/test/java/TestVoteStation.java

# Ejecutar los tests
echo "Ejecutando tests..."
java -cp "sistemaVotacion/build/classes/java/main:sistemaVotacion/build/classes/java/test:sistemaVotacion/build/libs/*:common/build/libs/*:build/libs/*" test.TestVoteStation

echo "Proceso completado." 