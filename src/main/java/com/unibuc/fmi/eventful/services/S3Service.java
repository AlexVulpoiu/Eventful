package com.unibuc.fmi.eventful.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class S3Service {

    public static final String EVENTS_FOLDER = "events";
    public static final String TICKETS_FOLDER = "tickets";

    @Value("${aws.s3.bucket}")
    private String bucketName;

    final AmazonS3 s3;

    public void uploadFile(String folder, String name, InputStream fileInputStream) {
        String key = String.join("/", folder, name);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, fileInputStream, null);
        s3.putObject(putObjectRequest);
    }

    public URL getObjectUrl(String folder, String name) {
        String key = String.join("/", folder, name);
        return s3.generatePresignedUrl(bucketName, key, Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
    }
}
