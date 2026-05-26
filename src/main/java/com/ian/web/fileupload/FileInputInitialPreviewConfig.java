package com.ian.web.fileupload;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor
@Data public class FileInputInitialPreviewConfig {

	private String key;
	private String fileId;
	private String caption;
	private String type;
	private String fileType;
	private double size;
	private Map<String, Object> exif;
	private boolean previewAsData = true;
	private String width;
	private String downloadUrl;
	private String url;
	private String frameClass;
	private Map<String, Object> frameAttr;
	private Map<String, Object> extra;
}
