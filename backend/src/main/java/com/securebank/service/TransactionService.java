package com. securebank.service;

import com.securebank.dto.TransactionDTO;
import com.securebank.model.BankAccount;
import com.securebank.model.Transaction;
import com.securebank.repository.BankAccountRepository;
import com.securebank.repository. TransactionRepository;
import org. springframework.beans.factory.annotation. Autowired;
import org. springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              BankAccountRepository bankAccountRepository) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    /**
     * Récupérer toutes les transactions d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByUser(Long userId) {
        // ✅ CORRECTION : Utilisation de findByUserId
        List<BankAccount> accounts = bankAccountRepository.findByUserId(userId);
        List<TransactionDTO> allTransactions = new ArrayList<>();

        for (BankAccount account : accounts) {
            List<Transaction> accountTransactions = transactionRepository
                    .findByAccountIdOrderByDateDesc(account.getId());

            for (Transaction transaction : accountTransactions) {
                allTransactions.add(convertToDTO(transaction));
            }
        }

        // Trier par date décroissante
        allTransactions.sort((t1, t2) -> t2.getDate(). compareTo(t1.getDate()));

        return allTransactions;
    }

    /**
     * Récupérer les transactions d'un compte spécifique appartenant à l'utilisateur
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByAccountAndUser(Long accountId, Long userId) {
        BankAccount account = bankAccountRepository. findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        // Vérifier que le compte appartient à l'utilisateur
        if (!account.getUser().getId().equals(userId)) {
            throw new SecurityException("Compte non autorisé");
        }

        List<Transaction> transactions = transactionRepository
                .findByAccountIdOrderByDateDesc(accountId);

        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer une transaction spécifique
     */
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                . orElseThrow(() -> new RuntimeException("Transaction non trouvée"));

        // Vérifier que la transaction appartient à l'utilisateur
        if (! transaction.getAccount().getUser().getId().equals(userId)) {
            throw new SecurityException("Transaction non autorisée");
        }

        return convertToDTO(transaction);
    }

    /**
     * Créer une nouvelle transaction
     */
    @Transactional
    public Transaction createTransaction(Long accountId, String type, BigDecimal amount,
                                         String description, String reference) {
        BankAccount account = bankAccountRepository. findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction. setReference(reference);
        transaction.setDate(LocalDateTime.now());

        // Mettre à jour le solde du compte
        if ("credit".equalsIgnoreCase(type)) {
            account.setBalance(account.getBalance().add(amount));
        } else if ("debit".equalsIgnoreCase(type)) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("Solde insuffisant");
            }
            account.setBalance(account.getBalance().subtract(amount));
        }

        bankAccountRepository.save(account);
        return transactionRepository.save(transaction);
    }

    /**
     * Obtenir des statistiques sur les transactions
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTransactionStatistics(Long userId) {
        List<TransactionDTO> transactions = getTransactionsByUser(userId);

        Map<String, Object> stats = new HashMap<>();

        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalDebit = BigDecimal.ZERO;
        int creditCount = 0;
        int debitCount = 0;

        for (TransactionDTO transaction : transactions) {
            if ("credit".equalsIgnoreCase(transaction.getType())) {
                totalCredit = totalCredit.add(transaction.getAmount());
                creditCount++;
            } else if ("debit".equalsIgnoreCase(transaction.getType())) {
                totalDebit = totalDebit.add(transaction.getAmount());
                debitCount++;
            }
        }

        stats.put("totalTransactions", transactions.size());
        stats.put("totalCredit", totalCredit);
        stats. put("totalDebit", totalDebit);
        stats.put("creditCount", creditCount);
        stats.put("debitCount", debitCount);
        stats.put("balance", totalCredit.subtract(totalDebit));

        return stats;
    }

    /**
     * Convertir une Transaction en TransactionDTO
     */
    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction. getId());
        dto.setAccountId(transaction.getAccount().getId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setReference(transaction.getReference());
        dto.setDate(transaction.getDate());
        return dto;
    }
    /**
     * Créer une nouvelle transaction avec vérification de propriété
     */
    @Transactional
    public Transaction createTransaction(Long accountId, Long userId, String type,
                                         BigDecimal amount, String description, String reference) {

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        // Vérifier que le compte appartient à l'utilisateur
        if (! account.getUser(). getId().equals(userId)) {
            throw new SecurityException("Compte non autorisé");
        }

        // Vérifier le solde pour les débits
        if ("debit".equalsIgnoreCase(type)) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("Solde insuffisant.  Solde disponible: " + account.getBalance() + " MAD");
            }
        }

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(type. toLowerCase());
        transaction.setAmount(amount);
        transaction. setDescription(description);
        transaction.setReference(reference);
        transaction.setDate(LocalDateTime.now());

        // Mettre à jour le solde du compte
        if ("credit".equalsIgnoreCase(type)) {
            account.setBalance(account.getBalance().add(amount));
        } else if ("debit".equalsIgnoreCase(type)) {
            account.setBalance(account.getBalance(). subtract(amount));
        }

        bankAccountRepository.save(account);
        return transactionRepository.save(transaction);
    }
}