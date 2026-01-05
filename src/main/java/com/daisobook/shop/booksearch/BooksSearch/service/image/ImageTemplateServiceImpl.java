package com.daisobook.shop.booksearch.BooksSearch.service.image;

import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImageDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public abstract class ImageTemplateServiceImpl implements ImageService {
    private final MinioClient minioClient;
    private final WebClient webClient;

    @Value("${minio.url}")
    private String minioUrl;

    public ImageTemplateServiceImpl(MinioClient minioClient, WebClient.Builder webClientBuilder) {
        this.minioClient = minioClient;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Map<Integer, String> createdProcess(ImagesReqDTO imagesReqDTO, Map<String, MultipartFile> fileMap) {
        long id = imagesReqDTO.connectedId();

        Map<Integer, String> urlMap = new HashMap<>();
        for(ImageMetadataReqDTO imageDTO: imagesReqDTO.imageMetadata()){
            String url = null;

            if(imageDTO.dataUrl() != null && !imageDTO.dataUrl().isEmpty()){
                String imageUrl = imageDTO.dataUrl();
                url = uploadImageFromUrl(imageUrl, id);

            } else if (imageDTO.fileKey() != null && !imageDTO.fileKey().isEmpty()) {
                MultipartFile file = fileMap.get(imageDTO.fileKey());

                if(file != null){
                    url = uploadImageFromFile(file, id);
                } else {
                    throw new RuntimeException();
                }
            } else {
                throw new RuntimeException();
            }

            if(url == null){
                throw new RuntimeException("업로드 실패");
            } else {
                urlMap.put(imageDTO.sequence(), url);
            }
        }

        return urlMap;
    }

    @Override
    public Map<Integer, String> updatedProcess(ImagesReqDTO imagesReqDTO, Map<String, MultipartFile> fileMap, List<ImageDTO> imageDTOList) {
        Map<Integer, String> urlMap = new HashMap<>();
        for(ImageMetadataReqDTO imageDTO: imagesReqDTO.imageMetadata()){
            String url = null;

            if(imageDTO.dataUrl() != null && !imageDTO.dataUrl().isEmpty()){
                String imageUrl = imageDTO.dataUrl();
                String preImageUrl = getLegalImagePath(imageDTO.sequence(), imageDTOList);

                if(!imageUrl.equals(preImageUrl)){
                    url = updateImageFromUrl(imageUrl, preImageUrl);
                } else {
                    url = preImageUrl;
                }

            } else if (imageDTO.fileKey() != null && !imageDTO.fileKey().isEmpty()) {
                MultipartFile file = fileMap.get(imageDTO.fileKey());

                if(file != null){
                    url = updateImageFromFile(file, getLegalImagePath(imageDTO.sequence(), imageDTOList));
                } else {
                    throw new RuntimeException();
                }
            } else {
                throw new RuntimeException();
            }

            if(url == null){
                throw new RuntimeException("업데이트 실패");
            } else {
                urlMap.put(imageDTO.sequence(), url);
            }
        }

        return urlMap;
    }

    private String getLegalImagePath(int sequence, List<ImageDTO> imageDTOList){
        return imageDTOList.stream()
                .filter(i -> i.no() == sequence)
                .map(ImageDTO::path)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("업데이트하려는 기존 이미지(Sequence: " + sequence + ")의 경로를 찾을 수 없습니다."));
    }

    @Override
    public String uploadImageFromUrl(String imageUrl, Long id) {
        try {
            // 1. 외부 이미지 다운로드
//            byte[] imageBytes = webClient.get()
//                    .uri(imageUrl)
//                    .retrieve()
//                    .bodyToMono(byte[].class)
//                    .toFuture().get();

            byte[] imageBytes;
            String fileExtension;
            if (imageUrl.startsWith("data:image")) {
                // 1. Base64 데이터인 경우 (직접 디코딩)
                String[] parts = imageUrl.split(",");
                String base64Data = parts[1];
                imageBytes = Base64.getDecoder().decode(base64Data);

                // 확장자 추출 (예: data:image/jpeg;base64 -> .jpeg)
                fileExtension = "." + parts[0].split("/")[1].split(";")[0];
            } else {
                // 2. 실제 URL인 경우 (기존처럼 WebClient 사용)
                imageBytes = webClient.get()
                        .uri(imageUrl)
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .toFuture().get();
                fileExtension = getFileExtension(imageUrl);
            }

//            String fileExtension = getFileExtension(imageUrl);
            String objectName = String.format("%d/%s%s",
                    id, UUID.randomUUID().toString(), fileExtension);

            String contentType = determineContentType(fileExtension);

            try (InputStream is = new ByteArrayInputStream(imageBytes)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(getBucketName())
                                .object(objectName)
                                .stream(is, imageBytes.length, -1)
                                .contentType(contentType)
                                .build()
                );
            }

            return String.format("%s/%s/%s", minioUrl, getBucketName(), objectName);

        } catch (MinioException e) {
            throw new RuntimeException("MinIO 서버 업로드 중 오류 발생", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("이미지 다운로드 중 오류 발생", e);
        } catch (Exception e) {
            throw new RuntimeException("알 수 없는 처리 오류 발생", e);
        }
    }

    @Override
    public String uploadImageFromFile(MultipartFile file, Long id) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String objectName = String.format("%d/%s%s",
                    id, UUID.randomUUID().toString(), fileExtension);

            String contentType = file.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = determineContentType(fileExtension);
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(getBucketName())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1) // 파일 크기를 명시
                            .contentType(contentType)
                            .build()
            );

            return String.format("%s/%s/%s", minioUrl, getBucketName(), objectName);

        } catch (MinioException e) {
            throw new RuntimeException("MinIO 서버 업로드 중 오류 발생", e);
        } catch (IOException e) {
            throw new RuntimeException("파일 스트림 처리 중 오류 발생", e);
        } catch (Exception e) {
            throw new RuntimeException("알 수 없는 처리 오류 발생", e);
        }
    }

    private String getFileExtension(String url) {
        int dotIndex = url.lastIndexOf('.');
        int queryIndex = url.lastIndexOf('?');
        int end = (queryIndex != -1 && queryIndex > dotIndex) ? queryIndex : url.length();

        if (dotIndex > 0 && dotIndex < end) {
            return url.substring(dotIndex, end).toLowerCase(); // .jpg, .png
        }
        return ".jpg";
    }

    @Override
    public String updateImageFromUrl(String newImageUrl, String fullImagePath) {
        try {
            String ObjectName = extractObjectNameFromPath(fullImagePath);
            // 1. 새 이미지 다운로드
            byte[] imageBytes = webClient.get()
                    .uri(newImageUrl)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .toFuture().get();

            String fileExtension = getFileExtension(ObjectName); // 기존 확장자 재사용
            String contentType = determineContentType(fileExtension);

            try (InputStream is = new ByteArrayInputStream(imageBytes)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(getBucketName())
                                .object(ObjectName) // <--- 기존 객체 이름 사용
                                .stream(is, imageBytes.length, -1)
                                .contentType(contentType)
                                .build()
                );
            } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                     InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new RuntimeException(e);
            }

            return String.format("%s/%s/%s", minioUrl, getBucketName(), fullImagePath);

        } catch (InterruptedException e) {
            throw new RuntimeException("이미지 업데이트 중 오류 발생: " + fullImagePath, e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String updateImageFromFile(MultipartFile newFile, String fullImagePath) {
        if (newFile.isEmpty()) {
            throw new IllegalArgumentException("업데이트할 파일이 비어 있습니다.");
        }
        String ObjectName = extractObjectNameFromPath(fullImagePath);

        try (InputStream inputStream = newFile.getInputStream()) {
            String fileExtension = getFileExtension(ObjectName);
            String contentType = newFile.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = determineContentType(fileExtension);
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(getBucketName())
                            .object(ObjectName) // <--- 기존 객체 이름 사용
                            .stream(inputStream, newFile.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            // 저장된 이미지의 접근 URL 반환
            return String.format("%s/%s/%s", minioUrl, getBucketName(), fullImagePath);

        } catch (MinioException e) {
            throw new RuntimeException("MinIO 서버 업데이트 중 오류 발생", e);
        } catch (IOException e) {
            throw new RuntimeException("파일 스트림 처리 중 오류 발생", e);
        } catch (Exception e) {
            throw new RuntimeException("알 수 없는 처리 오류 발생", e);
        }
    }

    @Override
    public void deleteObject(String fullImagePath) {
        String ObjectName = extractObjectNameFromPath(fullImagePath);
        try {
            minioClient.removeObject(
                    io.minio.RemoveObjectArgs.builder()
                            .bucket(getBucketName())
                            .object(ObjectName)
                            .build()
            );
        } catch (MinioException e) {
            throw new RuntimeException("MinIO 삭제 오류 발생: " + ObjectName, e);
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제 중 알 수 없는 오류 발생: " + ObjectName, e);
        }
    }

    @Override
    public void deleteObjects(List<String> fullImagePathList){
        for(String fullImagePath: fullImagePathList) {
            deleteObject(fullImagePath);
        }
    }

    private String determineContentType(String extension) {
        if (extension.contains("png")) return "image/png";
        if (extension.contains("gif")) return "image/gif";
        return "image/jpeg"; // 기본값
    }

    private String extractObjectNameFromPath(String fullImagePath) {
        if (fullImagePath == null || fullImagePath.isEmpty()) {
            throw new IllegalArgumentException("URL 또는 경로가 null이거나 비어있습니다.");
        }

        String fullPrefix = String.format("%s/%s/", this.minioUrl, getBucketName());

        String objectName;

        if (fullImagePath.startsWith(fullPrefix)) {
            objectName = fullImagePath.substring(fullPrefix.length());
        } else {
            objectName = fullImagePath;
        }

        int queryIndex = objectName.lastIndexOf('?');
        if (queryIndex != -1) {
            objectName = objectName.substring(0, queryIndex);
        }

        if (objectName.isEmpty()) {
            throw new IllegalArgumentException("URL 또는 경로에서 유효한 MinIO 객체 이름을 추출할 수 없습니다.");
        }

        return objectName;
    }
}
