package dispositivoPersonal;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import app.ServicePrx;


public class QueryLoadTest {

    
    private static final int SCENARIO1_QPS = 2_666;
    private static final int SCENARIO1_DURATION_SEC = 60;

    private static final int SCENARIO2_QPS = 1_000;
    private static final int SCENARIO2_DURATION_SEC = 60;

    
    private static final String SQL = """
            SELECT mv.consecutive AS mesa, d.nombre AS departamento, m.nombre AS municipio,
                   pv.id AS puesto_id, pv.nombre AS puesto_nombre, pv.direccion AS direccion
            FROM ciudadano c
            JOIN mesa_votacion mv ON mv.id = c.mesa_id
            JOIN puesto_votacion pv ON pv.id = mv.puesto_id
            JOIN municipio m ON m.id = pv.municipio_id
            JOIN departamento d ON d.id = m.departamento_id
            WHERE c.documento = ?
            """;

        
    private static final String[] DEFAULT_CEDULAS = {
            "711674049",
            "130204799",
            "527760767",
            "279696459",
            "309884552",
            "639845626",
            "545292431"
    };

    private final List<String> citizenIds = new ArrayList<>(java.util.Arrays.asList(DEFAULT_CEDULAS));

    private Communicator communicator;
    private volatile ServicePrx servicePrx;
    private final String[] proxyIds = {"ProxyCache-1", "ProxyCache-2"};

    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicInteger errorCount = new AtomicInteger();

    private final Random random = new Random();

    private String fixedCedula = null;  

    public static void main(String[] args) {
        int scenario = 1;
        String cedula = null;

        if (args.length >= 1) {
            try {
                scenario = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                    
                cedula = args[0];
            }
        }

        if (args.length >= 2) {
            cedula = args[1];
        }

        QueryLoadTest test = new QueryLoadTest();
        test.fixedCedula = cedula;
        test.runScenario(scenario);
    }

    private void runScenario(int scenario) {
        try {
            initIce();

            
            if (fixedCedula == null) {
                fixedCedula = null; 
            }

            if (scenario == 2) {
                System.out.println("Iniciando Escenario 2: Falla Parcial de Proxy");
                runLoadTest(SCENARIO2_QPS, SCENARIO2_DURATION_SEC, true);
            } else {
                System.out.println("Iniciando Escenario 1: Consulta Concurrente Masiva");
                runLoadTest(SCENARIO1_QPS, SCENARIO1_DURATION_SEC, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (communicator != null) communicator.destroy();
        }
    }

  
    private void runLoadTest(int qps, int durationS, boolean liveStats) {
        int totalRequests = qps * durationS;
        int threadPoolSize = Math.min(qps, 500); // limitar número de hilos

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor();

        successCount.set(0);
        errorCount.set(0);

        
        if (liveStats) {
            ticker.scheduleAtFixedRate(() -> {
                int succ = successCount.get();
                int err = errorCount.get();
                int total = succ + err;
                System.out.printf("[TIEMPO %ds] Total=%d  Éxitos=%d  Errores=%d%n",
                        (int) (Duration.between(startInstant, Instant.now()).toSeconds()), total, succ, err);
            }, 1, 1, TimeUnit.SECONDS);
        }

        startInstant = Instant.now();

        
        long startMillis = System.currentTimeMillis();
        for (int i = 0; i < totalRequests; i++) {
            long scheduledTime = startMillis + (i * 1000L) / qps;
            long delay = scheduledTime - System.currentTimeMillis();
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ignored) {
                }
            }
            executor.submit(this::sendRandomQuery);
        }

        executor.shutdown();
        try {
            boolean finished = executor.awaitTermination(durationS + 10, TimeUnit.SECONDS);
            if (!finished) {
                System.out.println("Advertencia: el thread pool no terminó a tiempo");
            }
        } catch (InterruptedException ignored) {
        }

        ticker.shutdownNow();

        long elapsedMs = Duration.between(startInstant, Instant.now()).toMillis();
        printSummary(totalRequests, elapsedMs);
    }

    private volatile Instant startInstant;

    /**
     * Envía una consulta aleatoria al ProxyCache.
     */
    private void sendRandomQuery() {
        try {
            String cedula;
            if (fixedCedula != null) {
                cedula = fixedCedula;
            } else if (!citizenIds.isEmpty()) {
                cedula = citizenIds.get(random.nextInt(citizenIds.size()));
            } else {
                
                cedula = String.valueOf(10_000_000 + random.nextInt(89_999_999));
            }
            String[] params = new String[]{cedula};
            String[] result;
            try {
                result = servicePrx.consultarBD(SQL, params);
            } catch (Exception ex) {
                
                System.out.println("Error en consulta: " + ex.getClass().getSimpleName() + ", intentando failover");
                switchProxy();
                try {
                    result = servicePrx.consultarBD(SQL, params);
                } catch (Exception ex2) {
                    errorCount.incrementAndGet();
                    return;
                }
            }
            if (result.length == 0 || (result.length > 0 && "ERROR".equals(result[0]))) {
                errorCount.incrementAndGet();
            } else {
                successCount.incrementAndGet();
            }
        } catch (Exception e) {
            errorCount.incrementAndGet();
        }
    }

    private void printSummary(int totalRequests, long elapsedMs) {
        double elapsedSec = elapsedMs / 1000.0;
        System.out.println("======================== RESUMEN ========================");
        System.out.printf("Total de solicitudes: %d%n", totalRequests);
        System.out.printf("Éxitos: %d%n", successCount.get());
        System.out.printf("Errores: %d%n", errorCount.get());
        System.out.printf("Duración: %.2f s%n", elapsedSec);
        System.out.printf("Throughput: %.2f req/s%n", totalRequests / elapsedSec);
        System.out.println("=========================================================");
    }

    /**
     * Inicializa IceGrid y obtiene un proxy al ProxyCache.
     */
    private void initIce() {
        String[] initData = {
                "--Ice.Default.Locator=SistemaVotacion/Locator:default -h localhost -p 4061"
        };
        communicator = Util.initialize(initData);

        ServicePrx connected = null;
        for (String id : proxyIds) {
            try {
                ObjectPrx base = communicator.stringToProxy(id);
                ServicePrx prx = ServicePrx.checkedCast(base);
                if (prx != null) {
                    
                    prx.ice_ping();
                    connected = prx;
                    System.out.println("Conectado exitosamente a " + id);
                    break;
                }
            } catch (Exception ex) {
                System.out.println(id + " no disponible: " + ex.getClass().getSimpleName());
            }
        }

        if (connected == null) {
            throw new RuntimeException("No se pudo conectar a ningún ProxyCache activo");
        }
        servicePrx = connected;
    }

    /**
     * Cambia al primer ProxyCache disponible distinto del actual.
     */
    private synchronized void switchProxy() {
        for (String id : proxyIds) {
            try {
                if (servicePrx != null && servicePrx.ice_getIdentity().name.equals(id)) {
                    continue; 
                }
                ObjectPrx base = communicator.stringToProxy(id);
                ServicePrx prx = ServicePrx.checkedCast(base);
                if (prx != null) {
                    prx.ice_ping(); 
                    servicePrx = prx;
                    System.out.println("[FAILOVER] Cambiado a " + id);
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        System.err.println("[FAILOVER] No se encontró ProxyCache activo");
    }
} 