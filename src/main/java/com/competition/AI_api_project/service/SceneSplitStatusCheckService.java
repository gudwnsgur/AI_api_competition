package com.competition.AI_api_project.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
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

    static public ArrayList<Double> sceneSplitStatusCheck(String fileID, String accessKey) throws ParseException {
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
}
