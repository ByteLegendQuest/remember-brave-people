package com.bytelegend.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static com.bytelegend.game.Constants.BRAVE_PEOPLE_JSON;
import static com.bytelegend.game.TestUtils.assertTileWritten;
import static com.bytelegend.game.Utils.writeString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CIDailyDataGeneratorTest {
    @Mock
    OssClient ossClient;

    @Test
    public void canGenerateAll(@TempDir File dir) throws Exception {
        Environment environment = Environment.EnvironmentBuilder.builder()
                .setWorkspaceDir(dir)
                .build();
        Environment spiedEnvironment = spy(environment);
        doReturn(ossClient).when(spiedEnvironment).createOssClient();

        String json = "[\n" +
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#FFFFFF\"},\n" +
                "{\"username\":\"blindpirate\",\"x\":3,\"y\":3,\"color\":\"#0000ff\"}\n" +
                "]\n";
        writeString(dir, BRAVE_PEOPLE_JSON, json);

        new CIDailyDataGeneratorJob(spiedEnvironment).run();
        File outputImage = environment.getOutputBravePeopleImage();
        assertTrue(outputImage.isFile());
        assertTileWritten(outputImage, 1, 1, "rgba(0,0,0,255)");
        assertTileWritten(outputImage, 2, 2, "rgba(255,255,255,255)");
        assertTileWritten(outputImage, 3, 3, "rgba(0,0,255,255)");

        verify(ossClient).upload();
    }
}
