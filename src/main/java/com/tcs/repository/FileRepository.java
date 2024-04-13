package com.tcs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.domain.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

	FileEntity findByFileName(String fileName);

	List<FileEntity> findAllByFileType(String fileType);
}