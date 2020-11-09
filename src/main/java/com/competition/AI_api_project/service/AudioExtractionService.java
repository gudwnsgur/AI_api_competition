package com.competition.AI_api_project.service;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.IOException;

public class AudioExtractionService {
    public static final String inputPath = System.getProperty("user.dir") + "/files/voice";
    public static final String outputPath = inputPath + "/voice";
    private FFmpeg fFmpeg;
    private FFprobe fFprobe;

    public void extractOriginVideo() throws IOException {
        fFmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
        fFprobe = new FFprobe("/usr/local/bin/ffprobe");

        FFmpegBuilder builder = new FFmpegBuilder()
                .overrideOutputFiles(true)
                .addInput(inputPath + "/test.mp4")
                .addExtraArgs("-vn")
                .addOutput(outputPath + "/" + "extractAudio.wav")
                .addExtraArgs("-ar", "16000")
                .done();
        FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
        executor.createJob(builder).run();
    }

    public int audioSplitter() throws IOException {
        VideoSplitService service = new VideoSplitService();
        double  videoLength = service.getVideoLength();
        double  splitMinute = 60.00;
        int     cnt = 0;

        if (videoLength == 0.0) {
            return 0;
        }
        for (double i = 0; i <= videoLength; i += splitMinute) {
            FFmpegBuilder builder = new FFmpegBuilder()
                    .overrideOutputFiles(true)
                    .addInput(inputPath + "/extractAudio.wav")
                    .addExtraArgs("-ss", i + "")
                    .addExtraArgs("-t", splitMinute + "")
                    .addOutput(outputPath + "/voiceSplit" + "/audioSplit" + cnt++ +".wav")
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
            executor.createJob(builder).run();
        }
        return cnt;
    }
}
