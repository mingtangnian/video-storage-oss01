package com.example.demo.controller;

import com.example.demo.utils.RtspToMP4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class ConvertController {
    @Autowired
    private RtspToMP4 rtspToMP4;

    private Map<Integer,Process> map=new HashMap<>();

    @GetMapping(value = "/start")
    public String Start(@RequestParam("id") Integer id,@RequestParam("fileName") String fileName) {
        String ffmpegPath="C:\\ffmpeg\\ffmpeg\\bin\\ffmpeg.exe";
        String streamUrl="rtsp://10.229.32.168:554/rtp/44010200491110000001_44010200491310000001";
        String FilePath="D://"+fileName;
        Process process = rtspToMP4.StartRecord(ffmpegPath, streamUrl, FilePath);
        if(null!=process){
            map.put(id,process);
            return "success";
        }
        return "fail";
    }

    @GetMapping(value = "/stop")
    public String stop(@RequestParam("id") Integer id) {
        if(map.containsKey(id)){
            Process process = map.get(id);
            if(null!=process){
                rtspToMP4.stopRecord(process);
                return "success";
            }
        }
        return "fail";
    }
}
