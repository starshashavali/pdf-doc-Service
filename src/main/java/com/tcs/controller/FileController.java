package com.tcs.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tcs.service.FileStorageService;

@RestController
public class FileController {

	@Autowired
	private FileStorageService fileStorageService;

	   @PostMapping("/upload")
	    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
	        try {
	            String fileName = fileStorageService.storeFile(file);
	            return ResponseEntity.ok().body("File uploaded successfully: " + fileName);
	        } catch (IOException e) {
	            return ResponseEntity.internalServerError().body("Could not upload the file: " + e.getMessage());
	        }
	    }

	 //fetch pdf
	   //http://localhost:9001/download/Angular Notes (1).pdf
	    @GetMapping("/download/{fileName:.+}")
	    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
	        try {
	            Path filePath = fileStorageService.loadFileAsResource(fileName);
	            Resource resource = new UrlResource(filePath.toUri());

	            if (resource.exists() || resource.isReadable()) {
	                String contentType = Files.probeContentType(filePath);
	                return ResponseEntity.ok()
	                    .contentType(MediaType.parseMediaType(contentType))
	                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
	                    .body(resource);
	            } else {
	                return ResponseEntity.notFound().build();
	            }
	        } catch (Exception e) {
	            return ResponseEntity.internalServerError().build();
	        }
	    }
	    @GetMapping("/files/list-pdfs")
	    public ResponseEntity<List<String>> listPdfFiles() {
	        try {
	            List<Path> pdfFiles = fileStorageService.getFilesByType("pdf");
	            if (pdfFiles.isEmpty()) {
	                return ResponseEntity.noContent().build();
	            }

	            List<String> downloadLinks = pdfFiles.stream()
	                    .map(path -> ServletUriComponentsBuilder.fromCurrentContextPath()
	                            .path("/download/")
	                            .path(path.getFileName().toString())
	                            .toUriString())
	                    .collect(Collectors.toList());

	            return ResponseEntity.ok(downloadLinks);
	        } catch (Exception e) {
	            return ResponseEntity.internalServerError().build();
	        }
	    }
	 /*
	  * 
	  *all pdf in zip folder
	    @GetMapping("/files/download-all-pdfs")
	    public void downloadAllPdfs(HttpServletResponse response) {
	        try {
	            List<Path> pdfFiles = fileStorageService.getFilesByType("pdf"); // Ensure this method exists and works as expected
	            if (pdfFiles.isEmpty()) {
	                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	                return;
	            }

	            response.setContentType("application/zip");
	            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"all_pdfs.zip\"");

	            try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
	                for (Path pdfFile : pdfFiles) {
	                    Resource resource = new UrlResource(pdfFile.toUri());
	                    if (resource.exists() && resource.isReadable()) {
	                        zipOut.putNextEntry(new ZipEntry(resource.getFilename()));
	                        Files.copy(pdfFile, zipOut);
	                        zipOut.closeEntry();
	                    }
	                }
	            }
	        } catch (IOException e) {
	            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        }
	    }
	*/

	    private ResponseEntity<List<Resource>> getResourcesByType(String fileType) {
	        try {
	            List<Resource> resources = fileStorageService.getFilesByType(fileType).stream()
	                .map(path -> {
	                    try {
	                        return new UrlResource(path.toUri());
	                    } catch (MalformedURLException e) {
	                        return null;
	                    }
	                })
	                .filter(Objects::nonNull)
	                .collect(Collectors.toList());
	            if (resources.isEmpty()) {
	                return ResponseEntity.noContent().build();
	            }
	            return ResponseEntity.ok().body(resources);
	        } catch (Exception e) {
	            return ResponseEntity.internalServerError().build();
	        }
	    }
	}