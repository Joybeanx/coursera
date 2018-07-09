package com.joybean.cryptocurrency.consensus;

import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private int numRounds;
    private int currentRound;
    private boolean[] followees;
    private Set<Transaction> pendingTransactions;
    //Transactions that all compliantNode initialized
    private static Set<Transaction> allCompliantTransactions = new HashSet<>();
    //Store transactions that current node has sent in order to improve efficiency
    // Current node will send a transaction to its followers once among all rounds
    private Set<Transaction> currentSentTransactions = new HashSet<>();
    private Set<Integer> maliciousNode = new HashSet<>();

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
        allCompliantTransactions.addAll(pendingTransactions);
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
        Set<Integer> senders = new HashSet<>();
        for (Candidate candidate : candidates) {
            senders.add(candidate.sender);
            //The node whose transaction is not included in allCompliantTransactions will be treated as malicious node.
            //No need to check candidate.sender is the followee of current node,because Simulation has handled it.
            if (!allCompliantTransactions.contains(candidate.tx)) {
                maliciousNode.add(candidate.sender);
            }
        }
        for (int i = 0; i < followees.length; i++) {
            //The followee who doesn't provide candidate in a round will be treated as malicious node
            if (!senders.contains(i))
                maliciousNode.add(i);
        }
        for (Candidate candidate : candidates) {
            //Only add transaction that isn't provided by malicious node and hasn't been sent ever
            if (!maliciousNode.contains(candidate.sender) && !currentSentTransactions.contains(candidate.tx)) {
                pendingTransactions.add(candidate.tx);
            }
        }
    }
}
