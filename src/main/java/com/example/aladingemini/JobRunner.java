package com.example.aladingemini;

import com.example.aladingemini.model.BookEnriched;
import com.example.aladingemini.model.BookRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * 애플리케이션이 실행될 때 한 번 전체 배치를 수행한다.
 */
@Component
public class JobRunner implements CommandLineRunner {

    private final AladinFetcherService aladinFetcherService;
    private final EnrichmentService enrichmentService;
    private final JobProperties jobProperties;
    private final ObjectMapper objectMapper;

    public JobRunner(AladinFetcherService aladinFetcherService,
                     EnrichmentService enrichmentService,
                     JobProperties jobProperties,
                     ObjectMapper objectMapper) {
        this.aladinFetcherService = aladinFetcherService;
        this.enrichmentService = enrichmentService;
        this.jobProperties = jobProperties;
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 알라딘 수집 + Gemini enrichment 배치 시작 ===");

        List<BookRecord> rawBooks = aladinFetcherService.fetchBooks();
        List<BookEnriched> enriched = enrichmentService.enrichAll(rawBooks);

        String dateStr = LocalDate.now().toString();
        File outDir = new File(jobProperties.getOutputDir());
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IllegalStateException("출력 디렉터리를 생성할 수 없습니다: " + outDir.getAbsolutePath());
        }

        File outFile = new File(outDir, "books_enriched_" + dateStr + ".json");
        objectMapper.writeValue(outFile, enriched);

        System.out.println("배치 완료. 결과 파일: " + outFile.getAbsolutePath());
    }
}
