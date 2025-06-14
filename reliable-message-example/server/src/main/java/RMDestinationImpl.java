import com.zeroc.Ice.Current;
import model.ReliableMessage;
import model.Vote;
import reliableMessage.ACKServicePrx;
import reliableMessage.RMDestination;
import java.io.FileInputStream;
import java.util.Properties;

public class RMDestinationImpl implements RMDestination {
    
    private static int getMesaId() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("mesa.properties"));
            return Integer.parseInt(props.getProperty("mesaId"));
        } catch (Exception e) {
            System.err.println("Error al leer mesa.properties: " + e.getMessage());
            return 1; // valor por defecto
        }
    }
    
    @Override
    public void reciveMessage(ReliableMessage rmessage, ACKServicePrx prx, Current current) {
        try {
            // Verificar si el mensaje es un voto
            if (rmessage.getMessage() instanceof Vote) {
                Vote vote = (Vote) rmessage.getMessage();
                // Registrar el voto con mesaId del archivo properties
                int mesaId = getMesaId();
                Server.addNewVotes(vote.getCandidateId(), vote.getUserId(), mesaId);
                System.out.println("Voto recibido y registrado: Candidato " + vote.getCandidateId() + 
                                  ", Usuario " + vote.getUserId() + ", Mesa " + mesaId);
            }
            
            // Enviar confirmación
            prx.ack(rmessage.getUuid());
            
        } catch (Exception e) {
            System.err.println("Error al procesar mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 