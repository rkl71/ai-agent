package com.hanyang.aiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDownloadToolTest {

    @Test
    void downloadResource() {
        ResourceDownloadTool downloadTool = new ResourceDownloadTool();
        String url = "https://kolin-blog.oss-cn-shanghai.aliyuncs.com/blog/202410272117648.jpg";
        String fileName = "logo.jpg";
        String result = downloadTool.downloadResource(url, fileName);
        assertNotNull(result);
    }
}