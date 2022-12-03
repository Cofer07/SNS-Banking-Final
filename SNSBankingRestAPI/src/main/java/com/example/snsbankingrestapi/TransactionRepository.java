package com.example.snsbankingrestapi;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransactionRepository  extends CrudRepository<Transaction, Integer> {

//    @Query(value= "select t from Transaction where t.transaction.sendingaccountid = ?1")
//    List<Transaction> findTransactionBySendingAccountId(int transactionId);

}
