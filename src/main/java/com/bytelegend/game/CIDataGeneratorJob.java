package com.bytelegend.game;

import java.io.File;

import static com.bytelegend.game.Constants.BRAVE_PEOPLE_JSON;
import static com.bytelegend.game.Constants.CI_BASE_REF;
import static com.bytelegend.game.Constants.DEFAULT_REPO_URL;
import static com.bytelegend.game.Environment.systemProperty;
import static com.bytelegend.game.Utils.readSystemPropertiesFromArgs;

/**
 * Triggered by player PR.
 *
 * The job assumes the workspace directory contains two remotes:
 * 1. origin -> https://github.com/ByteLegendQuest/remember-brave-people
 * 2. [YourGitHubUsername] -> https://github.com/[YourGitHubUsername]/remember-brave-people
 *
 * When the job starts, the workspace should be checked out to the PR branch.
 */
public class CIDataGeneratorJob {
    private final Environment environment;
    private final Git git;
    private final OssClient ossClient;
    private final IncrementalDataGenerator incrementalDataGenerator;

    public static void main(String[] args) throws Exception {
        readSystemPropertiesFromArgs(args);
        Environment environment = Environment.EnvironmentBuilder.builder()
                .setWorkspaceDir(new File(systemProperty("workspaceDir")))
                .setHeadRef(systemProperty("headRef"))
                .setPrTitle(systemProperty("prTitle"))
                .setPrNumber(systemProperty("prNumber"))
                .setPlayerGitHubUsername(systemProperty("playerGitHubUsername"))
                .setOssAccessKeyId(System.getProperty("ossAccessKeyId", ""))
                .setOssAccessKeySecret(System.getProperty("ossAccessKeySecret", ""))
                .setRepoPullUrl(DEFAULT_REPO_URL)
                .setRepoPushUrl(System.getProperty("repoPushUrl", ""))
                .build();

        new CIDataGeneratorJob(environment).run();
    }

    public CIDataGeneratorJob(Environment environment) {
        this.environment = environment;
        this.git = environment.createGit();
        this.ossClient = environment.createOssClient();
        this.incrementalDataGenerator = environment.createIncrementalDataGenerator();
    }

    void run() throws Exception {
        // - Do sanityCheck to make sure the change is legitimate.
        // - Rebase PR branch to origin/master, resolve conflict with best effort.
        //   - Upon failure, abort.
        // - Merge (--squash) changes to origin/master.
        // - Generate new data based on latest version at OSS.
        // - Push.
        //   - Upon failure, abort.
        //   - Otherwise, upload generated data to OSS.
        //
        // It's actually using GitHub as mutex to avoid race condition: only the job which successfully
        //   pushes can update OSS.
        TileDataDiff diff = sanityCheck();
        git.mergeToMaster(diff);

        incrementalDataGenerator.generate(diff);

        git.push();
        ossClient.upload();
    }

    private TileDataDiff sanityCheck() throws Exception {
        String forkPoint = git.findForkPointTo(CI_BASE_REF);
        String oldJson = git.show(forkPoint, BRAVE_PEOPLE_JSON);
        String newJson = Utils.readString(environment.getBravePeopleJson());
        return new TileDataDiff(oldJson, newJson, environment.getPlayerGitHubUsername());
    }
}
