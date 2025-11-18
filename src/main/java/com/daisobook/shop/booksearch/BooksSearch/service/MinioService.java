package com.daisobook.shop.booksearch.BooksSearch.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class MinioService {

    private final MinioClient minioClient;
    private final WebClient webClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    public MinioService(MinioClient minioClient, WebClient.Builder webClientBuilder) {
        this.minioClient = minioClient;
        // 등록된 WebClient Builder를 사용하여 인스턴스 생성
        this.webClient = webClientBuilder.build(); 
    }

    /**
     * 외부 URL의 이미지를 다운로드하고 MiniIO에 저장 후, 접근 가능한 URL을 반환합니다.
     * @param imageUrl 원본 이미지 URL
     * @param bookId 도서의 고유 ID (MiniIO 객체 경로에 사용)
     * @return MiniIO에 저장된 이미지의 전체 접근 URL
     */
    public String uploadImageFromUrl(String imageUrl, Long bookId) {
        try {
            // 1. 외부 이미지 다운로드
            byte[] imageBytes = webClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .toFuture().get(); 

            // 2. 객체 이름 생성 (bookId 폴더/UUID.확장자 형태)
            String fileExtension = getFileExtension(imageUrl);
            String objectName = String.format("%d/%s%s", 
                bookId, UUID.randomUUID().toString(), fileExtension);
            
            String contentType = determineContentType(fileExtension);

            // 3. MiniIO에 업로드 (byte array를 스트림으로 변환)
            try (InputStream is = new ByteArrayInputStream(imageBytes)) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(is, imageBytes.length, -1)
                        .contentType(contentType)
                        .build()
                );
            }

            // 4. 저장된 이미지의 접근 URL 반환
            // MiniIO 서버 URL + 버킷 이름 + 객체 이름
            return String.format("%s/%s/%s", minioUrl, bucketName, objectName);

        } catch (MinioException e) {
            throw new RuntimeException("MiniIO 서버 업로드 중 오류 발생", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("이미지 다운로드 중 오류 발생", e);
        } catch (Exception e) {
            throw new RuntimeException("알 수 없는 처리 오류 발생", e);
        }
    }

    private String getFileExtension(String url) {
        int dotIndex = url.lastIndexOf('.');
        int queryIndex = url.lastIndexOf('?');
        // 쿼리 파라미터가 있다면 그 직전까지, 없다면 끝까지 확인
        int end = (queryIndex != -1 && queryIndex > dotIndex) ? queryIndex : url.length();
        
        if (dotIndex > 0 && dotIndex < end) {
            return url.substring(dotIndex, end).toLowerCase(); // .jpg, .png
        }
        return ".jpg"; 
    }

    /**
     * 도서 표지 이미지를 새로운 이미지로 업데이트합니다.
     * 이 메서드는 기존 객체 이름을 그대로 사용하여 덮어쓰기(Overwrite)합니다.
     * * @param newImageUrl 새로운 이미지의 외부 URL
     * @param existingObjectName DB에 저장된 기존 객체 이름 (예: 123/uuid-file.jpg)
     * @return 업데이트된 이미지의 전체 접근 URL (기존 URL과 동일)
     */
    public String updateImageFromUrl(String newImageUrl, String existingObjectName) {
        try {
            // 1. 새 이미지 다운로드
            byte[] imageBytes = webClient.get()
                    .uri(newImageUrl)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .toFuture().get();

            // 2. 파일 정보 추출 및 ContentType 설정
            String fileExtension = getFileExtension(existingObjectName); // 기존 확장자 재사용
            String contentType = determineContentType(fileExtension);

            // 3. MiniIO에 덮어쓰기 업로드
            try (InputStream is = new ByteArrayInputStream(imageBytes)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(existingObjectName) // <--- 기존 객체 이름 사용
                                .stream(is, imageBytes.length, -1)
                                .contentType(contentType)
                                .build()
                );
            }

            // 4. 저장된 이미지의 접근 URL 반환 (URL은 변하지 않음)
            return String.format("%s/%s/%s", minioUrl, bucketName, existingObjectName);

        } catch (Exception e) {
            throw new RuntimeException("이미지 업데이트 중 오류 발생: " + existingObjectName, e);
        }
    }

    /**
     * MiniIO에 저장된 객체(파일)를 삭제합니다.
     * @param objectName 삭제할 파일의 객체 이름 (예: 123/uuid-file.jpg)
     */
    public void deleteObject(String objectName) {
        try {
            minioClient.removeObject(
                    io.minio.RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (MinioException e) {
            throw new RuntimeException("MiniIO 삭제 오류 발생: " + objectName, e);
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제 중 알 수 없는 오류 발생: " + objectName, e);
        }
    }
    
    private String determineContentType(String extension) {
        if (extension.contains("png")) return "image/png";
        if (extension.contains("gif")) return "image/gif";
        return "image/jpeg"; // 기본값
    }
}