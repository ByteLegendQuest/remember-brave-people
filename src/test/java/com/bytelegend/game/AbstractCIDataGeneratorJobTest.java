package com.bytelegend.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Files;

import static com.bytelegend.game.Constants.BRAVE_PEOPLE_JSON;
import static com.bytelegend.game.Constants.OUTPUT_BRAVE_PEOPLE_PNG;
import static com.bytelegend.game.TestUtils.assertExceptionWithMessage;
import static com.bytelegend.game.TestUtils.assertTileWritten;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractCIDataGeneratorJobTest extends AbstractDataGeneratorJobTest {
    protected abstract void runJob(String player, String headRef) throws Exception;
    protected abstract void verifyOssUpload();

    @Test
    public void playerCanAddTile() throws Exception {
        createPullRequest(workspace, fork, "blindpirate",
                "[" +
                        "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"}," +
                        "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"}," +
                        "{\"username\":\"blindpirate\",\"x\":0,\"y\":0,\"color\":\"#ff0000\"}" +
                        "]");

        runJob("blindpirate", "blindpirate_my-branch");

        assertTileWritten(getOutputBravePeopleImage(), 0, 0, "rgba(255,0,0,255)");
        verifyOssUpload();
        assertFinalJsonContains("blindpirate");
        assertLastCommitMessageContains("blindpirate");
    }


    @Test
    public void playerCanModifyTileColor() throws Exception {
        createPullRequest(workspace, fork, "ByteLegendBot",
                "[\n" +
                        "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#FFFFFF\"},\n" +
                        "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"}\n" +
                        "]\n");
        runJob("ByteLegendBot", "ByteLegendBot_my-branch");

        assertTileWritten(getOutputBravePeopleImage(), 1, 1, "rgba(255,255,255,255)");
        verifyOssUpload();
        assertFinalJsonContains("ByteLegendBot", "#FFFFFF");
        assertFinalJsonNotContains("#000000");
        assertLastCommitMessageContains("ByteLegendBot");
    }

    @Test
    public void playerCanDoEmptyChange() throws Exception {
        createPullRequest(workspace, fork, "ByteLegendBot",
                "[\n" +
                        "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#FFFFFF\"},\n " + // an extra space
                        "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"}\n" +
                        "]\n");
        runJob("ByteLegendBot", "ByteLegendBot_my-branch");

        assertTileWritten(getOutputBravePeopleImage(), 1, 1, "rgba(255,255,255,255)");
        verifyOssUpload();
        assertFinalJsonContains("ByteLegendBot", "#FFFFFF");
        assertFinalJsonNotContains("#000000");
        assertLastCommitMessageContains("ByteLegendBot");
    }

    @Test
    public void failIfPlayerChangeLocation() throws Exception {
        createPullRequest(workspace, fork, "ByteLegendBot",
                "[\n" +
                        "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":2,\"color\":\"#000000\"},\n" +
                        "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"}\n" +
                        "]\n");

        assertExceptionWithMessage("You are not allowed to change tile's location", () ->
                runJob("ByteLegendBot", "ByteLegendBot_my-branch")
        );
    }

    @Test
    public void failIfPlayerModifyOtherOnesTile() throws Exception {
        createPullRequest(workspace, fork, "ByteLegendBot",
                "[\n" +
                        "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                        "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#1a2b3c\"}\n" +
                        "]\n");

        assertExceptionWithMessage("You are not allowed to change other one's tile", () ->
                runJob("ByteLegendBot", "ByteLegendBot_my-branch")
        );
    }

    @Test
    public void failIfPlayerChangeIsStaleAndLocationConflictsWithOtherPeople() throws Exception {
        commitChangesInUpstream("[\n" +
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
                "{\"username\":\"octocat\",\"x\":3,\"y\":3,\"color\":\"#FFFFFF\"}]\n");
        createPullRequest(workspace, fork, "blindpirate",
                "[\n" +
                        "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                        "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
                        "{\"username\":\"blindpirate\",\"x\":3,\"y\":3,\"color\":\"#FFFFFF\"}]\n");

        assertExceptionWithMessage("Your change conflicts with other one's change, you need to sync upstream repository", () ->
                runJob("blindpirate", "blindpirate_my-branch")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"conflicts", "no conflicts"})
    public void playerCanAddTileFromStaleBranchWhen(String scenario) throws Exception {
        if ("conflicts".equals(scenario)) {
            commitChangesInUpstream("[\n" +
                    "{\"username\":\"octocat\",\"x\":0,\"y\":0,\"color\":\"#FFFFFF\"},\n" +
                    "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                    "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"}\n" +
                    "]\n");
        } else {
            commitChangesInUpstream("[\n" +
                    "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                    "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
                    "{\"username\":\"octocat\",\"x\":0,\"y\":0,\"color\":\"#FFFFFF\"}\n" +
                    "]\n");
        }
        createPullRequest(workspace, fork, "blindpirate",
                "[\n" +
                        "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                        "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
                        "{\"username\":\"blindpirate\",\"x\":3,\"y\":3,\"color\":\"#00FF00\"}\n" +
                        "]\n");

        runJob("blindpirate", "blindpirate_my-branch");

        assertTileWritten(getOutputBravePeopleImage(), 3, 3, "rgba(0,255,0,255)");
        verifyOssUpload();
        // pretty printed
        assertTrue(Files.readAllLines(new File(workspace, BRAVE_PEOPLE_JSON).toPath()).size() > 3);
        assertFinalJsonContains("blindpirate", "octocat", "#FFFFFF", "#00FF00");
        assertLastCommitMessageContains("blindpirate");
    }

    @Test
    public void failIfPlayerRemoveTile() throws Exception {
        createPullRequest(workspace, fork, "ByteLegendBot",
                "[" +
                        "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"}" +
                        "]");

        assertExceptionWithMessage("You are not allowed to remove tile", () ->
                runJob("ByteLegendBot", "ByteLegendBot_my-branch")
        );
    }

    void assertFinalJsonContains(String... keywords) throws Exception {
        String json = upstreamShell.execSuccessfully("git", "show", "master:" + BRAVE_PEOPLE_JSON).stdout;
        for (String keyword : keywords) {
            assertThat(json, containsString(keyword));
        }
    }

    void assertLastCommitMessageContains(String player) throws Exception {
        String commit = upstreamShell.execSuccessfully("git", "log", "--format=%B", "-n", "1", "master").stdout;
        assertThat(commit, containsString("MyPullRequest (#12345)"));
        assertThat(commit, containsString("Thanks to @" + player + "'s contribution"));
    }


    void createPullRequest(File workspaceDir, File forkRepo, String player, String bravePeopleJson) throws Exception {
        commitChangesInFork(forkRepo, player, bravePeopleJson);
        syncForkInWorkspace(workspaceDir, player, forkRepo);
    }

    void syncForkInWorkspace(File workspaceDir, String player, File forkRepo) throws Exception {
        Shell sh = new Shell(workspaceDir);
        sh.execSuccessfully("git", "remote", "add", player, forkRepo.getAbsolutePath());
        sh.execSuccessfully("git", "fetch", player);
        sh.execSuccessfully("git", "checkout", "-b", player + "_my-branch", player + "/my-branch");
    }

    File getOutputBravePeopleImage() {
        return new File(workspace, OUTPUT_BRAVE_PEOPLE_PNG);
    }
}
