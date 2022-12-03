package com.example.snsbankingrestapi;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
@Entity // This tells Hibernate to make a table out of this class
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int userid;

    private String name;

    @Column(nullable = false, length=64)
    private String password;

    @Column(name= "first_name", nullable = false, length = 20)
    private String fname;

    @Column(name="last_name", nullable= false, length=20)
    private String lname;

    @Column(nullable = false, unique = true, length= 45)
    private String email;

    @Column(nullable = true, unique = false, length=15)
    private String phoneNum;

    @Column(nullable = true, unique = false, length=150)
    private String address;

    @Column(nullable = true, unique = false, length=9)
    private int SIN;

    //Avatars need to be stored next, the datatype should probably be byte[]

    @OneToMany(mappedBy="user", fetch = FetchType.EAGER)
    private List<Account> accounts = new ArrayList<>();

    public String getAddress() {
        return address;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public void addAccount(Account a){
        accounts.add(a);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(Integer id) {
        this.userid = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public int getSIN() {
        return SIN;
    }

    public void setSIN(int SIN) {
        this.SIN = SIN;
    }
}
