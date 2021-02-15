package com.bytelegend.game;

import java.io.File;

import static com.bytelegend.game.Constants.DEFAULT_REPO_URL;
import static com.bytelegend.game.Environment.systemProperty;

public class DeveloperLocalDataGeneratorJob {
    private final Environment environment;
    private final FullImageGenerator fullyDataGenerator;
    private final IncrementalImageGenerator incrementalDataGenerator;
    private final Git git;

    public static void main(String[] args) throws Exception {
        new DeveloperLocalDataGeneratorJob(Environment.EnvironmentBuilder.builder()
                .setWorkspaceDir(new File(systemProperty("workspaceDir")))
                .setRepoPullUrl(DEFAULT_REPO_URL)
                .build()
        ).run();
    }

    DeveloperLocalDataGeneratorJob(Environment environment) {
        this.environment = environment;
        this.fullyDataGenerator = new FullImageGenerator(environment);
        this.incrementalDataGenerator = new IncrementalImageGenerator(environment);
        this.git = new Git(environment);
    }

    private String readOldJson() {
        try {
            git.fetchUpstream();
            String baseRef = git.findForkPointTo("upstream/master");
            return git.show(baseRef, Constants.BRAVE_PEOPLE_JSON);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    void run() throws Exception {
        String oldJson = readOldJson();
        if (oldJson == null) {
            System.err.println("Failed to run incrementally, fallback to fully generation;");
            fullyDataGenerator.generate();
        } else {
            String newJson = Utils.readString(environment.getBravePeopleJson());
            TileDataDiff diff = new TileDataDiff(oldJson, newJson, null);

            incrementalDataGenerator.generate(diff);
        }
    }
}
