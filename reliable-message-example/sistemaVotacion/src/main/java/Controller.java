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
        ConexionBD conexion = new ConexionBD();
        int mesaActual = MesaConfig.getMesaId();

        boolean valid = false;
        String citizenId = "";

        while (!valid) {
            System.out.print("Ingrese su cédula: ");
            citizenId = leerLinea();

            try {
                java.util.List<java.util.Map<String, Object>> res =
                        conexion.getInfoBDWithParams("SELECT mesa_id FROM ciudadano WHERE documento = ?", citizenId);

                if (res.isEmpty()) {
                    System.out.println("La cédula no se encuentra registrada.");
                } else {
                    int mesaAsignada = ((Number) res.get(0).get("mesa_id")).intValue();
                    if (mesaAsignada == mesaActual) {
                        // Verificar si ya votó
                        if (VoteRegistry.hasVoted(citizenId)) {
                            System.out.println("Esta cédula ya registró su voto en esta mesa.");
                        } else {
                            valid = true;
                            userId = Integer.parseInt(citizenId);
                        }
                    } else {
                        System.out.println("Esta cédula no está asignada a la mesa actual.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error consultando la base de datos: " + e.getMessage());
            }

            if (!valid) {
                System.out.print("¿Desea intentar con otra cédula? (S/N): ");
                String opt = leerLinea();
                if (!opt.equalsIgnoreCase("S")) {
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
        // Registrar que el ciudadano ya votó
        VoteRegistry.register(String.valueOf(userId));
        startUI();
    }

    public void readCandidates() throws FileNotFoundException {
        try {
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

    public void getInfoFromBD() {
        ConexionBD conexion = new ConexionBD();
        String sql = "SELECT * FROM votos";
        try {
            java.util.List<java.util.Map<String, Object>> rows = conexion.getInfoBD(sql);
            System.out.println("Información obtenida de la BD:");
            for (java.util.Map<String, Object> row : rows) {
                System.out.println(row);
            }
        } catch (Exception e) {
            System.err.println("Error al obtener información de la BD: " + e.getMessage());
        }
    }

    private String leerLinea() {
        if (System.console() != null) {
            return System.console().readLine();
        }
        java.util.Scanner sc = new java.util.Scanner(System.in);
        return sc.nextLine();
    }
}
