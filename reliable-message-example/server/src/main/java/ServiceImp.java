import com.zeroc.Ice.Current;

import model.ReliableMessage;
import model.Vote;
import reliableMessage.ACKServicePrx;
import reliableMessage.RMDestination;

public class ServiceImp implements RMDestination {

    @Override
    public void reciveMessage(ReliableMessage rmessage, ACKServicePrx prx, Current current) {
        System.out.println(rmessage.getMessage().message);
        
        if (rmessage.getMessage() instanceof Vote) {
            Vote vote = (Vote) rmessage.getMessage();
            VoteManager.getInstance().registerVote(vote);
        }
        
        prx.ack(rmessage.getUuid());
    }
}
