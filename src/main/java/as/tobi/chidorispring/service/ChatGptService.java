package as.tobi.chidorispring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import as.tobi.chidorispring.dto.RecommendationRequest;
import as.tobi.chidorispring.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatGptService {
    private static final Logger log = LoggerFactory.getLogger(ChatGptService.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String openAiApiUrl;

    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        try {
            log.info("Building prompt for ChatGPT");
            String prompt = buildPrompt(request);
            log.debug("Generated prompt: {}", prompt);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            String requestBody = buildRequestBody(prompt);
            log.debug("Request body for ChatGPT: {}", requestBody);
            
            HttpEntity<String> httpRequest = new HttpEntity<>(requestBody, headers);

            log.info("Sending request to ChatGPT API");
            String response = restTemplate.postForObject(openAiApiUrl, httpRequest, String.class);
            log.debug("Raw response from ChatGPT: {}", response);
            
            return parseResponse(response);
        } catch (Exception e) {
            log.error("Error while getting recommendations from ChatGPT", e);
            return new RecommendationResponse();
        }
    }

    private String buildPrompt(RecommendationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Based on the following favorite anime characters and their shows, ");
        prompt.append("recommend similar anime, genres, and characters. Respond ONLY in JSON format. Here are the favorites:\n\n");

        for (RecommendationRequest.PostData post : request.getFavoritePosts()) {
            prompt.append("Character: ").append(post.getCharacterName()).append("\n");
            prompt.append("Anime: ").append(post.getAnime()).append("\n");
            prompt.append("Genres: ").append(String.join(", ", post.getAnimeGenre())).append("\n\n");
        }

        prompt.append("JSON format:\n");
        prompt.append("{\"recommendedAnime\":[\"anime1\",\"anime2\"],");
        prompt.append("\"recommendedGenres\":[\"genre1\",\"genre2\"],");
        prompt.append("\"recommendedCharacters\":[\"character1\",\"character2\"]}");

        return prompt.toString();
    }

    private String buildRequestBody(String prompt) {
        String escapedPrompt = prompt.replace("\"", "\\\"")
                                     .replace("\n", "\\n");

        return String.format("""
            {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {
                        "role": "system",
                        "content": "You are an anime recommendation expert. Always respond in strict JSON format with recommendedAnime, recommendedGenres, and recommendedCharacters."
                    },
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ],
                "temperature": 0.7,
                "response_format": {"type": "json_object"}
            }""", escapedPrompt);
    }

    private RecommendationResponse parseResponse(String response) {
        try {
            log.info("Parsing ChatGPT response");
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode choicesNode = rootNode.path("choices");
            
            if (!choicesNode.isEmpty() && choicesNode.get(0).has("message")) {
                String content = choicesNode.get(0).path("message").path("content").asText();
                log.debug("Extracted content: {}", content);

                return objectMapper.readValue(content, RecommendationResponse.class);
            }

            log.warn("Could not extract JSON from response");
            return new RecommendationResponse();
        } catch (Exception e) {
            log.error("Error parsing ChatGPT response", e);
            return new RecommendationResponse();
        }
    }
} 