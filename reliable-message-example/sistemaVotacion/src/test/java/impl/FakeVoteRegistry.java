package test;

import sistemaVotacion.VoteRegistry;

/**
 * Clase de utilidad para simular el registro de votos en pruebas
 */
public class FakeVoteRegistry {
    
    private FakeVoteRegistry() {
        // Constructor privado para evitar instanciación
    }
    
    /**
     * Verifica si un documento ya votó, usando la implementación falsa
     */
    public static boolean hasVoted(String documento) {
        return FakeConexionBD.haVotado(documento);
    }
    
    /**
     * Registra un voto para un documento específico
     */
    public static void register(String documento) {
        FakeConexionBD.registrarVoto(documento);
    }
} 