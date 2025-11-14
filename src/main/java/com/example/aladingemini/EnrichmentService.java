package com.example.aladingemini;

import com.example.aladingemini.model.BookEnriched;
import com.example.aladingemini.model.BookRecord;
import com.example.aladingemini.model.LlmResponse;
import com.example.aladingemini.model.SaleStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class EnrichmentService {

    private final GeminiClient geminiClient;
    private final JobProperties jobProperties;

    public EnrichmentService(GeminiClient geminiClient, JobProperties jobProperties) {
        this.geminiClient = geminiClient;
        this.jobProperties = jobProperties;
    }

    public List<BookEnriched> enrichAll(List<BookRecord> rawBooks) {
        List<BookEnriched> enrichedList = new ArrayList<>();

        boolean useGemini = jobProperties.isEnableEnrichment() && geminiClient.isConfigured();

        for (BookRecord raw : rawBooks) {
            try {
                LlmResponse ai = null;
                if (useGemini) {
                    ai = geminiClient.enrichBook(raw);
                }
                BookEnriched enriched = merge(raw, ai);
                enrichedList.add(enriched);
            } catch (Exception e) {
                System.err.println("도서 enrichment 실패 (isbn13=" + raw.getIsbn13() + "): " + e.getMessage());
                // 실패 시, 최소한 Raw 정보를 기반으로 한 기본 객체라도 만들어 넣을 수 있다.
                BookEnriched fallback = merge(raw, null);
                enrichedList.add(fallback);
            }
        }

        return enrichedList;
    }

    private BookEnriched merge(BookRecord raw, LlmResponse ai) {
        BookEnriched e = new BookEnriched();

        e.setIsbn13(raw.getIsbn13());
        e.setTitle(raw.getTitle());
        e.setAuthor(raw.getAuthor());
        e.setPublisher(raw.getPublisher());

        // pubDate 파싱
        if (raw.getPubDate() != null && !raw.getPubDate().isBlank()) {
            e.setPublishedAt(raw.getPubDate());
        }

        e.setListPrice(raw.getPriceStandard());

        if (ai != null && ai.getDescription() != null && !ai.getDescription().isBlank()) {
            e.setDescription(ai.getDescription());
            e.setAiGeneratedDescription(true);
        } else {
            e.setDescription(raw.getDescription());
            e.setAiGeneratedDescription(false);
        }

        if (ai != null && ai.getToc() != null) {
            e.setToc(ai.getToc());
            e.setAiGeneratedToc(true);
        }

        if (ai != null && ai.getCategoryPath() != null) {
            String[] parts = ai.getCategoryPath().split(">");
            String l1 = parts.length > 0 ? parts[0].trim() : null;
            String l2 = parts.length > 1 ? parts[1].trim() : null;
            String l3 = parts.length > 2 ? parts[2].trim() : null;
            e.setCategoryLevel1(l1);
            e.setCategoryLevel2(l2);
            e.setCategoryLevel3(l3);
            e.setAiGeneratedCategory(true);
        }

        if (ai != null && ai.getTags() != null) {
            e.setTags(ai.getTags());
            e.setAiGeneratedTags(true);
        }

        if (ai != null && ai.getPackable() != null) {
            e.setPackable(ai.getPackable());
        } else {
            e.setPackable(true);
        }

        // 재고 / 상태는 raw 에서 가져오되, 혹시 0 이면 보정
        int stock = raw.getStock();
        if (stock <= 0) {
            stock = ThreadLocalRandom.current().nextInt(1, 11);
        }
        e.setStock(stock);

        SaleStatus saleStatus = raw.getSaleStatus();
        if (saleStatus == null) {
            saleStatus = SaleStatus.ON_SALE;
        }
        e.setSaleStatus(saleStatus);

        return e;
    }
}
