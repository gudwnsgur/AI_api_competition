package com.competition.AI_api_project.service;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Service
public class UploadService {
    private FFmpeg ffmpeg;
    private FFprobe ffprobe;

    public UploadService() throws IOException {
        ffmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
        ffprobe = new FFprobe("/usr/local/bin/ffprobe");
    }
    public String getRandomDirName() {
        // file name 지정
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_");
        Calendar time = Calendar.getInstance();
        String fileName = dateFormat.format(time.getTime());
        fileName += (Integer.toString((int) (Math.random() * 1000)));
        return fileName;
    }
    public void setNewDir(String path, String file, MultipartFile f) {
        try {
            if (!new File(path).exists()) {
                try {
                    new File(path).mkdir();
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }
            f.transferTo(new File(path + "/" + file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setNewDir(String path) {
        try {
            if (!new File(path).exists()) {
                try {
                    new File(path).mkdir();
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public int getFileSize(String path, String file) {
        try {
            FFmpegProbeResult probeResult = ffprobe.probe(path + "/" + file);
            FFmpegFormat format = probeResult.getFormat();
            return (int)format.duration;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
