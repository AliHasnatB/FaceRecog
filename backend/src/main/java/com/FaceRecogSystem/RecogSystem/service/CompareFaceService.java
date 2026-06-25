package com.FaceRecogSystem.RecogSystem.service;

import com.FaceRecogSystem.RecogSystem.dto.RecognitionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CompareFaceService {

    private static final Logger log = LoggerFactory.getLogger(CompareFaceService.class);
    private static final double THRESHOLD = 0.85;

    @Value("${compareface.url}")
    private String baseUrl;

    @Value("${compareface.api-key}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public boolean addFace(String subjectName, MultipartFile image) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(baseUrl + "/api/v1/recognition/faces?subject=" + subjectName);
            post.setHeader("x-api-key", apiKey);
            post.setEntity(MultipartEntityBuilder.create()
                    .addBinaryBody("file", image.getBytes(),
                            ContentType.IMAGE_JPEG, image.getOriginalFilename())
                    .build());
            String body = client.execute(post, r -> EntityUtils.toString(r.getEntity()));
            log.info("AddFace response: {}", body);
            return true;
        } catch (Exception e) {
            log.error("AddFace error", e);
            return false;
        }
    }

    public RecognitionResponse recognizeFromBytes(byte[] imageBytes) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(baseUrl + "/api/v1/recognition/recognize?limit=1&det_prob_threshold=0.8");
            post.setHeader("x-api-key", apiKey);
            post.setEntity(MultipartEntityBuilder.create()
                    .addBinaryBody("file", imageBytes, ContentType.IMAGE_JPEG, "frame.jpg")
                    .build());
            String body = client.execute(post, r -> EntityUtils.toString(r.getEntity()));
            JsonNode root = mapper.readTree(body);

            JsonNode results = root.path("result");
            if (results.isEmpty()) return RecognitionResponse.noFace();

            JsonNode best = results.get(0).path("subjects");
            if (best.isEmpty()) return RecognitionResponse.failed("Unknown face");

            String subject = best.get(0).path("subject").asText();
            double similarity = best.get(0).path("similarity").asDouble();

            if (similarity < THRESHOLD) return RecognitionResponse.failed("Low confidence: " + similarity);

            RecognitionResponse resp = new RecognitionResponse();
            resp.setAuthenticated(true);
            resp.setSubjectName(subject);
            resp.setSimilarity(similarity);
            return resp;

        } catch (Exception e) {
            log.error("Recognize error", e);
            return RecognitionResponse.failed("Recognition service error");
        }
    }

    public void deleteFace(String subjectName) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpDelete del = new HttpDelete(baseUrl + "/api/v1/recognition/faces?subject=" + subjectName);
            del.setHeader("x-api-key", apiKey);
            client.execute(del, r -> null);
        } catch (Exception e) {
            log.error("DeleteFace error", e);
        }
    }
}
