package com.example.epf.epf_utility.model.view;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

//import com.codeborne.selenide.Configuration;
//
//
//import com.codeborne.selenide.WebDriverRunner;
import com.example.epf.epf_utility.service.Constants;

import jakarta.annotation.PostConstruct;
@Component
public class WebDriverConfig {
//	 private static final Logger logger = LoggerFactory.getLogger(WebDriverConfig.class);
//
//	 @Value("${webdriver.path}")
//	    private String webdriverPath;
//
//	    @Value("${is.windows:false}")
//	    private boolean isWindows;
//	protected WebDriver driver = null;
//	
//	
//	public void setupDriver() {
//		// Setup WebDriverManager to manage ChromeDriver binary
//		  System.setProperty("webdriver.chrome.driver", Constants.WEB_PATH);
//
//        ChromeOptions options = new ChromeOptions();
//        options = getLambdaChromeOptions();
//        
//        // Initialize ChromeDriver
//        driver = new ChromeDriver(options);
//        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
//        driver.manage().timeouts().implicitlyWait(120, TimeUnit.SECONDS);
//        driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
//        
//        // Selenide configuration
//        Configuration.browser = "chrome";
//        Configuration.timeout = 10000;
//        if (isWindows) {
//            Configuration.reportsFolder = "c:\\rubix-scraper";
//            Configuration.downloadsFolder = "c:\\rubix-scraper";
//        } else {
//            Configuration.reportsFolder = "/tmp";
//            Configuration.downloadsFolder = "/tmp";
//        }
//
//        Configuration.headless = true;
//        Configuration.reportsUrl = "";
//        Configuration.savePageSource = false;
//        Configuration.screenshots = false;
//        
//        // Set WebDriver in WebDriverRunner
//        WebDriverRunner.setWebDriver(driver);
//    }
//	 private ChromeOptions getLambdaChromeOptions() {
//	        ChromeOptions options = new ChromeOptions();
//	        options.addArguments("--headless");
//	        options.addArguments("--disable-gpu");
//	        options.addArguments("--window-size=1920,1080");
//	        options.addArguments("--no-sandbox");
//	        options.addArguments("--disable-dev-shm-usage");
//	        return options;
//	    }
//	 public WebDriver getDriver() {
//	        return driver;
//	    }
//	 public void cleanup() {
//	        if (driver != null) {
//	            driver.quit();
//	        }
//	    }
	
}
