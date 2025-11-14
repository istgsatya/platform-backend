package com.charityplatform.backend.service;

import com.charityplatform.backend.dto.CharityApplicationRequest;
import com.charityplatform.backend.dto.CharityResponseDTO;
import com.charityplatform.backend.dto.DonationResponseDTO;
import com.charityplatform.backend.model.*;
import com.charityplatform.backend.repository.BlacklistedIdentifierRepository;
import com.charityplatform.backend.repository.CharityRepository;
import com.charityplatform.backend.repository.DonationRepository;
import com.charityplatform.backend.repository.UserRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CharityService {

    private static final Logger log = LoggerFactory.getLogger(CharityService.class);

    private final CharityRepository charityRepository;
    private final UserRepository userRepository;
    private final IpfsService ipfsService; // <-- REPLACED FileStorageService
    private final BlacklistedIdentifierRepository blacklistedIdentifierRepository;
    private final DonationRepository donationRepository;

    @Autowired
    public CharityService(CharityRepository charityRepository, UserRepository userRepository,
                          IpfsService ipfsService, BlacklistedIdentifierRepository blacklistedIdentifierRepository, // <-- CONSTRUCTOR UPDATED
                          DonationRepository donationRepository) {
        this.charityRepository = charityRepository;
        this.userRepository = userRepository;
        this.ipfsService = ipfsService; // <-- CONSTRUCTOR UPDATED
        this.blacklistedIdentifierRepository = blacklistedIdentifierRepository;
        this.donationRepository = donationRepository;
    }

    // --- THIS METHOD HAS BEEN REBUILT FOR THE NEW FLOW ---
    @Transactional
    public Charity applyForVerification(CharityApplicationRequest request, MultipartFile registrationDocument, User applicant) {
        User managedApplicant = userRepository.findById(applicant.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));

        if (managedApplicant.getCharity() != null) {
            throw new IllegalStateException("This user is already associated with a charity.");
        }

        // This blacklist check is good logic and remains
        boolean isDocBlacklisted = blacklistedIdentifierRepository.existsByIdentifierValueAndIdentifierType(
                registrationDocument.getOriginalFilename(), IdentifierType.REGISTRATION_DOCUMENT_URL
        );
        if (isDocBlacklisted) {
            throw new AccessDeniedException("Your application contains information linked to a previously blacklisted entity.");
        }

        // --- NEW IPFS LOGIC ---
        log.info("Uploading registration document to IPFS for new charity application '{}'...", request.getName());
        String documentCid = ipfsService.uploadFile(registrationDocument);
        log.info("Successfully uploaded registration document. CID: {}", documentCid);

        Charity charity = new Charity();
        charity.setName(request.getName());
        charity.setDescription(request.getDescription());
        charity.setPayoutWalletAddress(request.getPayoutWalletAddress());

        // Save the IPFS CID for the document
        charity.setRegistrationDocumentUrl(documentCid);

        // Save the IPFS CID for the banner
        charity.setBannerImageUrl(request.getBannerImageUrl());

        // Set initial status and admin
        charity.setStatus(VerificationStatus.PENDING);
        charity.setAdminUser(managedApplicant);

        // Save the charity to get its ID
        Charity savedCharity = charityRepository.save(charity);

        // Update the user to link them to the new charity
        managedApplicant.setCharity(savedCharity);
        userRepository.save(managedApplicant);

        log.info("New charity application for '{}' (ID: {}) created and is pending review.", savedCharity.getName(), savedCharity.getId());
        return savedCharity;
    }

    @Transactional(readOnly = true)
    public List<Charity> getCharitiesByStatus(VerificationStatus status) {
        if (status == null) {
            return charityRepository.findAll();
        }
        return charityRepository.findByStatus(status);
    }

    @Transactional
    public Charity approveCharity(Long charityId) {
        Charity charity = charityRepository.findById(charityId)
                .orElseThrow(() -> new RuntimeException("Charity not found with id: " + charityId));

        if (charity.getStatus() != VerificationStatus.PENDING) {
            throw new IllegalStateException("Charity is not in PENDING state.");
        }
        charity.setStatus(VerificationStatus.APPROVED);
        User user = charity.getAdminUser();
        if (user != null) {
            Set<Role> roles = user.getRoles();
            roles.remove(Role.ROLE_DONOR);
//            roles.add(Role.ROLE_CHARITY_ADMIN);
            user.setRoles(roles);
            userRepository.save(user);
        }
        return charityRepository.save(charity);
    }

    @Transactional
    public Charity rejectCharity(Long charityId) {
        Charity charity = charityRepository.findById(charityId)
                .orElseThrow(() -> new RuntimeException("Charity not found with id: " + charityId));
        if (charity.getStatus() != VerificationStatus.PENDING) {
            throw new IllegalStateException("Charity is not in PENDING state.");
        }
        charity.setStatus(VerificationStatus.REJECTED);
        return charityRepository.save(charity);
    }

    @Transactional(readOnly = true)
    public List<CharityResponseDTO> getApprovedCharities() {
        List<Charity> charities = charityRepository.findByStatus(VerificationStatus.APPROVED);
        charities.forEach(charity -> Hibernate.initialize(charity.getAdminUser()));
        return charities.stream()
                .map(CharityResponseDTO::fromCharity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CharityResponseDTO getApprovedCharityById(Long id) {
        Charity charity = charityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charity not found with id: " + id));

        if (charity.getStatus() != VerificationStatus.APPROVED) {
            throw new RuntimeException("Charity not found or is not approved.");
        }
        Hibernate.initialize(charity.getAdminUser());
        return CharityResponseDTO.fromCharity(charity);
    }

    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getDonationsForMyCharity(User currentUser) {
        Charity charity = currentUser.getCharity();
        if (charity == null) {
            throw new AccessDeniedException("The current user is not a charity admin.");
        }
        List<Donation> donations = donationRepository.findByCharityIdOrderByCreatedAtDesc(charity.getId());
        return donations.stream()
                .map(DonationResponseDTO::fromDonation)
                .collect(Collectors.toList());
    }
}