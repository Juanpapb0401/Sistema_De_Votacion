package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import sistemaVotacion.VoteRegistry;
import sistemaVotacion.VoteStationImpl;

/**
 * Test para verificar los diferentes casos de retorno de VoteStation
 */
public class TestVoteStation {
    private static final List<String> DOCUMENTOS = new ArrayList<>();
    
    static {
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
        DOCUMENTOS.add("848643928");  // Chucho Bustos
        DOCUMENTOS.add("181803560");  // Josep Gutierrez
        DOCUMENTOS.add("558729184");  // Gil Raya
        DOCUMENTOS.add("149878652");  // Yessica Gabaldon
        DOCUMENTOS.add("767648857");  // Adelardo Alfaro

    }
    
    public static void main(String[] args) {
        System.out.println("Iniciando pruebas de VoteStation...");
        
        limpiarVotantes();
        
        VoteStationImpl voteStation = new VoteStationImpl();
        
        // Caso 1: Votante valido (mesa 1, documento real)
        System.out.println("\nTest 1: Votante valido");
        System.out.println("Esperado: 0");
        int resultado = voteStation.vote(DOCUMENTOS.get(0), 1, null);
        if (resultado == 0) {
            VoteRegistry.register(DOCUMENTOS.get(0));
        }
        System.out.println("Actual:   " + resultado + " (0=Valido, 1=Mesa incorrecta, 2=Ya voto, 3=No registrado)");

        // Caso 2: Votante en mesa incorrecta 
        System.out.println("\nTest 2: Mesa incorrecta");
        System.out.println("Esperado: 1");
        resultado = voteStation.vote("130204799", 1, null); // Documento de mesa 2
        System.out.println("Actual:   " + resultado + " (0=Valido, 1=Mesa incorrecta, 2=Ya voto, 3=No registrado)");

        // Caso 3: Ya voto 
        System.out.println("\nTest 3: Ya voto");
        // Usar un documento diferente al del caso 1
        String documentoParaDobleVoto = DOCUMENTOS.get(1);
        
        // Primer voto 
        resultado = voteStation.vote(documentoParaDobleVoto, 1, null);
        if (resultado == 0) {
            // Registrar manualmente el voto en el registro
            VoteRegistry.register(documentoParaDobleVoto);
        }
        
        // Segundo voto
        System.out.println("Esperado: 2");
        resultado = voteStation.vote(documentoParaDobleVoto, 1, null);
        System.out.println("Actual:   " + resultado + " (0=Valido, 1=Mesa incorrecta, 2=Ya voto, 3=No registrado)");

        // Caso 4: No registrado
        System.out.println("\nTest 4: No registrado");
        System.out.println("Esperado: 3");
        resultado = voteStation.vote("999999999", 1, null); // Documento inexistente
        System.out.println("Actual:   " + resultado + " (0=Valido, 1=Mesa incorrecta, 2=Ya voto, 3=No registrado)");
        
        testCargaSimulada();
        
        System.out.println("\nPruebas completadas.");
    }
    

    private static void limpiarVotantes() {
        try {
            File file = new File("votantes.csv");
            if (file.exists()) {
                file.delete();
            }
            
            file.createNewFile();
            
            try {
                java.lang.reflect.Field votedField = VoteRegistry.class.getDeclaredField("voted");
                votedField.setAccessible(true);
                Set<String> voted = (Set<String>) votedField.get(null);
                voted.clear();
            } catch (Exception e) {
                System.err.println("Error al limpiar el registro interno: " + e.getMessage());
            }
            
            System.out.println("Archivo de votantes limpiado correctamente.");
        } catch (IOException e) {
            System.err.println("Error al limpiar el archivo de votantes: " + e.getMessage());
        }
    }
    

    private static void testCargaSimulada() {
        System.out.println("\nPRUEBAS DE CARGA");
        
        // Escenario 1: Transmision Normal
        testEscenario1TransmisionNormal();
        
        // Escenario 2: Perdida de Conexion Temporal
        testEscenario2PerdidaConexion();
        
        // Escenario 3: Reintento de Transmision
        testEscenario3Reintento();
        

    }
    
    private static void testEscenario1TransmisionNormal() {
        System.out.println("\nEscenario 1: Transmision Normal de carga");
        limpiarVotantes();
        
        int totalVotos = 25;
        int votosCorrectos = 0;
        
        VoteStationImpl voteStation = new VoteStationImpl();
        
        System.out.println("Procesando " + totalVotos + " votos...");
        
        for (int i = 0; i < Math.min(totalVotos, DOCUMENTOS.size()); i++) {
            String documento = DOCUMENTOS.get(i);
            int resultado = voteStation.vote(documento, 1, null);
            System.out.println(resultado); 
            if (resultado == 0) {
                votosCorrectos++;
                VoteRegistry.register(documento);
            }
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
        int totalVotos = 25; 
        int docsDisponibles = Math.min(totalVotos, DOCUMENTOS.size());
        
        System.out.println("Iniciando transmision de " + docsDisponibles + " votos...");
        System.out.println("Primera fase: votos antes de la desconexion");
        
        int primeraFase = docsDisponibles / 2;
        for (int i = 0; i < primeraFase; i++) {
            String documento = DOCUMENTOS.get(i);
            int resultado = voteStation.vote(documento, 1, null);
            System.out.println(resultado); 
            if (resultado == 0) {
                votosExitosos.incrementAndGet();
                VoteRegistry.register(documento);
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
        for (int i = primeraFase; i < docsDisponibles; i++) {
            String documento = DOCUMENTOS.get(i);
            int resultado = voteStation.vote(documento, 1, null);
            System.out.println(resultado); // Mostrar solo el código numérico
            if (resultado == 0) {
                votosExitosos.incrementAndGet();
                VoteRegistry.register(documento);
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
        String documento = DOCUMENTOS.get(0); 
        
        int resultado1 = voteStation.vote(documento, 1, null);
        System.out.println("Primer intento: " + resultado1 + " (Esperado: 0)");
        if (resultado1 == 0) {
            VoteRegistry.register(documento);
        }
        
        int resultado2 = voteStation.vote(documento, 1, null);
        System.out.println("Segundo intento: " + resultado2 + " (Esperado: 2)");
        
        int resultado3 = voteStation.vote(documento, 1, null);
        System.out.println("Tercer intento: " + resultado3 + " (Esperado: 2)");
    }
    
    
}