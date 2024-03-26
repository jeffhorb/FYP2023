package com.ecom.fyp2023.ModelClasses;

public class FileModel {
    private String fileName;
    private String downloadUrl;

    private String filePath;

    public FileModel() {
        // Default constructor required for Firebase
    }

    public FileModel(String fileName, String downloadUrl,  String filePath) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
