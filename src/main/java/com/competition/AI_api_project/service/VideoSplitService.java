package com.competition.AI_api_project.service;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;


@Service
public class VideoSplitService {
    private FFmpeg ffmpeg;
    private FFprobe ffprobe;

    public VideoSplitService() throws IOException {
        ffmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
        ffprobe = new FFprobe("/usr/local/bin/ffprobe");
    }
    public int Splitter(String inputPath, String outputPath, String file, double videoLen) {
        double  splitMinute = 300.0;
        int cnt = 0;

        if (videoLen == 0.0) {
            return 0;
        }
        for (double i = 0; i <= videoLen; i += splitMinute) {
            FFmpegBuilder builder = new FFmpegBuilder()
                    .overrideOutputFiles(true)
                    .addInput(inputPath + "/" + file)
                    .addExtraArgs("-ss", i + "")
                    .addExtraArgs("-t", splitMinute + "")
                    .addOutput(outputPath + "/" + "splitVideo_" + cnt++ +".mp4")
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();
        }
        return cnt;
    }
    public void splitByArray(ArrayList<Double> list, String path, String fileName) {
        int cnt = 0;
        double tmp = 0;
        for (int i =0; i < list.size() - 1; i++) {
            double gap = list.get(i + 1) - list.get(i);
            FFmpegBuilder builder = new FFmpegBuilder()
                    .overrideOutputFiles(true)
                    .addInput(path + "/" + fileName)
                    .addExtraArgs("-ss", tmp + "")
                    .addExtraArgs("-t", gap + "")
                    .addOutput(path + "/mergedVideo/" + "mergedVideo_" + cnt++ + ".mp4")
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();
            tmp += gap;
        }
        return;
    }
}
