package com.hanyang.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GoogleWebSearchToolTest {

    @Value("${search-api.serp-api-key}")
    private String serpApiKey;

    @Test
    void googleSearch() {
        GoogleWebSearchTool tool = new GoogleWebSearchTool(serpApiKey);
        String query = "医疗RAG知识问答助手";
        String result = tool.googleSearch(query);
        assertNotNull(result);
    }
}