import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import model.Vote;

public class Server {

    private static final ManejadorDB db = ManejadorDB.getInstance();
    private static Map<Integer, Map<Integer, Integer>> mesaVotes = new HashMap<>();
    private static List<String> candidateNames = new ArrayList<>();
    private static final String CANDIDATES_FILE_PATH = "sistemaVotacion/src/main/resources/Candidatos.txt";

    static {
        loadCandidateNames();
    }

    private static void loadCandidateNames() {
        try {
            File candidatesFile = new File(CANDIDATES_FILE_PATH);
            if (candidatesFile.exists()) {
                Scanner scanner = new Scanner(candidatesFile);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (!line.isEmpty()) {
                        candidateNames.add(line);
                    }
                }
                scanner.close();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar nombres de candidatos: " + e.getMessage());
        }
    }

    private static String getCandidateName(int candidateId) {
        if (candidateId >= 0 && candidateId < candidateNames.size()) {
            return candidateNames.get(candidateId);
        }
        return "Desconocido";
    }

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

    public static void addNewVotes(int candidateId, int userId, int mesaId) {
        Vote vote = new Vote(candidateId, userId);
        VoteManager.getInstance().registerVote(vote);
        
        // Persistir también en la base de datos
        try {
            db.addNewVotes(candidateId, userId, mesaId);
        } catch (java.sql.SQLException e) {
            System.err.println("Error al insertar voto en la BD: " + e.getMessage());
        }
        
        mesaVotes.computeIfAbsent(mesaId, k -> new HashMap<>());
        Map<Integer, Integer> mesaVoteCount = mesaVotes.get(mesaId);
        mesaVoteCount.put(candidateId, mesaVoteCount.getOrDefault(candidateId, 0) + 1);
        
        
        savePartialFile(mesaId);
    }

    private static void savePartialFile(int mesaId) {
        String fileName = "partial-" + mesaId + ".csv";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("candidateId,candidateName,totalVotes\n");
            
            Map<Integer, Integer> mesaVoteCount = mesaVotes.get(mesaId);
            for (Map.Entry<Integer, Integer> entry : mesaVoteCount.entrySet()) {
                int candidateId = entry.getKey();
                String candidateName = getCandidateName(candidateId);
                int totalVotes = entry.getValue();
                writer.write(candidateId + "," + candidateName + "," + totalVotes + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error al escribir archivo " + fileName + ": " + e.getMessage());
        }
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

        
        try (Communicator communicator = Util.initialize(args)) {
            
            com.zeroc.Ice.Properties props = communicator.getProperties();
            String idx = props.getProperty("ServerIndex");

            String adapterName;
            String serviceIdentity;

            if (idx != null && !idx.isBlank()) {
                
                adapterName = "Server-" + idx.trim();
                serviceIdentity = "Service-" + idx.trim();
            } else {
                
                adapterName = "VoteService";
                serviceIdentity = "VotingService";
            }

            ObjectAdapter adapter = communicator.createObjectAdapter(adapterName);

            
            adapter.add(new VotingServiceImp(), new Identity(serviceIdentity, ""));

            
            adapter.add(new QueryStationImp(), new Identity("QueryStation" + (idx != null && !idx.isBlank() ? ("-" + idx.trim()) : ""), ""));

            
            adapter.add(new RMDestinationImpl(), new Identity("RMDestination", ""));
            
            adapter.activate();
            
            System.out.println("Servidor iniciado. Esperando conexiones...");
            communicator.waitForShutdown();
        }
    }
}