package com.example.epf.epf_utility.service;


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.codeborne.selenide.Selenide;
import com.example.epf.epf_utility.model.view.EpfViewHandler;
import com.example.epf.epf_utility.model.view.WebDriverConfig;

//import static com.codeborne.selenide.Selenide.open;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.example.epf.epf_utility.model.view.EpfViewHandler;

public class EpfCrawlerTask implements Runnable {
	private final String startAt;
    private final XSSFWorkbook workbook;
    private final EpfViewHandler epfViewHandler;
    private final ScraperService scraperService;
    private final ExcelSerivce excelService;
    
    private int captchaCount = 0;
    private int reloadCount = 0;
    private String epfNoCode = "";
    private final Logger logger = LoggerFactory.getLogger(EpfCrawlerTask.class);
    boolean searchSuccessful = false;
    
    WebDriverConfig wd= new WebDriverConfig();
   
    public EpfCrawlerTask(String startAt, XSSFWorkbook workbook, EpfViewHandler epfViewHandler, ScraperService scraperService, ExcelSerivce excelService) {
        this.startAt = startAt;
        this.workbook = workbook;
        this.epfViewHandler = epfViewHandler;
        this.scraperService = scraperService;
        this.excelService = excelService;
    }

    @Override
    public void run() {
    	
    	
		for (int i =1; i<=workbook.getSheetAt(0).getLastRowNum(); i++ ) {
			Row row = workbook.getSheetAt(0).getRow(i);
		    String epfNumber = row.getCell(0).getStringCellValue();
		    if(epfNumber==null || epfNumber.isEmpty())continue;
		    try {
		    	logger.info("Scraping details of "+epfNumber);
		    	scraperService.scraping(epfNumber, epfViewHandler);
		    }
		    catch(Exception e) {
		    	e.printStackTrace();
		    }

		}
    	
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
    	// Save detailed excel
		excelService.writeFile(Constants.OUTPUT_DIRECTORY+startAt+Constants.FILE_TYPE);
		
		// Save excel of failed EPFO list 
		excelService.writeFailedFile(Constants.OUTPUT_FAILED_DIRECTORY+startAt+Constants.FILE_TYPE);
		
		epfViewHandler.setEndAt(LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE.toString())));
		
		//Update Status
		epfViewHandler.setStatus(Constants.COMPLETED);
		
		logger.info("Scraping completed");
		
		
    }
}
