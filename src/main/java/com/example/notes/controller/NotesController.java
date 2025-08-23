package com.example.notes.controller;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.example.notes.service.NotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NotesController {

    @Autowired
    private NotesService notesService;

    // Upload multiple files (or a folder containing files)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> uploadNotes(@RequestParam("files") MultipartFile[] files,
                                            @RequestParam(value = "customName", required = false) String customName,
                                            @RequestParam(value = "originalName", required = false) String originalName,
                                            @RequestParam(value = "fileType", required = false) String fileType,
                                            @RequestParam(value = "description", required = false) String description,
                                            @RequestParam(value = "batchUpload", required = false) String batchUpload,
                                            @RequestParam(value = "rootDirName", required = false) String rootDirName,
                                            @RequestParam(value = "paths", required = false) String[] paths,
                                            @RequestParam(value = "manifest", required = false) String manifest) {
        StringBuilder message = new StringBuilder();
        try {
            // Pre-flight duplicate check: if single-file with custom name, or folder root provided
            if ("true".equals(batchUpload)) {
                if (rootDirName != null && !rootDirName.trim().isEmpty()) {
                    if (notesService.anyWithPrefixExists(rootDirName)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Folder name already taken: " + rootDirName);
                    }
                }
            } else {
                String candidateName = customName != null && !customName.trim().isEmpty() ? customName : originalName;
                if (candidateName != null && !candidateName.trim().isEmpty()) {
                    if (notesService.objectExists(candidateName)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("File name already taken: " + candidateName);
                    }
                }
            }
            if ("true".equals(batchUpload)) {
                // Handle folder upload - preserve folder structure
                for (int i = 0; i < files.length; i++) {
                    MultipartFile file = files[i];
                    String path = (paths != null && i < paths.length) ? paths[i] : file.getOriginalFilename();
                    String keyName = notesService.uploadFileWithPath(file, path);
                    message.append("Uploaded: ").append(keyName).append("\n");
                }
                message.append("Folder upload completed successfully!");
            } else {
                // Handle single file upload with custom name
                for (MultipartFile file : files) {
                    String keyName = notesService.uploadFileWithCustomName(file, customName);
                    message.append("Uploaded: ").append(keyName).append("\n");
                }
            }
            return ResponseEntity.ok(message.toString());
        } catch (IOException e) {
            // Return a 400 Bad Request if a file is restricted
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (IllegalStateException dup) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(dup.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }
    

    // List (and search) files by filename
    @GetMapping
    public ResponseEntity<List<String>> listNotes(@RequestParam(value = "search", required = false) String search) {
        List<String> files = notesService.listNotes(search);
        return ResponseEntity.ok(files);
    }

    // Download a file by its key name
    @GetMapping("/download/{keyName}")
    public ResponseEntity<byte[]> downloadNote(@PathVariable String keyName) {
        try {
            S3Object s3Object = notesService.downloadFile(keyName);
            byte[] content = IOUtils.toByteArray(s3Object.getObjectContent());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(keyName)
                    .build());
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Download a folder as ZIP file
    @GetMapping("/download-folder")
    public ResponseEntity<byte[]> downloadFolder(@RequestParam(value = "prefix", required = false) String prefix,
                                               @RequestParam(value = "path", required = false) String path) {
        try {
            String folderPrefix = prefix != null ? prefix : path;
            if (folderPrefix == null || folderPrefix.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Ensure the prefix ends with '/' for folder structure
            if (!folderPrefix.endsWith("/")) {
                folderPrefix = folderPrefix + "/";
            }
            
            byte[] zipContent = notesService.downloadFolderAsZip(folderPrefix);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(folderPrefix.replace("/", "") + ".zip")
                    .build());
            return new ResponseEntity<>(zipContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Alternative endpoint for folder download with path parameter
    @GetMapping("/download-folder/{folderPath}")
    public ResponseEntity<byte[]> downloadFolderByPath(@PathVariable String folderPath) {
        try {
            String folderPrefix = folderPath;
            // Ensure the prefix ends with '/' for folder structure
            if (!folderPrefix.endsWith("/")) {
                folderPrefix = folderPrefix + "/";
            }
            
            byte[] zipContent = notesService.downloadFolderAsZip(folderPrefix);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(folderPath + ".zip")
                    .build());
            return new ResponseEntity<>(zipContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
