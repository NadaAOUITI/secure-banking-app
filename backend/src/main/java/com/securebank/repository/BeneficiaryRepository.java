package com.securebank.repository;


import com.securebank.model.Beneficiary;
import com.securebank. model.User;
import org.springframework. data.jpa. repository.JpaRepository;
import org. springframework.data.jpa.repository.Query;
import org. springframework.data.repository.query.Param;
import org. springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByUserAndIsActiveTrue(User user);

    List<Beneficiary> findByUser(User user);

    Optional<Beneficiary> findByIdAndUser(Long id, User user);

    @Query("SELECT b FROM Beneficiary b WHERE b.user = :user AND b.accountNumber = :accountNumber AND b.isActive = true")
    Optional<Beneficiary> findByUserAndAccountNumber(@Param("user") User user, @Param("accountNumber") String accountNumber);

    @Query("SELECT COUNT(b) FROM Beneficiary b WHERE b.user = :user AND b.isActive = true")
    long countActiveByUser(@Param("user") User user);

    boolean existsByUserAndAccountNumberAndIsActiveTrue(User user, String accountNumber);
}