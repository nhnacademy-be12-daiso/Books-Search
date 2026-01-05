package com.daisobook.shop.booksearch.util;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ast.Text;

public class MarkdownUtils {
    public static String extractPlainText(String markdown) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        
        // 1. 마크다운을 Node 트리로 파싱
        Node document = parser.parse(markdown);
        
        // 2. 텍스트 추출을 위한 StringBuilder
        StringBuilder sb = new StringBuilder();
        
        // 3. 트리 전체를 순회하며 텍스트 노드만 수집
        collectTextNodes(document, sb);
        
        return sb.toString().trim();
    }

    private static void collectTextNodes(Node node, StringBuilder sb) {
        // 실제 텍스트 내용을 담고 있는 노드인 경우
        if (node instanceof Text) {
            sb.append(node.getChars());
        }
        
        // 자식 노드가 있다면 재귀적으로 방문
        Node child = node.getFirstChild();
        while (child != null) {
            collectTextNodes(child, sb);
            child = child.getNext();
        }
    }
}