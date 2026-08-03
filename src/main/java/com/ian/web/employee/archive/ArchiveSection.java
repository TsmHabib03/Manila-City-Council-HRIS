package com.ian.web.employee.archive;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin-defined sub-folder under the Archive module (CR Request ID 023),
 * e.g. SALN, Resigned Employees, Leaves, or any folder an admin adds later.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveSection {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/** URL-safe code used as ArchiveFile.section / ArchiveLabel.section, e.g. "SALN". Immutable after creation. */
	private String code;

	/** Sidebar / page-title label, e.g. "Resigned Employees". */
	private String displayName;

	/** Sidebar ordering; lower first. */
	private int sortOrder;
}
