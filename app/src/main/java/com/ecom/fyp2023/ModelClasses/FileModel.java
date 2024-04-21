package com.ecom.fyp2023.ModelClasses;

public class FileModel {
    private String fileName;
    private String downloadUrl;

    private String filePath;

    private String userAuthId;

    String groupId;
    public FileModel() {
        // Default constructor required for Firebase
    }

    public FileModel(String fileName, String downloadUrl,  String filePath,String groupId,String userAuthId) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.filePath = filePath;
        this.groupId = groupId;
        this.userAuthId = userAuthId;
    }

    public String getUserAuthId() {
        return userAuthId;
    }

    public void setUserAuthId(String userAuthId) {
        this.userAuthId = userAuthId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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
