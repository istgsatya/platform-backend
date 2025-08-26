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

}
