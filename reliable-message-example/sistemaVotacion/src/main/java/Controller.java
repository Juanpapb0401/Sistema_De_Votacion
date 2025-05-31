package sistemaVotacion;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    public Controller() {
        this.candidates = new ArrayList<>();
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
        System.out.println("Ingrese su cedula: ");
        userId = Integer.parseInt(System.console().readLine());
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
