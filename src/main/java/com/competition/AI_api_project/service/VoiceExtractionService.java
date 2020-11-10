package com.competition.AI_api_project.service;

import org.springframework.stereotype.Service;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.IOException;

@Service
public class VoiceExtractionService {
    private static String inputPath = "C:\\AI_api_project\\AI_api_project\\src\\main\\resources\\static\\";
    private static String outputPath = "C:\\AI_api_project\\AI_api_project\\src\\main\\resources\\static\\";

    public void voiceSplitter() throws IOException {
        FFmpeg ffmpeg = new FFmpeg("C:\\ffmpeg-3.4.1-win64-static\\ffmpeg-3.4.1-win64-static\\bin\\ffmpeg");
        FFprobe ffprobe = new FFprobe("C:\\ffmpeg-3.4.1-win64-static\\ffmpeg-3.4.1-win64-static\\bin\\ffprobe");

        FFmpegBuilder builder = new FFmpegBuilder()
                .overrideOutputFiles(true)
                .addInput(inputPath + "2020-11-09_18-08-25_421.mp4")
                .addExtraArgs("-vn")
                .addOutput(outputPath + "2020-11-09_18-08-25_421_voice"+ ".wav")
                .addExtraArgs("-ar", "16000")
                .done();
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
    }
}
