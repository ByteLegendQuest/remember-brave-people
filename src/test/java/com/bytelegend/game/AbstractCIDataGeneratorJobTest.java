package com.bytelegend.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.bytelegend.game.Constants.HEROES_JSON;
import static com.bytelegend.game.Constants.OBJECT_MAPPER;
import static com.bytelegend.game.Constants.OUTPUT_HEROES_CURRENT_JSON;
import static com.bytelegend.game.Constants.OUTPUT_HEROES_CURRENT_PNG;
import static com.bytelegend.game.TestUtils.assertExceptionWithMessage;
import static com.bytelegend.game.TestUtils.assertImageWritten;
import static com.bytelegend.game.TestUtils.isClose;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractCIDataGeneratorJobTest extends AbstractDataGeneratorJobTest {
    protected abstract void runJob(String player, String headRef) throws Exception;

    protected abstract void mockHeroesCurrentJson(File workspace, String json) throws Exception;

    protected abstract void assertHeroesCurrentJson(Consumer<TilesInfo> consumer) throws Exception;

    protected abstract void assertUpload(String... fileRelativePaths);

    @Test
    public void playerCanAddTile() throws Exception {
        createPullRequest(workspace, fork, "blindpirate",
            "[" +
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"}," +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"}," +
                "{\"username\":\"blindpirate\",\"x\":0,\"y\":0,\"color\":\"#ff0000\"}" +
                "]");

        runJob("blindpirate", "blindpirate_my-branch");

        assertImageWritten(getOutputHeroesCurrentImage(), 0, 0, new RGBA(255, 0, 0, 255));
        assertUpload(
            OUTPUT_HEROES_CURRENT_PNG,
            OUTPUT_HEROES_CURRENT_JSON,
            OUTPUT_HEROES_CURRENT_PNG.replace("-current", "-42"),
            OUTPUT_HEROES_CURRENT_JSON.replace("-current", "-42")
        );
        assertHeroesCurrentJson(tiles -> {
                assertEquals(3, tiles.getTiles().size());
                AllInfoTile addedTile = tiles.getTiles().stream().filter(it -> it.getUsername().equals("blindpirate"))
                    .findFirst()
                    .get();
                assertNotNull(addedTile.getChangedAt());
                assertNotNull(addedTile.getCreatedAt());
                assertTrue(isClose(Instant.now(), addedTile.getChangedAt()));
            }
        );
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

        assertImageWritten(getOutputHeroesCurrentImage(), 1, 1, new RGBA(255, 255, 255, 255));
        assertUpload(
            OUTPUT_HEROES_CURRENT_PNG,
            OUTPUT_HEROES_CURRENT_JSON,
            OUTPUT_HEROES_CURRENT_PNG.replace("-current", "-42"),
            OUTPUT_HEROES_CURRENT_JSON.replace("-current", "-42")
        );
        assertHeroesCurrentJson(tiles -> {
                assertEquals(2, tiles.getTiles().size());
                AllInfoTile changedTile = tiles.getTiles().stream().filter(it -> it.getUsername().equals("ByteLegendBot"))
                    .findFirst()
                    .get();
                assertEquals(1, changedTile.getX());
                assertEquals(1, changedTile.getY());
                assertEquals("#FFFFFF", changedTile.getColor());
                assertNotNull(changedTile.getChangedAt());
                assertNotNull(changedTile.getCreatedAt());
                assertTrue(isClose(Instant.now(), changedTile.getChangedAt()));
                assertFalse(isClose(changedTile.getChangedAt(), changedTile.getCreatedAt()));
            }
        );
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

        assertImageWritten(getOutputHeroesCurrentImage(), 1, 1, new RGBA(255, 255, 255, 255));
        assertUpload(
            OUTPUT_HEROES_CURRENT_PNG,
            OUTPUT_HEROES_CURRENT_JSON,
            OUTPUT_HEROES_CURRENT_PNG.replace("-current", "-42"),
            OUTPUT_HEROES_CURRENT_JSON.replace("-current", "-42")
        );
        assertFinalJsonContains("ByteLegendBot", "#FFFFFF");
        assertFinalJsonNotContains("#000000");
        assertLastCommitMessageContains("ByteLegendBot");
    }

    // https://github.com/ByteLegendQuest/remember-brave-people/pull/298/files
    @Test
    public void failIfPlayerAddsMoreThanOneTiles() throws Exception {
        createPullRequest(workspace, fork, "ByteLegendBot",
            "[\n" +
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":2,\"color\":\"#000000\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"}\n" +
                "]\n");

        assertExceptionWithMessage("Duplicate username: torvalds", () ->
            runJob("ByteLegendBot", "ByteLegendBot_my-branch")
        );
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
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
                "{\"username\":\"octocat\",\"x\":0,\"y\":0,\"color\":\"#00FFFF\"}\n" +
                "]\n");
        } else {
            // ByteLegendBot's color is changed.
            commitChangesInUpstream("[\n" +
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#111111\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"}\n" +
                "]\n");
        }
        createPullRequest(workspace, fork, "blindpirate",
            "[\n" +
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
                "{\"username\":\"blindpirate\",\"x\":2,\"y\":1,\"color\":\"#FFFFFF\"}\n" +
                "]\n");

        runJob("blindpirate", "blindpirate_my-branch");

        assertImageWritten(getOutputHeroesCurrentImage(), 2, 1, new RGBA(255, 255, 255, 255));
        assertUpload(
            OUTPUT_HEROES_CURRENT_PNG,
            OUTPUT_HEROES_CURRENT_JSON,
            OUTPUT_HEROES_CURRENT_PNG.replace("-current", "-42"),
            OUTPUT_HEROES_CURRENT_JSON.replace("-current", "-42")
        );
        assertHeroesCurrentJson(tiles -> {
                if ("conflicts".equals(scenario)) {
                    assertEquals(Arrays.asList("ByteLegendBot", "torvalds", "octocat", "blindpirate"),
                        tiles.getTiles().stream().map(SimpleTile::getUsername).collect(Collectors.toList())
                    );
                    assertEquals(Arrays.asList(1, 2, 0, 2),
                        tiles.getTiles().stream().map(SimpleTile::getX).collect(Collectors.toList())
                    );
                    assertEquals(Arrays.asList(1, 2, 0, 1),
                        tiles.getTiles().stream().map(SimpleTile::getY).collect(Collectors.toList())
                    );
                    assertEquals(Arrays.asList("#000000", "#222222", "#00FFFF", "#FFFFFF"),
                        tiles.getTiles().stream().map(SimpleTile::getColor).collect(Collectors.toList())
                    );
                    assertTrue(isClose(Instant.now(), tiles.getTiles().get(3).getCreatedAt()));
                    assertTrue(isClose(Instant.now(), tiles.getTiles().get(3).getCreatedAt()));
                    assertTrue(isClose(Instant.now(), tiles.getTiles().get(3).getChangedAt()));
                } else {
                    assertEquals(Arrays.asList("ByteLegendBot", "torvalds", "blindpirate"),
                        tiles.getTiles().stream().map(SimpleTile::getUsername).collect(Collectors.toList())
                    );
                    assertEquals(Arrays.asList(1, 2, 2),
                        tiles.getTiles().stream().map(SimpleTile::getX).collect(Collectors.toList())
                    );
                    assertEquals(Arrays.asList(1, 2, 1),
                        tiles.getTiles().stream().map(SimpleTile::getY).collect(Collectors.toList())
                    );
                    assertEquals(Arrays.asList("#111111", "#222222", "#FFFFFF"),
                        tiles.getTiles().stream().map(SimpleTile::getColor).collect(Collectors.toList())
                    );
                    assertTrue(isClose(Instant.now(), tiles.getTiles().get(2).getCreatedAt()));
                    assertTrue(isClose(Instant.now(), tiles.getTiles().get(2).getChangedAt()));
                }
                assertTilesWithTimestamp(tiles.getTiles());
            }
        );
        assertLastCommitMessageContains("blindpirate");
    }

    private void assertTilesWithTimestamp(List<AllInfoTile> tiles) {
        tiles.forEach(it -> {
            assertNotNull(it.getCreatedAt());
            assertNotNull(it.getChangedAt());
        });
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

    @Override
    void commitChangesInUpstream(String json) throws Exception {
        super.commitChangesInUpstream(json);
        List<SimpleTile> simpleTiles = Utils.parseSimpleTiles(json);
        List<AllInfoTile> allInfoTiles = simpleTiles.stream()
            .map(tile -> {
                AllInfoTile allInfoTile = AllInfoTile.fromSimpleTile(tile);
                allInfoTile.setCreatedAt(Instant.parse("2021-02-14T00:00:00.00Z"));
                allInfoTile.setChangedAt(Instant.parse("2021-02-14T00:00:00.00Z"));
                return allInfoTile;
            }).collect(toList());
        TilesInfo tilesInfo = new TilesInfo();
        tilesInfo.setPage(42);
        tilesInfo.setTiles(allInfoTiles);
        mockHeroesCurrentJson(workspace, OBJECT_MAPPER.writeValueAsString(tilesInfo));
    }

    void assertFinalJsonContains(String... keywords) throws Exception {
        String json = upstreamShell.execSuccessfully("git", "show", "main:" + HEROES_JSON).stdout;
        for (String keyword : keywords) {
            assertThat(json, containsString(keyword));
        }
    }

    void assertLastCommitMessageContains(String player) throws Exception {
        String commit = upstreamShell.execSuccessfully("git", "log", "--format=%B", "-n", "1", "main").stdout;
        assertThat(commit, containsString("MyPullRequest (#12345)"));
        assertThat(commit, containsString("Thanks to @" + player + "'s contribution"));
        assertThat(commit, containsString("Co-authored-by: "));
    }

    void createPullRequest(File workspaceDir, File forkRepo, String player, String heroesJson) throws Exception {
        commitChangesInFork(forkRepo, player, heroesJson);
        syncForkInWorkspace(workspaceDir, player, forkRepo);
    }

    void syncForkInWorkspace(File workspaceDir, String player, File forkRepo) throws Exception {
        Shell sh = new Shell(workspaceDir);
        sh.execSuccessfully("git", "remote", "add", player, forkRepo.getAbsolutePath());
        sh.execSuccessfully("git", "fetch", player);
        sh.execSuccessfully("git", "checkout", "-b", player + "_my-branch", player + "/my-branch");
    }

    File getOutputHeroesCurrentImage() {
        return new File(workspace, OUTPUT_HEROES_CURRENT_PNG);
    }
}
