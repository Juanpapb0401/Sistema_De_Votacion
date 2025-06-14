import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import model.Vote;

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
    
    public static void main(String[] args) {
        int status = 0;
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        // Try with resources block - communicator se destruye automáticamente
        try(Communicator communicator = Util.initialize(args, extraArgs)) {
            
            // Shutdown hook para destruir communicator durante JVM shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> communicator.destroy()));

            if(!extraArgs.isEmpty()) {
                System.err.println("too many arguments");
                status = 1;
            } else {
                VotingServiceImp imp = new VotingServiceImp();
                
                // Crear adapter - el nombre debe coincidir con application.xml
                ObjectAdapter adapter = communicator.createObjectAdapter("Server");
                
                // Obtener Identity desde las propiedades (configurado en IceGrid)
                com.zeroc.Ice.Properties properties = communicator.getProperties();
                Identity id = Util.stringToIdentity(properties.getProperty("Identity"));
                
                adapter.add(imp, id);
                adapter.activate();

                System.out.println("Servidor de votación iniciado correctamente");
                communicator.waitForShutdown();
            }
        }
        
        System.exit(status);
    }
        
}
