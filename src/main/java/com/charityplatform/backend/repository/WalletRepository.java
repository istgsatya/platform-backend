package com.charityplatform.backend.repository;

import com.charityplatform.backend.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Checks if a wallet with the given address already exists in the database.
     * This is crucial to prevent the same wallet from being registered to multiple users.
     *
     * @param address The Ethereum address to check.
     * @return true if the wallet exists, false otherwise.
     */
    boolean existsByAddress(String address);
}