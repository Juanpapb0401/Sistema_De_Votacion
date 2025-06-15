import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.zeroc.Ice.Current;

import app.Service;

public class VotingServiceImp implements Service {
    
    
    private static final int THREAD_POOL_SIZE = 20; 
    private final ExecutorService threadPool;
    
    public VotingServiceImp() {
        
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE, r -> {
            Thread t = new Thread(r);
            t.setName("VotingService-Worker-" + t.getId());
            t.setDaemon(true); 
            return t;
        });
        
        System.out.println("VotingServiceImp iniciado con Thread Pool de " + THREAD_POOL_SIZE + " threads");
    }

    @Override
    public void print(Current current) {
        System.out.println("Servicio de votación funcionando correctamente!");
    }

    @Override
    public String[] consultarBD(String sqlQuery, String[] params, Current current) {
        
        Callable<String[]> queryTask = () -> {
            try {
                long startTime = System.currentTimeMillis();
                String threadName = Thread.currentThread().getName();
                System.out.println("[" + threadName + "] Consultando BD: " + sqlQuery);
                
                
                if (params != null && params.length > 0) {
                    System.out.println("[" + threadName + "] Parámetros: " + java.util.Arrays.toString(params));
                }
                
                
                List<Map<String, Object>> results;
                if (params != null && params.length > 0) {
                    results = Server.getInfo(sqlQuery, (Object[]) params);
                } else {
                    results = Server.getInfo(sqlQuery);
                }
                
               
                System.out.println("[" + threadName + "] Resultados encontrados: " + results.size());
                if (!results.isEmpty()) {
                    System.out.println("[" + threadName + "] Primer resultado: " + results.get(0));
                }
                
                
                if (results.isEmpty()) {
                    return new String[0];
                }
                
                
                Map<String, Object> firstResult = results.get(0);
                String[] resultArray = new String[firstResult.size() * 2]; 
                int index = 0;
                
                for (Map.Entry<String, Object> entry : firstResult.entrySet()) {
                    resultArray[index++] = entry.getKey();
                    resultArray[index++] = entry.getValue() != null ? entry.getValue().toString() : "";
                }
                
                long endTime = System.currentTimeMillis();
                System.out.println("[" + threadName + "] Consulta completada en " + (endTime - startTime) + "ms");
                
                return resultArray;
                
            } catch (Exception e) {
                System.err.println("[" + Thread.currentThread().getName() + "] Error en consultarBD: " + e.getMessage());
                e.printStackTrace();
                return new String[]{"ERROR", e.getMessage()};
            }
        };
        
        try {
           
            Future<String[]> future = threadPool.submit(queryTask);
            
            
            return future.get(10, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            System.err.println("Error en thread pool execution: " + e.getMessage());
            e.printStackTrace();
            return new String[]{"ERROR", "Thread pool execution failed: " + e.getMessage()};
        }
    }
    
   
    public void shutdown() {
        System.out.println("Cerrando thread pool...");
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("Forzando cierre del thread pool...");
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Thread pool cerrado correctamente");
    }
} 