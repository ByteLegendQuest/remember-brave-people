package com.bytelegend.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.bytelegend.game.Constants.HEROES_JSON;
import static com.bytelegend.game.Utils.parseSimpleTiles;
import static com.bytelegend.game.Utils.toFormattedJson;
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
     * Merge --squash to origin/main.
     * Resolve conflict with the best effort.
     * Commit.
     */
    void mergeToMain(TileDataDiff diff) throws Exception {
        shell.exec("git", "checkout", "-b", "origin_main", "origin/main").assertZeroExit();
        ExecResult mergeResult = shell.exec("git", "merge", environment.getHeadRef(), "--no-commit", "--no-ff").withLog();
        // If conflict or merged result has extra changes, fallback to auto resolution.
        if (mergeResult.exitValue != 0) {
            if (mergeResult.getOutput().contains("Merge conflict")) {
                List<SimpleTile> latestData = parseSimpleTiles(show("origin/main", HEROES_JSON));
                latestData.removeIf(tile -> tile.getUserid().equalsIgnoreCase(diff.getChangedTile().getUserid()));

                if (latestData.stream().anyMatch(it ->
                    it.getX() == diff.getChangedTile().getX() && it.getY() == diff.getChangedTile().getY())) {
                    throw new IllegalStateException("Your change conflicts with other one's change, you need to sync upstream repository!");
                }

                latestData.add(diff.getChangedTile());
                writeString(environment.getHeroesJson(), toFormattedJson(latestData));
                shell.exec("git", "add", ".");
            } else {
                throw new IllegalStateException("Merge failed, see the git error output.");
            }
        }

        if (environment.getPrTitle() == null || environment.getPrNumber() == null || environment.getPlayerGitHubUsername() == null) {
            throw new NullPointerException();
        }

        shell.execSuccessfully("git", "commit", "-m",
            String.format("%s (#%s)\n\n"
                    + "Thanks to @%s's contribution!\n\n"
                    + "%s",
                environment.getPrTitle(), environment.getPrNumber(),
                environment.getPlayerGitHubUsername(),
                getCoAuthoredBy()
            )
        );
    }

    void addCommit(String commitMessage, String... files) throws Exception {
        List<String> args = new ArrayList<>();
        args.addAll(Arrays.asList("git", "add"));
        args.addAll(Arrays.asList(files));
        shell.execSuccessfully(args.toArray(new String[0]));
        shell.execSuccessfully("git", "commit", "-m", commitMessage);
    }

    private String getCoAuthoredBy() throws Exception {
        ExecResult latestCommit = shell.exec("git", "log", "-1", "--format=%an <%ae>", environment.getHeadRef()).withLog();
        if (latestCommit.exitValue == 0) {
            return "Co-authored-by: " + latestCommit.getOutput().trim();
        } else {
            return "";
        }
    }

    void push() throws Exception {
        ExecResult result = shell.exec("git", "push", environment.getRepoPushUrl(), "origin_main:main").withLog();
        if (result.exitValue != 0) {
            throw new IllegalStateException("Push failed. There might be multiple jobs running simultaneously, try rerunning the job.");
        }
    }

    void fetchUpstream() throws Exception {
        shell.execSuccessfully("git", "remote", "add", "upstream", environment.getRepoPullUrl());
        shell.execSuccessfully("git", "fetch", "upstream");
    }

    String findForkPointTo(String targetBranch) throws Exception {
        return shell.exec("git", "merge-base", targetBranch, "HEAD").assertZeroExit().stdout.trim();
    }
}
