package com.tcs.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.tcs.domain.FileEntity;
import com.tcs.repository.FileRepository;

@Service
public class FileStorageService {

	@Value("${file.upload-dir}")
	private String uploadDir;

	@Autowired
	private FileRepository fileRepository;

	public String storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        Path targetLocation = Paths.get(uploadDir).resolve(fileName);

        Files.createDirectories(targetLocation.getParent());
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        long fileSize = file.getSize();
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(fileName);
        fileEntity.setFilePath(targetLocation.toString());
        fileEntity.setFileType(fileType);
        fileEntity.setFileSize(fileSize);
        fileRepository.save(fileEntity);

        return fileName;
    }

    public Path loadFileAsResource(String fileName) {
        return Paths.get(uploadDir).resolve(fileName).normalize();
    }

    public List<Path> getFilesByType(String fileType) {
        List<FileEntity> fileEntities = fileRepository.findAllByFileType(fileType);
        return fileEntities.stream()
                           .map(fe -> Paths.get(uploadDir, fe.getFileName()))
                           .collect(Collectors.toList());
    }
}