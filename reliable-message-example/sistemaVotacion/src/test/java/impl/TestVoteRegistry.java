package test;

import sistemaVotacion.VoteRegistry;

public class TestVoteRegistry {
    
    private TestVoteRegistry() {
        
    }
    
    public static boolean hasVoted(String documento) {
        return TestConexionBD.haVotado(documento);
    }
    
    public static void register(String documento) {
        TestConexionBD.registrarVoto(documento);
    }
} 