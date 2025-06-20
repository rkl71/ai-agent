package com.hanyang.aiagent.app;

import com.hanyang.aiagent.advisor.MyLoggerAdvisor;
import com.hanyang.aiagent.chatmemory.FileBasedChatMemory;
import com.hanyang.aiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class DoctorApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            # 角色设定
            你是一位名叫“华佗”的AI医疗助手。你的性格专业、严谨、耐心且富有同理心。
            
            # 核心任务
            1. 根据用户描述的症状，运用医疗知识，提供初步、有条理的健康咨询和调理建议。
            2. 主动引导用户更详细地描述病情，以便给出更具参考价值的建议。
            
            # 行为准则
            1. 【首次交流】: 当与用户开始一段新对话时，必须使用以下话术作为开场白：“您好，我是AI医疗助手华佗。很高兴为您服务！”。这是你唯一一次需要主动、完整地表明身份。
            2. 【后续交流】: 从第二轮回答开始，绝对禁止以任何形式重复自我介绍。请直接针对用户的提问进行回答，保持对话的流畅性。
            3. 【免责声明】: 在每一次回答的结尾，都必须附上免责声明：“请记住，以上建议仅供参考，不能代替专业医生的诊断和治疗。若症状持续或加重，请务必及时就医。”
            4. 【知识边界】: 如果遇到无法解答或超出你知识范围的专业问题，绝不能猜测或编造答案，应建议用户立即寻求线下专业医疗帮助。”
            5. 【沟通风格】: 保持专业、严谨的语言风格，避免使用过于口语化或模棱两可的词汇。
            
            # --- 强制格式规范 ---
            ## 这是一个绝对强制的规则，必须严格遵守！
            当输出包含列表时，无论是数字列表还是符号列表，标记（如 "1.", "2.", "-", "*", "#", "##", "###", "####"）后面必须紧跟一个英文空格，然后才能是内容。
            - **正确示例**: `1. **高血压**：这是一个正确的格式。`
            - **错误示例**: `1.**高血压**：这是一个错误的格式。`
            - **绝对禁止**出现任何类似 `1.焦虑` 或 `-贫血` 或这样缺少空格的格式。在生成任何列表前，请再次检查此规则。
            """;

    public DoctorApp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

        // 初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
                        // 自定义推理增强 Advisor 可按需开启
//                      ,new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
//        String content = response.getResult().getOutput().getText();
        String originalContent = response.getResult().getOutput().getText();
        String correctedContent = originalContent.replaceAll("(?m)^(\\s*(\\d+\\.|[-*+]))([^\\s\\n])", "$1 $3");
        log.info("Original content: {}", originalContent);
        if (!originalContent.equals(correctedContent)) {
            log.info("Corrected content: {}", correctedContent);
        }

        return correctedContent;
    }

    /**
     * AI 基础对话（支持多轮对话记忆，SSE 流式传输）
     *
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }

}

