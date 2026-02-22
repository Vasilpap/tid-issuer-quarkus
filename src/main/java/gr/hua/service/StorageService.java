package gr.hua.service;

import io.minio.*;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.InputStream;
import java.util.UUID;

@ApplicationScoped
public class StorageService {

    @ConfigProperty(name = "minio.url")
    String minioUrl;

    @ConfigProperty(name = "minio.access-key")
    String accessKey;

    @ConfigProperty(name = "minio.secret-key")
    String secretKey;

    @ConfigProperty(name = "minio.bucket")
    String bucket;

    private volatile MinioClient minioClient;
    private volatile boolean bucketEnsured = false;

    private MinioClient getClient() {
        if (minioClient == null) {
            synchronized (this) {
                if (minioClient == null) {
                    minioClient = MinioClient.builder()
                            .endpoint(minioUrl)
                            .credentials(accessKey, secretKey)
                            .build();
                }
            }
        }
        if (!bucketEnsured) {
            synchronized (this) {
                if (!bucketEnsured) {
                    try {
                        boolean exists = minioClient.bucketExists(
                                BucketExistsArgs.builder().bucket(bucket).build()
                        );
                        if (!exists) {
                            minioClient.makeBucket(
                                    MakeBucketArgs.builder().bucket(bucket).build()
                            );
                            Log.infof("Created MinIO bucket: %s", bucket);
                        }
                        bucketEnsured = true;
                    } catch (Exception e) {
                        throw new RuntimeException("MinIO bucket initialization failed", e);
                    }
                }
            }
        }
        return minioClient;
    }

    public String uploadFile(InputStream inputStream, String filename, String contentType, long size) {
        String objectKey = UUID.randomUUID() + "/" + filename;
        try {
            getClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    public InputStream downloadFile(String objectKey) {
        try {
            return getClient().getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }

    public void deleteFile(String objectKey) {
        try {
            getClient().removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }
}
