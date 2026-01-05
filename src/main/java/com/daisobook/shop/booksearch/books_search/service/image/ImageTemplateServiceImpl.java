package com.daisobook.shop.booksearch.books_search.service.image;

import com.daisobook.shop.booksearch.books_search.dto.service.ImageDTO;
import com.daisobook.shop.booksearch.books_search.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.books_search.exception.custom.image.ImageServiceException;
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

    private static final String UNKNOWN_ERROR_MESSAGE = "알 수 없는 처리 오류 발생";
    private static final String UPDATE_ERROR_PREFIX = "이미지 업데이트 중 오류 발생: ";

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
                    throw new ImageServiceException("[이미지 업로드] 파일이 비어있습니다");
                }
            } else {
                throw new ImageServiceException("[이미지 업로드] 관련 정보가 없습니다");
            }

            if(url == null){
                throw new ImageServiceException("[이미지 업로드] 업로드 실패");
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
                    throw new ImageServiceException("[이미지 업데이트] 파일이 비어있습니다");
                }
            } else {
                throw new ImageServiceException("[이미지 업데이트] 관련 정보가 없습니다");
            }

            if(url == null){
                throw new ImageServiceException("[이미지 업데이트] 업데이트 실패");
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
                .orElseThrow(() -> new ImageServiceException("업데이트하려는 기존 이미지(Sequence: " + sequence + ")의 경로를 찾을 수 없습니다."));
    }

    @Override
    public String uploadImageFromUrl(String imageUrl, Long id) {
        try {
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

            putObjectToMinio(objectName, imageBytes, contentType);

            return String.format("%s/%s/%s", minioUrl, getBucketName(), objectName);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ImageServiceException(UPDATE_ERROR_PREFIX + imageUrl);
        } catch (ExecutionException e) {
            throw new ImageServiceException("이미지 업데이트 중 스레드 오류 발생: " + imageUrl);
        } catch (Exception e) {
            throw new ImageServiceException(UNKNOWN_ERROR_MESSAGE);
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

            executeMinioPut(objectName, inputStream, file.getSize(), contentType);

            return String.format("%s/%s/%s", minioUrl, getBucketName(), objectName);

        } catch (IOException e) {
            throw new ImageServiceException("파일 스트림 처리 중 오류 발생");
        } catch (Exception e) {
            throw new ImageServiceException("알 수 없는 처리 오류 발생");
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
            String objectName = extractObjectNameFromPath(fullImagePath);
            // 1. 새 이미지 다운로드
            byte[] imageBytes = webClient.get()
                    .uri(newImageUrl)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .toFuture().get();

            String fileExtension = getFileExtension(objectName); // 기존 확장자 재사용
            String contentType = determineContentType(fileExtension);

            putObjectToMinio(objectName, imageBytes, contentType);

            return String.format("%s/%s/%s", minioUrl, getBucketName(), fullImagePath);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ImageServiceException(UPDATE_ERROR_PREFIX + fullImagePath);
        } catch (ExecutionException e) {
            throw new ImageServiceException("이미지 파일 업로드/처리 중 실행 오류 발생: " + fullImagePath);
        }
    }

    private void putObjectToMinio(String objectName, byte[] imageBytes, String contentType) {
        try (InputStream is = new ByteArrayInputStream(imageBytes)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(getBucketName())
                            .object(objectName)
                            .stream(is, imageBytes.length, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) { // 상세 예외들은 여기에 한데 모읍니다.
            throw new ImageServiceException(String.format("MinIO 업로드 중 오류 발생 (경로: %s)", objectName));
        }
    }

    @Override
    public String updateImageFromFile(MultipartFile newFile, String fullImagePath) {
        if (newFile.isEmpty()) {
            throw new IllegalArgumentException("업데이트할 파일이 비어 있습니다.");
        }
        String objectName = extractObjectNameFromPath(fullImagePath);

        try (InputStream inputStream = newFile.getInputStream()) {
            String fileExtension = getFileExtension(objectName);
            String contentType = newFile.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = determineContentType(fileExtension);
            }

            executeMinioPut(objectName, inputStream, newFile.getSize(), contentType);

            // 저장된 이미지의 접근 URL 반환
            return String.format("%s/%s/%s", minioUrl, getBucketName(), fullImagePath);

        } catch (IOException e) {
            throw new ImageServiceException("파일 스트림 처리 중 오류 발생");
        } catch (Exception e) {
            throw new ImageServiceException(UNKNOWN_ERROR_MESSAGE);
        }
    }

    private void executeMinioPut(String objectName, InputStream inputStream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(getBucketName())
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new ImageServiceException(String.format("STORAGE_ERROR: MinIO 작업 실패 (객체: %s)", objectName));
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
            throw new ImageServiceException("MinIO 삭제 오류 발생: " + ObjectName);
        } catch (Exception e) {
            throw new ImageServiceException("파일 삭제 중 알 수 없는 오류 발생: " + ObjectName);
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
