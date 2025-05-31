package model;

import java.io.Serializable;

public class Vote extends Message implements Serializable {
    private int candidateId;
    private int voteCount;
    
    public Vote(int candidateId, int voteCount) {
        this.candidateId = candidateId;
        this.voteCount = voteCount;
        this.message = "Vote for candidate " + candidateId;
    }
    
    public int getCandidateId() {
        return candidateId;
    }
    
    public int getVoteCount() {
        return voteCount;
    }
}
