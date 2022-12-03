package com.example.snsbankingrestapi;

import com.example.snsbankingrestapi.Account;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepository  extends CrudRepository<Account, Integer> {

    @Query(value= "select a from Account a where a.user.userid = ?1")
    List<Account> findByUserId (int userId);
}
