package test;

import com.zeroc.Ice.Current;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Test para verificar los diferentes casos de retorno de VoteStation
 */
public class TestVoteStation {
    public static void main(String[] args) {
        System.out.println("Iniciando pruebas de VoteStation...");
        
        // Limpiar votos previos
        FakeConexionBD.limpiarVotos();
        
        // Crear instancia de VoteStation para pruebas (mesa 1)
        VoteStationTestImpl voteStation = new VoteStationTestImpl(1);
        
        // Caso 1: Votante válido (mesa 1, documento 711674049)
        System.out.println("\nTest 1: Votante válido");
        System.out.println("Esperado: 0");
        System.out.println("Actual:   " + voteStation.vote("711674049", 1, null));

        // Caso 2: Votante en mesa incorrecta (mesa 2, documento 130204799)
        System.out.println("\nTest 2: Mesa incorrecta");
        System.out.println("Esperado: 1");
        System.out.println("Actual:   " + voteStation.vote("130204799", 1, null));

        // Caso 3: Ya votó (votar dos veces con 711674049)
        System.out.println("\nTest 3: Ya votó");
        // El primer voto ya se hizo en el caso 1, ahora intentamos votar de nuevo
        System.out.println("Esperado: 2");
        System.out.println("Actual:   " + voteStation.vote("711674049", 1, null));

        // Caso 4: No registrado
        System.out.println("\nTest 4: No registrado");
        System.out.println("Esperado: 3");
        System.out.println("Actual:   " + voteStation.vote("999999999", 1, null));
        
        // Pruebas de carga simuladas
        testCargaSimulada();
        
        System.out.println("\nPruebas completadas.");
    }
    
    /**
     * Simula pruebas de carga con múltiples votos
     */
    private static void testCargaSimulada() {
        System.out.println("\n=== PRUEBAS DE CARGA SIMULADAS ===");
        
        // Escenario 1: Transmisión Normal
        testEscenario1TransmisionNormal();
        
        // Escenario 2: Pérdida de Conexión Temporal
        testEscenario2PerdidaConexion();
        
        // Escenario 3: Reintento de Transmisión
        testEscenario3Reintento();
        
        // Escenario 4: Carga Máxima
        testEscenario4CargaMaxima();
    }
    
    /**
     * Escenario 1: Transmisión Normal
     * Emitir votos desde cada estación y verificar la recepción
     */
    private static void testEscenario1TransmisionNormal() {
        System.out.println("\nEscenario 1: Transmisión Normal");
        
        // Limpiar votos previos
        FakeConexionBD.limpiarVotos();
        
        int totalVotos = 30; // Simulamos 30 votos (10 por mesa)
        int votosCorrectos = 0;
        
        // Crear 3 estaciones de votación (mesas 1, 2 y 3)
        VoteStationTestImpl mesa1 = new VoteStationTestImpl(1);
        VoteStationTestImpl mesa2 = new VoteStationTestImpl(2);
        VoteStationTestImpl mesa3 = new VoteStationTestImpl(3);
        
        // Simular votos en cada mesa
        for (int i = 0; i < 10; i++) {
            // Generar documentos aleatorios para cada mesa
            String docMesa1 = "1" + String.format("%08d", i);
            String docMesa2 = "2" + String.format("%08d", i);
            String docMesa3 = "3" + String.format("%08d", i);
            
            // Registrar estos documentos en la "base de datos" simulada
            FakeConexionBD.agregarCiudadano(docMesa1, 1);
            FakeConexionBD.agregarCiudadano(docMesa2, 2);
            FakeConexionBD.agregarCiudadano(docMesa3, 3);
            
            // Emitir votos
            if (mesa1.vote(docMesa1, 1, null) == 0) votosCorrectos++;
            if (mesa2.vote(docMesa2, 2, null) == 0) votosCorrectos++;
            if (mesa3.vote(docMesa3, 3, null) == 0) votosCorrectos++;
        }
        
        System.out.println("Votos emitidos: " + totalVotos);
        System.out.println("Votos correctos: " + votosCorrectos);
        System.out.println("Tasa de éxito: " + (votosCorrectos * 100.0 / totalVotos) + "%");
    }
    
