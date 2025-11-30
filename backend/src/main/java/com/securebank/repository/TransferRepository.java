package com.securebank. repository;

import com.securebank. model.Transfer;
import com.securebank. model.User;
import com.securebank. model.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data. jpa.repository. JpaRepository;
import org.springframework. data.jpa. repository.Query;
import org.springframework. data.repository.query.Param;
import org.springframework. stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util. List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    // Find by user
    List<Transfer> findByUserOrderByCreatedAtDesc(User user);

    Page<Transfer> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // Find by reference
    Optional<Transfer> findByReference(String reference);

    Optional<Transfer> findByReferenceAndUser(String reference, User user);

    // Find by status
    List<Transfer> findByUserAndStatusOrderByCreatedAtDesc(User user, Transfer.TransferStatus status);

    // Find by account
    List<Transfer> findBySenderAccountOrderByCreatedAtDesc(BankAccount account);

    // Find by date range
    @Query("SELECT t FROM Transfer t WHERE t. user = :user AND t. createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transfer> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Find by amount range
    @Query("SELECT t FROM Transfer t WHERE t.user = :user AND t.amount BETWEEN :minAmount AND :maxAmount ORDER BY t.createdAt DESC")
    List<Transfer> findByUserAndAmountRange(
            @Param("user") User user,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount
    );

    // Find by beneficiary
    @Query("SELECT t FROM Transfer t WHERE t. user = :user AND t.beneficiary. id = :beneficiaryId ORDER BY t.createdAt DESC")
    List<Transfer> findByUserAndBeneficiary(
            @Param("user") User user,
            @Param("beneficiaryId") Long beneficiaryId
    );

    // Count transfers today for rate limiting
    @Query("SELECT COUNT(t) FROM Transfer t WHERE t.user = :user AND t.createdAt >= :startOfDay")
    long countTodayTransfersByUser(@Param("user") User user, @Param("startOfDay") LocalDateTime startOfDay);

    // Sum of today's transfers for daily limit
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t WHERE t. user = :user AND t.status = 'COMPLETED' AND t.createdAt >= :startOfDay")
    BigDecimal sumTodayTransfersByUser(@Param("user") User user, @Param("startOfDay") LocalDateTime startOfDay);

    // Check for duplicate transfer (anti-replay)
    @Query("SELECT COUNT(t) > 0 FROM Transfer t WHERE t.user = :user AND t.beneficiary.id = :beneficiaryId AND t.amount = :amount AND t.createdAt >= :since")
    boolean existsDuplicateTransfer(
            @Param("user") User user,
            @Param("beneficiaryId") Long beneficiaryId,
            @Param("amount") BigDecimal amount,
            @Param("since") LocalDateTime since
    );

    // Pending transfers requiring OTP
    List<Transfer> findByUserAndStatusAndOtpVerifiedFalse(User user, Transfer.TransferStatus status);
}