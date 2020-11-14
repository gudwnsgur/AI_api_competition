package com.competition.AI_api_project.service;

import com.google.gson.Gson;
import lombok.Synchronized;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class VideoService {
    private FFmpeg ffmpeg;
    private FFprobe ffprobe;
    private static final Logger logger = LoggerFactory.getLogger(VideoService.class);

    public VideoService() throws IOException {
        ffmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
        ffprobe = new FFprobe("/usr/local/bin/ffprobe");
    }
    //Splitter
    public int splitVideo(String inputPath, String outputPath, String file, double videoLen) {
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
    //splitByArray
    public void splitVideoByNewPoint(ArrayList<Double> list, String path, String fileName) {
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
    //sceneSplitStatusCheck

    @Synchronized
    public ArrayList<Double> splitSceneStatusApi(String fileID, String accessKey) throws ParseException {
        String openApiURL = "http://aiopen.etri.re.kr:8000/VideoParse/status";
        Gson gson = new Gson();

        Map<String, Object> request = new HashMap<>();
        Map<String, Object> file_id = new HashMap<>();
        file_id.put("file_id", fileID);
        request.put("request_id", "reserved field");
        request.put("access_key", accessKey);
        request.put("argument", file_id);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("json", gson.toJson(request));

        Integer responseCode = null;
        String responBody = null;
        try {
            CloseableHttpClient http = HttpClients.createDefault();
            HttpPost post = new HttpPost(openApiURL);
            post.setEntity(builder.build());
            CloseableHttpResponse response = http.execute(post);
            StatusLine status;
            StringBuffer result = new StringBuffer();

            try {
                status = response.getStatusLine();

                HttpEntity res = response.getEntity();
                BufferedReader br = new BufferedReader(new InputStreamReader(res.getContent(), Charset.forName("UTF-8")));
                String buffer = null;
                while ((buffer = br.readLine()) != null) {
                    result.append(buffer).append("\r\n");
                }
                responseCode = status.getStatusCode();
                responBody = result.toString();

                System.out.println("[장면분할 상태체크 API]");
                System.out.println("[responseCode]   " + responseCode);
                System.out.println("[responBody]    " + responBody);
            } finally {
                response.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Double> timeTable = new ArrayList<Double>();

        JSONParser jP = new JSONParser();
        JSONObject obj = ((JSONObject)jP.parse(responBody));
        JSONObject return_obj = (JSONObject)obj.get("return_object");
        JSONArray resArr = (JSONArray)return_obj.get("result");

        JSONObject res = (JSONObject)resArr.get(0);
        JSONArray time = (JSONArray)res.get("time");

        for(int i=0; i<time.size(); i++) {
            Object tmp = time.get(i);
            double d = Double.parseDouble(tmp.toString());
            timeTable.add(d);
        }
        return timeTable;
    }
    //sceneSplit

    @Async
    public CompletableFuture<String> splitSceneApi(String accessKey, String type, String file) {
        String openApiURL = "http://aiopen.etri.re.kr:8000/VideoParse";
        Gson gson = new Gson();

        Map<String, Object> request = new HashMap<>();
        Map<String, String> argument = new HashMap<>();
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        FileBody fileBody = new FileBody(new File(file));

        request.put("request_id", "reserved field");
        request.put("access_key", accessKey);
        request.put("argument", argument);

        builder.addPart("uploadfile", fileBody);
        builder.addTextBody("json", gson.toJson(request));
        Integer responseCode = null;
        String responBody = null;

        String returnFileId = "";
        try {
            CloseableHttpClient http = HttpClients.createDefault();
            HttpPost post = new HttpPost(openApiURL);
            post.setEntity(builder.build());
            CloseableHttpResponse response = http.execute(post);
            StatusLine status;
            StringBuffer result = new StringBuffer();

            try {
                status = response.getStatusLine();
                HttpEntity res = response.getEntity();
                BufferedReader br = new BufferedReader(new InputStreamReader(res.getContent(), Charset.forName("UTF-8")));

                String buffer = null;
                while ((buffer = br.readLine()) != null) {
                    result.append(buffer).append("\r\n");
                }
                responseCode = status.getStatusCode();
                responBody = result.toString();

                Object obj = JSONValue.parse(result.toString());
                JSONObject JSONobj = (JSONObject) obj;
                Object ac =  JSONobj.get("return_object");
                Object ad = ((JSONObject) ac).get("file_id");
                returnFileId = ad.toString();

                System.out.println("장면분할 API");
                System.out.println("[responseCode] " + responseCode);
                System.out.println("[responBody]" + responBody);


                System.out.println("[return File ID] : " + returnFileId);
            } finally {
                response.close();
            }
        }catch ( MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>(returnFileId).completable();
    }
    //threadApi
//    public String splitSceneApiThread(Map<String, Boolean> map, String type, String file) {
//        String openApiURL = "http://aiopen.etri.re.kr:8000/VideoParse";
//        Gson gson = new Gson();
//
//        String accessKey;
//        if (map.get("25601cd9-c413-4522-b3fb-e35de1ae040d") == false) {
//            accessKey = "25601cd9-c413-4522-b3fb-e35de1ae040d";
//            map.put("25601cd9-c413-4522-b3fb-e35de1ae040d", true);
//        }
//
//        ArrayList<Boolean> booleanArrayList = new ArrayList<>();
//        Map<String, Object> request = new HashMap<>();
//        Map<String, String> argument = new HashMap<>();
//        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//
//        FileBody fileBody = new FileBody(new File(file));
//
//        request.put("request_id", "reserved field");
//        request.put("access_key", accessKey);
//        request.put("argument", argument);
//
//        builder.addPart("uploadfile", fileBody);
//        builder.addTextBody("json", gson.toJson(request));
//        Integer responseCode = null;
//        String responBody = null;
//
//        String returnFileId = "";
//        try {
//            CloseableHttpClient http = HttpClients.createDefault();
//            HttpPost post = new HttpPost(openApiURL);
//            post.setEntity(builder.build());
//            CloseableHttpResponse response = http.execute(post);
//            StatusLine status;
//            StringBuffer result = new StringBuffer();
//
//            try {
//                status = response.getStatusLine();
//                HttpEntity res = response.getEntity();
//                BufferedReader br = new BufferedReader(new InputStreamReader(res.getContent(), Charset.forName("UTF-8")));
//
//                String buffer = null;
//                while ((buffer = br.readLine()) != null) {
//                    result.append(buffer).append("\r\n");
//                }
//                responseCode = status.getStatusCode();
//                responBody = result.toString();
//
//                Object obj = JSONValue.parse(result.toString());
//                JSONObject JSONobj = (JSONObject) obj;
//                Object ac =  JSONobj.get("return_object");
//                Object ad = ((JSONObject) ac).get("file_id");
//                returnFileId = ad.toString();
//
//                System.out.println("장면분할 API");
//                System.out.println("[responseCode] " + responseCode);
//                System.out.println("[responBody]" + responBody);
//
//
//                System.out.println("[return File ID] : " + returnFileId);
//            } finally {
//                response.close();
//            }
//        }catch ( MalformedURLException e) {
//            e.printStackTrace();
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
//        return returnFileId;
//    }
}
