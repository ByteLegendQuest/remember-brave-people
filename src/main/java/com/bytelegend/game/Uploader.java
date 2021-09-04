package com.bytelegend.game;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;

import static com.bytelegend.game.Constants.BRAVE_PEOPLE_ALL_JSON;
import static com.bytelegend.game.Constants.BRAVE_PEOPLE_PNG;
import static com.bytelegend.game.Constants.DEFAULT_S3_BUCKET;
import static com.bytelegend.game.Constants.DEFAULT_S3_REGION;

/**
 * Upload the updated JSON and image to data storage.
 */
interface Uploader {
    void uploadBravePeopleImage();

    void uploadBravePeopleAllJson();

    enum NoOpUploader implements Uploader {
        INSTANCE;

        @Override
        public void uploadBravePeopleImage() {
        }

        @Override
        public void uploadBravePeopleAllJson() {
        }
    }

    class S3Uploader implements Uploader {
        private final S3Client s3Client;
        private final Environment environment;

        S3Uploader(Environment environment) {
            this.environment = environment;
            this.s3Client = S3Client.builder()
                .region(Region.of(DEFAULT_S3_REGION))
                .credentialsProvider(() -> new AwsCredentials() {
                    @Override
                    public String accessKeyId() {
                        return environment.getAccessKeyId();
                    }

                    @Override
                    public String secretAccessKey() {
                        return environment.getAccessKeySecret();
                    }
                }).build();
        }

        private void upload(String path, File fileToUpload) {
            String contentType = null;
            if (fileToUpload.getName().endsWith("png")) {
                contentType = "image/png";
            } else if (fileToUpload.getName().endsWith("json")) {
                contentType = "application/json; charset=utf-8";
            } else {
                throw new IllegalArgumentException("Unrecognize: " + fileToUpload);
            }
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(DEFAULT_S3_BUCKET)
                .key(path)
                .contentType(contentType)
                .contentLength(fileToUpload.length())
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();
            s3Client.putObject(request, RequestBody.fromFile(fileToUpload));
        }

        @Override
        public void uploadBravePeopleImage() {
            upload(BRAVE_PEOPLE_PNG, environment.getOutputBravePeopleImage());
        }

        @Override
        public void uploadBravePeopleAllJson() {
            upload(BRAVE_PEOPLE_ALL_JSON, environment.getOutputBravePeopleAllJson());
        }
    }
}
