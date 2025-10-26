package com.charityplatform.backend.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple12;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.7.0.
 */
@SuppressWarnings("rawtypes")
public class PlatformLedger extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_ADMINAPPROVEREQUEST = "adminApproveRequest";

    public static final String FUNC_CREATEWITHDRAWALREQUEST = "createWithdrawalRequest";

    public static final String FUNC_EXECUTEREQUEST = "executeRequest";

    public static final String FUNC_RECORDDONATION = "recordDonation";

    public static final String FUNC_VOTEONREQUEST = "voteOnRequest";

    public static final String FUNC_CAMPAIGNBALANCES = "campaignBalances";

    public static final String FUNC_CAMPAIGNCONTRIBUTIONS = "campaignContributions";

    public static final String FUNC_DONATIONHISTORY = "donationHistory";

    public static final String FUNC_HASVOTED = "hasVoted";

    public static final String FUNC_NEXTREQUESTID = "nextRequestId";

    public static final String FUNC_PLATFORMFEEPERCENTAGE = "platformFeePercentage";

    public static final String FUNC_PLATFORMOWNER = "platformOwner";

    public static final String FUNC_WITHDRAWALREQUESTS = "withdrawalRequests";

    public static final Event DONATIONRECORDED_EVENT = new Event("DonationRecorded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event REQUESTFINALIZED_EVENT = new Event("RequestFinalized", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint8>() {}));
    ;

    public static final Event VOTECAST_EVENT = new Event("VoteCast", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event WITHDRAWALEXECUTED_EVENT = new Event("WithdrawalExecuted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event WITHDRAWALREQUESTCREATED_EVENT = new Event("WithdrawalRequestCreated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>(true) {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected PlatformLedger(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
                             BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected PlatformLedger(String contractAddress, Web3j web3j, Credentials credentials,
                             ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected PlatformLedger(String contractAddress, Web3j web3j, TransactionManager transactionManager,
                             BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected PlatformLedger(String contractAddress, Web3j web3j, TransactionManager transactionManager,
                             ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<DonationRecordedEventResponse> getDonationRecordedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DONATIONRECORDED_EVENT, transactionReceipt);
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

    public static DonationRecordedEventResponse getDonationRecordedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DONATIONRECORDED_EVENT, log);
        DonationRecordedEventResponse typedResponse = new DonationRecordedEventResponse();
        typedResponse.log = log;
        typedResponse.campaignId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.donor = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.paymentMethod = (String) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<DonationRecordedEventResponse> donationRecordedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDonationRecordedEventFromLog(log));
    }

    public Flowable<DonationRecordedEventResponse> donationRecordedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DONATIONRECORDED_EVENT));
        return donationRecordedEventFlowable(filter);
    }

    public static List<RequestFinalizedEventResponse> getRequestFinalizedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(REQUESTFINALIZED_EVENT, transactionReceipt);
        ArrayList<RequestFinalizedEventResponse> responses = new ArrayList<RequestFinalizedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RequestFinalizedEventResponse typedResponse = new RequestFinalizedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newStatus = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static RequestFinalizedEventResponse getRequestFinalizedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(REQUESTFINALIZED_EVENT, log);
        RequestFinalizedEventResponse typedResponse = new RequestFinalizedEventResponse();
        typedResponse.log = log;
        typedResponse.requestId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newStatus = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<RequestFinalizedEventResponse> requestFinalizedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getRequestFinalizedEventFromLog(log));
    }

    public Flowable<RequestFinalizedEventResponse> requestFinalizedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REQUESTFINALIZED_EVENT));
        return requestFinalizedEventFlowable(filter);
    }

    public static List<VoteCastEventResponse> getVoteCastEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(VOTECAST_EVENT, transactionReceipt);
        ArrayList<VoteCastEventResponse> responses = new ArrayList<VoteCastEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            VoteCastEventResponse typedResponse = new VoteCastEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.voter = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.approve = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.votingPower = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static VoteCastEventResponse getVoteCastEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(VOTECAST_EVENT, log);
        VoteCastEventResponse typedResponse = new VoteCastEventResponse();
        typedResponse.log = log;
        typedResponse.requestId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.voter = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.approve = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.votingPower = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<VoteCastEventResponse> voteCastEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getVoteCastEventFromLog(log));
    }

    public Flowable<VoteCastEventResponse> voteCastEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(VOTECAST_EVENT));
        return voteCastEventFlowable(filter);
    }

    public static List<WithdrawalExecutedEventResponse> getWithdrawalExecutedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(WITHDRAWALEXECUTED_EVENT, transactionReceipt);
        ArrayList<WithdrawalExecutedEventResponse> responses = new ArrayList<WithdrawalExecutedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            WithdrawalExecutedEventResponse typedResponse = new WithdrawalExecutedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.vendor = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static WithdrawalExecutedEventResponse getWithdrawalExecutedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(WITHDRAWALEXECUTED_EVENT, log);
        WithdrawalExecutedEventResponse typedResponse = new WithdrawalExecutedEventResponse();
        typedResponse.log = log;
        typedResponse.requestId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.vendor = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<WithdrawalExecutedEventResponse> withdrawalExecutedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getWithdrawalExecutedEventFromLog(log));
    }

    public Flowable<WithdrawalExecutedEventResponse> withdrawalExecutedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(WITHDRAWALEXECUTED_EVENT));
        return withdrawalExecutedEventFlowable(filter);
    }

    public static List<WithdrawalRequestCreatedEventResponse> getWithdrawalRequestCreatedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(WITHDRAWALREQUESTCREATED_EVENT, transactionReceipt);
        ArrayList<WithdrawalRequestCreatedEventResponse> responses = new ArrayList<WithdrawalRequestCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            WithdrawalRequestCreatedEventResponse typedResponse = new WithdrawalRequestCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.campaignId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.requestId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.requester = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.votingDeadline = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static WithdrawalRequestCreatedEventResponse getWithdrawalRequestCreatedEventFromLog(
            Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(WITHDRAWALREQUESTCREATED_EVENT, log);
        WithdrawalRequestCreatedEventResponse typedResponse = new WithdrawalRequestCreatedEventResponse();
        typedResponse.log = log;
        typedResponse.campaignId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.requestId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.requester = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.votingDeadline = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<WithdrawalRequestCreatedEventResponse> withdrawalRequestCreatedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getWithdrawalRequestCreatedEventFromLog(log));
    }

    public Flowable<WithdrawalRequestCreatedEventResponse> withdrawalRequestCreatedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(WITHDRAWALREQUESTCREATED_EVENT));
        return withdrawalRequestCreatedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> adminApproveRequest(BigInteger _requestId) {
        final Function function = new Function(
                FUNC_ADMINAPPROVEREQUEST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_requestId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> createWithdrawalRequest(BigInteger _campaignId,
            String _vendor, BigInteger _amount, String _purpose, String _financialProofHash,
            String _visualProofHash) {
        final Function function = new Function(
                FUNC_CREATEWITHDRAWALREQUEST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_campaignId), 
                new org.web3j.abi.datatypes.Address(160, _vendor), 
                new org.web3j.abi.datatypes.generated.Uint256(_amount), 
                new org.web3j.abi.datatypes.Utf8String(_purpose), 
                new org.web3j.abi.datatypes.Utf8String(_financialProofHash), 
                new org.web3j.abi.datatypes.Utf8String(_visualProofHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> executeRequest(BigInteger _requestId) {
        final Function function = new Function(
                FUNC_EXECUTEREQUEST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_requestId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> recordDonation(BigInteger _campaignId,
            String _donor, String _paymentMethod, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_RECORDDONATION, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_campaignId), 
                new org.web3j.abi.datatypes.Address(160, _donor), 
                new org.web3j.abi.datatypes.Utf8String(_paymentMethod)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> voteOnRequest(BigInteger _requestId,
            Boolean _approve) {
        final Function function = new Function(
                FUNC_VOTEONREQUEST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_requestId), 
                new org.web3j.abi.datatypes.Bool(_approve)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> campaignBalances(BigInteger param0) {
        final Function function = new Function(FUNC_CAMPAIGNBALANCES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> campaignContributions(BigInteger param0, String param1) {
        final Function function = new Function(FUNC_CAMPAIGNCONTRIBUTIONS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0), 
                new org.web3j.abi.datatypes.Address(160, param1)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple4<String, BigInteger, BigInteger, String>> donationHistory(
            BigInteger param0, BigInteger param1) {
        final Function function = new Function(FUNC_DONATIONHISTORY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0), 
                new org.web3j.abi.datatypes.generated.Uint256(param1)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}));
        return new RemoteFunctionCall<Tuple4<String, BigInteger, BigInteger, String>>(function,
                new Callable<Tuple4<String, BigInteger, BigInteger, String>>() {
                    @Override
                    public Tuple4<String, BigInteger, BigInteger, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<String, BigInteger, BigInteger, String>(
                                (String) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (String) results.get(3).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Boolean> hasVoted(BigInteger param0, String param1) {
        final Function function = new Function(FUNC_HASVOTED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0), 
                new org.web3j.abi.datatypes.Address(160, param1)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<BigInteger> nextRequestId() {
        final Function function = new Function(FUNC_NEXTREQUESTID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> platformFeePercentage() {
        final Function function = new Function(FUNC_PLATFORMFEEPERCENTAGE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> platformOwner() {
        final Function function = new Function(FUNC_PLATFORMOWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Tuple12<BigInteger, String, String, BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>> withdrawalRequests(
            BigInteger param0) {
        final Function function = new Function(FUNC_WITHDRAWALREQUESTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple12<BigInteger, String, String, BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple12<BigInteger, String, String, BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple12<BigInteger, String, String, BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> call(
                            ) throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple12<BigInteger, String, String, BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (String) results.get(4).getValue(), 
                                (String) results.get(5).getValue(), 
                                (String) results.get(6).getValue(), 
                                (BigInteger) results.get(7).getValue(), 
                                (BigInteger) results.get(8).getValue(), 
                                (BigInteger) results.get(9).getValue(), 
                                (BigInteger) results.get(10).getValue(), 
                                (BigInteger) results.get(11).getValue());
                    }
                });
    }

    @Deprecated
    public static PlatformLedger load(String contractAddress, Web3j web3j, Credentials credentials,
                                      BigInteger gasPrice, BigInteger gasLimit) {
        return new PlatformLedger(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static PlatformLedger load(String contractAddress, Web3j web3j,
                                      TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new PlatformLedger(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static PlatformLedger load(String contractAddress, Web3j web3j, Credentials credentials,
                                      ContractGasProvider contractGasProvider) {
        return new PlatformLedger(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static PlatformLedger load(String contractAddress, Web3j web3j,
                                      TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new PlatformLedger(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class DonationRecordedEventResponse extends BaseEventResponse {
        public BigInteger campaignId;

        public String donor;

        public BigInteger amount;

        public String paymentMethod;
    }

    public static class RequestFinalizedEventResponse extends BaseEventResponse {
        public BigInteger requestId;

        public BigInteger newStatus;
    }

    public static class VoteCastEventResponse extends BaseEventResponse {
        public BigInteger requestId;

        public String voter;

        public Boolean approve;

        public BigInteger votingPower;
    }

    public static class WithdrawalExecutedEventResponse extends BaseEventResponse {
        public BigInteger requestId;

        public String vendor;

        public BigInteger amount;
    }

    public static class WithdrawalRequestCreatedEventResponse extends BaseEventResponse {
        public BigInteger campaignId;

        public BigInteger requestId;

        public String requester;

        public BigInteger amount;

        public BigInteger votingDeadline;
    }
}
