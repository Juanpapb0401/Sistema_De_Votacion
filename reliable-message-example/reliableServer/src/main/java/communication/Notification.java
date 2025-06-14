package communication;

import com.zeroc.Ice.ConnectionRefusedException;

import model.ReliableMessage;
import reliableMessage.ACKServicePrx;
import reliableMessage.RMDestinationPrx;


public class Notification {


    private RMDestinationPrx service;
    private ACKServicePrx ackService;
    private boolean connectionWarningShown = false;

    public void setAckService(ACKServicePrx ackService) {
        this.ackService = ackService;
    }

    public void setService(RMDestinationPrx service) {
        System.out.println("Configurando servicio con proxy: " + service);
        this.service = service;
        // Resetear el flag cuando se establece un nuevo servicio
        connectionWarningShown = false;
    }

    public boolean sendMessage(ReliableMessage message) throws ConnectionRefusedException {
        try {
            System.out.println("Intentando enviar mensaje al servidor...");
            service.reciveMessage(message, ackService);
            // Si llegamos aquí, la conexión fue exitosa
            connectionWarningShown = false;
            System.out.println("Mensaje enviado exitosamente");
            return true;
        } catch (ConnectionRefusedException e) {
            if (!connectionWarningShown) {
                System.out.println("Esperando conexión con el servidor, los mensajes quedarán guardados hasta establecer conexión");
                connectionWarningShown = true;
            }
            throw e;
        }
    }
}
