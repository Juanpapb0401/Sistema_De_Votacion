package test;

import sistemaVotacion.VoteStationImpl;
import com.zeroc.Ice.Current;

public class TestVoteStation {
    public static void main(String[] args) {
        System.out.println("Iniciando pruebas de VoteStation...");
        
        // Crear instancia de VoteStation
        VoteStationImpl voteStation = new VoteStationImpl();
        
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
        voteStation.vote("711674049", 1, null); // Primer voto (ya votó en el caso 1)
        System.out.println("Esperado: 2");
        System.out.println("Actual:   " + voteStation.vote("711674049", 1, null));

        // Caso 4: No registrado
        System.out.println("\nTest 4: No registrado");
        System.out.println("Esperado: 3");
        System.out.println("Actual:   " + voteStation.vote("999999999", 1, null));
        
        System.out.println("\nPruebas completadas.");
    }
} 