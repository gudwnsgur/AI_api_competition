package com.competition.AI_api_project.service;

import com.google.gson.Gson;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class AudioService {
    private FFmpeg ffmpeg;
    private FFprobe ffprobe;

    public AudioService() throws IOException {
        ffmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
        ffprobe = new FFprobe("/usr/local/bin/ffprobe");
    }
    //audioRec
    public String audioRecoqnizeApi(String languageCode, String audioFilePath, String fileName) throws ParseException {
        String openApiURL = "http://aiopen.etri.re.kr:8000/WiseASR/Recognition";
        String accessKey = "25601cd9-c413-4522-b3fb-e35de1ae040d";
        String audioContents = null;

        audioFilePath += "/" + fileName;
        Gson gson = new Gson();

        Map<String, Object> request = new HashMap<>();
        Map<String, String> argument = new HashMap<>();

        try {
            Path path = Paths.get(audioFilePath);
            byte[] audioBytes = Files.readAllBytes(path);
            audioContents = Base64.getEncoder().encodeToString(audioBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        argument.put("language_code", languageCode);
        argument.put("audio", audioContents);

        request.put("access_key", accessKey);
        request.put("argument", argument);

        URL url;
        Integer responseCode = null;
        String responBody = null;
        try {
            url = new URL(openApiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(gson.toJson(request).getBytes("UTF-8"));
            wr.flush();
            wr.close();

            responseCode = con.getResponseCode();
            InputStream is = con.getInputStream();
            byte[] buffer = new byte[is.available()];
            int byteRead = is.read(buffer);
            responBody = new String(buffer);

            System.out.println("[responseCode] " + responseCode);
            System.out.println("[responBody]");
            System.out.println(responBody);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        JSONParser jP = new JSONParser();
        JSONObject obj = ((JSONObject)jP.parse(responBody));
        JSONObject obj2 = ((JSONObject)obj.get("return_object"));
        Object text = (obj2.get("recognized"));
        return text.toString();
    }
    //audioSplitter
    public ArrayList<Integer> splitAudioTo30(String path, ArrayList<Double> list, int fileCnt) {

        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            int cnt = 0;
            double audioLength = 0;

            audioLength = list.get(i + 1) - list.get(i);
            for (double j = 0; j <= audioLength; j += 30.00) {
                FFmpegBuilder builder = new FFmpegBuilder()
                        .overrideOutputFiles(true)
                        .addInput(path + "/audio_" + i + ".wav")
                        .addExtraArgs("-ss", j + "")
                        .addExtraArgs("-t", 30.00 + "")
                        .addOutput(path + "/audio_" + i + "_" + cnt++ + ".wav")
                        .done();
                FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
                executor.createJob(builder).run();
            }
            result.add(cnt);
        }
        return result;
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
