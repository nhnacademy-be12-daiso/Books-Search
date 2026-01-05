package com.daisobook.shop.booksearch.books_search.service;

import com.daisobook.shop.booksearch.books_search.exception.custom.image.MinIOServiceException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class MinIOService {

    private final MinioClient minioClient;
    private final WebClient webClient;

    private static final String UNKNOWN_ERROR_MESSAGE = "알 수 없는 처리 오류 발생";

    @Value("${minio.bucketName.test}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    public MinIOService(MinioClient minioClient, WebClient.Builder webClientBuilder) {
        this.minioClient = minioClient;
        // 등록된 WebClient Builder를 사용하여 인스턴스 생성
        this.webClient = webClientBuilder.build(); 
    }

    /**
     * 외부 URL의 이미지를 다운로드하고 MinIO에 저장 후, 접근 가능한 URL을 반환합니다.
     * @param imageUrl 원본 이미지 URL
     * @param bookId 도서의 고유 ID (MinIO 객체 경로에 사용)
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

            // 3. MinIO에 업로드 (byte array를 스트림으로 변환)
            putObjectByUrl(objectName, imageBytes, contentType);

            // 4. 저장된 이미지의 접근 URL 반환
            // MinIO 서버 URL + 버킷 이름 + 객체 이름
            return String.format("%s/%s/%s", minioUrl, bucketName, objectName);

        } catch (InterruptedException e) {
            // 1. 현재 스레드에 중단 상태를 다시 설정 (소나큐브 핵심 요구사항)
            Thread.currentThread().interrupt();

            // 2. 원래 하려던 예외 처리를 진행
            throw new MinIOServiceException("이미지 다운로드 중 인터럽트 발생");

        } catch (ExecutionException e) {
            // ExecutionException은 인터럽트와 상관없으므로 기존처럼 처리
            throw new MinIOServiceException("이미지 다운로드 실행 중 오류 발생");

        } catch (Exception e) {
            throw new MinIOServiceException(UNKNOWN_ERROR_MESSAGE);
        }
    }

    public String uploadImageFromFile(MultipartFile file, Long bookId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // 1. 객체 이름 생성 (bookId 폴더/UUID.확장자 형태)
            // MultipartFile의 원본 파일명에서 확장자를 추출하여 사용합니다.
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String objectName = String.format("%d/%s%s",
                    bookId, UUID.randomUUID().toString(), fileExtension);

            // 2. Content Type 설정 (MultipartFile의 ContentType을 우선 사용)
            String contentType = file.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = determineContentType(fileExtension);
            }

            // 3. MinIO에 업로드
            putObjectByFile(objectName, file, contentType);

            // 4. 저장된 이미지의 접근 URL 반환
            return String.format("%s/%s/%s", minioUrl, bucketName, objectName);

        } catch (IOException e) {
            throw new MinIOServiceException("파일 스트림 처리 중 오류 발생");
        } catch (Exception e) {
            throw new MinIOServiceException(UNKNOWN_ERROR_MESSAGE);
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

            // 3. MinIO에 덮어쓰기 업로드
            putObjectByUrl(existingObjectName, imageBytes, contentType);

            // 4. 저장된 이미지의 접근 URL 반환 (URL은 변하지 않음)
            return String.format("%s/%s/%s", minioUrl, bucketName, existingObjectName);

        } catch (InterruptedException e) {
            // 소나큐브 Reliability 대응: 인터럽트 상태 복구
            Thread.currentThread().interrupt();
            throw new MinIOServiceException("이미지 업데이트 중 인터럽트 발생: " + existingObjectName);

        } catch (ExecutionException e) {
            // 테스트 통과 대응: 메시지에 "이미지 업데이트"를 명시적으로 포함
            throw new MinIOServiceException("이미지 업데이트 중 오류 발생: " + existingObjectName);

        } catch (Exception e) {
            throw new MinIOServiceException("이미지 업데이트 중 알 수 없는 오류 발생");
        }
    }

    public String updateImageFromFile(MultipartFile newFile, String existingObjectName) {
        if (newFile.isEmpty()) {
            throw new IllegalArgumentException("업데이트할 파일이 비어 있습니다.");
        }

        try (InputStream inputStream = newFile.getInputStream()) {
            // 기존 객체 이름의 확장자를 사용하여 ContentType 결정
            String fileExtension = getFileExtension(existingObjectName);
            String contentType = newFile.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = determineContentType(fileExtension);
            }

            // MinIO에 덮어쓰기 업로드
            putObjectByFile(existingObjectName, newFile, contentType);

            // 저장된 이미지의 접근 URL 반환
            return String.format("%s/%s/%s", minioUrl, bucketName, existingObjectName);

        } catch (IOException e) {
            throw new MinIOServiceException("파일 스트림 처리 중 오류 발생");
        } catch (Exception e) {
            throw new MinIOServiceException(UNKNOWN_ERROR_MESSAGE);
        }
    }

    private void putObjectByUrl(String objectName, byte[] imageBytes, String contentType){
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, imageBytes.length, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new MinIOServiceException("이미지 url 업로드중 오류 발생");
        }
    }

    private void putObjectByFile(String objectName, MultipartFile file, String contentType){
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new MinIOServiceException("이미지 file 업로드중 오류 발생");
        }
    }

    /**
     * MinIO에 저장된 객체(파일)를 삭제합니다.
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
            throw new MinIOServiceException("MinIO 삭제 오류 발생: " + objectName);
        } catch (Exception e) {
            throw new MinIOServiceException("파일 삭제 중 알 수 없는 오류 발생: " + objectName);
        }
    }
    
    private String determineContentType(String extension) {
        if (extension.contains("png")) return "image/png";
        if (extension.contains("gif")) return "image/gif";
        return "image/jpeg"; // 기본값
    }
}