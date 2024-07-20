package com.example.epf.epf_utility.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.epf.epf_utility.entity.PaymentDetails;
import com.example.epf.epf_utility.entity.RegistrationData;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
//@Scope("request")
public class ExcelSerivce {
	
	private XSSFWorkbook workbook;
	private XSSFWorkbook failedListWorkbook;
	
	private List<RegistrationData> registrationDataList = new LinkedList<>();
	private List<String> failedEpfoList = new LinkedList<>();
	private List<PaymentDetails> paymentDetailList = new LinkedList<>();

	
	private ExcelSerivce() {
		workbook = new XSSFWorkbook();
		failedListWorkbook = new XSSFWorkbook();
	}
	
	public List<RegistrationData> getRegistrationDataList() {
		return registrationDataList;
	}
	
	public void addRegistrationData(RegistrationData rd) {
		registrationDataList.add(rd);
	}
	
	public List<String> getFailedEpfoListList() {
		return failedEpfoList;
	}
	
	public void addFailedEpfo(String rd) {
		failedEpfoList.add(rd);
	}
	
	public List<PaymentDetails> getPaymentDetailList() {
		return paymentDetailList;
	}
	
	public void addPaymentDetailList(PaymentDetails rd) {
		paymentDetailList.add(rd);
	}
	public void addAllPaymentDetailList(List<PaymentDetails> rdList) {
		paymentDetailList.addAll(rdList);
	}
	
	public Workbook getInstance() {
		if(workbook==null) new ExcelSerivce();
		return workbook;
	}
	
