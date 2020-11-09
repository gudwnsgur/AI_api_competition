package com.competition.AI_api_project.service;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VideoSplitService {
    public static final String inputPath = System.getProperty("user.dir") + "/files";
    public static final String outputPath = inputPath + "/split";
    private FFmpeg fFmpeg;
    private FFprobe fFprobe;

    public VideoSplitService() throws IOException {
        fFmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
        fFprobe = new FFprobe("/usr/local/bin/ffprobe");
    }

    public double getVideoLength() {
        try {
            FFmpegProbeResult probeResult = fFprobe.probe(inputPath + "/test.mp4");
            FFmpegFormat format = probeResult.getFormat();
            return format.duration;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int videoSplitter() {
        double  videoLength = getVideoLength();
        double  splitMinute = 300.0;
        int     cnt = 0;

        if (videoLength == 0.0) {
            return 0;
        }
        for (double i = 0; i <= videoLength; i += splitMinute) {
            FFmpegBuilder builder = new FFmpegBuilder()
                    .overrideOutputFiles(true)
                    .addInput(inputPath + "/test.mp4")
                    .addExtraArgs("-ss", i + "")
                    .addExtraArgs("-t", splitMinute + "")
                    .addOutput(outputPath + "/" + "splitFile_" + cnt++ +".mp4")
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
            executor.createJob(builder).run();
        }
        return cnt;
    }
}
