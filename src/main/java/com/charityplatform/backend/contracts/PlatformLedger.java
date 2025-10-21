package com.charityplatform.backend.contracts;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.5.4.
 */
@SuppressWarnings("rawtypes")
public class PlatformLedger extends Contract {
    private static final String BINARY = "Bin file was not provided";

    public static final String FUNC_RECORDDONATION = "recordDonation";

    public static final String FUNC_CAMPAIGNBALANCES = "campaignBalances";

    public static final String FUNC_DONATIONHISTORY = "donationHistory";

    public static final String FUNC_NEXTCAMPAIGNID = "nextCampaignId";

    public static final String FUNC_PLATFORMFEEPERCENTAGE = "platformFeePercentage";

    public static final String FUNC_PLATFORMOWNER = "platformOwner";

    public static final String FUNC_WITHDRAWALHISTORY = "withdrawalHistory";

    public static final Event DONATIONRECORDED_EVENT = new Event("DonationRecorded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event WITHDRAWALEXECUTED_EVENT = new Event("WithdrawalExecuted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event WITHDRAWALREQUESTCREATED_EVENT = new Event("WithdrawalRequestCreated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected PlatformLedger(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected PlatformLedger(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected PlatformLedger(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected PlatformLedger(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<DonationRecordedEventResponse> getDonationRecordedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(DONATIONRECORDED_EVENT, transactionReceipt);
        ArrayList<DonationRecordedEventResponse> responses = new ArrayList<DonationRecordedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DonationRecordedEventResponse typedResponse = new DonationRecordedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.campaignId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.donor = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.paymentMethod = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<DonationRecordedEventResponse> donationRecordedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, DonationRecordedEventResponse>() {
            @Override
            public DonationRecordedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(DONATIONRECORDED_EVENT, log);
                DonationRecordedEventResponse typedResponse = new DonationRecordedEventResponse();
                typedResponse.log = log;
                typedResponse.campaignId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.donor = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.paymentMethod = (String) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<DonationRecordedEventResponse> donationRecordedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DONATIONRECORDED_EVENT));
        return donationRecordedEventFlowable(filter);
    }

    public List<WithdrawalExecutedEventResponse> getWithdrawalExecutedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(WITHDRAWALEXECUTED_EVENT, transactionReceipt);
        ArrayList<WithdrawalExecutedEventResponse> responses = new ArrayList<WithdrawalExecutedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            WithdrawalExecutedEventResponse typedResponse = new WithdrawalExecutedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.campaignId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.requestId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.vendor = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<WithdrawalExecutedEventResponse> withdrawalExecutedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, WithdrawalExecutedEventResponse>() {
            @Override
            public WithdrawalExecutedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(WITHDRAWALEXECUTED_EVENT, log);
                WithdrawalExecutedEventResponse typedResponse = new WithdrawalExecutedEventResponse();
                typedResponse.log = log;
                typedResponse.campaignId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.requestId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.vendor = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<WithdrawalExecutedEventResponse> withdrawalExecutedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(WITHDRAWALEXECUTED_EVENT));
        return withdrawalExecutedEventFlowable(filter);
    }

    public List<WithdrawalRequestCreatedEventResponse> getWithdrawalRequestCreatedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(WITHDRAWALREQUESTCREATED_EVENT, transactionReceipt);
        ArrayList<WithdrawalRequestCreatedEventResponse> responses = new ArrayList<WithdrawalRequestCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            WithdrawalRequestCreatedEventResponse typedResponse = new WithdrawalRequestCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.campaignId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.requestId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.requester = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<WithdrawalRequestCreatedEventResponse> withdrawalRequestCreatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, WithdrawalRequestCreatedEventResponse>() {
            @Override
            public WithdrawalRequestCreatedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(WITHDRAWALREQUESTCREATED_EVENT, log);
                WithdrawalRequestCreatedEventResponse typedResponse = new WithdrawalRequestCreatedEventResponse();
                typedResponse.log = log;
                typedResponse.campaignId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.requestId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.requester = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<WithdrawalRequestCreatedEventResponse> withdrawalRequestCreatedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(WITHDRAWALREQUESTCREATED_EVENT));
        return withdrawalRequestCreatedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> recordDonation(BigInteger _campaignId, String _donor, String _paymentMethod) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_RECORDDONATION, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_campaignId), 
                new org.web3j.abi.datatypes.Address(160, _donor), 
                new org.web3j.abi.datatypes.Utf8String(_paymentMethod)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    // Correct version for campaignBalances
    public RemoteFunctionCall<BigInteger> campaignBalances(BigInteger param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_CAMPAIGNBALANCES,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }
    public RemoteFunctionCall<TransactionReceipt> donationHistory(BigInteger param0, BigInteger param1) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_DONATIONHISTORY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0), 
                new org.web3j.abi.datatypes.generated.Uint256(param1)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> nextCampaignId() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_NEXTCAMPAIGNID, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    // Correct version for platformFeePercentage
    public RemoteFunctionCall<BigInteger> platformFeePercentage() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_PLATFORMFEEPERCENTAGE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    // Correct version for platformOwner
    public RemoteFunctionCall<String> platformOwner() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_PLATFORMOWNER,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> withdrawalHistory(BigInteger param0, BigInteger param1) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_WITHDRAWALHISTORY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0), 
                new org.web3j.abi.datatypes.generated.Uint256(param1)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static PlatformLedger load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new PlatformLedger(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static PlatformLedger load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new PlatformLedger(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static PlatformLedger load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new PlatformLedger(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static PlatformLedger load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new PlatformLedger(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class DonationRecordedEventResponse extends BaseEventResponse {
        public BigInteger campaignId;

        public String donor;

        public BigInteger amount;

        public String paymentMethod;
    }

    public static class WithdrawalExecutedEventResponse extends BaseEventResponse {
        public BigInteger campaignId;

        public BigInteger requestId;

        public String vendor;

        public BigInteger amount;
    }

    public static class WithdrawalRequestCreatedEventResponse extends BaseEventResponse {
        public BigInteger campaignId;

        public BigInteger requestId;

        public String requester;

        public BigInteger amount;
    }
}
