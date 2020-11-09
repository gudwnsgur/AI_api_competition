package com.competition.AI_api_project.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.competition.AI_api_project.service.SceneSplitService;
import com.competition.AI_api_project.service.SceneSplitStatusCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {
    @Autowired
    SceneSplitService sceneSplitService;
    @Autowired
    SceneSplitStatusCheckService sceneSplitStatusCheckService;

    static private String returnedFileID = "";
    static private String accessKey = "-";
    static private String type = "mp4";
    static private String filePath = "C:\\AI_api_project\\AI_api_project\\src" +
            "\\main\\resources\\static\\";

    @PostMapping(value = "/upload", produces = "application/json; charset=utf8")
    public void upload(@RequestParam("uploadfile") MultipartFile f) {

        // file name 지정
        SimpleDateFormat dateFormat = new SimpleDateFormat ( "yyyy-MM-dd_HH-mm-ss_");
        Calendar time = Calendar.getInstance();
        String fileName = dateFormat.format(time.getTime());
        fileName += (Integer.toString((int)(Math.random()*1000))) + ".mp4";
        System.out.println(fileName);

        // file 업로드
        try {
            byte[] bytes = f.getBytes();
            String insPath = filePath + fileName;

            Files.write(Paths.get(insPath), bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String fileID = sceneSplitService.sceneSplit(accessKey, type, filePath+fileName);
        returnedFileID = fileID;

        System.out.println("FILE ID IN CONTROLLER : " + returnedFileID);

        /* 나눠서 사용 */
        // sceneSplitStatusCheckService.sceneSplitStatusCheck(returnedFileID, accessKey);
        return ;
    }

    @PostMapping(value = "/sceneSplit", produces = "application/json; charset=utf8")
    public String sceneSplit() {
        String fileID = returnedFileID;
        sceneSplitStatusCheckService.sceneSplitStatusCheck(fileID, accessKey);
        return "index";
    }
}