	public void writeFile(String filePath) {
		try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
			addRegistrationData();
			addPaymentDetails();
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
                workbook = new XSSFWorkbook();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	
	public void writeFailedFile(String filePath) {
		try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
			addFailedList();
			failedListWorkbook.write(fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				failedListWorkbook.close();
				failedListWorkbook = new XSSFWorkbook();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addRegistrationData() {
		XSSFSheet sheet = workbook.createSheet("EPF Registration Data");
		
		Row headerRow = sheet.createRow(0);

        // Create a bold font for headers
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        
        // Create a cell style with the bold font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        String[] headers = {
        		"EPF Number",
        		"EPF Establishment Code",
        		"EPF Establishment Name",
        		"EPF Establishment Status",
        		"EPF Establishment Status",
        		"EPF Registration Status",
        		"EPF Post Coverage Status",
        		"EPF Exemption Status",
        		"EPF Working Status",
        		"EPF Coverage Section",
        		"EPF Actionable Status",
        		"EPF Date Of Coverage",
        		"EPF Pan Status",
        		"EPF Section Applicable",
        		"EPF ESIC Code",
        		"EPF Ownership Type",
        		"EPF EPFO Office Name",
        		"EPF EPFO Office Address",
        		"EPF Primary Business Activity",
        		"EPF Date Of Setup Of Establishment",
        		"EPF Address",
        		"EPF City",
        		"EPF State",
        		"EPF Zone",
        		"EPF Pin Code",
        		"EPF District",
        		"EPF Country",
        		"EPF Region"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
        
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        for (int i = 1; i < registrationDataList.size(); i++) {
            Row row = sheet.createRow(i);
            RegistrationData rd = registrationDataList.get(i);

            row.createCell(0).setCellValue(rd.EPFNumber);
            row.createCell(1).setCellValue(rd.EPFEstablishmentCode);
            row.createCell(2).setCellValue(rd.EPFEstablishmentName);
            row.createCell(3).setCellValue(rd.EPFEstablishmentStatus);
            row.createCell(4).setCellValue(rd.EPFRegistrationStatus);
            row.createCell(5).setCellValue(rd.EPFPostCoverageStatus);
            row.createCell(6).setCellValue(rd.EPFExemptionStatus);
            row.createCell(7).setCellValue(rd.EPFWorkingStatus);
            row.createCell(8).setCellValue(rd.EPFCoverageSection);
            row.createCell(9).setCellValue(rd.EPFActionableStatus);
            row.createCell(10).setCellValue(rd.EPFPanStatus);
            row.createCell(11).setCellValue(rd.EPFDateOfCoverage);
            row.createCell(12).setCellValue(rd.EPFSectionApplicable);
            row.createCell(13).setCellValue(rd.EPFESICCode);
            row.createCell(14).setCellValue(rd.EPFOwnershipType);
            row.createCell(15).setCellValue(rd.EPFEPFOOfficeName);
            row.createCell(16).setCellValue(rd.EPFEPFOOfficeAddress);
            row.createCell(17).setCellValue(rd.EPFPrimaryBusinessActivity);
            row.createCell(18).setCellValue(rd.EPFDateOfSetupOfEstablishment);
            row.createCell(19).setCellValue(rd.EPFAddress);
            row.createCell(20).setCellValue(rd.EPFCity);
            row.createCell(21).setCellValue(rd.EPFState);
            row.createCell(22).setCellValue(rd.EPFZone);
            row.createCell(23).setCellValue(rd.EPFPinCode);
            row.createCell(24).setCellValue(rd.EPFDistrict);
            row.createCell(25).setCellValue(rd.EPFCountry);
            row.createCell(26).setCellValue(rd.EPFRegion);
        }
	}
	
	public void addPaymentDetails() {
		XSSFSheet sheet = workbook.createSheet("EPF Payment Details");
		
		Row headerRow = sheet.createRow(0);
		
		// Create a bold font for headers
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		
		// Create a cell style with the bold font
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		
		String[] headers = {
				"Counterparty Name",
				"EPF Number",
				"Establishment Code",
				"TRRN",
				"Date Of Credit",
				"Amount",
				"Wage Month",
				"No Of Employee",
				"ECR"
		};
		
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerCellStyle);
		}
		
		
		for (int i = 0; i < headers.length; i++) {
			sheet.autoSizeColumn(i);
		}
		
		for (int i = 1; i < paymentDetailList.size(); i++) {
			Row row = sheet.createRow(i);
			PaymentDetails pd = paymentDetailList.get(i);
			
			row.createCell(0).setCellValue(pd.Counterparty);
			row.createCell(1).setCellValue(pd.EPFNumber);
			row.createCell(2).setCellValue(pd.EstablishmentCode);
			row.createCell(3).setCellValue(pd.TRRN);
			row.createCell(4).setCellValue(pd.DateOfCredit);
			row.createCell(5).setCellValue(pd.Amount);
			row.createCell(6).setCellValue(pd.WageMonth);
			row.createCell(7).setCellValue(pd.NoOfEmployee);
			row.createCell(8).setCellValue(pd.ECR);
		}
	}

	public void addFailedList() {
		
		XSSFSheet sheet = failedListWorkbook.createSheet("Failed EPFO");
		
		Row headerRow = sheet.createRow(0);
		
		// Create a bold font for headers
		Font headerFont = failedListWorkbook.createFont();
		headerFont.setBold(true);
		
		// Create a cell style with the bold font
		CellStyle headerCellStyle = failedListWorkbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		
		String[] headers = {
				"EPFO Number",
		};
		
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerCellStyle);
		}
		
		
		for (int i = 0; i < headers.length; i++) {
			sheet.autoSizeColumn(i);
		}
		
		for (int i = 1; i < failedEpfoList.size(); i++) {
			Row row = sheet.createRow(i);
			row.createCell(0).setCellValue(failedEpfoList.get(i));
		}
	}
	
	public ResponseEntity<Resource> downloadOutputFile(String filePath) {
		
	  	File file = new File(filePath);
	  	
	  	Path path = Paths.get(file.getAbsolutePath());
	    ByteArrayResource resource = null;
	    
		try {
			resource = new ByteArrayResource(Files.readAllBytes(path));
		} catch (IOException e) {
			log.error("Downloading failed !"+e);
		}
		
		 HttpHeaders headers = new HttpHeaders();
	        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");     //HTTP 1.1
	        headers.add("Content-Disposition", "attachment;  filename="+filePath.split("/")[1]);
	        headers.add("Pragma", "no-cache");
	        headers.add("Expires", "0");

		    return ResponseEntity.ok()
		            .headers(headers)
		            .contentLength(file.length())
		            .contentType(MediaType.APPLICATION_OCTET_STREAM)
		            .body(resource);
	}

}
