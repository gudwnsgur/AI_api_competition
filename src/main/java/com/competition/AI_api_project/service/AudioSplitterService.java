package com.competition.AI_api_project.service;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;

@Service
public class AudioSplitterService {
    private FFmpeg ffmpeg;
    private FFprobe ffprobe;

    public AudioSplitterService() throws IOException {
        ffmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
        ffprobe = new FFprobe("/usr/local/bin/ffprobe");
    }
    public ArrayList<Integer> audioSplitter(String path, ArrayList<Double> list, int fileCnt) {

        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < fileCnt-1; i++) {
            int cnt = 0;
            double audioLength = 0;

            audioLength = list.get(i + 1) - list.get(i);
            for (double j = 0; j <= audioLength; j += 30.00) {
                FFmpegBuilder builder = new FFmpegBuilder()
                        .overrideOutputFiles(true)
                        .addInput(path + "/audio_" + i + ".wav")
                        .addExtraArgs("-ss", i + "")
                        .addExtraArgs("-t", 30.00 + "")
                        .addOutput(path + "/audio_" + i + "_" + cnt++ + ".wav")
                        .done();
                FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
                executor.createJob(builder).run();
            }
            result.add(cnt);
            System.out.println(result);
        }
        return result;
    }
}
