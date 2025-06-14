import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import model.Vote;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Server {

    private static final ManejadorDB db = ManejadorDB.getInstance();

    public static java.util.List<java.util.Map<String, Object>> getInfo(String sqlQuery) {
        try {
            return db.readData(sqlQuery);
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error al leer datos: " + e.getMessage(), e);
        }
    }

    public static java.util.List<java.util.Map<String, Object>> getInfo(String sqlQuery, Object... params) {
        try {
            return db.readData(sqlQuery, params);
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error al leer datos: " + e.getMessage(), e);
        }
    }

    public static void addNewVotes(int candidateId, int userId) {
        Vote vote = new Vote(candidateId, userId);
        VoteManager.getInstance().registerVote(vote);
    }

    private static void copyVotesFile() {
        try {
            File sourceFile = new File("reliableServer_votos.csv");
            File destFile = new File("resume.csv");
            
            if (sourceFile.exists()) {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Archivo resume.csv generado exitosamente");
            } else {
                System.out.println("No se encontró el archivo reliableServer_votos.csv");
            }
        } catch (IOException e) {
            System.err.println("Error al copiar el archivo: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Iniciar thread para escuchar la tecla 'q'
        new Thread(() -> {
            System.out.println("Presione 'q' para cerrar el servidor...");
            while (true) {
                try {
                    int input = System.in.read();
                    if (input == 'q' || input == 'Q') {
                        System.out.println("Cerrando servidor...");
                        copyVotesFile();
                        System.exit(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Iniciar el servidor Ice
        try (Communicator communicator = Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapter("VoteService");
            
            // Registrar el servicio de votación
            adapter.add(new VotingServiceImp(), new Identity("VotingService", "VoteService"));
            
            // Registrar el servicio RMDestination con el identity correcto
            adapter.add(new RMDestinationImpl(), new Identity("RMDestination", ""));
            
            adapter.activate();
            
            System.out.println("Servidor iniciado. Esperando conexiones...");
            communicator.waitForShutdown();
        }
    }
}