package com.ian.web.common.model;

import java.time.LocalDate;

import javax.persistence.Transient;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

public class Attachment {
	
	private String nameOfAttachment;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate attachmentDate;
	
	private String attachmentUrl;
	
	@Transient
	private MultipartFile attachedFile;

}
