package com.competition.AI_api_project.service;

import com.google.gson.Gson;
import freemarker.template.SimpleDate;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class VideoService {
    public static final String inputPath = System.getProperty("user.dir") + "/files";
    public static final String outputPath = inputPath + "/split";
    private FFmpeg fFmpeg;
    private FFprobe fFprobe;

    public VideoService() throws IOException {
        fFmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
        fFprobe = new FFprobe("/usr/local/bin/ffprobe");
    }

    public double getVideoLength(String path, String fileName) {
        try {
            FFmpegProbeResult probeResult = fFprobe.probe(path + "/" + fileName);
            FFmpegFormat format = probeResult.getFormat();
            return format.duration;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public String videoSplitter() {
        double  videoLength = getVideoLength("test","test");
        double  splitMinute = 300.0;
        int     cnt = 0;
        String folder = "";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        long time = System.currentTimeMillis();
        folder = simpleDateFormat.format(time);

        if (videoLength == 0.0) {
            return folder;
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
        return folder;
    }

    public void videoSplitApi(String folder) {
        String openApiURL = "http://aiopen.etri.re.kr:8000/VideoParse";
        String accessKey = "25601cd9-c413-4522-b3fb-e35de1ae040d";	// 발급받은 API Key
        String type = ".mp4";  	// 비디오 파일 확장자
        String file = inputPath + "/" + folder;  	// 비디오 파일 경로
        Gson gson = new Gson();

        Map<String, Object> request = new HashMap<>();
        Map<String, String> argument = new HashMap<>();
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        FileBody fileBody = new FileBody(new File(file));

        request.put("access_key", accessKey);
        request.put("argument", argument);

        builder.addPart("uploadfile", fileBody);
        builder.addTextBody("json", gson.toJson(request));

        Integer responseCode = null;
        String responBody = null;
        try {
            CloseableHttpClient http = HttpClients.createDefault();
            HttpPost post = new HttpPost(openApiURL);
            post.setEntity(builder.build());
            CloseableHttpResponse response = http.execute(post);
            StatusLine status;
            try{
                StringBuffer result = new StringBuffer();
                status = response.getStatusLine();
                HttpEntity res = response.getEntity();
                BufferedReader br = new BufferedReader(new InputStreamReader(res.getContent(), Charset.forName("UTF-8")));

                String buffer = null;
                while( (buffer = br.readLine())!=null ){
                    result.append(buffer).append("\r\n");
                }

                responseCode = status.getStatusCode();
                responBody = result.toString();
            }finally{
                response.close();
            }

            System.out.println("[responseCode] " + responseCode);
            System.out.println("[responBody]");
            System.out.println(responBody);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void 형준혁(ArrayList<Integer> list, String path, String fileName) {
        int     cnt = 0;
        double  tmp = 0;
        for (int i =0; i < list.size() - 1; i++) {
            double gap = list.get(i + 1) - list.get(i);
            FFmpegBuilder builder = new FFmpegBuilder()
                    .overrideOutputFiles(true)
                    .addInput(path + "/" + fileName)
                    .addExtraArgs("-ss", tmp + "")
                    .addExtraArgs("-t", gap + "")
                    .addOutput(path + "/mergedVideo/" + "mergedVideo_" + cnt++ + ".mp4")
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
            executor.createJob(builder).run();
            tmp += gap;
        }
    }
}
