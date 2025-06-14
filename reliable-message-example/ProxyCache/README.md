# ProxyCache - Sistema de Votación

## Descripción

El **ProxyCache** es un componente clave que implementa el patrón **Proxy Cache** en el sistema de votación. Actúa como intermediario inteligente entre los clientes y los servidores de base de datos, proporcionando:

- **Cache con TTL**: Almacena resultados temporalmente para reducir carga en BD
- **Balanceamiento de carga**: Distribuye consultas entre múltiples servidores backend
- **Alta disponibilidad**: Failover automático entre servidores
- **Métricas**: Estadísticas de cache hits/misses para monitoreo

## Arquitectura

```
Cliente (DispositivoPersonal)
    ↓
IceGrid Registry (Broker)
    ↓
ProxyCache (Puerto 90011-90012)
    ↓
Servidores Backend (Puertos 10011-10015)
    ↓
Base de Datos PostgreSQL
```

## Características del Cache

### TTL Inteligente
- **Datos de votantes**: 30 minutos (información estable)
- **Datos de votos**: 5 minutos (información más dinámica)
- **Consultas generales**: 5 minutos (por defecto)

### Gestión de Memoria
- **Capacidad máxima**: 10,000 entradas
- **Limpieza automática**: Cada minuto
- **LRU**: Remueve entradas más antiguas cuando está lleno

### Balanceamiento
- **Round-robin**: Distribución equitativa entre 5 servidores backend
- **Failover**: Intenta otros servidores si uno falla
- **Pool de conexiones**: Mantiene conexiones activas

## Configuración

### config.proxycache
```properties
ProxyCache.Endpoints=tcp -h localhost -p 90011
Ice.Default.Locator=SistemaVotacion/Locator:default -h localhost -p 4061
Ice.ThreadPool.Client.Size=10
Ice.ThreadPool.Client.SizeMax=50
```

### application.xml
```xml
<server-template id="ProxyCacheTemplate">
  <server id="ProxyCache-${index}" exe="java">
    <option>-jar</option>
    <option>../ProxyCache/build/libs/ProxyCache.jar</option>
    <adapter name="ProxyCache-${index}" endpoints="tcp -h localhost -p 9001${index}">
      <object identity="ProxyCache-${index}" type="::app::Service"/>
    </adapter>
  </server>
</server-template>
```

## Métricas y Monitoreo

El ProxyCache proporciona métricas en tiempo real:

- **Cache Size**: Número de entradas almacenadas
- **Cache Hits**: Consultas resueltas desde cache
- **Cache Misses**: Consultas que requirieron backend
- **Hit Rate**: Porcentaje de eficiencia del cache
- **Total Requests**: Número total de consultas

### Ver estadísticas:
```bash
icegridadmin --Ice.Config=config.registry -e "server ProxyCache-1 print"
```

## Beneficios de Rendimiento

### Sin Cache (Arquitectura Original)
- Cada consulta va directo a BD
- Latencia: ~50-100ms por consulta
- Throughput: ~500-1000 consultas/segundo

### Con ProxyCache
- **Cache Hit (90%+)**: Latencia ~1-5ms
- **Cache Miss**: Latencia ~50-100ms
- **Throughput estimado**: 5000-10000 consultas/segundo
- **Reducción de carga en BD**: 90%+

## Casos de Uso Ideales

1. **Consulta de puesto de votación**: Datos estables durante el día
2. **Verificación de votantes**: Información que no cambia frecuentemente
3. **Consultas repetidas**: El mismo ciudadano consulta múltiples veces

## Compilación y Despliegue

```bash
# Compilar
./gradlew ProxyCache:build

# Iniciar sistema completo
./build-and-start.sh

# Detener sistema
./stop-system.sh
```

## Testing de Carga

Para verificar el rendimiento:

```bash
# Múltiples clientes simultáneos
for i in {1..10}; do
  java -jar DispositivoPersonal/build/libs/DispositivoPersonal.jar &
done

# Monitor de métricas
watch -n 1 'icegridadmin --Ice.Config=broker-proxy/config.registry -e "server ProxyCache-1 print"'
```

## Troubleshooting

### ProxyCache no inicia
1. Verificar que IceGrid Registry esté corriendo
2. Verificar configuración de puertos (90011-90012)
3. Revisar logs: `icegridadmin -e "server ProxyCache-1 stderr"`

### Cache no funciona
1. Verificar conectividad con servidores backend (10011-10015)
2. Revisar TTL en determineTTL()
3. Verificar si los servidores backend están corriendo

### Bajo hit rate
1. Verificar que las consultas sean idénticas (parámetros iguales)
2. Ajustar TTL según el tipo de datos
3. Verificar que el cache no se esté llenando (MAX_CACHE_SIZE)

## Extensiones Futuras

1. **Cache distribuido**: Redis/Hazelcast para múltiples nodos
2. **Cache warming**: Pre-cargar datos frecuentes
3. **Políticas de invalidación**: Invalidar cache cuando datos cambian
4. **Compresión**: Comprimir datos grandes en cache
5. **Métricas avanzadas**: Prometheus/Grafana integration 