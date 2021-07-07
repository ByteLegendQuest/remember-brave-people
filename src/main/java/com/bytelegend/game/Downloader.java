package com.bytelegend.game;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

class Downloader {
    private final int THREAD_NUM = 20;
    private final Environment environment;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_NUM, (ThreadFactory) r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    Downloader(Environment environment) {
        this.environment = environment;
    }

    Map<String, File> downloadAvatars(List<SimpleTile> tiles) throws ExecutionException, InterruptedException {
        List<Future<?>> futures = new ArrayList<>();

        Map<String, File> usernameToFile = tiles.stream()
                .collect(Collectors.toMap(SimpleTile::getUsername,
                        (tile) -> new File(environment.getWorkspaceDir(), "build/avatars/" + tile.getUsername() + ".png")));

        usernameToFile.forEach((username, file) -> {
            String url = String.format("https://avatars.githubusercontent.com/%s", username);
            futures.add(threadPool.submit(() -> {
                download(url, file);
            }));
        });
        for (Future<?> future : futures) {
            future.get();
        }
        return usernameToFile;
    }

    void download(String url, File file) {
        try {
            file.getParentFile().mkdirs();
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
