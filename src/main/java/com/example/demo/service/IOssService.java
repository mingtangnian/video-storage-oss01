package com.example.demo.service;

import com.example.demo.entity.UploadFileResult;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public interface IOssService {

    /**
     * 上传文件到 OSS
     * @param file
     * @return
     */
    UploadFileResult uploadFile(File file);

    /**
     * 根据流 上传文件到 OSS
     * @param inputStream
     * @param extension   文件扩展名
     * @return
     */
    UploadFileResult uploadFileByInputStream(InputStream inputStream, String extension);

    /**
     * 根据byte 上传文件到 OSS
     * @param bytes
     * @param extension
     * @return
     */
    UploadFileResult uploadFileByBytes(byte[] bytes,String extension);

    /**
     * 根据oss相对路径 获取 oss绝对路径
     * @param filePath
     * @param fileName
     * @return
     */
    String getAbsoluteUrlByRelativeUrl(String filePath,String fileName);

    /**
     * oss存储文件下载
     * @param fileName
     * @param response
     * @return
     */
    String download(String filePath,String fileName, HttpServletResponse response) throws UnsupportedEncodingException;

    /**
     * 删除文件
     * @param filePath
     * @param fileName
     * @return
     */
    String delete(String filePath,String fileName) ;
}
