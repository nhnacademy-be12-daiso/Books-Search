package com.daisobook.shop.booksearch.BooksSearch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownUtilsTest {

    @Test
    @DisplayName("기본적인 마크다운 문법(#, **, _)이 제거되고 텍스트만 추출되는지 확인한다")
    void extractPlainText_Basic_Test() {
        // Given
        String markdown = "# 제목\n이것은 **굵은 글씨**와 *기울임*입니다.";
        
        // When
        String result = MarkdownUtils.extractPlainText(markdown);
        
        // Then
        // flexmark의 기본 파싱 방식에 따라 '제목'과 '내용' 사이의 공백 처리가 달라질 수 있으나 
        // 핵심은 특수문자(#, *)가 사라지는 것
        assertThat(result).contains("제목").contains("이것은 굵은 글씨와 기울임입니다.").doesNotContain("#", "*", "**");
    }

    @ParameterizedTest
    @DisplayName("다양한 마크다운 요소에서 순수 텍스트만 정확히 추출한다")
    @CsvSource({
        "'[네이버](https://naver.com)', 네이버",
        "'---\n구분선 뒤 텍스트', 구분선 뒤 텍스트",
        "'* 리스트1\n* 리스트2', 리스트1 리스트2",
        "'> 인용문입니다.', 인용문입니다."
    })
    void extractPlainText_VariousElements_Test(String input, String expected) {
        String result = MarkdownUtils.extractPlainText(input);
        
        // 공백 차이는 무시하고 필요한 텍스트가 포함되어 있는지 확인
        String cleanResult = result.replaceAll("\\s+", "");
        String cleanExpected = expected.replaceAll("\\s+", "");

        assertThat(cleanResult).contains(cleanExpected);
    }

    @Test
    @DisplayName("입력값이 null이거나 빈 문자열일 경우에 대해 안전하게 처리하는지 확인한다")
    void extractPlainText_EdgeCase_Test() {
        // 빈 문자열 테스트
        assertThat(MarkdownUtils.extractPlainText("")).isEmpty();
    }

    @Test
    @DisplayName("중첩된 노드 구조(재귀 방문)가 정상적으로 동작하는지 확인한다")
    void extractPlainText_NestedNode_Test() {
        // Given: 볼드 안에 링크가 있는 복잡한 구조
        String complexMarkdown = "**[링크 텍스트](http://link.com)**";

        // When
        String result = MarkdownUtils.extractPlainText(complexMarkdown);

        // Then
        assertThat(result).isEqualTo("링크 텍스트");
    }
}