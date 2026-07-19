package com.ian.web.employee.archive;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User-defined label for grouping Archive files within a section
 * (CR Request ID 015: "customize the labels, editable and able to add labels").
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveLabel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/** SALN, RESIGNED or LEAVES. */
	private String section;

	private String labelName;
}
