package com.charityplatform.backend.repository;

import com.charityplatform.backend.model.BlacklistedIdentifier;
import com.charityplatform.backend.model.IdentifierType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedIdentifierRepository extends JpaRepository<BlacklistedIdentifier, Long> {

    /**
     * Checks if a specific identifier value and type combination already exists in the blacklist.
     * This is the core method for the "Fuck you and your fake charities" check.
     *
     * @param value The value to check (e.g., "0x...").
     * @param type The type of the identifier (e.g., ETH_ADDRESS).
     * @return true if the identifier is on the blacklist, false otherwise.
     */
    boolean existsByIdentifierValueAndIdentifierType(String value, IdentifierType type);
}