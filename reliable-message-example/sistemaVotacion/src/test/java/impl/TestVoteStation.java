package test;

import sistemaVotacion.VoteStationImpl;
import sistemaVotacion.VoteRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;

/**
 * Test para verificar los diferentes casos de retorno de VoteStation
 */
public class TestVoteStation {
    // Lista de documentos reales de la base de datos (todos en mesa 1)
    private static final List<String> DOCUMENTOS = new ArrayList<>();
    
    static {
        // Inicializar la lista de documentos
        DOCUMENTOS.add("711674049");  // Fanny Aguilar
        DOCUMENTOS.add("768298263");  // Marisa Carrion
        DOCUMENTOS.add("409474314");  // Cesar Busquets
        DOCUMENTOS.add("544296936");  // Duilio Alcalde
        DOCUMENTOS.add("465954474");  // Rogelio Gomez
        DOCUMENTOS.add("234832732");  // Isa Almeida
        DOCUMENTOS.add("338494393");  // Buenaventura Corral
        DOCUMENTOS.add("126858785");  // Georgina Zamorano
        DOCUMENTOS.add("248440809");  // Francisco Javier Tejedor
        DOCUMENTOS.add("238946476");  // Jose Manuel Hervas
        // Agregar más documentos según sea necesario
        DOCUMENTOS.add("918996842");  // Angela Corominas
        DOCUMENTOS.add("279247930");  // Odalys Echeverria
        DOCUMENTOS.add("690927299");  // Joel Fuertes
        DOCUMENTOS.add("188720413");  // Odalis Moliner
        DOCUMENTOS.add("412743055");  // Lucas Moya
        DOCUMENTOS.add("154760794");  // Renato Camara
        DOCUMENTOS.add("554615099");  // Crescencia Verdugo
        DOCUMENTOS.add("156265735");  // Armando Arellano
        DOCUMENTOS.add("382273574");  // Cloe Clemente
        DOCUMENTOS.add("421833265");  // Herberto Barros
        // Más documentos
        DOCUMENTOS.add("848643928");  // Chucho Bustos
        DOCUMENTOS.add("181803560");  // Josep Gutierrez
        DOCUMENTOS.add("558729184");  // Gil Raya
        DOCUMENTOS.add("149878652");  // Yessica Gabaldon
        DOCUMENTOS.add("767648857");  // Adelardo Alfaro
        // Y así sucesivamente...
    }
    
    public static void main(String[] args) {
        System.out.println("Iniciando pruebas de VoteStation...");
        
        // Limpiar votos previos
        limpiarVotantes();
        
        // Crear instancia de VoteStation para pruebas (mesa 1)
        VoteStationImpl voteStation = new VoteStationImpl();
        
        // Caso 1: Votante valido (mesa 1, documento real)
        System.out.println("\nTest 1: Votante valido");
        System.out.println("Esperado: 0");
        int resultado = voteStation.vote(DOCUMENTOS.get(0), 1, null);
        System.out.println("Actual:   " + resultado + " (0=Valido, 1=Mesa incorrecta, 2=Ya voto, 3=No registrado)");

        // Caso 2: Votante en mesa incorrecta (asumimos que este documento está en otra mesa)
        System.out.println("\nTest 2: Mesa incorrecta");
        System.out.println("Esperado: 1");
        resultado = voteStation.vote("130204799", 1, null); // Documento de mesa 2
        System.out.println("Actual:   " + resultado + " (0=Valido, 1=Mesa incorrecta, 2=Ya voto, 3=No registrado)");

        // Caso 3: Ya voto (votar dos veces con el mismo documento)
        System.out.println("\nTest 3: Ya voto");
        // El primer voto ya se hizo en el caso 1, ahora intentamos votar de nuevo
        System.out.println("Esperado: 2");
        resultado = voteStation.vote(DOCUMENTOS.get(0), 1, null);
        System.out.println("Actual:   " + resultado + " (0=Valido, 1=Mesa incorrecta, 2=Ya voto, 3=No registrado)");

        // Caso 4: No registrado
        System.out.println("\nTest 4: No registrado");
        System.out.println("Esperado: 3");
        resultado = voteStation.vote("999999999", 1, null); // Documento inexistente
        System.out.println("Actual:   " + resultado + " (0=Valido, 1=Mesa incorrecta, 2=Ya voto, 3=No registrado)");
        
        // Pruebas de carga simuladas con datos reales
        testCargaSimulada();
        
        System.out.println("\nPruebas completadas.");
    }
    
