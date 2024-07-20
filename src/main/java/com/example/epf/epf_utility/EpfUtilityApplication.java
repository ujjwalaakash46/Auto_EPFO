package com.example.epf.epf_utility;

import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EpfUtilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpfUtilityApplication.class, args);
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

}
