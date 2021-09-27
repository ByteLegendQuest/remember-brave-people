package com.bytelegend.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bytelegend.game.Constants.OBJECT_MAPPER;
import static com.bytelegend.game.Constants.OUTPUT_HEROES_CURRENT_JSON;
import static com.bytelegend.game.Constants.OUTPUT_HEROES_CURRENT_PNG;
import static com.bytelegend.game.TestUtils.assertExceptionWithMessage;
import static com.bytelegend.game.TestUtils.assertImageWritten;
import static com.bytelegend.game.Utils.parseHeroesCurrentJson;
import static com.bytelegend.game.Utils.readString;
import static com.bytelegend.game.Utils.writeString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CIDataGeneratorJobEmbeddedTest extends AbstractCIDataGeneratorJobTest {
    private static final String MOCK_HEROES_CURRENT_JSON_PATH = "build/heroes-current.mock.json";
    @Mock
    Uploader uploader;

    @Override
    protected void runJob(String player, String headRef) throws Exception {
        Environment environment = createEnvironment(workspace, player, headRef);
        new CIDataGeneratorJob(environment).run();
    }

    @Override
    protected void mockHeroesCurrentJson(File workspace, String json) throws Exception {
        new File(workspace, "build").mkdirs();
        writeString(workspace, MOCK_HEROES_CURRENT_JSON_PATH, json);
    }

    @Override
    protected void assertHeroesCurrentJson(Consumer<TilesInfo> consumer) throws Exception {
        consumer.accept(parseHeroesCurrentJson(readString(workspace, OUTPUT_HEROES_CURRENT_JSON)));
    }

    @Override
    protected void assertUpload(String... fileRelativePaths) {
        verify(uploader).uploadAssets(
            Stream.of(fileRelativePaths).map(it -> new File(workspace, it)).collect(Collectors.toList())
        );
    }

    private Environment createEnvironment(File workspace, String player, String headRef) {
        Environment environment = Environment.EnvironmentBuilder.builder()
            .setPrTitle("MyPullRequest")
            .setPrNumber("12345")
            .setWorkspaceDir(workspace)
            .setHeadRef(headRef)
            .setPlayerGitHubUsername(player)
            .setRepoPushUrl(upstream.getAbsolutePath())
            .setPublicHeroesCurrentJsonUrl(new File(workspace, MOCK_HEROES_CURRENT_JSON_PATH).toURI().toString())
            .build();
        Environment spiedEnvironment = spy(environment);
        doReturn(uploader).when(spiedEnvironment).createUploader();

        return spiedEnvironment;
    }

    @Test
    public void refreshImageAndJsonsIfFull() throws Exception {
        List<SimpleTile> tiles = new ArrayList<>(400);
        for (int i = 0; i < 399; ++i) {
            tiles.add(TestUtils.createTile("user-" + i, i / 20, i % 20, "#000000"));
        }
        commitChangesInUpstream(Utils.toFormattedJson(tiles));
        new Shell(fork).execSuccessfully("git", "pull");

        tiles.add(TestUtils.createTile("blindpirate", 19, 19, "#111111"));
        createPullRequest(workspace, fork, "blindpirate", Utils.toFormattedJson(tiles));

        TilesInfo oldTilesInfo = new TilesInfo();
        oldTilesInfo.setPage(2);
        oldTilesInfo.setTiles(tiles.stream().map(TestUtils::createTile).collect(Collectors.toList()));
        mockHeroesCurrentJson(workspace, OBJECT_MAPPER.writeValueAsString(oldTilesInfo));

        runJob("blindpirate", "blindpirate_my-branch");

        TilesInfo newTilesInfo = OBJECT_MAPPER.readValue(
            Utils.readString(workspace, OUTPUT_HEROES_CURRENT_JSON),
            TilesInfo.class);
        assertEquals(3, newTilesInfo.getPage());
        assertTrue(newTilesInfo.getTiles().isEmpty());

        TestUtils.readAllPixels(new File(workspace, OUTPUT_HEROES_CURRENT_PNG)).stream()
            .flatMap(List::stream)
            .forEach(rgba -> assertEquals(0, rgba.a));

        assertUpload(
            OUTPUT_HEROES_CURRENT_PNG,
            OUTPUT_HEROES_CURRENT_JSON,
            OUTPUT_HEROES_CURRENT_PNG.replace("-current", "-2"),
            OUTPUT_HEROES_CURRENT_JSON.replace("-current", "-2"),
            OUTPUT_HEROES_CURRENT_PNG.replace("-current", "-3"),
            OUTPUT_HEROES_CURRENT_JSON.replace("-current", "-3")
        );
    }

    @Test
    public void failIfTwoJobsRunSimultaneously() throws Exception {
        File fork2 = new File(tmpDir, "fork2");
        upstreamShell.execSuccessfully("git", "clone", upstream.getAbsolutePath(), fork2.getAbsolutePath());
        File workspace2 = new File(tmpDir, "clone2");
        upstreamShell.execSuccessfully("git", "clone", upstream.getAbsolutePath(), workspace2.getAbsolutePath());

        mockHeroesCurrentJson(workspace2, readString(workspace, MOCK_HEROES_CURRENT_JSON_PATH));
        createPullRequest(workspace2, fork2, "octocat", "[\n" +
            "{\"userid\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
            "{\"userid\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
            "{\"userid\":\"octocat\",\"x\":0,\"y\":0,\"color\":\"#FFFFFF\"}\n" +
            "]\n");

        createPullRequest(workspace, fork, "blindpirate",
            "[\n" +
                "{\"userid\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                "{\"userid\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
                "{\"userid\":\"blindpirate\",\"x\":3,\"y\":3,\"color\":\"#00FF00\"}\n" +
                "]\n");

        // fork pushes first and succeeds, fork2 pushes later and fails
        Environment environmentForFork = createEnvironment(workspace, "blindpirate", "blindpirate_my-branch");
        Environment environmentForFork2 = createEnvironment(workspace2, "octocat", "octocat_my-branch");
        doAnswer(invocation -> {
                Thread.sleep(5000);
                return upstream.getAbsolutePath();
            }
        ).when(environmentForFork2).getRepoPushUrl();

        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        Future<?> futureForFork = threadPool.submit(() -> {
            new CIDataGeneratorJob(environmentForFork).run();
            return null;
        });
        Future<?> futureForFork2 = threadPool.submit(() -> {
            new CIDataGeneratorJob(environmentForFork2).run();
            return null;
        });

        futureForFork.get();
        assertExceptionWithMessage("Push failed", futureForFork2::get);

        assertImageWritten(environmentForFork.getOutputHeroesCurrentImage(), 3, 3, new RGBA(0, 255, 0, 255));
        assertUpload(
            OUTPUT_HEROES_CURRENT_PNG,
            OUTPUT_HEROES_CURRENT_JSON,
            OUTPUT_HEROES_CURRENT_PNG.replace("-current", "-42"),
            OUTPUT_HEROES_CURRENT_JSON.replace("-current", "-42")
        );
        assertFinalJsonContains("blindpirate", "#00FF00");
        assertFinalJsonNotContains("octocat");
        assertLastCommitMessageContains("blindpirate");
    }
}
