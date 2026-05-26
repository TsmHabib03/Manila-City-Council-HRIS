package com.ian.web.fileupload;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface StorageStrategy {
    String[] uploadFile(MultipartFile multipartFile, String fileName) throws Exception;
    String[] uploadFile201(MultipartFile multipartFile, String fileName, long empId) throws Exception;
    ResponseEntity<Object> downloadFile(String fileUrl, HttpServletRequest request) throws Exception;
}
