package com.bytelegend.game;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Properties;

import static com.bytelegend.game.Constants.BRAVE_PEOPLE_JSON;
import static com.bytelegend.game.TestUtils.assertImageWritten;
import static com.bytelegend.game.Utils.writeString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CIDailyDataGeneratorTest {
    @Mock
    OssClient ossClient;

    @TempDir
    File dir;

    @ParameterizedTest
    @ValueSource(strings = {"with main", "without main"})
    public void canGenerateAll(String scenario) throws Exception {
        String json = "[\n" +
                "{\"username\":\"ByteLegendBot\",\"x\":1,\"y\":1,\"color\":\"#000000\"},\n" +
                "{\"username\":\"torvalds\",\"x\":2,\"y\":2,\"color\":\"#FFFFFF\"},\n" +
                "{\"username\":\"blindpirate\",\"x\":3,\"y\":3,\"color\":\"#0000ff\"}\n" +
                "]\n";
        writeString(dir, BRAVE_PEOPLE_JSON, json);

        if ("with main".equals(scenario)) {
            Properties originalProperties = System.getProperties();
            try {
                System.setProperty("workspaceDir", dir.getAbsolutePath());
                System.setProperty("ossAccessKeyId", "mock");
                System.setProperty("ossAccessKeySecret", "mock");

                CIDailyDataGeneratorJob.main(new String[0]);
            } finally {
                System.setProperties(originalProperties);
            }
        } else {
            Environment environment = Environment.EnvironmentBuilder.builder()
                    .setWorkspaceDir(dir)
                    .build();
            Environment spiedEnvironment = spy(environment);
            doReturn(ossClient).when(spiedEnvironment).createOssClient();
            new CIDailyDataGeneratorJob(spiedEnvironment).run();

            verify(ossClient).upload();
        }

        File outputImage = new File(dir, Constants.OUTPUT_BRAVE_PEOPLE_PNG);
        assertTrue(outputImage.isFile());
        assertImageWritten(outputImage, 1, 1, "rgba(0,0,0,255)");
        assertImageWritten(outputImage, 2, 2, "rgba(255,255,255,255)");
        assertImageWritten(outputImage, 3, 3, "rgba(0,0,255,255)");
    }
}
