package com.bytelegend.game;

import java.io.File;

import static com.bytelegend.game.Constants.BRAVE_PEOPLE_JSON;
import static com.bytelegend.game.Constants.INPUT_BRAVE_PEOPLE_PNG;
import static com.bytelegend.game.Constants.OUTPUT_BRAVE_PEOPLE_PNG;

class Environment {
    private final File workspaceDir;
    /**
     * Pull request's head ref so we can merge it.
     * Usually set to a local branch [playerUsername]_[PR branch], e.g. alice_my-branch
     */
    private final String headRef;
    private final String prNumber;
    private final String prTitle;
    private final String playerGitHubUsername;
    private final String ossAccessKeyId;
    private final String ossAccessKeySecret;
    private final String repoPullUrl;
    private final String repoPushUrl;

    static String systemProperty(String name) {
        String value = System.getProperty(name);
        if (value == null) {
            throw new IllegalArgumentException(name + " must be set!");
        }
        return value;
    }

    private Environment(
            File workspaceDir,
            String headRef,
            String prNumber,
            String prTitle,
            String playerGitHubUsername,
            String ossAccessKeyId,
            String ossAccessKeySecret,
            String repoPullUrl,
            String repoPushUrl
    ) {
        this.workspaceDir = workspaceDir;
        this.headRef = headRef;
        this.prNumber = prNumber;
        this.prTitle = prTitle;
        this.playerGitHubUsername = playerGitHubUsername;
        this.ossAccessKeyId = ossAccessKeyId;
        this.ossAccessKeySecret = ossAccessKeySecret;
        this.repoPullUrl = repoPullUrl;
        this.repoPushUrl = repoPushUrl;
    }

    File getWorkspaceDir() {
        return workspaceDir;
    }

    String getHeadRef() {
        return headRef;
    }

    String getPrNumber() {
        return prNumber;
    }

    String getPrTitle() {
        return prTitle;
    }

    String getPlayerGitHubUsername() {
        return playerGitHubUsername;
    }

    String getOssAccessKeyId() {
        return ossAccessKeyId;
    }

    String getOssAccessKeySecret() {
        return ossAccessKeySecret;
    }

    File getInputBravePeopleImage() {
        return new File(workspaceDir, INPUT_BRAVE_PEOPLE_PNG);
    }

    File getOutputBravePeopleImage() {
        return new File(workspaceDir, OUTPUT_BRAVE_PEOPLE_PNG);
    }

    File getBravePeopleJson() {
        return new File(workspaceDir, BRAVE_PEOPLE_JSON);
    }

    Git createGit() {
        return new Git(this);
    }

    OssClient createOssClient() {
        return new OssClient(this);
    }

    IncrementalDataGenerator createIncrementalDataGenerator() {
        return new IncrementalDataGenerator(this);
    }

    String getRepoPushUrl() {
        return repoPushUrl;
    }

    String getRepoPullUrl() {
        return repoPullUrl;
    }

    static final class EnvironmentBuilder {
        private File workspaceDir;
        private String headRef;
        private String prNumber;
        private String prTitle;
        private String playerGitHubUsername;
        private String ossAccessKeyId;
        private String ossAccessKeySecret;
        private String repoPullUrl;
        private String repoPushUrl;

        private EnvironmentBuilder() {
        }

        static EnvironmentBuilder builder() {
            return new EnvironmentBuilder();
        }

        EnvironmentBuilder setWorkspaceDir(File workspaceDir) {
            this.workspaceDir = workspaceDir;
            return this;
        }

        EnvironmentBuilder setHeadRef(String headRef) {
            this.headRef = headRef;
            return this;
        }

        EnvironmentBuilder setPrNumber(String prNumber) {
            this.prNumber = prNumber;
            return this;
        }

        EnvironmentBuilder setPrTitle(String prTitle) {
            this.prTitle = prTitle;
            return this;
        }

        EnvironmentBuilder setPlayerGitHubUsername(String playerGitHubUsername) {
            this.playerGitHubUsername = playerGitHubUsername;
            return this;
        }

        EnvironmentBuilder setRepoPullUrl(String repoPullUrl) {
            this.repoPullUrl = repoPullUrl;
            return this;
        }

        EnvironmentBuilder setRepoPushUrl(String repoPushUrl) {
            this.repoPushUrl = repoPushUrl;
            return this;
        }

        EnvironmentBuilder setOssAccessKeyId(String ossAccessKeyId) {
            this.ossAccessKeyId = ossAccessKeyId;
            return this;
        }

        EnvironmentBuilder setOssAccessKeySecret(String ossAccessKeySecret) {
            this.ossAccessKeySecret = ossAccessKeySecret;
            return this;
        }

        Environment build() {
            return new Environment(
                    workspaceDir,
                    headRef,
                    prNumber,
                    prTitle,
                    playerGitHubUsername,
                    ossAccessKeyId,
                    ossAccessKeySecret,
                    repoPullUrl,
                    repoPushUrl
            );
        }
    }
}
