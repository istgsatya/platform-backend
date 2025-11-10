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
    private final CharityRepository charityRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final BlacklistedIdentifierRepository blacklistedIdentifierRepository;
    private final DonationRepository donationRepository;

    @Autowired
    public CharityService(CharityRepository charityRepository, UserRepository userRepository,
                          FileStorageService fileStorageService, BlacklistedIdentifierRepository blacklistedIdentifierRepository,DonationRepository donationRepository) {
        this.charityRepository = charityRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.blacklistedIdentifierRepository = blacklistedIdentifierRepository;
        this.donationRepository = donationRepository;
    }

    @Transactional
    public Charity applyForVerification(CharityApplicationRequest request, MultipartFile document, User applicant) {
        if (applicant.getCharity() != null) {
            throw new IllegalStateException("This user is already associated with a charity.");
        }

        boolean isDocBlacklisted = blacklistedIdentifierRepository.existsByIdentifierValueAndIdentifierType(
                document.getOriginalFilename(), IdentifierType.REGISTRATION_DOCUMENT_URL
        );
        if (isDocBlacklisted) {
            throw new AccessDeniedException("Your application contains information linked to a previously blacklisted entity. Fuck you and your fake charities.");
        }

        String documentFileName = fileStorageService.storeFile(document);

        Charity charity = new Charity();
        charity.setName(request.getName());
        charity.setDescription(request.getDescription());
        charity.setRegistrationDocumentUrl(documentFileName);
        charity.setPayoutWalletAddress(request.getPayoutWalletAddress());


        Charity savedCharity = charityRepository.save(charity);

        applicant.setCharity(savedCharity);
        savedCharity.setAdminUser(applicant); // Ensure both sides are set before exiting transaction
        userRepository.save(applicant);

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
            throw new IllegalStateException("Charity is not in pending state or maybe already approved");
        }
        charity.setStatus(VerificationStatus.APPROVED);
        User user = charity.getAdminUser();
        if (user != null) {
            Set<Role> roles = user.getRoles();
            roles.remove(Role.ROLE_DONOR);
            roles.add(Role.ROLE_CHARITY_ADMIN);
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
            throw new IllegalStateException("Charity is not in pending state and cannot be rejected");
        }
        charity.setStatus(VerificationStatus.REJECTED);
        return charityRepository.save(charity);
    }

    // --- START: THE FINAL FIX (LIST VERSION) ---
    @Transactional(readOnly = true)
    public List<CharityResponseDTO> getApprovedCharities() {
        List<Charity> charities = charityRepository.findByStatus(VerificationStatus.APPROVED);

        // This is the Exorcism. Force the lazy proxy to load NOW.
        charities.forEach(charity -> Hibernate.initialize(charity.getAdminUser()));

        return charities.stream()
                .map(CharityResponseDTO::fromCharity)
                .collect(Collectors.toList());
    }
    // --- END: THE FINAL FIX (LIST VERSION) ---

    // --- START: THE FINAL FIX (SINGLE VERSION) ---
    @Transactional(readOnly = true)
    public CharityResponseDTO getApprovedCharityById(Long id) {
        Charity charity = charityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charity not found with id: " + id));

        if (charity.getStatus() != VerificationStatus.APPROVED) {
            throw new RuntimeException("Charity not found or not approved.");
        }

        // This is the kill shot.
        Hibernate.initialize(charity.getAdminUser());

        return CharityResponseDTO.fromCharity(charity);
    }
    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getDonationsForMyCharity(User currentUser) {
        Charity charity = currentUser.getCharity();
        if (charity == null) {
            throw new AccessDeniedException("The current user is not a charity admin.");
        }

        // Call the final, bulletproof repository method
        List<Donation> donations = donationRepository.findByCharityIdOrderByCreatedAtDesc(charity.getId());

        // Convert to DTOs
        return donations.stream()
                .map(DonationResponseDTO::fromDonation)
                .collect(Collectors.toList());
    }
    // --- END: THE FINAL FIX (SINGLE VERSION) ---
}