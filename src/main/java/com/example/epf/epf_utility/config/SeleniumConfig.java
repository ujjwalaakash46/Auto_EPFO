package com.example.epf.epf_utility.config;

import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.epf.epf_utility.service.Constants;

import jakarta.annotation.PostConstruct;

@Configuration
public class SeleniumConfig {
	
	@PostConstruct
	void postConstruct() {
		System.setProperty("webdriver.chrome.driver", Constants.DRIVER_LOCATION);
	}
	
	@Bean
	public ChromeDriver driver() {
		return new ChromeDriver();
	}
}
