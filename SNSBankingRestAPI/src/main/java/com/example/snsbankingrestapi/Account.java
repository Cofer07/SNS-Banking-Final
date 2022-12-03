package com.example.snsbankingrestapi;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity // This tells Hibernate to make a table out of this class
public class Account {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private int accountid;

    private String type;

    private Double balance;

    @ManyToOne
    @JoinColumn(name="userid", nullable=false)
    private User user;

    @OneToMany(mappedBy = "sendingAccount")
    private List<Transaction> sent_transactions = new ArrayList<>();

    public Account() {
    }

    public void addSentTransaction(Transaction t){
        this.sent_transactions.add(t);
    }

    @OneToMany(mappedBy = "receivingAccount")
    private List<Transaction> received_transactions = new ArrayList<>();

    public void addReceivedTransaction(Transaction t){
        this.received_transactions.add(t);
    }

    public int getAccountid() {
        return accountid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Account(User user){
        this.user = user;
    }

    public List<Transaction> getReceived_transactions() {
        return received_transactions;
    }

    public List<Transaction> getSent_transactions() {
        return sent_transactions;
    }
}