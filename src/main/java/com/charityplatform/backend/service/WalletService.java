package com.charityplatform.backend.service;

import com.charityplatform.backend.model.User;
import com.charityplatform.backend.model.Wallet;
import com.charityplatform.backend.repository.UserRepository;
import com.charityplatform.backend.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Autowired
    public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public User registerWallet(String address, User currentUser) {
        // Re-fetch the user to ensure a managed entity
        User managedUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));

        // Check if this wallet address is already registered to *any* user
        if (walletRepository.existsByAddress(address)) {
            throw new IllegalStateException("This wallet address is already registered to another account.");
        }

        Wallet newWallet = new Wallet();
        newWallet.setUser(managedUser);
        newWallet.setAddress(address);

        managedUser.getWallets().add(newWallet);

        return userRepository.save(managedUser);
    }
}