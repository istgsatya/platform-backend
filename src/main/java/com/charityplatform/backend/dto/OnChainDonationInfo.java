package com.charityplatform.backend.dto;

import java.math.BigInteger;

public record OnChainDonationInfo (BigInteger campaignId, String donorAddress, BigInteger amountInWei){


}
