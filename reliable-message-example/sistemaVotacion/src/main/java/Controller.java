package sistemaVotacion;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;

import model.Vote;
import reliableMessage.RMDestinationPrx;
import reliableMessage.RMSourcePrx;


public class Controller {
    
    private List<String> candidates;
    private RMSourcePrx rm;
    private Communicator com;
    private RMDestinationPrx dest;
    private int userId;
    private boolean isTxtLoaded = false;
    private Set<String> validCitizens;
    private boolean isValidCitizensLoaded = false;

    public Controller() {
        this.candidates = new ArrayList<>();
        this.validCitizens = new HashSet<>();
        this.com = Util.initialize();
        this.rm = RMSourcePrx.checkedCast(com.stringToProxy("Sender:tcp -h localhost -p 10010"));
        this.dest = RMDestinationPrx.uncheckedCast(com.stringToProxy("Service:tcp -h localhost -p 10012"));
    }

    public static void main(String[] args)throws Exception {
        // rm.setServerProxy(dest);
        /*Message msg = new Message();
        for (int i = 0; i < 10; i++) {
            msg.message = "Send with RM "+i;
            rm.sendMessage(msg);
            System.out.println("sended "+i);
            Thread.sleep(5000);
        }
        com.shutdown();
        */
    }

    public void login() {
        if (!isValidCitizensLoaded) {
            loadValidCitizens();
            isValidCitizensLoaded = true;
        }
        
        boolean isValidCitizen = false;
        String citizenId = "";
        
        while (!isValidCitizen) {
            System.out.println("Ingrese su cedula: ");
            citizenId = System.console().readLine();
            
            if (validCitizens.contains(citizenId)) {
                isValidCitizen = true;
                userId = Integer.parseInt(citizenId);
            } else {
                System.out.println("Lo sentimos, usted no esta habilitado para votar en esta mesa de votacion.");
                System.out.println("Desea intentar con otra cedula? S/N: ");
                String option = System.console().readLine();
                if (!option.equalsIgnoreCase("S")) {
                    System.exit(0);
                }
            }
        }
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
                }
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Error al cargar los ciudadanos habilitados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void startUI() throws FileNotFoundException{
        login();
        System.out.println("Bienvenido al sistema de votacion");
        System.out.println("1. Iniciar votacion");
        System.out.println("2. Salir");
        int option = Integer.parseInt(System.console().readLine());
        switch(option) {
            case 1:
                startVotation();
                break;
            case 2:
                System.out.println("Saliendo del sistema");
                break;
        }
    }

    public void startVotation() throws FileNotFoundException {
        rm.setServerProxy(dest);
        System.out.println("Estos son los candidatos disponibles: ");
        if (!isTxtLoaded) {
            readCandidates();
            isTxtLoaded = true;
        }
        for (int i = 0; i < candidates.size(); i++) {
            System.out.println(i + ". " + candidates.get(i));
        }
        System.out.println("Ingrese el numero del candidato que desea votar: ");
        int candidateNumber = Integer.parseInt(System.console().readLine());
        Vote vote = new Vote(candidateNumber, userId);
        addVote(vote);
        startUI();
    }

    public void readCandidates() throws FileNotFoundException {
        try {
            // Cargar como recurso desde el classpath
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Candidatos.txt");
            if (inputStream == null) {
                throw new FileNotFoundException("No se pudo encontrar Candidatos.txt");
            }
            
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                candidates.add(line);
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("Error al leer el archivo Candidatos.txt");
            e.printStackTrace();
        }
    }

    public void addVote(Vote vote) throws FileNotFoundException {
        rm.sendMessage(vote);
        System.out.println("Voto agregado correctamente");
    }
}
