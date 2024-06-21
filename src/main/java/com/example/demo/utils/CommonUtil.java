package com.example.demo.utils;

import org.springframework.stereotype.Component;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;

@Component
public class CommonUtil {

    // 支持的视频格式MIME类型列表
    private static final String[] SUPPORTED_VIDEO_FORMATS = new String[]{
            "video/mp4", "video/quicktime", "video/x-msvideo", "video/x-ms-wmv", "video/3gpp", "video/x-flv", "video/mpeg"
    };
    /**
     * 验证文件是否为支持的视频格式
     * @param file 要验证的文件
     * @return 如果文件是支持的视频格式，则返回true；否则返回false
     */
    public static boolean isSupportedVideoFormat(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        // 使用MimetypesFileTypeMap获取文件MIME类型
        MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
        String mimeType = mimetypesFileTypeMap.getContentType(file.getName());
        // 判断MIME类型是否为支持的视频格式
        for (String supportedFormat : SUPPORTED_VIDEO_FORMATS) {
            if (mimeType.equalsIgnoreCase(supportedFormat)) {
                return true;
            }
        }
        return false;
    }
}
