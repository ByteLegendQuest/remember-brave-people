package com.bytelegend.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;

import static com.bytelegend.game.Constants.HEROES_JSON;
import static com.bytelegend.game.Constants.DEFAULT_REPO_URL;
import static com.bytelegend.game.TestUtils.assertExceptionWithMessage;
import static com.bytelegend.game.TestUtils.assertImageWritten;
import static com.bytelegend.game.Utils.writeString;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeveloperLocalDataGeneratorJobTest extends AbstractDataGeneratorJobTest {
    @ParameterizedTest
    @ValueSource(strings = {"commit", "no commit"})
    public void generateIncrementallyIfForkPointFount(String scenario) throws Exception {
        String newJson = "[\n" +
            "{\"userid\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
            "{\"userid\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
            "{\"userid\":\"blindpirate\",\"x\":3,\"y\":3,\"color\":\"#0000ff\"}\n" +
            "]\n";
        if ("commit".equals(scenario)) {
            commitChangesInFork(fork, "blindpirate", newJson);
        } else {
            writeString(fork, HEROES_JSON, newJson);
        }

        Environment environment = Environment.EnvironmentBuilder.builder()
            .setRepoPullUrl(upstream.getAbsolutePath())
            .setWorkspaceDir(fork)
            .build();
        new DeveloperLocalDataGeneratorJob(environment).run();

        File outputImage = environment.getOutputHeroesCurrentImage();
        assertTrue(outputImage.isFile());
        assertImageWritten(outputImage, 3, 3, new RGBA(0, 0, 255, 255));
        assertFinalJsonNotContains("blindpirate", "#0000ff");
    }

    @ParameterizedTest
    @ValueSource(strings = {"commit", "no commit"})
    public void failIfConflictWithOthers(String scenario) throws Exception {
        String newJson = "[\n" +
            "{\"userid\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
            "{\"userid\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#222222\"},\n" +
            "{\"userid\":\"blindpirate\",\"x\":2,\"y\":2,\"color\":\"#0000ff\"}\n" +
            "]\n";
        if ("commit".equals(scenario)) {
            commitChangesInFork(fork, "blindpirate", newJson);
        } else {
            writeString(fork, HEROES_JSON, newJson);
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
            "{\"userid\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
            "{\"userid\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#FFFFFF\"},\n" +
            "{\"userid\":\"blindpirate\",\"x\":3,\"y\":3,\"color\":\"#0000ff\"}\n" +
            "]\n";
        writeString(dir, HEROES_JSON, newJson);
        Environment environment = Environment.EnvironmentBuilder.builder()
            .setRepoPullUrl(DEFAULT_REPO_URL)
            .setWorkspaceDir(dir)
            .build();
        new DeveloperLocalDataGeneratorJob(environment).run();
        File outputImage = environment.getOutputHeroesCurrentImage();
        assertTrue(outputImage.isFile());
        assertImageWritten(outputImage, 1, 1, new RGBA(0, 0, 0, 255));
        assertImageWritten(outputImage, 2, 2, new RGBA(255, 255, 255, 255));
        assertImageWritten(outputImage, 3, 3, new RGBA(0, 0, 255, 255));
    }
}

