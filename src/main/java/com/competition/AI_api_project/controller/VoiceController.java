package com.competition.AI_api_project.controller;

import com.competition.AI_api_project.service.VoiceExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

public class VoiceController {
    @Autowired
    VoiceExtractionService voiceExtractionService;

    @PostMapping(value = "/voiceRecognize")
    public String voiceRec() throws IOException {
        return "index";
    }
}
