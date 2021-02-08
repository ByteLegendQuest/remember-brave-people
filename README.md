### Java Island Newbie Village

This is inspired by https://open-pixel-art.com/.


### Implementation details


There are 3 GitHub jobs:

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
