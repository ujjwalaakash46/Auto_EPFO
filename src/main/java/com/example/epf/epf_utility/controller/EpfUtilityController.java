package com.example.epf.epf_utility.controller;

import org.springframework.stereotype.Controller;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.example.epf.epf_utility.model.view.EpfListHandler;
import com.example.epf.epf_utility.model.view.EpfViewHandler;
import com.example.epf.epf_utility.service.Constants;
import com.example.epf.epf_utility.service.EpfCrawlerTask;
import com.example.epf.epf_utility.service.ExcelSerivce;
import com.example.epf.epf_utility.service.FileValidator;
import com.example.epf.epf_utility.service.ScraperService;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
public class EpfUtilityController {
	private EpfListHandler epfListHandler = new EpfListHandler();
	private ExecutorService executor = Executors.newFixedThreadPool(1);
	
	
	
	@Autowired
	private ScraperService scraperService;
	
	@Autowired
	private ExcelSerivce excelSerivce;
	
	
	@RequestMapping("/")
	public void errorHandler() {
        throw new RuntimeException();
	}

    
    @GetMapping("/")
    public String index(Model model) {
    	model.addAttribute("status",Constants.START_PROCESSING);
        model.addAttribute("epfViewHandler", new EpfViewHandler());
        model.addAttribute("leiListHandler", epfListHandler.getHandlerList());
        return "index";
    }
    
    @PostMapping("/upload")
    public String uploadData(@ModelAttribute EpfViewHandler epfViewHandler, @RequestPart("file") MultipartFile excelMultipartFile, Model model) throws IOException {
        epfViewHandler.setStatus(Constants.CRAWLING);
        epfViewHandler.setStartAt(LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE.toString())));

        if (!FileValidator.validateInputFile(excelMultipartFile)) {
            model.addAttribute("failedStatus", "*Note: Please upload a valid file!!");
            model.addAttribute("status", "");
            model.addAttribute("leiListHandler", epfListHandler.getHandlerList());
            model.addAttribute("leiViewHandler", new EpfViewHandler());
            return "redirect:/";
        }
        
        // Save file locally
        File file = new File("uploaded_file.xlsx");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(excelMultipartFile.getBytes());
        }

        // Start the scraping task
        XSSFWorkbook workbook = new XSSFWorkbook(excelMultipartFile.getInputStream());
        EpfCrawlerTask epfCrawlerTask = new EpfCrawlerTask(epfViewHandler.getStartAt(), workbook, epfViewHandler, scraperService, excelSerivce);
        workbook.close();
        executor.execute(epfCrawlerTask);

        epfListHandler.pushAtStart(epfViewHandler);
        log.info("Crawling started!");

        model.addAttribute("leiListHandler", epfListHandler.getHandlerList());
        model.addAttribute("leiViewHandler", new EpfViewHandler());

        return "redirect:/";
    }
    
    //for download file
    @GetMapping("/downloadFile/{startAt}")
    	public ResponseEntity<Resource> download(@PathVariable("startAt") String startAt, Model model) { 	
    		log.info("Download request received!");
    		
    		return excelSerivce.downloadOutputFile(Constants.OUTPUT_DIRECTORY+startAt+Constants.FILE_TYPE);
    	}
    	
     
    //for download failed file epfo number
    @GetMapping("/downloadFailedFile/{startAt}")
    	public ResponseEntity<Resource> downloadFailed(@PathVariable("startAt") String startAt, Model model) { 	
    		log.info("Download request received!");
    		
    		return excelSerivce.downloadOutputFile(Constants.OUTPUT_FAILED_DIRECTORY+startAt+Constants.FILE_TYPE);
    	}


	


}
