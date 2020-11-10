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
    static private String filePath = "C:\\AI_api_project\\AI_api_project\\src" +
            "\\main\\resources\\static\\";

    @PostMapping(value = "/fileUpload")
    public String upload(@RequestParam("file") MultipartFile files) {
        try {
            String fileName = files.getOriginalFilename();
            String savePath = filePath + "newFolder";
            if (!new File(savePath).exists()) {
                try {
                    new File(savePath).mkdir();
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }
            String filePath = savePath + "/" + fileName;
            files.transferTo(new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "index";
    }
}