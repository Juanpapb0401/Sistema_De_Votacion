import java.util.logging.Logger;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;


public class ProxyCacheServer {
    
    private static final Logger logger = Logger.getLogger(ProxyCacheServer.class.getName());
    private static ProxyCacheServiceImp cacheService;
    
    public static void main(String[] args) {
        
        try (Communicator communicator = Util.initialize(args)) {
            
                
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Cerrando ProxyCache Server...");
                if (cacheService != null) {
                    cacheService.shutdown();
                }
            }));
            
            
            String adapterName = System.getProperty("AdapterName", "ProxyCache-1");
            String endpoints = System.getProperty("Endpoints", "tcp -h localhost");
            
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(adapterName, endpoints);
            
            
            cacheService = new ProxyCacheServiceImp(communicator);
            
            
            String identityName = System.getProperty("IdentityName", "ProxyCache-1");
            Identity identity = Util.stringToIdentity(identityName);
            adapter.add(cacheService, identity);
            
            
            adapter.activate();
            
            logger.info("ProxyCache Server iniciado correctamente");
            logger.info("Adapter: " + adapterName);
            logger.info("Identity: " + identityName);
            logger.info("Endpoints: " + adapter.getEndpoints());
            
            
            communicator.waitForShutdown();
            
        } catch (Exception e) {
            logger.severe("Error en ProxyCache Server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        logger.info("ProxyCache Server terminado");
    }
} 