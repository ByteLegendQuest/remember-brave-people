package com.bytelegend.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.bytelegend.game.Constants.OUTPUT_BRAVE_PEOPLE_ALL_JSON;
import static com.bytelegend.game.TestUtils.assertExceptionWithMessage;
import static com.bytelegend.game.TestUtils.assertImageWritten;
import static com.bytelegend.game.Utils.parseAllInfoTiles;
import static com.bytelegend.game.Utils.readString;
import static com.bytelegend.game.Utils.writeString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CIDataGeneratorJobEmbeddedTest extends AbstractCIDataGeneratorJobTest {
    private static final String MOCK_BRAVE_PEOPLE_ALL_JSON_PATH = "build/brave-people-all.mock.json";
    @Mock
    Uploader uploader;

    @Override
    protected void runJob(String player, String headRef) throws Exception {
        Environment environment = createEnvironment(workspace, player, headRef);
        new CIDataGeneratorJob(environment).run();
    }

    @Override
    protected void mockBravePeopleAllJson(File workspace, String json) throws Exception {
        new File(workspace, "build").mkdirs();
        writeString(workspace, MOCK_BRAVE_PEOPLE_ALL_JSON_PATH, json);
    }

    @Override
    protected void assertBravePeopleAllJson(Consumer<List<AllInfoTile>> consumer) throws Exception {
        consumer.accept(parseAllInfoTiles(readString(workspace, OUTPUT_BRAVE_PEOPLE_ALL_JSON)));
    }

    @Override
    protected void assertUpload() {
        verify(uploader).uploadBravePeopleImage();
        verify(uploader).uploadBravePeopleAllJson();
    }

    private Environment createEnvironment(File workspace, String player, String headRef) {
        Environment environment = Environment.EnvironmentBuilder.builder()
                .setPrTitle("MyPullRequest")
                .setPrNumber("12345")
                .setWorkspaceDir(workspace)
                .setHeadRef(headRef)
                .setPlayerGitHubUsername(player)
                .setRepoPushUrl(upstream.getAbsolutePath())
                .setPublicBravePeopleAllJsonUrl(new File(workspace, MOCK_BRAVE_PEOPLE_ALL_JSON_PATH).toURI().toString())
                .build();
        Environment spiedEnvironment = spy(environment);
        doReturn(uploader).when(spiedEnvironment).createUploader();

        return spiedEnvironment;
    }

    @Test
    public void failIfTwoJobsRunSimultaneously() throws Exception {
        File fork2 = new File(tmpDir, "fork2");
        upstreamShell.execSuccessfully("git", "clone", upstream.getAbsolutePath(), fork2.getAbsolutePath());
        File workspace2 = new File(tmpDir, "clone2");
        upstreamShell.execSuccessfully("git", "clone", upstream.getAbsolutePath(), workspace2.getAbsolutePath());

        mockBravePeopleAllJson(workspace2, readString(workspace, MOCK_BRAVE_PEOPLE_ALL_JSON_PATH));
        createPullRequest(workspace2, fork2, "octocat", "[\n" +
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
                "{\"username\":\"octocat\",\"x\":0,\"y\":0,\"color\":\"#FFFFFF\"}\n" +
                "]\n");

        createPullRequest(workspace, fork, "blindpirate",
                "[\n" +
                        "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                        "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
                        "{\"username\":\"blindpirate\",\"x\":3,\"y\":3,\"color\":\"#00FF00\"}\n" +
                        "]\n");

        // fork pushes first and succeeds, fork2 pushes later and fails
        Environment environmentForFork = createEnvironment(workspace, "blindpirate", "blindpirate_my-branch");
        Environment environmentForFork2 = createEnvironment(workspace2, "octocat", "octocat_my-branch");
        doAnswer(invocation -> {
                    Thread.sleep(2000);
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

        assertImageWritten(environmentForFork.getOutputBravePeopleImage(), 3, 3, "rgba(0,255,0,255)");
        assertUpload();
        assertFinalJsonContains("blindpirate", "#00FF00");
        assertFinalJsonNotContains("octocat");
        assertLastCommitMessageContains("blindpirate");
    }
}
