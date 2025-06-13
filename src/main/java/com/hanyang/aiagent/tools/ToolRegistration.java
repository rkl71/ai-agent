package com.hanyang.aiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集中的工具注册类
 */
@Configuration
public class ToolRegistration {

//    @Value("${search-api.api-key}")
//    private String searchApiKey;
        @Value("${search-api.serp-api-key}")
        private String serpApiKey;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
//        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        GoogleWebSearchTool googleWebSearchTool = new GoogleWebSearchTool(serpApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();

        return ToolCallbacks.from(
                fileOperationTool,
//                webSearchTool,
                googleWebSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool
        );
    }
}
