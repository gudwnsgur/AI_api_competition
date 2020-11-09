package com.competition.AI_api_project.controller;

import com.competition.AI_api_project.service.AudioExtractionService;
import com.competition.AI_api_project.service.VideoSplitService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@Controller
public class TestController {
    @PostMapping("/test")
    public String testMethod() throws IOException {
        VideoSplitService service = new VideoSplitService();
        service.videoSplitter();
        return "redirect:/";
    }

    @PostMapping("/test/audio")
    public String audioTest() throws IOException {
        AudioExtractionService service = new AudioExtractionService();
        //service.extractOriginVideo();
        service.audioSplitter();
        return "redirect:/";
    }
}
