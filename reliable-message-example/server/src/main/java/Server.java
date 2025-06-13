import com.zeroc.Ice.Communicator;
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
        Communicator com = Util.initialize();
        ServiceImp imp = new ServiceImp();
        ObjectAdapter adapter = com.createObjectAdapterWithEndpoints("Server", "tcp -h localhost -p 10012");
        adapter.add(imp, Util.stringToIdentity("Service"));
        adapter.activate();
        com.waitForShutdown();
    }
}
