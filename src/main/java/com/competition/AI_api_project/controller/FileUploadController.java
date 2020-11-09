package com.competition.AI_api_project.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@Controller
public class FileUploadController {
    @PostMapping(value = "/upload")
    public String upload(@RequestParam("file") MultipartFile files) {
        log.info("### upload");
        try {
            String fileName = files.getOriginalFilename();
            String savePath = System.getProperty("user.dir") + "/files";
            if (!new File(savePath).exists()) {
                try {
                    new File(savePath).mkdir();
                }
                catch(Exception e) {
                    e.getStackTrace();
                }
            }
            String filePath = savePath + "/" + fileName;
            files.transferTo(new File(filePath));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "redirect:/";
    }
}
