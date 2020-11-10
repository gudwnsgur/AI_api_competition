package com.competition.AI_api_project.service;

import java.io.IOException;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class VideoSplitService {
    public int Splitter(String inputPath, String outputPath, String file, double videoLen) throws IOException {
        FFmpeg ffmpeg = new FFmpeg("C:\\ffmpeg-3.4.1-win64-static\\ffmpeg-3.4.1-win64-static\\bin\\ffmpeg");
        FFprobe ffprobe = new FFprobe("C:\\ffmpeg-3.4.1-win64-static\\ffmpeg-3.4.1-win64-static\\bin\\ffprobe");

        double  splitMinute = 10.0;
        int cnt = 0;

        if (videoLen == 0.0) {
            return 0;
        }
        for (double i = 0; i <= videoLen; i += splitMinute) {
            FFmpegBuilder builder = new FFmpegBuilder()
                    .overrideOutputFiles(true)
                    .addInput(inputPath + "\\" + file)
                    .addExtraArgs("-ss", i + "")
                    .addExtraArgs("-t", splitMinute + "")
                    .addOutput(outputPath + "\\" + "splitVideo_" + cnt++ +".mp4")
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();
        }
        return cnt;
    }
}
