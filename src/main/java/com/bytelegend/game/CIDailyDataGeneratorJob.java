package com.bytelegend.game;

import java.io.File;

import static com.bytelegend.game.Environment.systemProperty;

/**
 * Triggered by daily cron job
 */
public class CIDailyDataGeneratorJob {
    private final FullDataGenerator fullyDataGenerator;
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
        this.fullyDataGenerator = new FullDataGenerator(environment);
        this.ossClient = environment.createOssClient();
    }

    void run() throws Exception {
        fullyDataGenerator.generate();
        ossClient.upload();
    }
}

