package com.competition.AI_api_project.service;

import com.google.gson.Gson;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
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
import java.util.HashMap;
import java.util.Map;

@Service
public class SceneSplitAPI {
    static private String openApiURL = "http://aiopen.etri.re.kr:8000/VideoParse";
   static public String sceneSplit(String accessKey, String type, String file) {
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
        System.out.println("1 : " + request);
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
                System.out.println(result);
                responseCode = status.getStatusCode();
                responBody = result.toString();

                Object obj = JSONValue.parse(result.toString());
                JSONObject JSONobj = (JSONObject) obj;
                Object ac =  JSONobj.get("return_object");
                JSONObject JSONobj2 = (JSONObject) ac;
                Object ad = ((JSONObject) ac).get("file_id");
                returnFileId = ad.toString();
                System.out.println(returnFileId);

                System.out.println("장면분할 API");
                System.out.println("[responseCode] " + responseCode);
                System.out.println("[responBody]" + responBody);


                System.out.println("[return File ID] : " + returnFileId);
                System.out.println();
            } finally {
                response.close();
            }
        }catch ( MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return returnFileId;
    }
}
