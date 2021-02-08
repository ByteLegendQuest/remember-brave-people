package com.bytelegend.game;

import java.util.List;

import static com.bytelegend.game.Constants.BRAVE_PEOPLE_JSON;
import static com.bytelegend.game.Constants.OBJECT_MAPPER;
import static com.bytelegend.game.Utils.parse;
import static com.bytelegend.game.Utils.writeString;

class Git {
    final Shell shell;
    final Environment environment;

    Git(Environment environment) {
        this.shell = new Shell(environment.getWorkspaceDir());
        this.environment = environment;
    }

    String show(String ref, String fileName) throws Exception {
        return shell.exec("git", "show", ref + ":" + fileName).assertZeroExit().stdout;
    }

    /**
     * Merge --squash to origin/master.
     * Resolve conflict with best effort.
     * Commit.
     */
    void mergeToMaster(TileDataDiff diff) throws Exception {
        shell.exec("git", "checkout", "-b", "origin_master", "origin/master").assertZeroExit();
        ExecResult mergeResult = shell.exec("git", "merge", environment.getHeadRef(), "--squash").withLog();
        // If conflict or merged result has extra changes, fallback to auto resolution.
        if (mergeResult.exitValue != 0) {
            if (mergeResult.getOutput().contains("Merge conflict")) {
                List<InputTileData> latestData = parse(show("origin/master", BRAVE_PEOPLE_JSON));
                latestData.removeIf(tile -> tile.getUsername().equals(diff.getChangedTile().getUsername()));

                if (latestData.stream().anyMatch(it ->
                        it.getX() == diff.getChangedTile().getX() && it.getY() == diff.getChangedTile().getY())) {
                    throw new IllegalStateException("Your change conflicts with other one's change, you need to sync upstream repository!");
                }

                latestData.add(diff.getChangedTile());
                latestData.sort(InputTileData.COMPARATOR);
                writeString(environment.getBravePeopleJson(), OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(latestData));
                shell.exec("git", "add", ".");
            } else {
                throw new IllegalStateException("Merge failed, see the git error output.");
            }
        }

        if (environment.getPrTitle() == null || environment.getPrNumber() == null || environment.getPlayerGitHubUsername() == null) {
            throw new NullPointerException();
        }

        shell.execSuccessfully("git", "commit", "-m",
                String.format("%s (#%s)\n\nThanks to @%s's contribution",
                        environment.getPrTitle(), environment.getPrNumber(), environment.getPlayerGitHubUsername()));
    }

    void push() throws Exception {
        ExecResult result = shell.exec("git", "push", environment.getRepoPushUrl(), "origin_master:master").withLog();
        if (result.exitValue != 0) {
            throw new IllegalStateException("Push failed. There might be multiple jobs running simultaneously, try rerunning the job.");
        }
    }

    void fetchUpstream() throws Exception {
        shell.execSuccessfully("git", "remote", "add", "upstream", environment.getRepoPullUrl());
        shell.execSuccessfully("git", "fetch", "upstream");
    }

    String findForkPointTo(String targetBranch) throws Exception {
        return shell.exec("git", "merge-base", "--fork-point", targetBranch, "HEAD").assertZeroExit().stdout.trim();
    }
}
