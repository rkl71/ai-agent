package com.hanyang.aiagent.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 创建上下文查询增强器的工厂
 */
public class LoveAppContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter createInstance(){
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate(
                """
                        抱歉，为了给您提供更专注的服务，我的功能目前主要集中在恋爱咨询方面。对于其他问题，暂时无法为您提供帮助，敬请谅解。
                        如果您有任何疑问或需要进一步的协助，请随时通过邮件联系我们的客服团队：renkelin00@gmail.com
                        感谢您的理解与支持。
                """);
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
    }
}
