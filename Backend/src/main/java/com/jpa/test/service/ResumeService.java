package com.jpa.test.service;

import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ResumeService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public ResumeService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> generateResume(Map<String, Object> request) {
        String prompt = buildResumePrompt(request);
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                });

        try {
            String response = webClient.post()
                    .uri(geminiApiUrl + "?key=" + geminiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Gemini API Response: " + response);
            return extractGeneratedResume(response);

        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    private Map<String, Object> extractGeneratedResume(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            System.out.println("Parsed JSON Response: " + rootNode.toPrettyString());

            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            if (textNode.isMissingNode() || textNode.asText().isEmpty()) {
                return Map.of("error", "Invalid response format from AI.");
            }

            // Remove AI-generated code block formatting 
            String rawJson = textNode.asText()
                    .replaceAll("^```json", "")  // Remove starting ```json
                    .replaceAll("```$", "")      // Remove ending ```
                    .trim();

            return objectMapper.readValue(rawJson, HashMap.class);

        } catch (Exception e) {
            return Map.of("error", "Error parsing AI response: " + e.getMessage());
        }
    }

    private String buildResumePrompt(Map<String, Object> request) {
        return String.format("""
                Generate a professional IT job resume in JSON format based on the following description:

                Input Description:
                "%s"

                JSON Structure Requirements:

                personalInformation: Include the following keys:
                - fullName (string)
                - email (string)
                - phoneNumber (string)
                - location (string)
                - linkedin (string or null if not provided)
                - gitHub (string or null if not provided)
                - portfolio (string or null if not provided)

                summary: Short professional summary (string).

                skills: Array of objects with keys:
                - title (string)
                - level (string)

                experience: Array of objects with keys:
                - jobTitle (string)
                - company (string)
                - location (string)
                - duration (string)
                - responsibility (string)

                education: Array of objects with keys:
                - degree (string)
                - university (string)
                - location (string)
                - graduationYear (string)

                certifications: Array of objects with keys:
                - title (string)
                - issuingOrganization (string)
                - year (string)

                projects: Array of objects with keys:
                - title (string)
                - description (string)
                - technologiesUsed (string)
                - githubLink (string)

                languages: Array of objects with key:
                - name (string)

                interests: Array of objects with key:
                - name (string)

                Return the output strictly in JSON format matching this structure.
                """, request.get("userDescription"));
    }

}
