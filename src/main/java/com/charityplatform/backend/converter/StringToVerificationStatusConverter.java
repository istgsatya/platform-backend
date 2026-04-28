package com.charityplatform.backend.converter;

import com.charityplatform.backend.model.VerificationStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToVerificationStatusConverter implements Converter<String, VerificationStatus> {

    @Override
    public VerificationStatus convert(String source) {
        try {
            // Convert the incoming string (e.g., "PENDING") to uppercase
            // and then to the corresponding enum constant.
            return VerificationStatus.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle cases where the string doesn't match any enum constant
            return null; // or throw a custom exception
        }
    }
}