package com.securebank.repository;

import com.securebank.model.BankAccount;
import com.securebank.model.BankCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Long> {
    
    List<BankCard> findByAccountAndIsActiveTrue(BankAccount account);
    
    List<BankCard> findByAccount_UserIdAndIsActiveTrue(Long userId);
}