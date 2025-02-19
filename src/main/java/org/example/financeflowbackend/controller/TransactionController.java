package org.example.financeflowbackend.controller;

import org.example.financeflowbackend.model.Transaction;
import org.example.financeflowbackend.repository.TransactionRepository;
import org.example.financeflowbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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

    // GET by ID: Return a transaction for the authenticated user
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
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id,
                                               @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String userEmail = jwtUtil.validateToken(token);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id " + id));
        if (!transaction.getUserEmail().equals(userEmail)) {
            return ResponseEntity.status(403).body("Not authorized to delete this transaction");
        }
        transactionRepository.deleteById(id);
        return ResponseEntity.ok("Transaction deleted successfully");
    }

    // PUT: Update an existing transaction if it belongs to the authenticated user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id,
                                               @RequestHeader("Authorization") String authHeader,
                                               @RequestBody Transaction updatedTxn) {
        String token = authHeader.substring(7);
        String userEmail = jwtUtil.validateToken(token);
        Transaction existingTxn = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id " + id));
        if (!existingTxn.getUserEmail().equals(userEmail)) {
            return ResponseEntity.status(403).body("Not authorized to update this transaction");
        }
        // Update fields
        existingTxn.setAmount(updatedTxn.getAmount());
        existingTxn.setDescription(updatedTxn.getDescription());
        existingTxn.setCategory(updatedTxn.getCategory());
        if (updatedTxn.getDate() != null) {
            existingTxn.setDate(updatedTxn.getDate());
        }
        Transaction savedTxn = transactionRepository.save(existingTxn);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Transaction updated successfully");
        response.put("transaction", savedTxn);
        return ResponseEntity.ok(response);
    }
}
