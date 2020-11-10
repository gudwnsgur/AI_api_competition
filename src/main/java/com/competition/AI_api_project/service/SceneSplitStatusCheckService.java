package com.competition.AI_api_project.service;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.google.gson.Gson;

@Service
public class SceneSplitStatusCheckService {
    static private String openApiURL = "http://aiopen.etri.re.kr:8000/VideoParse/status";

    static public String sceneSplitStatusCheck(String fileID, String accessKey) {
        Gson gson = new Gson();

        Map<String, Object> request = new HashMap<>();
        Map<String, Object> file_id = new HashMap<>();
        file_id.put("file_id", fileID);
        request.put("request_id", "reserved field");
        request.put("access_key", accessKey);
//        System.out.println("[return File ID] : " + fileID);
        request.put("argument", file_id);
        System.out.println("2 : " + request);
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
                System.out.println("STATUS : " + status);
                System.out.println("SECOND_REQUEST : " + request);

                HttpEntity res = response.getEntity();
                BufferedReader br = new BufferedReader(new InputStreamReader(res.getContent(), Charset.forName("UTF-8")));
                String buffer = null;
                while ((buffer = br.readLine()) != null) {
                    result.append(buffer).append("\r\n");
                }
                System.out.println(result);
                responseCode = status.getStatusCode();
                responBody = result.toString();

                System.out.println("장면분할 상태체크 API");
                System.out.println("[responseCode] " + responseCode);
                System.out.println("[responBody]" + responBody);
            } finally {
                response.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(responBody);
        Object obj = JSONValue.parse(responBody);
        Object res = ((JSONObject) obj).get("result");
        return res.toString();
    }
}
