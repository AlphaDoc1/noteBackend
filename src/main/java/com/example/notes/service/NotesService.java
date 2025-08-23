package com.example.notes.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class NotesService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException {
        // Get the original filename
        String keyName = file.getOriginalFilename();
    
        if (keyName != null) {
            // Sanitize the filename: trim, normalize slashes, replace spaces
            keyName = sanitizeKey(keyName);
        } else {
            // Fallback to a default name if the original is null
            keyName = "file_" + System.currentTimeMillis();
        }
        // Prevent overwrite if object already exists
        if (amazonS3.doesObjectExist(bucketName, keyName)) {
            throw new IllegalStateException("Name already taken: " + keyName);
        }
        
        // Set up metadata for the file
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        String contentType = guessContentType(keyName, file.getContentType());
        metadata.setContentType(contentType);
        
        // Upload the file to S3
        amazonS3.putObject(bucketName, keyName, file.getInputStream(), metadata);
        
        return keyName;
    }

    public String uploadFileWithCustomName(MultipartFile file, String customName) throws IOException {
        if (customName == null || customName.trim().isEmpty()) {
            return uploadFile(file);
        }
        
        String keyName = sanitizeKey(customName);
        // Prevent overwrite if object already exists
        if (amazonS3.doesObjectExist(bucketName, keyName)) {
            throw new IllegalStateException("Name already taken: " + keyName);
        }
        
        // Set up metadata for the file
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        String contentType = guessContentType(keyName, file.getContentType());
        metadata.setContentType(contentType);
        
        // Upload the file to S3 with custom name
        amazonS3.putObject(bucketName, keyName, file.getInputStream(), metadata);
        
        return keyName;
    }

    public String uploadFileWithPath(MultipartFile file, String path) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            return uploadFile(file);
        }
        
        String keyName = sanitizeKey(path);
        // Prevent overwrite of exact object key
        if (amazonS3.doesObjectExist(bucketName, keyName)) {
            throw new IllegalStateException("Name already taken: " + keyName);
        }
        
        // Set up metadata for the file
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        String contentType = guessContentType(keyName, file.getContentType());
        metadata.setContentType(contentType);
        
        // Upload the file to S3 with the specified path (preserves folder structure)
        amazonS3.putObject(bucketName, keyName, file.getInputStream(), metadata);
        
        return keyName;
    }
    
    public S3Object downloadFile(String keyName) {
        return amazonS3.getObject(bucketName, keyName);
    }

    public String sanitizeKey(String raw) {
        String key = raw.trim();
        key = key.replace('\\', '/');
        key = key.replaceAll("\\s+", "_");
        // remove any ../ traversal segments
        while (key.contains("../")) {
            key = key.replace("../", "");
        }
        // strip leading slashes
        while (key.startsWith("/")) {
            key = key.substring(1);
        }
        return key;
    }

    private String guessContentType(String filename, String providedContentType) {
        if (providedContentType != null && !providedContentType.trim().isEmpty() &&
                !"application/octet-stream".equalsIgnoreCase(providedContentType)) {
            return providedContentType;
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".csv")) return "text/csv";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".wav")) return "audio/wav";
        if (lower.endsWith(".zip")) return "application/zip";
        return "application/octet-stream";
    }

    public byte[] downloadFolderAsZip(String folderPrefix) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        
        try {
            // List all objects with the given prefix
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucketName)
                    .withPrefix(folderPrefix);
            
            ListObjectsV2Result result;
            do {
                result = amazonS3.listObjectsV2(request);
                
                for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                    String key = objectSummary.getKey();
                    
                    // Skip the folder prefix itself (if it exists as an object)
                    if (key.equals(folderPrefix)) {
                        continue;
                    }
                    
                    // Get the relative path within the folder
                    String relativePath = key.substring(folderPrefix.length());
                    if (relativePath.isEmpty()) {
                        continue;
                    }
                    
                    // Download the file from S3
                    S3Object s3Object = amazonS3.getObject(bucketName, key);
                    
                    // Create a ZIP entry
                    ZipEntry zipEntry = new ZipEntry(relativePath);
                    zos.putNextEntry(zipEntry);
                    
                    // Copy the file content to the ZIP
                    IOUtils.copy(s3Object.getObjectContent(), zos);
                    
                    zos.closeEntry();
                    s3Object.close();
                }
                
                request.setContinuationToken(result.getNextContinuationToken());
            } while (result.isTruncated());
            
        } finally {
            zos.close();
        }
        
        return baos.toByteArray();
    }

    public List<String> listNotes(String search) {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName);
        ListObjectsV2Result result;
        List<String> notes = new ArrayList<>();
        do {
            result = amazonS3.listObjectsV2(req);
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                // If no search query is provided or if the key contains the search text (case-insensitive), add it
                if (search == null || search.isEmpty() ||
                    objectSummary.getKey().toLowerCase().contains(search.toLowerCase())) {
                    notes.add(objectSummary.getKey());
                }
            }
            req.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
        return notes;
    }

    public boolean objectExists(String key) {
        String sanitized = sanitizeKey(key);
        return amazonS3.doesObjectExist(bucketName, sanitized);
    }

    public boolean anyWithPrefixExists(String prefix) {
        String sanitizedPrefix = sanitizeKey(prefix);
        if (!sanitizedPrefix.endsWith("/")) {
            sanitizedPrefix = sanitizedPrefix + "/";
        }
        ListObjectsV2Request req = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(sanitizedPrefix)
                .withMaxKeys(1);
        ListObjectsV2Result res = amazonS3.listObjectsV2(req);
        return res.getKeyCount() > 0;
    }
}
