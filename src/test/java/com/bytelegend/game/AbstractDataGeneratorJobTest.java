package com.bytelegend.game;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.nio.file.Files;

import static com.bytelegend.game.Constants.HEROES_JSON;
import static com.bytelegend.game.Utils.writeString;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class AbstractDataGeneratorJobTest {
    File tmpDir;

    // mock the remote repository
    File upstream;
    // mock the player's forked repository
    File fork;
    // mock the cloned repository, i.e. the workspace.
    // There are two remotes in it: 1. origin -> upstream, 2. [PlayerGitHubUsername] -> player's fork
    File workspace;

    Shell upstreamShell;
    Shell workspaceShell;

    @BeforeEach
    void setUp() throws Exception {
        // Don't use @TempDir, some files created in docker containers
        // can't be deleted in GitHub workflows
        tmpDir = Files.createTempDirectory("tmp").toFile();

        upstream = new File(tmpDir, "upstream");
        upstream.mkdirs();

        upstreamShell = new Shell(upstream);
        upstreamShell.execSuccessfully("git", "init", "--bare", "--initial-branch=main");

        workspace = new File(tmpDir, "clone");
        upstreamShell.execSuccessfully("git", "clone", upstream.getAbsolutePath(), workspace.getAbsolutePath());
        workspaceShell = new Shell(workspace);

        commitChangesInUpstream("[\n" +
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"}\n" +
                "]\n");

        fork = new File(tmpDir, "fork");
        upstreamShell.execSuccessfully("git", "clone", upstream.getAbsolutePath(), fork.getAbsolutePath());
    }

    @AfterEach
    void cleanUp() {
        FileUtils.deleteQuietly(tmpDir);
    }

    void assertFinalJsonNotContains(String... keywords) throws Exception {
        String json = upstreamShell.execSuccessfully("git", "show", "main:" + HEROES_JSON).stdout;
        for (String keyword : keywords) {
            assertThat(json, not(containsString(keyword)));
        }
    }

    void commitChangesInFork(File dir, String player, String newJson) throws Exception {
        Shell forkShell = new Shell(dir);
        forkShell.execSuccessfully("git", "checkout", "-b", "my-branch");
        writeString(dir, HEROES_JSON, newJson);
        forkShell.execSuccessfully("git", "commit", "-a", "-m", "Change from " + player);
    }

    void commitChangesInUpstream(String json) throws Exception {
        writeString(workspace, HEROES_JSON, json);
        writeString(workspace, ".gitignore", "build/");
        workspaceShell.execSuccessfully("git", "add", ".");
        workspaceShell.execSuccessfully("git", "commit", "-m", "Commit in upstream");
        workspaceShell.execSuccessfully("git", "push");
    }
}
