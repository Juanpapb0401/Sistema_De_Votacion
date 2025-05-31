package services;

import com.zeroc.Ice.Current;

import communication.Notification;
import model.Message;
import model.Vote;
import model.VoteManager;
import reliableMessage.RMDestinationPrx;
import reliableMessage.RMSource;
import threads.RMJob;

public class RMSender implements RMSource{

    private RMJob jobM;
    private Notification notification;

    
    public RMSender(RMJob job, Notification not) {
        notification = not;
        jobM = job;
    }


    @Override
    public void sendMessage(Message msg, Current current) {
        // Verificar si el mensaje es un voto
        if (msg instanceof Vote) {
            Vote vote = (Vote) msg;
            VoteManager.getInstance().registerVote(vote);
            System.out.println("Voto recibido para enviar al candidato " + vote.getCandidateId());
        }
        
        // Agregar a la cola de mensajes para envío
        jobM.add(msg);
    }
    @Override
    public void setServerProxy(RMDestinationPrx destination, Current current){
        notification.setService(destination);
    }


    
}
