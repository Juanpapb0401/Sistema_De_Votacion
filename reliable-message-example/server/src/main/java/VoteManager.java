import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import model.Vote;

public class VoteManager {
    private static final String CSV_FILE_PATH = "votos.csv";
    private static VoteManager instance;
    private Map<Integer, Integer> voteCount = new HashMap<>();
    
    private VoteManager() {
        loadVotesFromCSV();
    }
    
    public static synchronized VoteManager getInstance() {
        if (instance == null) {
            instance = new VoteManager();
        }
        return instance;
    }
    
    public synchronized void registerVote(Vote vote) {
        int candidateId = vote.getCandidateId();
        int currentVotes = voteCount.getOrDefault(candidateId, 0);
        voteCount.put(candidateId, currentVotes + 1);
        
        saveVotesToCSV();
    }
    
    private void loadVotesFromCSV() {
        File csvFile = new File(CSV_FILE_PATH);
        
        if (!csvFile.exists()) {
            return;
        }
        
        try {
            Files.lines(Paths.get(CSV_FILE_PATH))
                 .skip(1)
                 .forEach(line -> {
                     String[] parts = line.split(",");
                     if (parts.length == 2) {
                         try {
                             int candidateId = Integer.parseInt(parts[0]);
                             int votes = Integer.parseInt(parts[1]);
                             voteCount.put(candidateId, votes);
                         } catch (NumberFormatException e) {
                             System.err.println("Error al parsear línea: " + line);
                         }
                     }
                 });
        } catch (IOException e) {
            System.err.println("Error al leer archivo CSV: " + e.getMessage());
        }
    }
    
    private void saveVotesToCSV() {
        try (FileWriter writer = new FileWriter(CSV_FILE_PATH)) {
            writer.write("CandidatoID,Votos\n");
            
            // Escribir datos
            for (Map.Entry<Integer, Integer> entry : voteCount.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
            
        } catch (IOException e) {
            System.err.println("Error al escribir archivo CSV: " + e.getMessage());
        }
    }
} 