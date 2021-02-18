package com.bytelegend.game;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;

import static com.bytelegend.game.Constants.BRAVE_PEOPLE_ALL_JSON;
import static com.bytelegend.game.Constants.BRAVE_PEOPLE_PNG;
import static com.bytelegend.game.Constants.DEFAULT_OSS_BUCKET;
import static com.bytelegend.game.Constants.DEFAULT_OSS_ENDPOINT;

class OssClient {
    private final Environment environment;
    private final OSS oss;

    OssClient(Environment environment) {
        this.environment = environment;
        if (environment.getOssAccessKeyId() == null || environment.getOssAccessKeySecret() == null) {
            throw new NullPointerException();
        }
        if ("mock".equals(environment.getOssAccessKeyId())) {
            this.oss = new MockOSS();
        } else {
            this.oss = new OSSClientBuilder()
                    .build(DEFAULT_OSS_ENDPOINT, environment.getOssAccessKeyId(), environment.getOssAccessKeySecret());
        }
    }

    void uploadBravePeopleImage() {
        oss.putObject(new PutObjectRequest(DEFAULT_OSS_BUCKET, BRAVE_PEOPLE_PNG, environment.getOutputBravePeopleImage()));
    }

    void uploadBravePeopleAllJson() {
        oss.putObject(new PutObjectRequest(DEFAULT_OSS_BUCKET, BRAVE_PEOPLE_ALL_JSON, environment.getOutputBravePeopleAllJson()));
    }

    private static class MockOSS extends OSSClient {
        MockOSS() {
            super(DEFAULT_OSS_ENDPOINT, (CredentialsProvider) null, null);
        }

        public PutObjectResult putObject(PutObjectRequest putObjectRequest) throws OSSException, ClientException {
            return null;
        }
    }
}
