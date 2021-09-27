package com.bytelegend.game;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import static com.bytelegend.game.Environment.systemProperty;
import static com.bytelegend.game.Utils.parseHeroesCurrentJson;
import static com.bytelegend.game.Utils.readString;

/**
 * Triggered by daily cron job
 */
public class CIDailyDataGeneratorJob {
    private final FullImageGenerator fullyDataGenerator;
    private final Uploader uploader;
    private final Downloader downloader;
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
        this.downloader = new Downloader(environment);
    }

    void run() throws Exception {
        downloader.download(environment.getPublicHeroesCurrentJsonUrl(), environment.getInputHeroesCurrentJson());
        TilesInfo tilesInfo = parseHeroesCurrentJson(readString(environment.getInputHeroesCurrentJson()));

        File currentImage = environment.getOutputHeroesCurrentImage();
        File pageImage = Utils.renameCurrentToPage(currentImage, tilesInfo.getPage());

        fullyDataGenerator.generate();
        Files.copy(currentImage.toPath(), pageImage.toPath(), StandardCopyOption.REPLACE_EXISTING);

        uploader.uploadAssets(Arrays.asList(currentImage, pageImage));
    }
}

