package com.ian.web.employee.archive;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One archived document under the Archive module (CR Request ID 015):
 * SALN, Resigned Employees and past Leaves file repositories with
 * customizable labels, tags, search and CSV/XLS export.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/** SALN, RESIGNED or LEAVES. */
	private String section;

	/** Original/display file name, e.g. SALN_JuanDelaCruz.pdf. */
	private String fileName;

	/** Download URL returned by StorageService (/file/download/...). */
	private String fileUrl;

	/** File extension shown in the Type column, e.g. PDF, DOCX, XLSX. */
	private String fileType;

	private String uploader;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate uploadDate;

	/** Reference date: SALN year, resignation date or leave date. */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate refDate;

	private String employeeName;

	private String department;

	/** Document type, e.g. Resignation Letter, Vacation, Clearance Certificate. */
	private String docType;

	/** Status, e.g. Submitted, Reviewed, Archived, Cleared. */
	private String status;

	/** User-defined label this file is grouped under. */
	private String label;

	/** Free-text tags, e.g. Confidential, Draft, Final. */
	private String tags;
}
