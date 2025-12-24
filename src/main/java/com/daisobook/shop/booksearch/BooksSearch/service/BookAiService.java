//package com.daisobook.shop.booksearch.BooksSearch.service;
//
//import org.springframework.ai.chat.model.ChatModel;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.ai.chat.prompt.PromptTemplate;
//import org.springframework.stereotype.Service;
//import lombok.RequiredArgsConstructor;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class BookAiService {
//
//    private final ChatModel chatModel; // Spring AI에서 자동으로 주입해주는 빈
//
//    public String refineBookInfo(String title, String description) {
//        // 1. AI에게 시킬 일(프롬프트) 설계
//        String instructions = """
//            당신은 도서 추천 전문가입니다.
//            제공된 도서의 제목과 줄거리를 바탕으로 다음 형식에 맞춰 가공해주세요.
//
//            1. 한 줄 요약: (독자의 흥미를 끌 수 있게)
//            2. 핵심 키워드: (3가지, #해시태그 형태)
//            3. 추천 대상: (어떤 사람에게 좋은지)
//
//            도서 제목: {title}
//            도서 줄거리: {description}
//            """;
//
//        // 2. 템플릿에 실제 데이터 넣기
//        PromptTemplate promptTemplate = new PromptTemplate(instructions);
//        Prompt prompt = promptTemplate.create(Map.of(
//            "title", title,
//            "description", description
//        ));
//
//        // 3. AI에게 요청 보내고 결과 받기
//        return chatModel.call(prompt).getResult().getOutput().getContent();
//    }
//}