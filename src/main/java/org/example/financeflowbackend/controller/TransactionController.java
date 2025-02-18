package org.example.financeflowbackend.controller;

import org.example.financeflowbackend.model.Transaction;
import org.example.financeflowbackend.repository.TransactionRepository;
import org.example.financeflowbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public TransactionController(TransactionRepository transactionRepository, JwtUtil jwtUtil) {
        this.transactionRepository = transactionRepository;
        this.jwtUtil = jwtUtil;
    }

    // GET: Returns transactions for the authenticated user
    @GetMapping
    public List<Transaction> getTransactions(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String userEmail = jwtUtil.validateToken(token);
        return transactionRepository.findByUserEmail(userEmail);
    }

    // POST: Create a new transaction for the authenticated user
    @PostMapping
    public Transaction createTransaction(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody Transaction transaction) {
        String token = authHeader.substring(7);
        String userEmail = jwtUtil.validateToken(token);
        transaction.setUserEmail(userEmail);
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDateTime.now());
        }
        return transactionRepository.save(transaction);
    }

    // GET by ID: Return a transaction (and optionally check if it belongs to the user)
    @GetMapping("/{id}")
    public Transaction getTransactionById(@PathVariable Long id,
                                          @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String userEmail = jwtUtil.validateToken(token);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id " + id));
        if (!transaction.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("Not authorized");
        }
        return transaction;
    }

    // DELETE: Deletes a transaction if it belongs to the authenticated user
    @DeleteMapping("/{id}")
    public void deleteTransaction(@PathVariable Long id,
                                  @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String userEmail = jwtUtil.validateToken(token);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id " + id));
        if (!transaction.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("Not authorized");
        }
        transactionRepository.deleteById(id);
    }
}
