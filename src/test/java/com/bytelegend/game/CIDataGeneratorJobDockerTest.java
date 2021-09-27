package com.bytelegend.game;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.util.function.Consumer;

public class CIDataGeneratorJobDockerTest extends AbstractCIDataGeneratorJobTest {
    @BeforeEach
    public void checkDocker() {
        Assumptions.assumeTrue(Boolean.getBoolean("includeDockerTest"));
    }

    @Override
    protected void runJob(String player, String headRef) throws Exception {
        ExecResult result = workspaceShell.exec("docker", "run",
            "-v", workspace.getAbsolutePath() + ":/workspace",
            "-v", upstream.getAbsolutePath() + ":/upstream",
            "blindpirate/remember-brave-people",
            "-DworkspaceDir=/workspace",
            "-DplayerGitHubUsername=" + player,
            "-DprTitle=MyPullRequest",
            "-DprNumber=12345",
            "-DaccessKeyId=",
            "-DaccessKeySecret=",
            "-DheadRef=" + headRef,
            "-DrepoPushUrl=/upstream"
        );
        if (result.exitValue != 0) {
            throw new IllegalStateException(result.getOutput());
        }
    }

    @Override
    protected void mockHeroesCurrentJson(File workspace, String json) {
    }

    @Override
    protected void assertHeroesCurrentJson(Consumer<TilesInfo> consumer) {
    }

    @Override
    protected void assertUpload(String... fileRelativePaths) {
    }
}
