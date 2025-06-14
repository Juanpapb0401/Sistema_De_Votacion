import com.zeroc.Ice.Current;
import model.ReliableMessage;
import model.Vote;
import reliableMessage.ACKServicePrx;
import reliableMessage.RMDestination;

public class RMDestinationImpl implements RMDestination {
    
    @Override
    public void reciveMessage(ReliableMessage rmessage, ACKServicePrx prx, Current current) {
        try {
            // Verificar si el mensaje es un voto
            if (rmessage.getMessage() instanceof Vote) {
                Vote vote = (Vote) rmessage.getMessage();
                // Registrar el voto
                Server.addNewVotes(vote.getCandidateId(), vote.getUserId());
                System.out.println("Voto recibido y registrado: Candidato " + vote.getCandidateId() + 
                                  ", Usuario " + vote.getUserId());
            }
            
            // Enviar confirmación
            prx.ack(rmessage.getUuid());
            
        } catch (Exception e) {
            System.err.println("Error al procesar mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 