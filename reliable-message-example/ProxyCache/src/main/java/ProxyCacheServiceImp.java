import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.ObjectPrx;

import app.Service;
import app.ServicePrx;

/**
 * Implementación del Proxy Cache que actúa como intermediario
 * entre los clientes y los servidores de base de datos.
 */
public class ProxyCacheServiceImp implements Service {
    
    private static final Logger logger = Logger.getLogger(ProxyCacheServiceImp.class.getName());
    
    // Cache principal con LRU automático
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    // Configuración del cache
    private static final int MAX_CACHE_SIZE = 10000;
    private static final long DEFAULT_TTL_MINUTES = 5; // 5 minutos para datos de votación
    private static final long VOTER_DATA_TTL_MINUTES = 30; // 30 minutos para datos de votantes (más estables)
    
    // Pool de conexiones a servidores backend
    private final List<ServicePrx> backendServers = new ArrayList<>();
    private final Random random = new Random();
    
    // Métricas del cache
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    
    // Executor para limpieza periódica del cache
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    public ProxyCacheServiceImp(Communicator communicator) {
        try {
            initializeBackendConnections(communicator);
        } catch (Exception e) {
            logger.warning("No se pudieron inicializar las conexiones backend: " + e.getMessage());
            logger.info("ProxyCache iniciará sin conexiones backend. Se intentará conectar dinámicamente.");
        }
        startCacheCleanupTask();
        logger.info("ProxyCache inicializado con " + backendServers.size() + " servidores backend");
    }
    
