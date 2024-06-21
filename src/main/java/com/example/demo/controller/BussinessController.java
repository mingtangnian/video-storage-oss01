package com.example.demo.controller;


import com.example.demo.service.impl.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/bussiness")
public class BussinessController {

    @Autowired
    private OssService ossService;

    @Autowired
    private HttpServletResponse response;

    /**
     * 下载文件
     * @param filePath
     * @param fileName
     * @throws Exception
     */
   @GetMapping(value = "/download")
   public void download(@RequestParam("filePath") String filePath
           ,@RequestParam("fileName") String fileName) throws Exception{
       ossService.download(filePath,fileName,response);
   }

    /**
     * 查询地址
     * @param filePath
     * @param fileName
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getUrl")
    public String getUrl(@RequestParam("filePath") String filePath
            ,@RequestParam("fileName") String fileName) throws Exception{
        return ossService.getAbsoluteUrlByRelativeUrl(filePath,fileName);
    }
}
