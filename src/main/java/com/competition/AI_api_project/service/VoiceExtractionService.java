package com.competition.AI_api_project.service;

import org.springframework.stereotype.Service;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.IOException;

@Service
public class VoiceExtractionService {

    private FFmpeg ffmpeg;
    private FFprobe ffprobe;

    public VoiceExtractionService() throws IOException {
        ffmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
        ffprobe = new FFprobe("/usr/local/bin/ffprobe");
    }

    public void mp4Tomp3(String path, int fileCnt) {
        int cnt = 0;
        for (int i= 0; i < fileCnt; i++) {
            String inputPath = path + "/mergedVideo/mergedVideo_" + cnt + ".mp4";
            String outputPath = path + "/splitAudio/audio_" + cnt + ".mp3";

            FFmpegBuilder builder = new FFmpegBuilder()
                    .overrideOutputFiles(true)
                    .addInput(inputPath)
                    .addOutput(outputPath)
                    .addExtraArgs("-vn")
                    .addExtraArgs("-acodec", "libmp3lame")
                    .addExtraArgs("-ar", "16000")
                    .addExtraArgs("-ac", "1")
                    .addExtraArgs("-ab", "256k")
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();
            cnt++;
        }
    }
    public void mp3ToWav(String path, int fileCnt) {
        int cnt = 0;
        for (int i = 0; i < fileCnt; i++) {
            String inputPath = path + "/splitAudio/audio_" + cnt + ".mp3";
            String outputPath = path + "/splitAudio/audio_" + cnt + ".wav";
            FFmpegBuilder builder = new FFmpegBuilder()
                    .overrideOutputFiles(true)
                    .addInput(inputPath)
                    .addOutput(outputPath)
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();
            cnt++;
        }
    }
}
