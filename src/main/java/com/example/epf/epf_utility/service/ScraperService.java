package com.example.epf.epf_utility.service;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.bouncycastle.util.test.TestRandomBigInteger;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.epf.epf_utility.entity.PaymentDetails;
import com.example.epf.epf_utility.entity.RegistrationData;
import com.example.epf.epf_utility.model.view.EpfViewHandler;

import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * 
 */
@Service
@AllArgsConstructor
public class ScraperService {

		private static final String URL = Constants.TARGET_URL;
		
		@Autowired
		private ChromeDriver driver;
		
		@Autowired
		private ImagePreprocessing imgImagePreprocessing;
		
		@Autowired
		private ExcelSerivce excelSerivce;
		
		private static String currentEPFO;
		private static String originalWindow;
		private static String recordsWindow;
		private static RegistrationData currentRegistrationData;
		private static boolean isInitialSetupDone=false;
		private static boolean skipEPFO=false;
		
	    private final Logger logger = LoggerFactory.getLogger(ScraperService.class);
		
		//Setting up the Driver
		public void initialSetUp() {
			driver.get(URL);
			driver.manage().window().maximize();
			originalWindow = driver.getWindowHandle();
			isInitialSetupDone = true;
		}
		
		
		/**
		 * 
		 * Main extract EPFO details method
		 * 
		 * @param epfo
		 * @param epfViewHandler 
		 * @throws IOException
		 * @throws InterruptedException
		 * @throws TesseractException
		 */
		public void scraping(String epfo, EpfViewHandler epfViewHandler) throws IOException, InterruptedException, TesseractException {
			
			if(!isInitialSetupDone)initialSetUp();
			
			
			skipEPFO=false;
			
			mainSearchByEpfo(epfo, epfViewHandler);
			
			
			extractRegistrationData();
			if(skipEPFO)return;
			
			//paymentDetails of current epfo;
			extractPaymentDetails(true);
			if(skipEPFO)return;
			
			openRecords();
			
			System.out.println(excelSerivce.getRegistrationDataList());
			System.out.println(excelSerivce.getPaymentDetailList());
			
		}
		
