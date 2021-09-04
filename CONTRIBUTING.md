## Contributing

Note: this doc is intended for maintainers. If you're a player, you don't need to read and understand this.

### Scenarios

There are a few scenarios this repository covers:

- Player modifies `brave-people.json`, runs the build locally to verify the change.
  - Run `./gradlew run`, the files are generated at `./build`.
- Player modifies `brave-people.json`, then creates PR.
  - `rememeber-brave-people` GitHub workflow is triggered.
    - This is executed in Docker container to make it super fast.
    - If there are any changes other than `rememeber-brave-people.json`, fail.
    - Checks the changes are legitimate, for example, you can't overwrite other people's tile.
    - Generate new data based on existing `brave-people.png`/`brave-people-info.json`.
    - Commit and push new data to `brave-people.png`/`brave-people-info.json`.
  - When the workflow starts, ByteLegend webhook is triggered to display animation in the game page.
    - If the workflow fails, display failure animation.
    - Otherwise, `ByteLegendBot` merges the PR, says congratulations to the player.
- A daily trigger refreshes `brave-people.png`/`brave-people-info.json` and upload them to GitHub (we use `data` branch as the storage).
- Player helps improve the code, makes changes other than `brave-people.json`, then creates PR.
  - `check` GitHub workflow is triggered.
  - `./gradlew check` is executed to make sure nothing breaks.
  - Wait for the administrator approve and merge manually.
- Update the docker image manually after changes to production code.
  - TODO: this is triggered manually now. We can make `ByteLegendBot` do this.
  - TODO: we should update the data after changes to production code.

### Workflows

- `remember-brave-people`: triggered upon PR with changes on `brave-people.json`.
  - If there are changes other than `brave-people.json`, cancel the workflow.
  - Otherwise, do `sanityCheck`, make sure the player only change allowed part.
  - Generate the new image and JSON at `build/brave-people.png`/`build/brave-people-output.json`
  - Move `build/brave-people.png` to root directory, commit and push.
  - Push `build/brave-people.png`/`build/brave-people-output.json` to CDN so the player can see it in the game.
  - Merge current branch to `master`.
- `check`: triggered upon PR with changes other than `brave-people.json`.
  - Run `test` to make sure nothing is broken.
  - Wait for administrator review and merge the PR.
- `refresh-data-daily`: for better performance, `remember-brave-people` only writes the
  modified tile, not all. This job runs per day to refresh the data.
