package com.competition.AI_api_project.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.competition.AI_api_project.dto.ResultDto;
import com.competition.AI_api_project.service.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {
    @Autowired
    UploadService uploadService;

    @Autowired
    AudioService audioService;

    @Autowired
    VideoService videoService;

    private String returnedFileID = "";
    private String accessKey = "4c4ef167-bcc0-4824-9dd5-0f0c6126ac97";
    private String type = ".mp4";
    private String filePath = System.getProperty("user.dir") + "/files/";
    private String localFilePath;
    private String originalFileName;
    private double fileSize = 0;
    private String accessKey1 = "4c4ef167-bcc0-4824-9dd5-0f0c6126ac97";
    private String accessKey2="25601cd9-c413-4522-b3fb-e35de1ae040d";
    private ArrayList<String> returnedIdList = new ArrayList<>();

    // 분석하기
    @PostMapping(value = "/upload", produces = "application/json; charset=utf8")
    public String upload(@RequestParam("uploadfile") MultipartFile f, Model model) throws InterruptedException, IOException, ParseException {
        // 시간 + random 값으로 이뤄진 file name 생성
        String dirName = uploadService.getRandomDirName(); // only name
        localFilePath = filePath  + dirName;
        String fileName = FilenameUtils.getBaseName(f.getOriginalFilename()) + ".mp4";
        originalFileName = fileName;
        System.out.println("[file Path]  " + localFilePath);
        System.out.println("[file Name]  " + fileName);

        // 원본 file upload
        uploadService.setNewDir(localFilePath, fileName, f);
        // get file size
        fileSize = uploadService.getFileSize(localFilePath, fileName);
        System.out.println("[file size]   " + fileSize);

        // Dir 생성
        uploadService.setNewDir(localFilePath +"/splitVideo");
        uploadService.setNewDir(localFilePath + "/splitAudio");
        uploadService.setNewDir(localFilePath +"/mergedVideo");

        int videoCnt = videoService.splitVideo(localFilePath, localFilePath+"/splitVideo", fileName, fileSize);
        // 장면 분할 API 사용
        //String totalFileID = "";
        System.out.println("[video cnt]   : " + videoCnt);

        int vCnt = 0;
        while (vCnt < videoCnt) {
            CompletableFuture<String> str1 = videoService.splitSceneApi(accessKey1, type, localFilePath + "/splitVideo/splitVideo_" + (vCnt) + ".mp4");
            CompletableFuture<String> str2 = videoService.splitSceneApi(accessKey1, type, localFilePath + "/splitVideo/splitVideo_" + (vCnt + 1) + ".mp4");
            returnedIdList.add(str1.toString());
            returnedIdList.add(str2.toString());
            vCnt+=2;
        }
        ArrayList<Double> timeTable = new ArrayList<Double>();
        double time = 0;

        for(int i = 0; i < returnedIdList.size(); i++) {
            ArrayList<Double> cur = videoService.splitSceneStatusApi(returnedIdList.get(i), accessKey);
            for (int j = 1; i < cur.size(); i++) {
                timeTable.add(cur.get(i) + time);
            }
            time += 300;
        }
        timeTable.add(fileSize);
        //System.out.println(timeTable);
        // 동영상 장면 분할 결과와 같이 split
        videoService.splitVideoByNewPoint(timeTable, localFilePath, originalFileName);
        //
        audioService.mp4Tomp3(localFilePath, timeTable.size()-1);
        // mp3 to wav
        audioService.mp3ToWav(localFilePath, timeTable.size()-1);


        int x = timeTable.size() - 1;
        ArrayList<Integer> xList = new ArrayList<Integer>();
        xList = audioService.splitAudioTo30(localFilePath + "/splitAudio", timeTable, timeTable.size() - 1);

        String languageCode = "korean";
        ArrayList<ResultDto> dtoList = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            String str = "";
            for (int j = 0; j < xList.get(i); j++) {
                String curResult = audioService.audioRecoqnizeApi(languageCode,
                        localFilePath + "/splitAudio",
                        "audio_" + i + "_" + j + ".wav");
                if(!curResult.equals("ASR_NOTOKEN") || !curResult.equals("다 ")) str += curResult + "\n";

            }
            ResultDto dto = new ResultDto();
            dto.setNum(i + "");
            dto.setStartTime(timeTable.get(i) + "");
            dto.setDurTime((timeTable.get(i + 1) - timeTable.get(i)) + "");
            dto.setText(str);
            dtoList.add(dto);
        }
        model.addAttribute("dList", dtoList);
        return "index";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}