package com.charityplatform.backend.service;

import com.charityplatform.backend.dto.CharityApplicationRequest;
import com.charityplatform.backend.dto.CharityResponseDTO;
import com.charityplatform.backend.model.*;
import com.charityplatform.backend.repository.BlacklistedIdentifierRepository;
import com.charityplatform.backend.repository.CharityRepository;
import com.charityplatform.backend.repository.UserRepository;
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
    private final BlacklistedIdentifierRepository blacklistedIdentifierRepository; // <-- New Dependency

    @Autowired
    public CharityService(CharityRepository charityRepository, UserRepository userRepository,
                          FileStorageService fileStorageService, BlacklistedIdentifierRepository blacklistedIdentifierRepository) {
        this.charityRepository = charityRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.blacklistedIdentifierRepository = blacklistedIdentifierRepository; // <-- Initialize Dependency
    }

    @Transactional
    public Charity applyForVerification(CharityApplicationRequest request, MultipartFile document, User applicant) {
        if (applicant.getCharity() != null) {
            throw new IllegalStateException("This user is already associated with a charity.");
        }

        // --- START: THE NEW BLACKLIST WALL ---
        // We will assume for the MVP that the document name is a unique identifier.
        // In a real system, you'd use a hash of the document content.
        boolean isDocBlacklisted = blacklistedIdentifierRepository.existsByIdentifierValueAndIdentifierType(
                document.getOriginalFilename(), IdentifierType.REGISTRATION_DOCUMENT_URL
        );
        if (isDocBlacklisted) {
            throw new AccessDeniedException("Your application contains information linked to a previously blacklisted entity. Fuck you and your fake charities.");
        }
        // You could add checks for other fields from the request here too if they are meant to be unique.
        // --- END: THE NEW BLACKLIST WALL ---

        String documentFileName = fileStorageService.storeFile(document);

        Charity charity = new Charity();
        charity.setName(request.getName());
        charity.setDescription(request.getDescription());
        charity.setRegistrationDocumentUrl(documentFileName); // Saving the stored file name now
        charity.setAdminUser(applicant);

        Charity savedCharity = charityRepository.save(charity);

        applicant.setCharity(savedCharity);
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

    @Transactional(readOnly = true)
    public List<CharityResponseDTO> getApprovedCharities() {
        List<Charity> charities = charityRepository.findByStatus(VerificationStatus.APPROVED);
        return charities.stream()
                .map(CharityResponseDTO::fromCharity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CharityResponseDTO getApprovedCharityById(Long id) {
        Charity charity = charityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charity not found with id: " + id));

        if (charity.getStatus() != VerificationStatus.APPROVED) {
            throw new RuntimeException("Charity not found or not approved.");
        }
        return CharityResponseDTO.fromCharity(charity);
    }
}