    /**
     * Limpia el archivo de votantes para reiniciar las pruebas
     */
    private static void limpiarVotantes() {
        try {
            File file = new File("votantes.csv");
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }
            System.out.println("Archivo de votantes limpiado correctamente.");
        } catch (IOException e) {
            System.err.println("Error al limpiar el archivo de votantes: " + e.getMessage());
        }
    }
    
    /**
     * Simula pruebas de carga con multiples votos
     */
    private static void testCargaSimulada() {
        System.out.println("\n=== PRUEBAS DE CARGA SIMULADAS ===");
        
        // Escenario 1: Transmision Normal
        testEscenario1TransmisionNormal();
        
        // Escenario 2: Perdida de Conexion Temporal
        testEscenario2PerdidaConexion();
        
        // Escenario 3: Reintento de Transmision
        testEscenario3Reintento();
        
        // Escenario 4: Carga Maxima
        testEscenario4CargaMaxima();
    }
    
    private static void testEscenario1TransmisionNormal() {
        System.out.println("\nEscenario 1: Transmision Normal");
        limpiarVotantes();
        
        int totalVotos = 30;
        int votosCorrectos = 0;
        
        // Crear instancia de VoteStation
        VoteStationImpl voteStation = new VoteStationImpl();
        
        System.out.println("Procesando 30 votos...");
        
        // Usar documentos reales para los votos
        for (int i = 0; i < Math.min(30, DOCUMENTOS.size()); i++) {
            String documento = DOCUMENTOS.get(i);
            int resultado = voteStation.vote(documento, 1, null);
            if (resultado == 0) votosCorrectos++;
        }
        
        System.out.println("Votos emitidos: " + totalVotos);
        System.out.println("Votos correctos: " + votosCorrectos);
        System.out.println("Tasa de exito: " + (votosCorrectos * 100.0 / totalVotos) + "%");
    }
    
    private static void testEscenario2PerdidaConexion() {
        System.out.println("\nEscenario 2: Perdida de Conexion Temporal");
        limpiarVotantes();
        
        VoteStationImpl voteStation = new VoteStationImpl();
        AtomicInteger votosExitosos = new AtomicInteger(0);
        int totalVotos = 100;
        int docsDisponibles = Math.min(totalVotos, DOCUMENTOS.size());
        
        System.out.println("Iniciando transmision de " + docsDisponibles + " votos...");
        System.out.println("Primera fase: 40 votos antes de la desconexion");
        
        // Primera fase: enviar 40 votos
        for (int i = 0; i < Math.min(40, docsDisponibles); i++) {
            String documento = DOCUMENTOS.get(i);
            int resultado = voteStation.vote(documento, 1, null);
            if (resultado == 0) {
                votosExitosos.incrementAndGet();
            }
        }
        
        System.out.println("Simulando desconexion por 2 segundos...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Conexion restaurada, continuando transmision...");
        System.out.println("Segunda fase: restantes votos despues de la reconexion");
        
        // Segunda fase: enviar votos restantes
        for (int i = 40; i < docsDisponibles; i++) {
            String documento = DOCUMENTOS.get(i);
            int resultado = voteStation.vote(documento, 1, null);
            if (resultado == 0) {
                votosExitosos.incrementAndGet();
            }
        }
        
        System.out.println("Transmision completada.");
        System.out.println("Votos emitidos: " + docsDisponibles);
        System.out.println("Votos exitosos: " + votosExitosos.get());
        System.out.println("Tasa de exito: " + (votosExitosos.get() * 100.0 / docsDisponibles) + "%");
    }
    
    private static void testEscenario3Reintento() {
        System.out.println("\nEscenario 3: Reintento de transmision");
        limpiarVotantes();
        
        VoteStationImpl voteStation = new VoteStationImpl();
        String documento = DOCUMENTOS.get(0); // Usar el primer documento real
        
        int resultado1 = voteStation.vote(documento, 1, null);
        System.out.println("Primer intento: " + resultado1 + " (Esperado: 0)");
        
        int resultado2 = voteStation.vote(documento, 1, null);
        System.out.println("Segundo intento: " + resultado2 + " (Esperado: 2)");
        
        int resultado3 = voteStation.vote(documento, 1, null);
        System.out.println("Tercer intento: " + resultado3 + " (Esperado: 2)");
    }
    
    private static void testEscenario4CargaMaxima() {
        System.out.println("\nEscenario 4: Carga Maxima");
        limpiarVotantes();
        
        final int TOTAL_VOTOS = Math.min(DOCUMENTOS.size(), 400);
        
        AtomicInteger votosExitosos = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(10); // 10 hilos para simular concurrencia
        CountDownLatch latch = new CountDownLatch(10);
        
        System.out.println("Iniciando votacion masiva con " + TOTAL_VOTOS + " votos...");
        
        // Dividir los votos entre los hilos
        int votosPerThread = TOTAL_VOTOS / 10;
        
        for (int thread = 0; thread < 10; thread++) {
            final int startIdx = thread * votosPerThread;
            final int endIdx = (thread == 9) ? TOTAL_VOTOS : (thread + 1) * votosPerThread;
            
            executor.submit(() -> {
                try {
                    VoteStationImpl voteStation = new VoteStationImpl(); // Cada hilo tiene su propia instancia
                    for (int i = startIdx; i < endIdx; i++) {
                        if (i < DOCUMENTOS.size()) {
                            String documento = DOCUMENTOS.get(i);
                            int resultado = voteStation.vote(documento, 1, null);
                            if (resultado == 0) {
                                votosExitosos.incrementAndGet();
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Prueba interrumpida: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        
        executor.shutdown();
        
        System.out.println("Votacion masiva completada.");
        System.out.println("Votos emitidos: " + TOTAL_VOTOS);
        System.out.println("Votos exitosos: " + votosExitosos.get());
        System.out.println("Tasa de exito: " + (votosExitosos.get() * 100.0 / TOTAL_VOTOS) + "%");
    }
}