    /**
     * Escenario 2: Pérdida de Conexión Temporal
     * Simular desconexión durante la transmisión
     */
    private static void testEscenario2PerdidaConexion() {
        System.out.println("\nEscenario 2: Pérdida de Conexión Temporal");
        
        // Limpiar votos previos
        FakeConexionBD.limpiarVotos();
        
        // Crear estación de votación
        VoteStationTestImpl mesa = new VoteStationTestImpl(1);
        
        // Contador de votos
        AtomicInteger votosExitosos = new AtomicInteger(0);
        int totalVotos = 100;
        
        System.out.println("Iniciando transmisión de " + totalVotos + " votos...");
        
        // Primera fase: enviar 40 votos
        for (int i = 0; i < 40; i++) {
            String documento = "1" + String.format("%08d", i);
            FakeConexionBD.agregarCiudadano(documento, 1);
            if (mesa.vote(documento, 1, null) == 0) {
                votosExitosos.incrementAndGet();
            }
        }
        
        System.out.println("Simulando desconexión por 2 segundos...");
        
        // Simular desconexión (no procesamos votos durante este tiempo)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Conexión restaurada, continuando transmisión...");
        
        // Segunda fase: enviar 60 votos restantes
        for (int i = 40; i < 100; i++) {
            String documento = "1" + String.format("%08d", i);
            FakeConexionBD.agregarCiudadano(documento, 1);
            if (mesa.vote(documento, 1, null) == 0) {
                votosExitosos.incrementAndGet();
            }
        }
        
        System.out.println("Transmisión completada.");
        System.out.println("Votos emitidos: " + totalVotos);
        System.out.println("Votos exitosos: " + votosExitosos.get());
        System.out.println("Tasa de éxito: " + (votosExitosos.get() * 100.0 / totalVotos) + "%");
    }
    
    /**
     * Escenario 3: Reintento de Transmisión
     * Verificar que los votos no sean contabilizados múltiples veces
     */
    private static void testEscenario3Reintento() {
        System.out.println("\nEscenario 3: Reintento de transmisión");
        
        // Limpiar votos previos
        FakeConexionBD.limpiarVotos();
        
        // Crear una mesa de votación
        VoteStationTestImpl mesa = new VoteStationTestImpl(1);
        
        // Documento de prueba
        String documento = "711674049";
        
        // Primer intento (debería ser exitoso)
        int resultado1 = mesa.vote(documento, 1, null);
        System.out.println("Primer intento: " + (resultado1 == 0 ? "Exitoso" : "Fallido"));
        
        // Segundo intento (debería detectar voto duplicado)
        int resultado2 = mesa.vote(documento, 1, null);
        System.out.println("Segundo intento: " + (resultado2 == 2 ? "Correctamente rechazado" : "Error - No detectó duplicado"));
        
        // Tercer intento (debería seguir detectando voto duplicado)
        int resultado3 = mesa.vote(documento, 1, null);
        System.out.println("Tercer intento: " + (resultado3 == 2 ? "Correctamente rechazado" : "Error - No detectó duplicado"));
    }
    
    /**
     * Escenario 4: Carga Máxima
     * Simular día de elecciones con máxima participación
     */
    private static void testEscenario4CargaMaxima() {
        System.out.println("\nEscenario 4: Carga Máxima");
        
        // Limpiar votos previos
        FakeConexionBD.limpiarVotos();
        
        // Parámetros de la prueba
        final int TOTAL_VOTOS = 400; // Simulamos 400 votos (100 por mesa)
        final int NUM_MESAS = 4;     // 4 mesas de votación
        final int VOTOS_POR_MESA = TOTAL_VOTOS / NUM_MESAS;
        
        // Crear estaciones de votación
        VoteStationTestImpl[] mesas = new VoteStationTestImpl[NUM_MESAS];
        for (int i = 0; i < NUM_MESAS; i++) {
            mesas[i] = new VoteStationTestImpl(i + 1);
        }
        
        // Contador atómico para votos exitosos
        AtomicInteger votosExitosos = new AtomicInteger(0);
        
        // Usar un pool de hilos para simular votación concurrente
        ExecutorService executor = Executors.newFixedThreadPool(NUM_MESAS);
        CountDownLatch latch = new CountDownLatch(NUM_MESAS);
        
        System.out.println("Iniciando votación masiva con " + NUM_MESAS + " mesas y " + TOTAL_VOTOS + " votos...");
        
        // Crear tareas para cada mesa
        for (int mesaId = 0; mesaId < NUM_MESAS; mesaId++) {
            final int finalMesaId = mesaId;
            executor.submit(() -> {
                try {
                    // Cada mesa procesa su cuota de votos
                    for (int i = 0; i < VOTOS_POR_MESA; i++) {
                        // Generar documento único para esta mesa
                        String documento = (finalMesaId + 1) + String.format("%08d", i);
                        
                        // Registrar en la "base de datos" simulada
                        FakeConexionBD.agregarCiudadano(documento, finalMesaId + 1);
                        
                        // Emitir voto
                        if (mesas[finalMesaId].vote(documento, finalMesaId + 1, null) == 0) {
                            votosExitosos.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Esperar a que todas las mesas terminen
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Prueba interrumpida: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        
        // Apagar el executor
        executor.shutdown();
        
        System.out.println("Votación masiva completada.");
        System.out.println("Votos emitidos: " + TOTAL_VOTOS);
        System.out.println("Votos exitosos: " + votosExitosos.get());
        System.out.println("Tasa de éxito: " + (votosExitosos.get() * 100.0 / TOTAL_VOTOS) + "%");
    }
} 