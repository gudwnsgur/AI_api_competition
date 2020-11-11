package com.competition.AI_api_project.service;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.IOException;
import java.util.ArrayList;

public class AudioExtractionService {

    private FFmpeg fFmpeg;
    private FFprobe fFprobe;

    public AudioExtractionService() throws IOException {
        fFmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
        fFprobe = new FFprobe("/usr/local/bin/ffprobe");
    }

    public void mp4Tomp3(String path, int lsitSiz) {
        int     cnt = 0;

        for (int i= 0; i < fileCnt; i++) {
            String inputPath = path + "/mergedVideo/mergedVideo_" + cnt + ".mp4";
            String outputPath = path + "/splitAudio/audio_" + cnt + ".mp3";

            FFmpegBuilder builder = new FFmpegBuilder()
                    .overrideOutputFiles(true)
                    .addInput(inputPath)
                    .addExtraArgs("-vn")
                    .addOutput(outputPath)
                    .addExtraArgs("-ar", "16000")
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
            executor.createJob(builder).run();
            cnt++;
        }
    }

    public void mp3ToWav(String path, int fileCnt) {
        int     cnt = 0;

        for (int i = 0; i < fileCnt; i++) {
            String inputPath = path + "/splitAudio/audio_" + cnt + ".mp3";
            String outputPath = path + "/splitAudio/audio_" + cnt + ".wav";
            FFmpegBuilder builder = new FFmpegBuilder()
                    .overrideOutputFiles(true)
                    .addInput(inputPath)
                    .addExtraArgs("-ss", "0")
                    .addExtraArgs("-t", "30")
                    .addOutput(outputPath)
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
            executor.createJob(builder).run();
            cnt++;
        }
    }

    public void extractMergedVideo(String path, String fileName) {
        String inputPath = path + "/mergedVideo";
        String outputPath = path + "/splitAudio";
    }

    public ArrayList<Integer> audioSplitter(String path, ArrayList<Double> list, int fileCnt) throws IOException {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            int         cnt = 0;
            int         tmp = 0;
            double      audioLength = 0;

            audioLength = list.get(i + 1) - list.get(i);
            for (double j = 0; j <= audioLength; j +=60.00) {
                FFmpegBuilder builder = new FFmpegBuilder()
                    .overrideOutputFiles(true)
                    .addInput(path + "/splitAudio/extractAudio_" + i + ".wav")
                    .addExtraArgs("-ss", i + "")
                    .addExtraArgs("-t", 30.00 + "")
                    .addOutput(path + "/splitAudio/extractAudio_" + i + "_" + cnt++ +".wav")
                    .done();
                FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
                executor.createJob(builder).run();
            }
            result.add(cnt);
        }

//        for (double i = 0; i <= videoLength; i += splitMinute) {
//            FFmpegBuilder builder = new FFmpegBuilder()
//                    .overrideOutputFiles(true)
//                    .addInput(path + "/splitAudio/extractAudio_" + fi)
//                    .addExtraArgs("-ss", i + "")
//                    .addExtraArgs("-t", splitMinute + "")
//                    .addOutput(path + cnt++ +".wav")
//                    .done();
//            FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
//            executor.createJob(builder).run();
//        }
    }
}
