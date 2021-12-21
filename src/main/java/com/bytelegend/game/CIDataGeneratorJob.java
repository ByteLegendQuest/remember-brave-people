package com.bytelegend.game;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.bytelegend.game.Constants.CI_BASE_REF;
import static com.bytelegend.game.Constants.DEFAULT_REPO_URL;
import static com.bytelegend.game.Constants.HEROES_JSON;
import static com.bytelegend.game.Constants.IMAGE_GRID_HEIGHT;
import static com.bytelegend.game.Constants.OBJECT_MAPPER;
import static com.bytelegend.game.Environment.systemProperty;
import static com.bytelegend.game.Utils.readSystemPropertiesFromArgs;
import static com.bytelegend.game.Utils.renameCurrentToPage;

/**
 * Triggered by player PR.
 * <p>
 * The job assumes the workspace directory contains two remotes:
 * 1. origin -> https://github.com/ByteLegendQuest/remember-brave-people
 * 2. [YourGitHubUsername] -> https://github.com/[YourGitHubUsername]/remember-brave-people
 * <p>
 * When the job starts, the workspace should be checked out to the PR branch.
 */
public class CIDataGeneratorJob {
    private final Environment environment;
    private final Git git;
    private final IncrementalImageGenerator incrementalImageGenerator;
    private final FullImageGenerator fullImageGenerator;
    private final JsonGenerator jsonGenerator;
    private final Uploader uploader;

    public static void main(String[] args) throws Exception {
        readSystemPropertiesFromArgs(args);
        Environment environment = Environment.EnvironmentBuilder.builder()
            .setWorkspaceDir(new File(systemProperty("workspaceDir")))
            .setHeadRef(systemProperty("headRef"))
            .setPrTitle(systemProperty("prTitle"))
            .setPrNumber(systemProperty("prNumber"))
            .setPlayerGitHubUsername(systemProperty("playerGitHubUsername"))
            .setRepoPullUrl(DEFAULT_REPO_URL)
            .setAccessKeyId(System.getProperty("accessKeyId", ""))
            .setAccessKeySecret(System.getProperty("accessKeySecret", ""))
            .setRepoPushUrl(System.getProperty("repoPushUrl", ""))
            .build();

        new CIDataGeneratorJob(environment).run();
    }

    public CIDataGeneratorJob(Environment environment) {
        this.environment = environment;
        this.git = environment.createGit();
        this.uploader = environment.createUploader();
        this.incrementalImageGenerator = environment.createIncrementalDataGenerator();
        this.fullImageGenerator = new FullImageGenerator(environment);
        this.jsonGenerator = environment.createJsonGenerator();
    }

    void run() throws Exception {
        // - Do sanityCheck to make sure the change is legitimate.
        // - Rebase PR branch to origin/main, resolve conflict with the best effort.
        //   - Upon failure, abort.
        // - Merge changes to origin/main.
        // - Generate new data based on latest version at GitHub.
        //   - If current page is full, generate a new page.
        // - Push.
        //   - Upon failure, abort.
        //   - Otherwise, upload generated data to GitHub.
        //
        // It's actually using GitHub as mutex to avoid race condition: only the job which successfully
        //   pushes can upload.
        TileDataDiff diff = sanityCheck();
        git.mergeToMain(diff);

        incrementalImageGenerator.generate(diff);
        TilesInfo tilesInfo = jsonGenerator.generate(diff);

        List<File> assets = determineAssetsToUpload(tilesInfo);

        git.push();
        uploader.uploadAssets(assets);
    }

    // - If current board is not full, upload:
    //   - heroes-current.png
    //   - heroes-{page}.png
    //   - heroes-current.json
    //   - heroes-{page}.json
    // Otherwise, refresh heroes-current.png and heroes-current.json, commit changes to `heroes.json`, and upload
    //   - heroes-current.png (blank)
    //   - heroes-{page+1}.png (blank)
    //   - heroes-current.json (blank)
    //   - heroes-{page+1}.json (blank)
    //   - heroes-{page}.png (full)
    //   - heroes-{page}.json (full)
    private List<File> determineAssetsToUpload(TilesInfo tilesInfo) throws Exception {
        List<File> assets = new ArrayList<>();
        File heroesCurrentImage = environment.getOutputHeroesCurrentImage();
        File heroesCurrentJson = environment.getOutputHeroesCurrentJson();
        File heroesPageImage = renameCurrentToPage(heroesCurrentImage, tilesInfo.getPage());
        File heroesPageJson = renameCurrentToPage(heroesCurrentJson, tilesInfo.getPage());

        assets.add(heroesCurrentImage);
        assets.add(heroesCurrentJson);
        assets.add(heroesPageImage);
        assets.add(heroesPageJson);

        if (tilesInfo.getTiles().size() != Constants.IMAGE_GRID_WIDTH * IMAGE_GRID_HEIGHT) {
            Files.copy(heroesCurrentImage.toPath(), heroesPageImage.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(heroesCurrentJson.toPath(), heroesPageJson.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            // it's full!
            File heroesNextPageImage = renameCurrentToPage(heroesCurrentImage, tilesInfo.getPage() + 1);
            File heroesNextPageJson = renameCurrentToPage(heroesCurrentJson, tilesInfo.getPage() + 1);

            Files.move(heroesCurrentImage.toPath(), heroesPageImage.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.move(heroesCurrentJson.toPath(), heroesCurrentJson.toPath(), StandardCopyOption.REPLACE_EXISTING);

            TilesInfo newTilesInfo = new TilesInfo();
            newTilesInfo.setPage(tilesInfo.getPage() + 1);

            Utils.writeString(heroesCurrentJson, OBJECT_MAPPER.writeValueAsString(newTilesInfo));
            fullImageGenerator.generate(Collections.emptyList());

            Files.copy(heroesCurrentImage.toPath(), heroesNextPageImage.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(heroesCurrentJson.toPath(), heroesNextPageJson.toPath(), StandardCopyOption.REPLACE_EXISTING);

            assets.add(heroesNextPageImage);
            assets.add(heroesNextPageJson);

            Utils.writeString(environment.getHeroesJson(), "[\n]\n");
            git.addCommit("Refresh heroes.json", HEROES_JSON);
        }
        return assets;
    }

    private TileDataDiff sanityCheck() throws Exception {
        String forkPoint = git.findForkPointTo(CI_BASE_REF);
        String oldJson = git.show(forkPoint, HEROES_JSON);
        String newJson = Utils.readString(environment.getHeroesJson());

        checkJsonFormatted(newJson);

        return new TileDataDiff(oldJson, newJson, environment.getPlayerGitHubUsername());
    }

    /**
     * We don't allow players to format the JSON.
     * The lines in the JSON file must be one of:
     * - Empty line.
     * - Square brackets. '[' or ']'.
     * - Entire object: "x": "y": "color": "username":
     */
    private void checkJsonFormatted(String newJson) {
        List<String> lines = Arrays.asList(newJson.split("\\n"));
        for (int i = 0; i < lines.size(); i++) {
            if (!isValidLine(lines.get(i))) {
                throw new IllegalStateException("Invalid line " + (i + 1) + ": " + lines.get(i)
                    + "\nNote that we don't allow you to format (prettify) the JSON.");
            }
        }
    }

    private boolean isValidLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        if ("[".equals(trimmed) || "]".equals(trimmed)) {
            return true;
        }
        return trimmed.contains("\"x\"") &&
            trimmed.contains("\"y\"") &&
            trimmed.contains("\"color\"") &&
            trimmed.contains("\"username\"");
    }
}
