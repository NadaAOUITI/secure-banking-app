package com.securebank.service;

import com.securebank.dto.*;
import com.securebank.exception.*;
import com.securebank. model.BankCard;
import com.securebank. repository.BankCardRepository;
import org.springframework.beans. factory.annotation. Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework. transaction.annotation. Transactional;

import java.math. BigDecimal;
import java.time. LocalDate;

@Service
public class CardSecurityService {

    private final BankCardRepository bankCardRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CardSecurityService(BankCardRepository bankCardRepository,
                               PasswordEncoder passwordEncoder) {
        this.bankCardRepository = bankCardRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Vérifie le code PIN d'une carte
     */
    @Transactional
    public boolean verifyPin(Long cardId, Long userId, String pin) {
        BankCard card = getCardForUser(cardId, userId);

        // Vérifier si la carte est bloquée
        if (card.isBlocked()) {
            throw new CardBlockedException(card.getBlockReason());
        }

        // Vérifier si la carte est active
        if (!card.isActive()) {
            throw new CardBlockedException("Carte inactive");
        }

        // Vérifier le PIN
        boolean pinValid = passwordEncoder.matches(pin, card. getPinHash());

        if (pinValid) {
            card.resetFailedAttempts();
            bankCardRepository.save(card);
            return true;
        } else {
            handleFailedPin(card);
            return false; // Ne sera jamais atteint car handleFailedPin lance une exception
        }
    }

    /**
     * Gère une tentative de PIN échouée
     */
    private void handleFailedPin(BankCard card) {
        boolean shouldBlock = card.incrementFailedAttempts();

        if (shouldBlock) {
            card. blockCard("Trop de tentatives PIN incorrectes (" + card.getMaxPinAttempts() + " max)");
        }

        bankCardRepository.save(card);

        throw new InvalidPinException(card.getRemainingAttempts(), shouldBlock);
    }

    /**
     * Valide et exécute une transaction
     */
    @Transactional
    public TransactionResponse processTransaction(Long userId, CardTransactionRequest request) {
        BankCard card = getCardForUser(request.getCardId(), userId);

        // 1. Vérifier le statut de la carte
        validateCardStatus(card);

        // 2. Vérifier le PIN
        if (!passwordEncoder.matches(request.getPin(), card.getPinHash())) {
            handleFailedPin(card);
        }

        // PIN correct - réinitialiser les tentatives
        card.resetFailedAttempts();

        // 3. Réinitialiser les compteurs journaliers si nécessaire
        card.resetDailyCountersIfNeeded();

        // 4.  Valider les limites
        validateLimits(card, request. getAmount());

        // 5. Mettre à jour les compteurs
        card.setDailyTransactionCount(card.getDailyTransactionCount() + 1);
        card.setDailyTransactionTotal(card.getDailyTransactionTotal(). add(request.getAmount()));
        card.setLastTransactionDate(LocalDate.now());

        bankCardRepository.save(card);

        // 6.  Retourner le résultat
        String refNumber = "TXN" + System.currentTimeMillis();
        BigDecimal remainingLimit = card. getDailyTransactionLimit().subtract(card. getDailyTransactionTotal());
        int remainingTx = card.getMaxDailyTransactions() - card. getDailyTransactionCount();

        return TransactionResponse.success(refNumber, request.getAmount(), remainingLimit, remainingTx);
    }

    /**
     * Valide le statut de la carte
     */
    private void validateCardStatus(BankCard card) {
        if (card.isBlocked()) {
            throw new CardBlockedException(card.getBlockReason());
        }
        if (!card.isActive()) {
            throw new CardBlockedException("Carte inactive");
        }
    }

    /**
     * Valide les limites de transaction
     */
    private void validateLimits(BankCard card, BigDecimal amount) {
        // Limite par transaction
        if (amount.compareTo(card.getSingleTransactionLimit()) > 0) {
            throw new TransactionLimitException(
                    TransactionLimitException.LimitType. SINGLE_TRANSACTION,
                    amount,
                    card. getSingleTransactionLimit()
            );
        }

        // Nombre de transactions journalières
        if (card.getDailyTransactionCount() >= card.getMaxDailyTransactions()) {
            throw new TransactionLimitException(
                    TransactionLimitException. LimitType. DAILY_COUNT,
                    BigDecimal.valueOf(card.getDailyTransactionCount() + 1),
                    BigDecimal.valueOf(card.getMaxDailyTransactions())
            );
        }

        // Limite journalière de montant
        BigDecimal newTotal = card.getDailyTransactionTotal(). add(amount);
        if (newTotal.compareTo(card.getDailyTransactionLimit()) > 0) {
            throw new TransactionLimitException(
                    TransactionLimitException.LimitType. DAILY_AMOUNT,
                    newTotal,
                    card. getDailyTransactionLimit()
            );
        }
    }

    /**
     * Obtient le statut d'une carte
     */
    public CardStatusResponse getCardStatus(Long cardId, Long userId) {
        BankCard card = getCardForUser(cardId, userId);
        return CardStatusResponse.fromEntity(card);
    }

    /**
     * Bloque une carte
     */
    @Transactional
    public void blockCard(Long cardId, Long userId, String reason) {
        BankCard card = getCardForUser(cardId, userId);
        card.blockCard(reason != null ? reason : "Blocage demandé par l'utilisateur");
        bankCardRepository.save(card);
    }

    /**
     * Débloque une carte (admin)
     */
    @Transactional
    public void unblockCard(Long cardId) {
        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée"));
        card.unblockCard();
        bankCardRepository. save(card);
    }

    /**
     * Met à jour les limites
     */
    @Transactional
    public void updateLimits(Long cardId, Long userId, BigDecimal singleLimit,
                             BigDecimal dailyLimit, Integer maxTx) {
        BankCard card = getCardForUser(cardId, userId);
        BankCard. CardType type = card.getCardType();

        if (singleLimit != null) {
            if (singleLimit.compareTo(type.getDefaultSingleLimit()) > 0) {
                throw new IllegalArgumentException("Limite max: " + type.getDefaultSingleLimit());
            }
            card.setSingleTransactionLimit(singleLimit);
        }

        if (dailyLimit != null) {
            if (dailyLimit.compareTo(type.getDefaultDailyLimit()) > 0) {
                throw new IllegalArgumentException("Limite max: " + type. getDefaultDailyLimit());
            }
            card.setDailyTransactionLimit(dailyLimit);
        }

        if (maxTx != null) {
            if (maxTx > type.getDefaultMaxTransactions()) {
                throw new IllegalArgumentException("Max transactions: " + type.getDefaultMaxTransactions());
            }
            card.setMaxDailyTransactions(maxTx);
        }

        bankCardRepository.save(card);
    }

    /**
     * Récupère une carte pour un utilisateur
     */
    private BankCard getCardForUser(Long cardId, Long userId) {
        return bankCardRepository.findByIdAndAccount_UserId(cardId, userId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée"));
    }
}