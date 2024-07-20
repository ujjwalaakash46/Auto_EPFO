package com.example.epf.epf_utility.model.view;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.epf.epf_utility.service.Constants;

import lombok.Data;

@Data
public class EpfViewHandler {
	
	private String startAt;
	private String endAt;
	private String status;
	
	
	public void setStartAt(LocalDateTime startAt) {
		this.startAt = DateTimeFormatter.ofPattern(Constants.DATE_TIME_PATTERN.toString()).format(startAt);
	}

	public void setEndAt(LocalDateTime endAt) {
		this.endAt = DateTimeFormatter.ofPattern(Constants.DATE_TIME_PATTERN.toString()).format(endAt);
	}

}
