package com.example.epf.epf_utility.service;

import java.io.File;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileValidator {
//	public static boolean validateInputFile(MultipartFile file) {
//        // Add validation logic (e.g., check file type, size)
//        String fileType = file.getContentType();
//        return fileType != null && fileType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//    }
	
	
	    public static boolean validateInputFile(MultipartFile excelMultipartFile) {
	        String filename = excelMultipartFile.getOriginalFilename();
	        return filename != null && filename.endsWith(Constants.FILE_TYPE);
	    }

}
