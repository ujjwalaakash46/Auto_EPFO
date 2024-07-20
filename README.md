# EFPO number automation 

To automate the extraction process of regitarion data and their payments details also the other records associated with that EPFO number from EPFO website by uploading an Excel.

This is just to get hands-on experience on automation.

For captcha reading, we are using tesseract-ocr and OpenCV for image processing.

### Setup
1. Install Chrome.
2. Download Chrome driver of same version as of Chrome. Store the path of `chromedriver.exe` in constant file.
3. Download tessdata 'eng.traineddata' from https://github.com/tesseract-ocr/tessdata. Store it in project folder. Set `TESSDATA_PREFIX` in evn system variabes with path of tessdata folder.
4. Download OpenCV 4.6 from https://opencv.org/releases/. and Install it. store the path of `opencv_java460.dll` in constant file.
