package com.medibook.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupabaseStorageServiceImplTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private SupabaseStorageServiceImpl supabaseStorageService;

    private final String SUPABASE_S3_ENDPOINT = "https://zfkjwcngqgmmlpngtsbg.storage.supabase.co/storage/v1/s3";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(supabaseStorageService, "supabaseS3Endpoint", SUPABASE_S3_ENDPOINT);
    }

    @Test
    void validateFile_ValidPdfFile_ShouldNotThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        // Act & Assert
        assertDoesNotThrow(() -> supabaseStorageService.validateFile(file));
    }

    @Test
    void validateFile_ValidJpgFile_ShouldNotThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // Act & Assert
        assertDoesNotThrow(() -> supabaseStorageService.validateFile(file));
    }

    @Test
    void validateFile_ValidPngFile_ShouldNotThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test content".getBytes()
        );

        // Act & Assert
        assertDoesNotThrow(() -> supabaseStorageService.validateFile(file));
    }

    @Test
    void validateFile_NullFile_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> supabaseStorageService.validateFile(null));
        assertEquals("File cannot be null or empty", exception.getMessage());
    }

    @Test
    void validateFile_EmptyFile_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> supabaseStorageService.validateFile(file));
        assertEquals("File cannot be null or empty", exception.getMessage());
    }

    @Test
    void validateFile_FileTooLarge_ShouldThrowException() {
        // Arrange - Create a file larger than 5MB
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                largeContent
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> supabaseStorageService.validateFile(file));
        assertEquals("File size exceeds maximum limit of 5MB", exception.getMessage());
    }

    @Test
    void validateFile_InvalidContentType_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> supabaseStorageService.validateFile(file));
        assertEquals("File type not allowed. Only PDF, JPG, and PNG files are accepted", exception.getMessage());
    }

    @Test
    void validateFile_InvalidExtension_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "application/pdf", // Valid content type but wrong extension
                "test content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> supabaseStorageService.validateFile(file));
        assertEquals("File extension not allowed. Only .pdf, .jpg, .jpeg, and .png files are accepted", exception.getMessage());
    }

    @Test
    void getPublicUrl_ShouldReturnCorrectUrl() {
        // Arrange
        String bucketName = "test-bucket";
        String fileName = "test.pdf";

        // Act
        String result = supabaseStorageService.getPublicUrl(bucketName, fileName);

        // Assert
        String expectedUrl = "https://zfkjwcngqgmmlpngtsbg.storage.supabase.co/storage/v1/object/public/test-bucket/test.pdf";
        assertEquals(expectedUrl, result);
    }

    @Test
    void uploadFile_Success_ShouldReturnPublicUrl() {
        // Arrange
        String bucketName = "test-bucket";
        String fileName = "test.pdf";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        // Act
        String result = supabaseStorageService.uploadFile(bucketName, fileName, file).block();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("test-bucket"));
        assertTrue(result.contains("test.pdf"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    }

    @Test
    void uploadFile_IOException_ShouldThrowRuntimeException() throws IOException {
        // Arrange
        String bucketName = "test-bucket";
        String fileName = "test.pdf";

        // Create a mock MultipartFile that throws IOException
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(100L);
        when(file.getInputStream()).thenThrow(new IOException("Test IO Exception"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> supabaseStorageService.uploadFile(bucketName, fileName, file).block());
        assertTrue(exception.getMessage().contains("Error uploading file"));
    }

    @Test
    void deleteFile_Success_ShouldCompleteWithoutError() {
        // Arrange
        String bucketName = "test-bucket";
        String fileName = "test.pdf";

        // Act
        supabaseStorageService.deleteFile(bucketName, fileName).block();

        // Assert
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteFile_Exception_ShouldThrowRuntimeException() {
        // Arrange
        String bucketName = "test-bucket";
        String fileName = "test.pdf";

        // Mock exception during delete
        doThrow(new RuntimeException("S3 delete error")).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> supabaseStorageService.deleteFile(bucketName, fileName).block());
        assertTrue(exception.getMessage().contains("Error deleting file"));
    }
}