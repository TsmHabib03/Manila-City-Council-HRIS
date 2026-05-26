package com.ian.web.fileupload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Lazy
@Service
public class FileStorageStrategy implements StorageStrategy {

	private final Path fileStorageLocation;
	
	public FileStorageStrategy(FileStorageProperties fileStorageProperties) throws IOException {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
				.toAbsolutePath().normalize();
		Files.createDirectories(fileStorageLocation);
	}
	
	@Override
	public String[] uploadFile(MultipartFile multipartFile, String fileName) throws Exception {
		log.info("FileStorageStrategy ==> uploading file");
		//String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
		if (fileName.contains("..")) {
			throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
		}
		Path targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(multipartFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        /*String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/download/")
                .path(fileName)
                .toUriString();*/
        
        String downloadUrl = UriComponentsBuilder.newInstance()
        		//.scheme("http").host("localhost").port(8080)
        		.path("/file/download/")
        		.path(fileName)
        		.build().toUriString();
        return new String[]{downloadUrl, fileName};
	}
	
	@Override
	public String[] uploadFile201(MultipartFile multipartFile, String fileName, long empId) throws Exception {
		log.info("FileStorageStrategy ==> uploading file");
		//String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
		if (fileName.contains("..")) {
			throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
		}
		
		String folderPath = "/uploads/201/" + empId + "/";
//		Path targetLocation = this.fileStorageLocation.resolve(fileName);
		Path targetLocation = this.fileStorageLocation.resolve(folderPath).resolve(fileName);
		
		// Create directories if they don't exist
		Files.createDirectories(targetLocation.getParent());
		
        Files.copy(multipartFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        String downloadUrl = UriComponentsBuilder.newInstance()
        		//.scheme("http").host("localhost").port(8080)
        		//.path("/uploads/201/" + empId)
        		.path(folderPath)
        		.path(fileName)
        		.build().toUriString();
        
        return new String[]{downloadUrl, fileName};
	}
	
	@Override
	public ResponseEntity<Object> downloadFile(String fileUrl, HttpServletRequest request) throws Exception {
        log.info("FileStorageStrategy==> downloading file");
        // String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        Path filePath = this.fileStorageLocation.resolve(fileUrl).normalize();
        @SuppressWarnings("null")
		Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists())
            throw new ResourceNotFoundException("File not found " + fileUrl);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
