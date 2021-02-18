package com.bytelegend.game;

import java.io.File;

import static com.bytelegend.game.Environment.systemProperty;

/**
 * Triggered by daily cron job
 */
public class CIDailyDataGeneratorJob {
    private final FullImageGenerator fullyDataGenerator;
    private final OssClient ossClient;

    public static void main(String[] args) throws Exception {
        Environment environment = Environment.EnvironmentBuilder.builder()
                .setWorkspaceDir(new File(systemProperty("workspaceDir")))
                .setOssAccessKeyId(systemProperty("ossAccessKeyId"))
                .setOssAccessKeySecret(systemProperty("ossAccessKeySecret"))
                .build();

        new CIDailyDataGeneratorJob(environment).run();
    }

    CIDailyDataGeneratorJob(Environment environment) {
        this.fullyDataGenerator = new FullImageGenerator(environment);
        this.ossClient = environment.createOssClient();
    }

    void run() throws Exception {
        fullyDataGenerator.generate();
        ossClient.uploadBravePeopleImage();
    }
}

