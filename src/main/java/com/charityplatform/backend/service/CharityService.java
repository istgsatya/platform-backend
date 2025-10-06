package com.charityplatform.backend.service;


import com.charityplatform.backend.dto.CharityApplicationRequest;
import com.charityplatform.backend.model.Charity;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.repository.UserRepository;
import com.charityplatform.backend.repository.CharityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.SchedulingAwareRunnable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.charityplatform.backend.model.Role;
import com.charityplatform.backend.model.VerificationStatus;
import java.util.List;
import java.util.Set;

@Service


public class CharityService {
    private final CharityRepository charityRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public CharityService(CharityRepository charityRepository, UserRepository userRepository, FileStorageService fileStorageService) {
        this.charityRepository = charityRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;

    }
    @Transactional
    public Charity applyForVerification(CharityApplicationRequest request, MultipartFile document,User applicant) {
        if(applicant.getCharity()!=null){
            throw new IllegalStateException("This user is already associated with a charity .");

        }
        String documentFIleName= fileStorageService.storeFile(document);

        Charity charity=new Charity();
        charity.setName(request.getName());
        charity.setDescription(request.getDescription());
        charity.setRegistrationDocumentUrl(documentFIleName);
        charity.setAdminUser(applicant);


        Charity savedCharity=charityRepository.save(charity);

        applicant.setCharity(savedCharity);
        userRepository.save(applicant);

        return savedCharity;

    }
    @Transactional(readOnly = true)
    public List<Charity> getCharitiesByStatus(VerificationStatus status) {
       if(status==null){
           return charityRepository.findAll();


       }
       return charityRepository.findByStatus(status);
    }
    @Transactional
    public Charity approveCharity(Long charityId){
        Charity charity=charityRepository.findById(charityId).orElseThrow(() -> new RuntimeException("Charity nto found with id: "+charityId));

        if(charity.getStatus()!=VerificationStatus.PENDING){
            throw new IllegalStateException("Charity is not in pending state or maybe already approved");
        }
        charity.setStatus(VerificationStatus.APPROVED);
        User user=charity.getAdminUser();
        if(user!=null){
            Set<Role> roles=user.getRoles();
            roles.remove(Role.ROLE_DONOR);
            roles.add(Role.ROLE_CHARITY_ADMIN);
            user.setRoles(roles);
            userRepository.save(user);
        }
        return charityRepository.save(charity);
    }
    @Transactional
    public Charity rejectCharity(Long charityId) {
        Charity charity = charityRepository.findById(charityId).orElseThrow(() -> new RuntimeException("Charity not found with id: " + charityId));
        if (charity.getStatus() != VerificationStatus.PENDING) {

            throw new IllegalStateException("Charity is not in pending state and cannot be rejected");
        }
        charity.setStatus(VerificationStatus.REJECTED);
        return charityRepository.save(charity);
    }



}
