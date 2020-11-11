package com.competition.AI_api_project.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.competition.AI_api_project.service.*;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
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
    @Autowired
    VoiceExtractionService voiceExtractionService;
    @Autowired
    AudioRecognizeService audioRecognizeService;
    @Autowired
    AudioSplitterService audioSplitterService;

    private String returnedFileID = "";
    private String accessKey = "My KEY";
    private String type = ".mp4";
    private String filePath = System.getProperty("user.dir") + "\\files\\";
    private String localFilePath = "";
    private String originalFileName = "";
    private double fileSize = 0;


    // 분석하기
    @PostMapping(value = "/upload", produces = "application/json; charset=utf8")
    public String upload(@RequestParam("uploadfile") MultipartFile f) throws InterruptedException, IOException {
        // 시간 + random 값으로 이뤄진 file name 생성
        String dirName = uploadService.getRandomDirName(); // only name
        localFilePath = filePath + "\\" + dirName;
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
        uploadService.setNewDir(localFilePath +"\\splitVideo");
        uploadService.setNewDir(localFilePath + "\\splitAudio");
        uploadService.setNewDir(localFilePath +"\\mergedVideo");

        int videoCnt = videoSplitService.Splitter(localFilePath, localFilePath+"\\splitVideo", fileName, fileSize);
        // 장면 분할 API 사용
        String totalFileID = "";
        System.out.println("[video cnt]   : " + videoCnt);
        for(int i=0; i<videoCnt; i++) {
            returnedFileID = sceneSplitService.sceneSplit(accessKey, type, localFilePath + "\\splitVideo\\splitVideo_" + Integer.toString(i) + ".mp4");
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
        return "index";
    }

    // 분석 결과 보기
    @PostMapping(value = "/sceneSplit", produces = "application/json; charset=utf8")
    public String sceneSplit() throws IOException, ParseException {
        ArrayList<Double> timeTable = new ArrayList<Double>();

        timeTable.add(0.0);
        try{
            File file = new File(localFilePath + "\\fileID.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(fileReader);
            String line = "";
            double time = 0;
            while((line = bufReader.readLine()) != null) {
                ArrayList<Double> cur = sceneSplitStatusCheckService.sceneSplitStatusCheck(line, accessKey);
                for(int i=1; i<cur.size(); i++) {
                    timeTable.add(cur.get(i) + time);
                }
                time += 300;
            }
            bufReader.close();
//            timeTable.add(fileSize);
            timeTable.add(990.0);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        System.out.println(timeTable);

        // 동영상 장면 분할 결과와 같이 split
        videoSplitService.splitByArray(timeTable, localFilePath, originalFileName);
        // mp4 to mp3
        voiceExtractionService.mp4Tomp3(localFilePath, timeTable.size()-1);
        // mp3 to wav
        voiceExtractionService.mp3ToWav(localFilePath, timeTable.size()-1);


        int x = timeTable.size()-1;
        ArrayList<Integer> xList = new ArrayList<Integer>();
        xList = audioSplitterService.audioSplitter(localFilePath + "\\splitAudio", timeTable, timeTable.size()-1);

        String languageCode = "korean";
        String code = "\n---\n";
        BufferedOutputStream bs = null;
        try {
            bs = new BufferedOutputStream(new FileOutputStream(localFilePath + "\\result.txt"));
            for(int i=0; i<x; i++) {
                for(int j=0; j<xList.get(i); j++) {
                    bs.write((audioRecognizeService.audioRec(languageCode,
                            localFilePath + "\\splitAudio",
                            "audio_" + i + "_" + j +".wav" )).getBytes());
                }
                bs.write(code.getBytes());
            }
        }catch (Exception e) {
            e.getStackTrace();
        }finally {
            bs.close();
        }

        return "index";
    }
}