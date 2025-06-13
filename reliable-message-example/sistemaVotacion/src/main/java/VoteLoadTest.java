package sistemaVotacion;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;

import model.Vote;
import reliableMessage.RMDestinationPrx;
import reliableMessage.RMSourcePrx;

public class VoteLoadTest {
    
    private static final int TOTAL_VOTES = 25000;
    private static final int NUM_CANDIDATES = 4;
    private static final boolean USE_REAL_CITIZENS = false;
    
    private Set<String> validCitizens;
    private List<String> citizenList;
    private RMSourcePrx rm;
    private Communicator com;
    private RMDestinationPrx dest;
    private Random random = new Random();
    private AtomicInteger successfulVotes = new AtomicInteger(0);
    private AtomicInteger[] votesPerCandidate;
    
    public VoteLoadTest() {
        this.validCitizens = new HashSet<>();
        this.citizenList = new ArrayList<>();
        this.com = Util.initialize();
        this.rm = RMSourcePrx.checkedCast(com.stringToProxy("Sender:tcp -h localhost -p 10010"));
        this.dest = RMDestinationPrx.uncheckedCast(com.stringToProxy("Service:tcp -h localhost -p 10012"));
        this.votesPerCandidate = new AtomicInteger[NUM_CANDIDATES];
        
        for (int i = 0; i < NUM_CANDIDATES; i++) {
            votesPerCandidate[i] = new AtomicInteger(0);
        }
    }
    
    public static void main(String[] args) {
        VoteLoadTest test = new VoteLoadTest();
        test.loadValidCitizens();
        test.startTest();
    }
    
    private void loadValidCitizens() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Ciudadanos.csv");
            if (inputStream == null) {
                throw new FileNotFoundException("No se pudo encontrar Ciudadanos.csv");
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    validCitizens.add(line);
                    citizenList.add(line);
                }
            }
            reader.close();
            System.out.println("Se han cargado " + validCitizens.size() + " ciudadanos para la prueba.");
        } catch (Exception e) {
            System.out.println("Error al cargar los ciudadanos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void startTest() {
        System.out.println("25000 votos x maquina");
        System.out.println("Iniciando prueba de carga " + TOTAL_VOTES);
        rm.setServerProxy(dest);
        
        long startTime = System.currentTimeMillis();
        // Enviando por lotes
        final int BATCH_SIZE = 100;
        
        for (int i = 0; i < TOTAL_VOTES; i += BATCH_SIZE) {
            final int batchStart = i;
            
            Thread thread = new Thread(() -> {
                int end = Math.min(batchStart + BATCH_SIZE, TOTAL_VOTES);
                for (int j = batchStart; j < end; j++) {
                    sendRandomVote();
                    //DEBUG
                    if (successfulVotes.incrementAndGet() % 1000 == 0) {
                        System.out.println("Votos enviados: " + successfulVotes.get() + "/" + TOTAL_VOTES);
                    }
                }
            });
            
            thread.start();
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (successfulVotes.get() < TOTAL_VOTES) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        long endTime = System.currentTimeMillis();
        double elapsedTimeInSeconds = (endTime - startTime) / 1000.0;
        
        for (int i = 0; i < votesPerCandidate.length; i++) {
            System.out.println("Candidato " + i + ": " + votesPerCandidate[i].get() + " votos");
        }
        
        com.destroy();
    }
    
    private void sendRandomVote() {
        try {
            int candidateId = random.nextInt(NUM_CANDIDATES);
            int userId;
            if (USE_REAL_CITIZENS) {
                String citizenId = citizenList.get(random.nextInt(citizenList.size()));
                userId = Integer.parseInt(citizenId);
            } else {
                userId = 20000 + random.nextInt(10000);
            }
            Vote vote = new Vote(candidateId, userId);
            rm.sendMessage(vote);
            votesPerCandidate[candidateId].incrementAndGet();
            
        } catch (Exception e) {
            System.err.println("Error al enviar voto: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 