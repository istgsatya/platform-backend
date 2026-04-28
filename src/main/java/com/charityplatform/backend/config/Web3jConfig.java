package com.charityplatform.backend.config;

import com.charityplatform.backend.contracts.PlatformLedger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

@Configuration
public class Web3jConfig {

    @Value("${blockchain.private-key}")
    private String privateKey;

    @Value("${blockchain.rpc-url}")
    private String rpcUrl;

    @Value("${blockchain.contract-address}")
    private String contractAddress;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(rpcUrl));
    }

    @Bean
    public Credentials credentials() {
        return Credentials.create(privateKey);
    }

    // THIS IS THE BEAN THAT WILL BE FIXED
    @Bean
    public PlatformLedger platformLedger(Web3j web3j, Credentials credentials) {
        // Sepolia's chain ID
        long chainId = 11155111L;

        // Create the TransactionManager with the explicit chainId
        TransactionManager transactionManager = new RawTransactionManager(web3j, credentials, chainId);

        // Load the contract using the TransactionManager, which BYPASSES the ENS lookup.
        return PlatformLedger.load(contractAddress, web3j, transactionManager, new DefaultGasProvider());
    }
}