		/**
		 * Open All Records with pagination.
		 * 
		 * it calls extractRecordRegistrationData() and extractPaymentDetails()
		 * 
		 * @throws InterruptedException
		 */
		private void openRecords() throws InterruptedException {
			try {
				driver.findElement(By.xpath("//a[@title='Click to view details.']"));
			}catch(Exception e) {
				System.out.println("no records present");
				return;
			}
			
			driver.findElement(By.xpath("//a[@title='Click to view details.']")).click();
			
			for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    recordsWindow=windowHandle;
                    break;
                }
            }
			Thread.sleep(1000);
			
			while(true) {
				WebElement table = driver.findElement(By.id("table_pop_up"));
				List<WebElement> rows = table.findElements(By.tagName("tr"));
				for(WebElement row : rows) {
					
					List<WebElement> cells = row.findElements(By.tagName("td"));
					if(cells.size()==0)continue;
					cells.get(0).click();
					extractRecordRegistrationData();
					extractPaymentDetails(false);
				}
				WebElement nextBtn = driver.findElement(By.id("table_pop_up_next"));
				String nextBtnClasses = nextBtn.getAttribute("class");
				if(nextBtnClasses.contains("disabled")) {
					break;
				}
				
				nextBtn.click();
			}
			
			 driver.close();

	         // Switch back to the original window
	         driver.switchTo().window(originalWindow);
			
			
		}
		

		/**
		 * 
		 * Extract Payment Details
		 * 
		 * @param isMainPayementDetailPage
		 * @throws InterruptedException
		 */
		private void extractPaymentDetails(boolean isMainPayementDetailPage) throws InterruptedException {
			try {
				driver.findElement(By.xpath("//u[normalize-space()='View Payment Details']")).click();
				Thread.sleep(500);
			} catch (Exception e) {
				excelSerivce.addFailedEpfo(currentEPFO);
				return;
			}
			
			Thread.sleep(1000);
			
			for (String windowHandle : driver.getWindowHandles()) {
				if(isMainPayementDetailPage) {
					if (!windowHandle.equals(originalWindow)){
						driver.switchTo().window(windowHandle);
						break;
					}
				}else {
					if (!windowHandle.equals(originalWindow) && !windowHandle.equals(recordsWindow)){
						driver.switchTo().window(windowHandle);
						break;
					}
					
				}
            }
			
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("table_pop_up")));

			
			while(true) {
				WebElement table = driver.findElement(By.id("table_pop_up"));
				List<WebElement> rows = table.findElements(By.tagName("tr"));
				List<PaymentDetails> paymentList = new LinkedList<>();
				for(WebElement row : rows) {
					List<WebElement> cells = row.findElements(By.tagName("td"));
					if(cells.size()==0)continue;
					PaymentDetails pd = new PaymentDetails();
					pd.TRRN = cells.get(0).getText();
                    pd.DateOfCredit = cells.get(1).getText();
                    pd.Amount = cells.get(2).getText();
                    pd.WageMonth = cells.get(3).getText();
                    pd.NoOfEmployee = cells.get(4).getText();
                    pd.ECR = cells.get(5).getText();
                    pd.Counterparty=currentRegistrationData.EPFEstablishmentName;
                    pd.EPFNumber=currentRegistrationData.EPFNumber;
                    pd.EstablishmentCode=currentRegistrationData.EPFEstablishmentCode;
                    
                    paymentList.add(pd);
				}
				
				excelSerivce.addAllPaymentDetailList(paymentList);
				
				//For pagination
				WebElement nextBtn = driver.findElement(By.id("table_pop_up_next"));
				String nextBtnClasses = nextBtn.getAttribute("class");
				if(nextBtnClasses.contains("disabled")) {
					break;
				}
				
				nextBtn.click();
			}
			
			
			 driver.close();

	         // Switch back to the previous window
	         driver.switchTo().window(isMainPayementDetailPage ? originalWindow: recordsWindow);
			
			
		}

		/**
		 * 
		 * Extract EPFO details
		 * 
		 * @throws InterruptedException
		 */
		public void extractRegistrationData() throws InterruptedException {
			Thread.sleep(200);
			
			try {
				driver.findElement(By.xpath("//td[@class='dataTables_empty']"));
				excelSerivce.addFailedEpfo(currentEPFO);
				skipEPFO=true;
				return;
			}catch(Exception e) {
				
			}
			
			
			RegistrationData rd = new RegistrationData();
			rd.EPFEstablishmentCode = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[5]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[1]/td[2]")).getText();
			rd.EPFEstablishmentName = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[5]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[2]/td[2]")).getText();
			rd.EPFEstablishmentStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[5]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[3]/td[2]")).getText();
			rd.EPFRegistrationStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[5]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[4]/td[2]")).getText();
			rd.EPFPostCoverageStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[5]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[5]/td[2]")).getText();
			
			rd.EPFExemptionStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[6]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[1]/td[2]")).getText();
			rd.EPFWorkingStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[6]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[2]/td[2]")).getText();
			rd.EPFCoverageSection = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[6]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[3]/td[2]")).getText();
			rd.EPFActionableStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[6]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[4]/td[2]")).getText();
			rd.EPFDateOfCoverage = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[6]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[5]/td[2]")).getText();
//			
			rd.EPFPanStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[2]/td[2]")).getText();
			rd.EPFSectionApplicable = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[3]/td[2]")).getText();
			rd.EPFESICCode = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[4]/td[2]")).getText();
			rd.EPFOwnershipType = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[5]/td[2]")).getText();
			
			rd.EPFEPFOOfficeAddress = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[9]/td[4]")).getText();
			rd.EPFPrimaryBusinessActivity = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[3]/td[4]")).getText();
			rd.EPFEPFOOfficeName = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[9]/td[2]")).getText();

			rd.EPFDateOfSetupOfEstablishment = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[5]/td[4]")).getText();
			rd.EPFAddress = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[6]/td[2]")).getText();
			rd.EPFCity = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[7]/td[2]")).getText();
			rd.EPFState = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[8]/td[2]")).getText();
			rd.EPFZone = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[10]/td[2]")).getText();
			rd.EPFPinCode = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[6]/td[4]")).getText();
			rd.EPFDistrict = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[7]/td[4]")).getText();
			rd.EPFCountry = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[8]/td[4]/span[1]")).getText();
			rd.EPFRegion = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[7]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[10]/td[4]")).getText();
			rd.EPFNumber=rd.EPFEstablishmentCode;
			excelSerivce.addRegistrationData(rd);
			System.out.println(rd);
			currentRegistrationData=rd;
			
		}

		/**
		 * Extract Details of Establishment/s with Same PAN
		 * 
		 * Same as extractRegistrationData() but scraping is different
		 * @throws InterruptedException
		 */
		public void extractRecordRegistrationData() throws InterruptedException {
			Thread.sleep(200);
			
			
			RegistrationData rd = new RegistrationData();
			rd.EPFEstablishmentCode = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[2]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[1]/td[2]")).getText();
			rd.EPFEstablishmentName = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[2]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[2]/td[2]")).getText();
			rd.EPFEstablishmentStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[2]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[3]/td[2]")).getText();
			rd.EPFRegistrationStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[2]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[4]/td[2]")).getText();
			rd.EPFPostCoverageStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[2]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[5]/td[2]")).getText();
			
			rd.EPFExemptionStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[1]/td[2]")).getText();
			rd.EPFWorkingStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[2]/td[2]")).getText();
			rd.EPFCoverageSection = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[3]/td[2]")).getText();
			rd.EPFActionableStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[4]/td[2]")).getText();
			rd.EPFDateOfCoverage = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[5]/td[2]")).getText();
//			
			rd.EPFPanStatus = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[2]/td[2]")).getText();
			rd.EPFSectionApplicable = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[3]/td[2]")).getText();
			rd.EPFESICCode = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[4]/td[2]")).getText();
			rd.EPFOwnershipType = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[5]/td[2]")).getText();
			
			rd.EPFEPFOOfficeAddress = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[9]/td[4]")).getText();
			rd.EPFPrimaryBusinessActivity = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[3]/td[4]")).getText();
			rd.EPFEPFOOfficeName = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[9]/td[2]")).getText();

			rd.EPFDateOfSetupOfEstablishment = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[5]/td[4]")).getText();
			rd.EPFAddress = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[6]/td[2]")).getText();
			rd.EPFCity = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[7]/td[2]")).getText();
			rd.EPFState = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[8]/td[2]")).getText();
			rd.EPFZone = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[10]/td[2]")).getText();
			rd.EPFPinCode = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[6]/td[4]")).getText();
			rd.EPFDistrict = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[7]/td[4]")).getText();
			rd.EPFCountry = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[8]/td[4]")).getText();
			rd.EPFRegion = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[4]/div[1]/div[2]/div[1]/div[2]/table[1]/tbody[1]/tr[10]/td[4]")).getText();
			rd.EPFNumber=rd.EPFEstablishmentCode;
			excelSerivce.addRegistrationData(rd);
			System.out.println(rd);
			currentRegistrationData=rd;
			
		}

		
		
		/**
		 * 
		 * For main EPFO Search.
		 * Does Captcha read.
		 * 
		 * 
		 * @param epfo
		 * @param epfViewHandler 
		 * @throws TesseractException
		 * @throws InterruptedException
		 * @throws IOException
		 */
		public void mainSearchByEpfo(String epfo, EpfViewHandler epfViewHandler) throws TesseractException, InterruptedException, IOException {
			
			//to bypass voilation;
			driver.navigate().refresh();
			Thread.sleep(2000);
			
			originalWindow = driver.getWindowHandle();
			
			String estCode = epfo.substring(5, 12);
			currentEPFO=epfo;
			
			int i=0;
			
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            // You can also wait for a specific element to be present
			WebElement estCodeInputField = null;
			try {
				estCodeInputField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='estCode']")));
			}catch(Exception e) {
				epfViewHandler.setStatus(Constants.FAILED);
				return;
			}
			
			
            estCodeInputField.clear();
			while(true) {
				
				//entering est code
				estCodeInputField.sendKeys(currentEPFO);
				
				Thread.sleep(1000);
				
				//fetching Captcha Img
				WebElement captchaImg = driver.findElement(By.id("captchaImg"));
				File file = captchaImg.getScreenshotAs(OutputType.FILE);
				String path = Constants.CAPTCHA_PATH;
				
				FileHandler.copy(file, new File(path));
				
				Thread.sleep(2000);
				
				ITesseract tesseract = new Tesseract();
				
		        tesseract.setVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
		        tesseract.setPageSegMode(7);
		        tesseract.setVariable("load_system_dawg", "F");
		        tesseract.setVariable("load_freq_dawg", "F");
		        tesseract.setVariable("min_characters_to_try", "5");
//		        tesseract.setVariable("classify_bln_numeric_mode", "1");
		        
				//Image Processing
				imgImagePreprocessing.preprocessImage(path, path);
				
				//Reading Image
				String captchaValue = tesseract.doOCR(new File(path)).replaceAll("\\s+", "");
				
				//entering Captcha
				driver.findElement(By.id("captcha")).sendKeys(captchaValue);
				
				//click search btn
				driver.findElement(By.id("searchEmployer")).click();
				Thread.sleep(2000);
				
				try {
					//If this is found then it's a Failed EPFO.
					driver.findElement(By.xpath("//div[contains(text(),'No details found for this criteria. Please enter v')]"));
					excelSerivce.addFailedEpfo(estCode);
				}catch(Exception e) {
					
				}
				
				try {
					
					//If this is found then Captcha was wrong.
					if(driver.findElement(By.xpath("//div[normalize-space()='Please enter valid captcha.']")).getText().isEmpty()){
						break;
					};
					logger.debug("Captcha failed. Trying again");
					
					if(i>=Constants.TRY_COUNT) {

//						driver.navigate().refresh();
//						Thread.sleep(2000);
					}
					i++;
					
				} catch (Exception e) {
					break;
				}
			}
			i=0;
			logger.info("captcha passed");
				
			//waiting for data loading
			while(true) {
				try {
					driver.findElement(By.xpath("//h4[normalize-space()='Loading...']"));
					Thread.sleep(1000);
				} catch (Exception e) {
					break;
				}
			}
			Thread.sleep(1000);
			
			
			//Searching the extact EPFO number
			driver.findElement(By.xpath("//input[@type='search']")).sendKeys(epfo);
			Thread.sleep(500);
			
			
			try {
				//If this is not found then it's a Failed EPFO.
				driver.findElement(By.xpath("//a[@title='Click to view establishment details.']")).click();
				
			}catch(Exception e) {
				excelSerivce.addFailedEpfo(estCode);
			}
			
			//waiting for data loading
			while(true) {
				try {
					driver.findElement(By.xpath("//h4[normalize-space()='Loading...']"));
					Thread.sleep(1000);
				} catch (Exception e) {
					break;
				}
			}
			
		}

		@PreDestroy
		private void preDestroy() {
			closeDriver();			
		}
		
		
		private void closeDriver() {
			driver.close();
			isInitialSetupDone=false;
		}
		
		
}