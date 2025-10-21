package com.charityplatform.backend.config;

import com.charityplatform.backend.contracts.PlatformLedger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
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
        // This creates the main connection object to the blockchain node
        return Web3j.build(new HttpService(rpcUrl));
    }

    @Bean
    public Credentials credentials() {
        // This loads our backend's wallet from the private key
        return Credentials.create(privateKey);
    }

    @Bean
    public PlatformLedger platformLedger(Web3j web3j, Credentials credentials) {
        // This creates a usable Java instance of our smart contract
        // It knows the contract's address and has the credentials to send transactions to it.
        return PlatformLedger.load(contractAddress, web3j, credentials, new DefaultGasProvider());
    }
}