package com.competition.AI_api_project.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.competition.AI_api_project.service.*;
import org.apache.commons.io.FilenameUtils;
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
    @Autowired
    VideoSplitService videoSplitService;
    @Autowired
    UploadService uploadService;
    static private String returnedFileID = "";
    static private String accessKey = "MY KEY";
    static private String type = ".mp4";
    static private String filePath = "C:\\AI_api_project\\AI_api_project\\src" +
            "\\main\\resources\\static\\";
    static private String localFilePath = "";


    // 분석하기
    @PostMapping(value = "/upload", produces = "application/json; charset=utf8")
    public String upload(@RequestParam("uploadfile") MultipartFile f) throws InterruptedException, IOException {
        // 시간 + random 값으로 이뤄진 file name 생성
        String dirName = uploadService.getRandomDirName(); // only name
        localFilePath = filePath + "\\" + dirName;
        String fileName = FilenameUtils.getBaseName(f.getOriginalFilename()) + ".mp4";
        System.out.println("[file Path]  " + localFilePath);
        System.out.println("[file Name]  " + fileName);

        // 원본 file upload
        uploadService.setNewDir(localFilePath, fileName, f);
        // get file size
        double fileSize = uploadService.getFileSize(localFilePath, fileName);
        System.out.println("[file size]   " + fileSize);

        uploadService.setNewDir(localFilePath +"\\splitVideo");
        uploadService.setNewDir(localFilePath + "\\splitAudio");

        int videoCnt = videoSplitService.Splitter(localFilePath, localFilePath+"\\splitVideo", fileName, fileSize);

        String totalFileID = "";
        String splitVideoPath = localFilePath+"\\splitVideo";
        System.out.println("[video cnt]   : " + videoCnt);

        for(int i=0; i<videoCnt; i++) {
            returnedFileID = sceneSplitService.sceneSplit(accessKey, type, localFilePath + "\\splitVideo_" + Integer.toString(i) + ".mp4");
            totalFileID += returnedFileID + "\n";
        }

        // file ID 를 fileID.txt 파일에 저장
        BufferedOutputStream bs = null;
        try {
            bs = new BufferedOutputStream(new FileOutputStream(localFilePath + "\\fileID.txt"));
            bs.write(totalFileID.getBytes()); //Byte형으로만 넣을 수 있음
        } catch (Exception e) {
            e.getStackTrace();
        }finally {
            bs.close();
        }

        System.out.println("FILE ID IN CONTROLLER : " + returnedFileID);
        //String statusResult = sceneSplitStatusCheckService.sceneSplitStatusCheck(returnedFileID, accessKey);
        return "index";
    }
    // 분석 결과 보기
    @PostMapping(value = "/sceneSplit", produces = "application/json; charset=utf8")
    public String sceneSplit() {
        String fileID = returnedFileID;
        String totalResult = "";
        String statusResult = sceneSplitStatusCheckService.sceneSplitStatusCheck(fileID, accessKey);
        return "index";
    }
}