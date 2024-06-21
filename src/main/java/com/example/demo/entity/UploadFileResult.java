package com.example.demo.entity;

import lombok.Data;

@Data
public class UploadFileResult {
    //文件名称
    private String fileName;

    //文件全地址名
    private String fullFileName;

    //文件路径名称
    private String filePath;

    //根路径
    private String fileHost;

    // oss
    private String bucketName;

    public UploadFileResult(String bucketName,
                            String fileHost,
                            String filePath,
                            String fileName,
                            String fullFileName){
        this.bucketName=bucketName;
        this.fileHost=fileHost;
        this.filePath=filePath;
        this.fileName=fileName;
        this.fullFileName=fullFileName;
    }
}
