package com.shanghaiwind.inspection.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanghaiwind.inspection.entity.AiFinding;
import com.shanghaiwind.inspection.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * AI推理服务调用实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.service.url:http://localhost:8090}")
    private String aiServiceUrl;

    @Override
    public List<AiFinding> detectImage(String imageUrl, Long taskId, Long mediaId, String detectType) {
        try {
            String url = aiServiceUrl + "/detect/frame";

            // 构建请求体
            JsonNode requestBody = objectMapper.createObjectNode()
                    .put("image_url", imageUrl)
                    .put("task_id", taskId != null ? taskId.longValue() : null)
                    .put("media_id", mediaId != null ? mediaId.longValue() : null);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseDetections(response.getBody(), taskId, mediaId);
            }
        } catch (Exception e) {
            log.error("调用AI图片检测失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<AiFinding> detectVideo(String videoUrl, Long taskId, Long mediaId, String detectType) {
        // 视频检测需要先获取预签名URL，然后传给AI服务
        try {
            String url = aiServiceUrl + "/detect/frame";

            // 对视频逐帧检测（简化版本，实际可调用视频检测接口）
            JsonNode requestBody = objectMapper.createObjectNode()
                    .put("image_url", videoUrl)
                    .put("task_id", taskId != null ? taskId.longValue() : null)
                    .put("media_id", mediaId != null ? mediaId.longValue() : null);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseDetections(response.getBody(), taskId, mediaId);
            }
        } catch (Exception e) {
            log.error("调用AI视频检测失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean isHealthy() {
        try {
            String url = aiServiceUrl + "/health";
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("AI服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    private List<AiFinding> parseDetections(JsonNode response, Long taskId, Long mediaId) {
        List<AiFinding> findings = new ArrayList<>();

        JsonNode detections = response.get("detections");
        if (detections == null || !detections.isArray()) {
            return findings;
        }

        for (JsonNode det : detections) {
            AiFinding finding = new AiFinding();
            finding.setTaskId(taskId);
            finding.setMediaId(mediaId);
            finding.setType(det.has("type") ? det.get("type").asText() : null);
            finding.setSeverity(det.has("severity") ? det.get("severity").asText() : null);
            finding.setConfidence(det.has("confidence") ? det.get("confidence").asDouble() : null);
            finding.setDescription(det.has("description") ? det.get("description").asText() : null);
            finding.setStatus("PENDING");

            // 解析检测框
            if (det.has("bbox")) {
                try {
                    finding.setBboxJson(objectMapper.writeValueAsString(det.get("bbox")));
                } catch (Exception ignored) {}
            }

            // 帧时间
            if (response.has("frame_time_sec")) {
                finding.setFrameTimeSec(response.get("frame_time_sec").asDouble());
            }

            findings.add(finding);
        }

        return findings;
    }
}
