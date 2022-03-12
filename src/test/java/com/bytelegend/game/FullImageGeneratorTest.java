package com.bytelegend.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Collections;

import static com.bytelegend.game.TestUtils.assertImageWritten;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FullImageGeneratorTest {
    @Mock
    Environment environment;

    @TempDir
    File dir;

    @Test
    public void canGenerateBlankImage() throws Exception {
        File imageFile = new File(dir, "image.png");
        when(environment.getOutputHeroesCurrentImage()).thenReturn(imageFile);
        when(environment.getInputHeroesCurrentImage()).thenReturn(imageFile);

        FullImageGenerator fullGenerator = new FullImageGenerator(environment);
        fullGenerator.generate(Collections.emptyList());

        IncrementalImageGenerator incrementalGenerator = new IncrementalImageGenerator(environment);
        incrementalGenerator.writeTileDiff(new TileDataDiff("[]", "[{\"username\":\"ByteLegendBot\",\"x\":0,\"y\":0,\"color\":\"#ff0000\"}]", "ByteLegendBot"));

        assertImageWritten(imageFile, 0, 0, new RGBA(255, 0, 0, 255));
    }
}