    /**
     * Inicializa las conexiones a los servidores backend
     */
    private void initializeBackendConnections(Communicator communicator) {
        try {
            // Conectar a múltiples instancias del servidor backend
            for (int i = 1; i <= 5; i++) {
                String proxyString = "Service-" + i + ":default -h localhost -p 1001" + i;
                ObjectPrx base = communicator.stringToProxy(proxyString);
                ServicePrx backendService = ServicePrx.checkedCast(base);
                
                if (backendService != null) {
                    backendServers.add(backendService);
                    logger.info("Conectado al servidor backend: Service-" + i);
                } else {
                    logger.warning("No se pudo conectar al servidor backend: Service-" + i);
                }
            }
            
            if (backendServers.isEmpty()) {
                throw new RuntimeException("No se pudo conectar a ningún servidor backend");
            }
            
        } catch (Exception e) {
            logger.severe("Error inicializando conexiones backend: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Inicia la tarea de limpieza periódica del cache
     */
    private void startCacheCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanExpiredEntries, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * Limpia las entradas expiradas del cache
     */
    private void cleanExpiredEntries() {
        int initialSize = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int removed = initialSize - cache.size();
        
        if (removed > 0) {
            logger.info(String.format("Cache cleanup: removidas %d entradas expiradas, tamaño actual: %d", 
                    removed, cache.size()));
        }
        
        // Si el cache está muy lleno, remover las entradas más antiguas
        if (cache.size() > MAX_CACHE_SIZE) {
            removeOldestEntries();
        }
    }
    
    /**
     * Remueve las entradas más antiguas cuando el cache está lleno
     */
    private void removeOldestEntries() {
        int targetSize = (int) (MAX_CACHE_SIZE * 0.8); // Reducir al 80%
        
        List<Map.Entry<String, CacheEntry>> entries = new ArrayList<>(cache.entrySet());
        entries.sort(Comparator.comparing(e -> e.getValue().getTimestamp()));
        
        for (int i = 0; i < entries.size() - targetSize; i++) {
            cache.remove(entries.get(i).getKey());
        }
        
        logger.info(String.format("Cache LRU cleanup: reducido de %d a %d entradas", 
                entries.size(), cache.size()));
    }
    
    @Override
    public void print(Current current) {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        double hitRate = (hits + misses > 0) ? (double) hits / (hits + misses) * 100 : 0;
        
        String stats = String.format(
            "ProxyCache Stats - Tamaño: %d, Hits: %d, Misses: %d, Hit Rate: %.2f%%, Total Requests: %d",
            cache.size(), hits, misses, hitRate, totalRequests.get()
        );
        
        System.out.println(stats);
        logger.info(stats);
    }
    
    @Override
    public String[] consultarBD(String sqlQuery, String[] params, Current current) {
        totalRequests.incrementAndGet();
        
        // Generar clave del cache basada en la query y parámetros
        String cacheKey = generateCacheKey(sqlQuery, params);
        
        // Intentar obtener del cache primero
        CacheEntry entry = cache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            cacheHits.incrementAndGet();
            List<String> data = entry.getData();
            logger.info(String.format("Cache HIT para query: %s (edad: %ds)", 
                    sqlQuery.substring(0, Math.min(50, sqlQuery.length())), entry.getAge()));
            return data.toArray(new String[0]);
        }
        
        // Cache miss - consultar servidor backend
        cacheMisses.incrementAndGet();
        logger.info(String.format("Cache MISS para query: %s", 
                sqlQuery.substring(0, Math.min(50, sqlQuery.length()))));
        
        try {
            // Seleccionar servidor backend (round-robin simple)
            ServicePrx backendService = selectBackendServer();
            String[] result = backendService.consultarBD(sqlQuery, params);
            
            // Almacenar en cache con TTL apropiado
            long ttl = determineTTL(sqlQuery);
            cache.put(cacheKey, new CacheEntry(Arrays.asList(result), ttl));
            
            logger.info(String.format("Resultado almacenado en cache con TTL de %d minutos", ttl));
            return result;
            
        } catch (Exception e) {
            logger.severe("Error consultando servidor backend: " + e.getMessage());
            // En caso de error, intentar con otro servidor
            return retryWithDifferentServer(sqlQuery, params, cacheKey);
        }
    }
    
    /**
     * Reintenta la consulta con un servidor diferente en caso de error
     */
    private String[] retryWithDifferentServer(String sqlQuery, String[] params, String cacheKey) {
        for (ServicePrx backupServer : backendServers) {
            try {
                String[] result = backupServer.consultarBD(sqlQuery, params);
                long ttl = determineTTL(sqlQuery);
                cache.put(cacheKey, new CacheEntry(Arrays.asList(result), ttl));
                logger.info("Consulta exitosa usando servidor de respaldo");
                return result;
            } catch (Exception e) {
                logger.warning("Error en servidor de respaldo: " + e.getMessage());
            }
        }
        throw new RuntimeException("Todos los servidores backend no disponibles");
    }
    
    /**
     * Selecciona un servidor backend usando round-robin
     */
    private ServicePrx selectBackendServer() {
        if (backendServers.isEmpty()) {
            throw new RuntimeException("No hay servidores backend disponibles");
        }
        return backendServers.get(random.nextInt(backendServers.size()));
    }
    
    /**
     * Determina el TTL apropiado basado en el tipo de consulta
     */
    private long determineTTL(String sqlQuery) {
        String query = sqlQuery.toLowerCase();
        
        // Consultas de datos de votantes (más estables)
        if (query.contains("votantes") || query.contains("cedula")) {
            return VOTER_DATA_TTL_MINUTES;
        }
        
        // Consultas de votos (pueden cambiar más frecuentemente)
        if (query.contains("votos") || query.contains("resultados")) {
            return DEFAULT_TTL_MINUTES;
        }
        
        // Default para otras consultas
        return DEFAULT_TTL_MINUTES;
    }
    
    /**
     * Genera una clave única para el cache basada en la query y parámetros
     */
    private String generateCacheKey(String sqlQuery, String[] params) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(sqlQuery.trim().toLowerCase());
            
            if (params != null) {
                for (String param : params) {
                    sb.append("|").append(param != null ? param.trim() : "null");
                }
            }
            
            // Generar hash MD5 para clave compacta
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hashString = new StringBuilder();
            for (byte b : hashBytes) {
                hashString.append(String.format("%02x", b));
            }
            
            return hashString.toString();
            
        } catch (Exception e) {
            logger.warning("Error generando clave de cache: " + e.getMessage());
            return sqlQuery + Arrays.toString(params);
        }
    }
    
    /**
     * Limpia el cache (útil para testing o mantenimiento)
     */
    public void clearCache() {
        cache.clear();
        cacheHits.set(0);
        cacheMisses.set(0);
        totalRequests.set(0);
        logger.info("Cache limpiado completamente");
    }
    
    /**
     * Cierra el proxy cache limpiamente
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
        }
        logger.info("ProxyCache cerrado correctamente");
    }
} 