package com.bytelegend.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;

import static com.bytelegend.game.Constants.BRAVE_PEOPLE_JSON;
import static com.bytelegend.game.Constants.DEFAULT_REPO_URL;
import static com.bytelegend.game.TestUtils.assertExceptionWithMessage;
import static com.bytelegend.game.TestUtils.assertTileWritten;
import static com.bytelegend.game.Utils.writeString;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeveloperLocalDataGeneratorJobTest extends AbstractDataGeneratorJobTest {
    @ParameterizedTest
    @ValueSource(strings = {"commit", "no commit"})
    public void generateIncrementallyIfForkPointFount(String scenario) throws Exception {
        String newJson = "[\n" +
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
                "{\"username\":\"blindpirate\",\"x\":3,\"y\":3,\"color\":\"#0000ff\"}\n" +
                "]\n";
        if ("commit".equals(scenario)) {
            commitChangesInFork(fork, "blindpirate", newJson);
        } else {
            writeString(fork, BRAVE_PEOPLE_JSON, newJson);
        }

        Environment environment = Environment.EnvironmentBuilder.builder()
                .setRepoPullUrl(upstream.getAbsolutePath())
                .setWorkspaceDir(fork)
                .build();
        new DeveloperLocalDataGeneratorJob(environment).run();

        File outputImage = environment.getOutputBravePeopleImage();
        assertTrue(outputImage.isFile());
        assertTileWritten(outputImage, 3, 3, "rgba(0,0,255,255)");
        assertFinalJsonNotContains("blindpirate", "#0000ff");
    }

    @ParameterizedTest
    @ValueSource(strings = {"commit", "no commit"})
    public void failIfConflictWithOthers(String scenario) throws Exception {
        String newJson = "[\n" +
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
                "{\"username\":\"blindpirate\",\"x\":2,\"y\":2,\"color\":\"#0000ff\"}\n" +
                "]\n";
        if ("commit".equals(scenario)) {
            commitChangesInFork(fork, "blindpirate", newJson);
        } else {
            writeString(fork, BRAVE_PEOPLE_JSON, newJson);
        }
        Environment environment = Environment.EnvironmentBuilder.builder()
                .setRepoPullUrl(upstream.getAbsolutePath())
                .setWorkspaceDir(fork)
                .build();
        assertExceptionWithMessage("Conflict: tile (2,2) already exists!", () -> {
            new DeveloperLocalDataGeneratorJob(environment).run();
        });
    }

    @Test
    public void generateFullyAsFallback(@TempDir File dir) throws Exception {
        String newJson = "[\n" +
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#FFFFFF\"},\n" +
                "{\"username\":\"blindpirate\",\"x\":3,\"y\":3,\"color\":\"#0000ff\"}\n" +
                "]\n";
        // Force git to fail
        new File(".git").createNewFile();
        writeString(dir, BRAVE_PEOPLE_JSON, newJson);
        Environment environment = Environment.EnvironmentBuilder.builder()
                .setRepoPullUrl(DEFAULT_REPO_URL)
                .setWorkspaceDir(dir)
                .build();
        new DeveloperLocalDataGeneratorJob(environment).run();
        File outputImage = environment.getOutputBravePeopleImage();
        assertTrue(outputImage.isFile());
        assertTileWritten(outputImage, 1, 1, "rgba(0,0,0,255)");
        assertTileWritten(outputImage, 2, 2, "rgba(255,255,255,255)");
        assertTileWritten(outputImage, 3, 3, "rgba(0,0,255,255)");
    }
}

