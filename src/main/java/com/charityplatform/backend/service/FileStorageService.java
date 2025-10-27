package com.charityplatform.backend.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Service
public class FileStorageService {


    private final Path fileStorageLocation;
    public FileStorageService(@Value("${file.upload-dir}")String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try{
            Files.createDirectories(this.fileStorageLocation);

        }catch (Exception ex){
            throw new RuntimeException("Could not create directory where the uploaded files was supposed to be stored",ex);
        }
    }
    public String storeFile(MultipartFile file){
        String fileName = UUID.randomUUID().toString()+"."+file.getOriginalFilename();
        try{
            if(fileName.contains("..")){
                throw new RuntimeException("Sorry nigga filename contain invalid af path sequnce "+fileName);

            }
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(),targetLocation,StandardCopyOption.REPLACE_EXISTING);
            return fileName;

        }catch (Exception ex){
            throw new RuntimeException("Could not store file "+fileName+".Please try again",ex);
        }
    }

    // --- START: NEW METHOD TO ADD ---
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            System.err.println("Attempted to delete a null or blank file name.");
            return;
        }

        try {
            // Your storeFile method correctly returns just the unique filename, so this works.
            Path targetLocation = this.fileStorageLocation.resolve(fileName);

            Files.deleteIfExists(targetLocation);
            System.out.println("Cleanup: Successfully deleted orphaned file: " + fileName);

        } catch (Exception ex) {
            // Even if deletion fails (e.g. permissions), we log it but don't crash.
            // The primary error (e.g. from the blockchain) is more important.
            System.err.println("Could not delete file: " + fileName + ". Reason: " + ex.getMessage());
        }
    }
    // --- END: NEW METHOD TO ADD ---

}