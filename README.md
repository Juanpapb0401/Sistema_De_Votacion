# Sistema_De_Votacion

## Integrantes
- Juan Pablo Parra
- Stick Martinez
- Alejandro Mejía
- Pablo Guzman Alarcon

# Tarea implementación de patrones.

En el marco del sistema de votaciones desarrollado para la Registraduría, la empresa XYZ le ha encargado la implementación de los módulos responsables de transmitir y recibir los votos desde cada estación de votación hacia el servidor central, encargado de su consolidación. La funcionalidad a desarrollar debe garantizar, de manera estricta, que el 100% de los votos emitidos sean registrados correctamente y que ningún voto sea contado más de una vez. Esta es una funcionalidad crítica, por lo cual se espera una solución confiable, segura y coherente con principios de diseño robusto.

Para la parte de consultar puesto de votación se deben cumplir con los requisitos críticos requeridos que son Throughput y disponibilidad. Para abordar los requerimientos de alto rendimiento y disponibilidad en el caso de uso de Consulta de Mesa de Votación, se diseñó una arquitectura distribuida basada en un Broker de mensajes y múltiples instancias de Proxy Caché. Esta solución permite enrutar solicitudes de manera eficiente, almacenar en caché un rango de cédulas y garantizar una respuesta rápida y constante incluso bajo alta carga o fallos parciales.

## Ejecución del Proyecto:

Primero buildear el proyecto, dentro de la carpeta `reliable-message-example`, ejecutar: `\.gradlew build`.

Ahora, por terminal ejecutar una de estas lineas, en este orden:

1. Ejecutar Servidor Central: `java -jar server/build/libs/server.jar --Ice.Config=sistemaVotacion/src/main/resources/server.config`

2. Ejecutar Reliable Message Server: `java -jar reliableServer/build/libs/reliableServer.jar`

3. Ejecutar un puesto de votación: `java -jar sistemaVotacion/build/libs/sistemaVotacion.jar`. Si se desea se puede modificar el archivo `mesa.properties` para simular un puesto de votación con un id diferente, por defecto esta con id=1, definiendo que solo los votantes que les toque en el puesto de votación con id 1 pueden votar alli.

4. Ejecutar Broker: `cd broker-proxy` y luego `icegridregistry --Ice.Config=config.registry`

5. Ejecutar ProxyCache-1: `cd broker-proxy` y luego `icegridnode --Ice.Config=config.node`

6. Ejecutar ProxyCache-2: `cd broker-proxy` y luego `icegridnode --Ice.Config=config.node2`

7. Ejecutar un dispositivo personal, que es aquel que realiza la consulta del puesto de votación que le toca: `java -jar DispositivoPersonal/build/libs/DispositivoPersonal-all.jar`

## Ejecución de Tests

- Ejecutar tests para el caso de uso de Votar: `.\run_test.bat`

- Ejecutar tests para el caso de uso de Consultar Puesto de Votación: `.\run_test_query.bat`


## Entregables

Código fuente: Implementación completa de los módulos solicitados. (El mismo código que esta en el repositorio)

Diagrama de deployment con los módulos solicitados que cumplan con los requerimientos funcionales y no funcionales. (Ubicado en la carpeta Docs del repositorio) 

Diseño de experimentos: Documentos que describan cómo se valido que el sistema cumple con los requisitos de confiabilidad y unicidad del conteo de votos, y de la disponibilidad y el throughput para el tema de consultar puesto de votación. (Ubicado en la carpeta Docs del repositorio)

Video explicativo: Grabación en la que se exponga el funcionamiento del sistema, la estrategia utilizada y los patrones de diseño aplicados en la solución. (Link del Video en este Readme)

https://icesiedu-my.sharepoint.com/:v:/g/personal/1110362743_u_icesi_edu_co/EZsHwESx-f9AkY0Din3qo6oBsD3NKJhM9hCKoTacf709UQ?e=2wcO8v&nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJTdHJlYW1XZWJBcHAiLCJyZWZlcnJhbFZpZXciOiJTaGFyZURpYWxvZy1MaW5rIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXcifX0%3D

https://youtu.be/WIZA1ZHEiWQ




