package com.charityplatform.backend.service;



import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Service
public class IpfsService {

    private static final Logger log = LoggerFactory.getLogger(IpfsService.class);
    private final RestTemplate restTemplate;


    @Value("${pinata.jwt}")
    private String pinataJwt;

    public IpfsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String uploadFile(MultipartFile file) {
        log.info("IPFS_UPLOAD_START: Uploading file '{}' to Pinata...", file.getOriginalFilename());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + pinataJwt);


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            // Pinata needs a ByteArrayResource with the filename
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("file", fileResource);
        } catch (IOException e) {
            log.error("IPFS_UPLOAD_ERROR: Failed to read file bytes.", e);
            throw new RuntimeException("Failed to read file bytes.", e);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    "https://api.pinata.cloud/pinning/pinFileToIPFS",
                    requestEntity,
                    JsonNode.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String ipfsHash = response.getBody().get("IpfsHash").asText();
                log.info("IPFS_UPLOAD_SUCCESS: File uploaded. CID: {}", ipfsHash);
                return ipfsHash;
            } else {
                throw new RuntimeException("Pinata API returned an error: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("IPFS_UPLOAD_FAILURE: A critical error occurred while calling Pinata API.", e);
            throw new RuntimeException("Could not upload file to IPFS.", e);
        }
    }
}

