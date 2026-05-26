package com.ian.web.fileupload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor
@Data public class FileInputResponse {

	private String error = "";
	private List<String> errorkeys;
	private List<String> initialPreview;
	private List<FileInputInitialPreviewConfig> initialPreviewConfig;
	private List<String> initialPreviewThumbTags;
	private boolean append = false;
}
