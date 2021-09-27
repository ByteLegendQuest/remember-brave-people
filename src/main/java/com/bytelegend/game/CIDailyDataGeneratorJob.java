package com.bytelegend.game;

import java.io.File;
import java.util.Collections;

import static com.bytelegend.game.Environment.systemProperty;

/**
 * Triggered by daily cron job
 */
public class CIDailyDataGeneratorJob {
    private final FullImageGenerator fullyDataGenerator;
    private final Uploader uploader;
    private final Environment environment;

    public static void main(String[] args) throws Exception {
        Environment environment = Environment.EnvironmentBuilder.builder()
            .setWorkspaceDir(new File(systemProperty("workspaceDir")))
            .setAccessKeyId(systemProperty("accessKeyId"))
            .setAccessKeySecret(systemProperty("accessKeySecret"))
            .build();

        new CIDailyDataGeneratorJob(environment).run();
    }

    CIDailyDataGeneratorJob(Environment environment) {
        this.environment = environment;
        this.fullyDataGenerator = new FullImageGenerator(environment);
        this.uploader = environment.createUploader();
    }

    void run() throws Exception {
        fullyDataGenerator.generate();
        uploader.uploadAssets(Collections.singletonList(environment.getOutputHeroesCurrentImage()));
    }
}

