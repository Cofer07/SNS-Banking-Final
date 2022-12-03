package com.example.snsbankingrestapi;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.text.DateFormat;
import java.util.Date;

@Entity // This tells Hibernate to make a table out of this class
public class Transaction {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "transactionId")
    private int transactionId;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date date;
    private double amount;

    private String description;

    private int sendingID;

    private int recievingID;

    public void setSendingAccount(Account sendingAccount) {
        this.sendingAccount = sendingAccount;
    }

    public int getSendingID() {
        return sendingID;
    }

    public void setSendingID(int sendingID) {
        this.sendingID = sendingID;
    }

    public int getRecievingID() {
        return recievingID;
    }

    public void setRecievingID(int recievingID) {
        this.recievingID = recievingID;
    }

    public Account getSendingAccount() {
        return sendingAccount;
    }

    @ManyToOne
    @JoinColumn(name = "sendingaccountid")
    private Account sendingAccount;

    @ManyToOne
    @JoinColumn(name = "receivingaccountid")
    private Account receivingAccount;

     public Transaction(Account sendingAccount, Account receivingAccount, Double amount) {
        this.sendingAccount = sendingAccount;
        this.receivingAccount = receivingAccount;
        this.amount = amount;
    }

    public Transaction() {

    }

    public int getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Account getReceivingAccount() {
        return receivingAccount;
    }

    public void setReceivingAccount(Account receivingAccount) {
        this.receivingAccount = receivingAccount;
    }

    public String getDate(){
         DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT);
         String date = dateFormat.format(new Date());
         return date;
    }

    public void setDate(Date date){
         this.date = date;
    }
}
