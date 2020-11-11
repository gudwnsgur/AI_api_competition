package com.competition.AI_api_project.controller;

import com.competition.AI_api_project.service.AudioExtractionService;
import com.competition.AI_api_project.service.VideoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.ArrayList;

@Controller
public class TestController {
    public static final String inputPath = System.getProperty("user.dir") + "/files";
    @PostMapping("/test")
    public String testMethod() throws IOException {

        VideoService service = new VideoService();
        ArrayList<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(180);
        service.형준혁(list, inputPath, "test.mp4");
        return "redirect:/";
    }

    @PostMapping("/test/audio")
    public String audioTest() throws IOException {
        AudioExtractionService service = new AudioExtractionService();
//        service.extractOriginVideo(inputPath + "/testFolder");
        service.audioSplitter(inputPath + "/testFolder");
        return "redirect:/";
    }
}
