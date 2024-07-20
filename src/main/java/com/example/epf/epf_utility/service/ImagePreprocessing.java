package com.example.epf.epf_utility.service;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

@Service
public class ImagePreprocessing {
	
//	public ImagePreprocessing() {
//		// TODO Auto-generated constructor stub
//	}
	
	static {
        // Load the OpenCV native library
		 System.load(Constants.OPENCV_LOCATION);
        
    }

    public void preprocessImage(String inputImagePath, String outputImagePath) {
        // Read the input image
        Mat src = Imgcodecs.imread(inputImagePath, Imgcodecs.IMREAD_COLOR);
        
        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

//         Apply Gaussian blur to reduce noise and improve OCR accuracy

        // Apply adaptive thresholding to binarize the image
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 115, 255, Imgproc.THRESH_BINARY);

        Mat blurred = new Mat();
        Imgproc.GaussianBlur(binary, blurred, new Size(1, 1), 0);
        // Save the processed image
        Imgcodecs.imwrite(outputImagePath, blurred);
    }

}
