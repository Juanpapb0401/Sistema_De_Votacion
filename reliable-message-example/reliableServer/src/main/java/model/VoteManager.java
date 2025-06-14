package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class VoteManager {
    private static final String CSV_FILE_PATH = "reliableServer_votos.csv";
    private static final String CANDIDATES_FILE_PATH = "C:/Users/guzma/Desktop/Sistema_De_Votacion/reliable-message-example/sistemaVotacion/src/main/resources/Candidatos.txt";
    private static VoteManager instance;
    private Map<Integer, Integer> voteCount = new HashMap<>();
    private List<String> candidateNames = new ArrayList<>();
    
    private VoteManager() {
        loadCandidateNames();
        loadVotesFromCSV();
    }
    
    public static synchronized VoteManager getInstance() {
        if (instance == null) {
            instance = new VoteManager();
        }
        return instance;
    }
    
    private void loadCandidateNames() {
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
                System.out.println("Nombres de candidatos cargados: " + candidateNames.size());
                for (int i = 0; i < candidateNames.size(); i++) {
                    System.out.println("Candidato " + i + ": " + candidateNames.get(i));
                }
            } else {
                System.err.println("No se pudo encontrar el archivo de candidatos en: " + CANDIDATES_FILE_PATH);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar nombres de candidatos: " + e.getMessage());
        }
    }
    
    public synchronized void registerVote(Vote vote) {
        int candidateId = vote.getCandidateId();
        int currentVotes = voteCount.getOrDefault(candidateId, 0);
        voteCount.put(candidateId, currentVotes + 1);
        
        saveVotesToCSV();
        System.out.println("Voto registrado en reliableServer para candidato " + candidateId +
                " (" + getCandidateName(candidateId) + ")" +
                ". Total de votos: " + voteCount.get(candidateId));
    }
    
    private void loadVotesFromCSV() {
        File csvFile = new File(CSV_FILE_PATH);
        
        if (!csvFile.exists()) {
            System.out.println("Archivo CSV no encontrado. Se creará uno nuevo al guardar votos.");
            return;
        }
        
        try {
            Files.lines(Paths.get(CSV_FILE_PATH))
                 .skip(1) // Saltar la cabecera
                 .forEach(line -> {
                     String[] parts = line.split(",");
                     if (parts.length >= 2) { // Puede tener 2 o 3 columnas
                         try {
                             int candidateId = Integer.parseInt(parts[0]);
                             int votes = Integer.parseInt(parts[parts.length - 1]);
                             voteCount.put(candidateId, votes);
                         } catch (NumberFormatException e) {
                             System.err.println("Error al parsear línea: " + line);
                         }
                     }
                 });
            System.out.println("Datos de votos cargados desde CSV exitosamente.");
        } catch (IOException e) {
            System.err.println("Error al leer archivo CSV: " + e.getMessage());
        }
    }
    
    private void saveVotesToCSV() {
        try (FileWriter writer = new FileWriter(CSV_FILE_PATH)) {
            // Escribir cabecera
            writer.write("candidateId,candidateName,totalVotes\n");
            
            // Escribir datos
            for (Map.Entry<Integer, Integer> entry : voteCount.entrySet()) {
                int candidateId = entry.getKey();
                String candidateName = getCandidateName(candidateId);
                int totalVotes = entry.getValue();
                
                writer.write(candidateId + "," + candidateName + "," + totalVotes + "\n");
            }
            
            System.out.println("Datos de votos guardados");
        } catch (IOException e) {
            System.err.println("Error al escribir archivo CSV: " + e.getMessage());
        }
    }
    
    private String getCandidateName(int candidateId) {
        if (candidateId >= 0 && candidateId < candidateNames.size()) {
            return candidateNames.get(candidateId);
        }
        return "Desconocido";
    }
    
    public void closeElection(int mesaId) {
        // Generate partial results for this mesa
        generatePartialResults(mesaId);
        // Generate overall results
        generateResumeFile();
    }
    
    public void generateResumeFile() {
        try (FileWriter writer = new FileWriter("resume.csv")) {
            writer.write("candidateId,candidateName,totalVotes\n");
            
            // Write data
            for (Map.Entry<Integer, Integer> entry : voteCount.entrySet()) {
                int candidateId = entry.getKey();
                String candidateName = getCandidateName(candidateId);
                int totalVotes = entry.getValue();
                
                writer.write(candidateId + "," + candidateName + "," + totalVotes + "\n");
            }
            
            System.out.println("Archivo resume.csv generado exitosamente");
        } catch (IOException e) {
            System.err.println("Error al escribir archivo resume.csv: " + e.getMessage());
        }
    }
    
    private void generatePartialResults(int mesaId) {
        String fileName = "partial-" + mesaId + ".csv";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("candidateId,candidateName,totalVotes\n");
            
            // Write data - for partial results we use the same data for a simple demo
            // In a real system, we would filter by mesaId
            for (Map.Entry<Integer, Integer> entry : voteCount.entrySet()) {
                int candidateId = entry.getKey();
                String candidateName = getCandidateName(candidateId);
                int totalVotes = entry.getValue();
                
                writer.write(candidateId + "," + candidateName + "," + totalVotes + "\n");
            }
            
            System.out.println("Archivo " + fileName + " generado exitosamente");
        } catch (IOException e) {
            System.err.println("Error al escribir archivo " + fileName + ": " + e.getMessage());
        }
    }
} 