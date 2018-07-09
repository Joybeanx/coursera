package com.joybean.cryptocurrency.consensus;

import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private double pgraph;
    private double pmalicous;
    private double ptxDistribution;
    private int numRounds;
    private int currentRound;
    private boolean[] followees;
    private Set<Transaction> pendingTransactions;
    private static Set<Transaction> allSentTransactions = new HashSet<>();
    private Set<Transaction> currentSentTransactions = new HashSet<>();

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.pgraph = p_graph;
        this.pmalicous = p_malicious;
        this.ptxDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
        allSentTransactions.addAll(pendingTransactions);
    }

    public Set<Transaction> sendToFollowers() {
        Set<Transaction> pts = pendingTransactions;
        currentSentTransactions.addAll(pts);
        pendingTransactions = new HashSet<>();
        if (currentRound == numRounds) {
            return currentSentTransactions;
        }
        currentRound++;
        return pts;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            if (followees[candidate.sender] && allSentTransactions.contains(candidate.tx) && !currentSentTransactions.contains(candidate.tx)) {
                pendingTransactions.add(candidate.tx);
            }
        }
    }
}
