package test;

import com.zeroc.Ice.Current;

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
        
        // Limpiar votos previos
        FakeConexionBD.limpiarVotos();
        
        // Escenario 1: Transmisión Normal (simulado a escala reducida)
        System.out.println("\nEscenario 1: Transmisión Normal");
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
        
        // Escenario 3: Reintento de transmisión
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
} 