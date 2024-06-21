package com.example.demo.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.example.demo.config.OSSConfig;
import com.example.demo.entity.UploadFileResult;
import com.example.demo.enums.StatusCode;
import com.example.demo.service.IOssService;
import com.example.demo.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class OssService implements IOssService {

    @Autowired
    private OSSConfig ossConfig;

    @Autowired
    private OSS ossClient;

    private final static String endpoint = "oss-cn-nanjing-zsh-d01-a.njcloud-dev-inc.sinopec.com";

    private final static String accessKeyId = "olf3NkqLXhrCtppE";

    private final static String accessKeySecret = "Ec1ohHB5Y3iHlfOWyUyBGUqO5FJUWu";

    private final static String bucketName = "shbd-vpc5-oss001";

    private final static String fileHost = "video-ming";

//    private static OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

    /**
     * 文件上传
     * @param file
     * @return
     */
    @Override
    public UploadFileResult uploadFile(File file) {
        //校验视频格式类型
        if(CommonUtil.isSupportedVideoFormat(file)){
            log.trace("【video upload】 judge fail!");
            return null;
        }
        String extension = StringUtils.getFilenameExtension(file.getName());
        //新增文件扩展校验
        try {
            return uploadFileByInputStream(new FileInputStream(file), extension);
        } catch (IOException e) {
            log.error("upload file error:{}",e.getMessage());
        }
        return null;
    }

    /**
     * 以字节流形式上传到oss
     * @param inputStream
     * @param extension 文件扩展名
     * @return
     */
    @Override
    public UploadFileResult uploadFileByInputStream(InputStream inputStream, String extension) {
        String fileName = UUID.randomUUID() + "." + extension;
        try {
            // 设置对象
            ObjectMetadata objectMetadata = new ObjectMetadata();
            // 设置数据流里有多少个字节可以读取
            objectMetadata.setContentLength(inputStream.available());
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setHeader("Pragma", "no-cache");
            objectMetadata.setContentDisposition("inline;filename=" + fileName);

            // demo/20240605/fc0dfd45-b872-407b-ae84-d510b04439f9.mp4
            String filePath = new SimpleDateFormat("yyyyMMdd").format(new Date());
            StringBuilder stringBuilder=new StringBuilder(fileHost)
                    .append(File.separator)
                    .append(filePath)
                    .append(File.separator)
                    .append(fileName);
            String fullFileName = stringBuilder.toString();
            log.debug("【文件上传】，fullFileName is:{}",fullFileName);
            ossClient.putObject(bucketName, fullFileName, inputStream, objectMetadata);
            log.info("【文件上传成功】，fullFileName is:{}",fullFileName);
            return new UploadFileResult(bucketName,fileHost,filePath,fileName,fullFileName);
        } catch (IOException e) {
            log.error("【文件上传异常】，异常：{}",e.getMessage());
        }
        return null;
    }

    /**
     * 获取字节码上传到OSS
     * @param bytes
     * @param extension 文件扩展名
     * @return
     */
    @Override
    public UploadFileResult uploadFileByBytes(byte[] bytes,String extension) {
        return uploadFileByInputStream(new ByteArrayInputStream(bytes), extension);
    }

    /**
     * 绝对路径 （在根据相对路径和目录获取可访问的绝对路径）
     * @param filePath
     * @param fileName
     * @return
     */
    @Override
    public String getAbsoluteUrlByRelativeUrl(String filePath,String fileName) {
        if(com.aliyuncs.utils.StringUtils.isEmpty(filePath)
                || com.aliyuncs.utils.StringUtils.isEmpty(fileName)){
            return null;
        }
        StringBuilder stringBuilder=new StringBuilder(fileHost)
                .append(File.separator)
                .append(filePath)
                .append(File.separator)
                .append(fileName);
        String ossFilePath= stringBuilder.toString();
        Date expireDate = DateUtils.addDays(new Date(), 1);
        try{
            URL url = ossClient.generatePresignedUrl(bucketName, ossFilePath, expireDate);
            if (url != null) {
                return url.toString();
            }
        }catch (Exception e){
            log.error("getAbsoluteUrlByRelativeUrl error:{}",e.getMessage());
        }
        return null;
    }

    /**
     * oss文件下载
     * @param filePath
     * @param fileName
     * @param response
     * @return
     * @throws UnsupportedEncodingException
     */
    @Override
    public String download(String filePath, String fileName, HttpServletResponse response) throws UnsupportedEncodingException {
        //        // 设置响应头为下载
//        response.setContentType("application/x-download");
//        // 设置下载的文件名
//        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
//        response.setCharacterEncoding("UTF-8");
        // 文件名以附件的形式下载
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

        // 获取oss的Bucket名称
//        String bucketName = ossConfig.getBucketName();
//        // 获取oss目标文件夹
//        String fileHost = ossConfig.getFileHost();
        // 日期目录
        // demo/20240605/fc0dfd45-b872-407b-ae84-d510b04439f9.mp4
        StringBuilder stringBuilder=new StringBuilder(fileHost)
                .append(File.separator)
                .append(filePath)
                .append(File.separator)
                .append(fileName);
        String fileKey = stringBuilder.toString();
        // ossObject包含文件所在的存储空间名称、文件名称、文件元信息以及一个输入流。
        OSSObject ossObject = ossClient.getObject(bucketName, fileKey);
        try {
            // 读取文件内容。
            InputStream inputStream = ossObject.getObjectContent();
            BufferedInputStream in = new BufferedInputStream(inputStream);// 把输入流放入缓存流
            ServletOutputStream outputStream = response.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(outputStream);// 把输出流放入缓存流
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            if (out != null) {
                out.flush();
                out.close();
            }
            if (in != null) {
                in.close();
            }
            return StatusCode.SUCCESS.getMsg();
        } catch (Exception e) {
            return StatusCode.ERROR.getMsg();
        }
    }

    /**
     * oss文件删除
     * @param filePath
     * @param fileName
     * @return
     */
    @Override
    public String delete(String filePath, String fileName) {
//        // 获取oss的Bucket名称
//        String bucketName = ossConfig.getBucketName();
//        // 获取oss的地域节点
//        String endpoint = ossConfig.getEndPoint();
//        // 获取oss的AccessKeySecret
//        String accessKeySecret = ossConfig.getAccessKeySecret();
//        // 获取oss的AccessKeyId
//        String accessKeyId = ossConfig.getAccessKeyId();
//        // 获取oss目标文件夹
//        String fileHost = ossConfig.getFileHost();

        try {
            /**
             * 注意：在实际项目中，不需要删除OSS文件服务器中的文件，
             * 只需要删除数据库存储的文件路径即可！
             */
            // 建议在方法中创建OSSClient 而不是使用@Bean注入，不然容易出现Connection pool shut down
//            OSSClient ossClient = new OSSClient(endpoint,
//                    accessKeyId, accessKeySecret);
            // 根据BucketName,filetName删除文件
            // 删除目录中的文件，如果是最后一个文件fileoath目录会被删除。
            StringBuilder sb=new StringBuilder(fileHost)
                    .append(File.separator)
                    .append(filePath)
                    .append(File.separator)
                    .append(fileName);
            String fileKey = sb.toString();
            ossClient.deleteObject(bucketName, fileKey);

            try {
            } finally {
                ossClient.shutdown();
            }
            log.info("【文件删除成功】，filekey is:{}",fileKey);
            return StatusCode.SUCCESS.getMsg();
        } catch (Exception e) {
            e.printStackTrace();
            return StatusCode.ERROR.getMsg();
        }
    }

    public static void main(String[] args){
        String filePath = "20240620";
        String fileName = "71b32722-360a-49d1-b313-66d89b39ced1.mp4";
        File file1= new File("D:/test.mp4");
        OssService ossService=new OssService();
//        String str = ossService.delete(filePath,fileName);
//        UploadFileResult uploadFileResult = ossService.uploadFile(file1);
        String url = ossService.getAbsoluteUrlByRelativeUrl(filePath,fileName);
//        String url = "";
        log.info("result url is:{}",url);
    }

    public static void main2(String[] args){
        try{
            // 此处使用的是外网地址
            String inputFile = "rtsp://10.229.32.168:554/rtp/44010200491110000001_44010200491310000001";
            String outputFile = "D:/out.mp4";
            frameRecord(inputFile, outputFile, 1);
        }catch (Exception ee){
            log.info("转存出错：{}",ee.getMessage());
        }
    }

    public static void frameRecord(String inputFile, String outputFile, int audioChannel)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        // 设置为全局控制变量，用于控制录制结束
        boolean isStart = true;
        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        // 如果不设置成tcp连接时，默认使用UDP，丢包现象比较严重
//        grabber.setOption("rtsp_transport", "udp"); // 设置成tcp以后比较稳定
        // 流媒体输出地址，分辨率（长，高），是否录制音频（0：不录制/1：录制）
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1280, 720, audioChannel);
        // 不进行转码时，编码格式默认为HFYU,使用VLC播放器时无法播放下载的视频 --可能和海康的摄像头有关
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);// avcodec.AV_CODEC_ID_H264，编码
        // 开始取视频源
        recordByFrame(grabber, recorder, isStart);
    }

    private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        try {
            // 建议在线程中使用该方法
            grabber.start();
            recorder.start();

            org.bytedeco.javacv.Frame frame = null;

            // 此处仅为本地预览
            // CanvasFrame cframe = new CanvasFrame("欢迎来到直播间", CanvasFrame.getDefaultGamma() /
            // grabber.getGamma());
            // cframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 窗口关闭，则程序关闭
            // cframe.setAlwaysOnTop(true);

            while (status && (frame = grabber.grabFrame()) != null) {
                recorder.record(frame);
                // cframe.showImage(frame);
            }
            recorder.stop();
            grabber.stop();
        } finally {
            if (grabber != null) {
                grabber.stop();
            }
        }
    }
}
