package model;

import java.io.Serializable;

public class Vote extends Message implements Serializable {
    private int candidateId;
    private int userId;
    
    public Vote(int candidateId, int userId) {
        this.candidateId = candidateId;
        this.userId = userId;
        this.message = "Vote for candidate " + candidateId + " by user " + userId;
    }
    
    public int getCandidateId() {
        return candidateId;
    }
    
    public int getUserId() {
        return userId;
    }
}
