<details>
  <summary>简体中文</summary>

  # 勇士挑战：人过留名，雁过留声

  这是一个教学，旨在帮助你学习创建GitHub的pull request，这是向世界上任何开源项目贡献代码的第一步。

  ## 太长不看的描述

  - 修改`brave-people.json`，在里面加入你的**GitHub用户ID (不是你自己的名字)**，坐标和喜欢的颜色，然后创建一个pull request。
  - 回到[字节传说](https://bytelegend.com)，然后查看`(V, 92)`游戏坐标处的公告牌。

  ## 详细步骤
  点击[这里](https://github.com/ByteLegendQuest/remember-brave-people/blob/master/docs/zh/create-your-first-pull-request.md)查看详细步骤。

</details>

# Brave people challenge - leave your name!

This is inspired by https://open-pixel-art.com/. This is a part of [ByteLegend](https://bytelegend.com), a game to learn programming.

This challenge is intended to teach you how to create a pull request.

## TL;DR

- Change `brave-people.json`, add **your GitHub ID (NOT your name)**, coordinate and favorite color, then create a pull request.
- Go back to [ByteLegend](https://bytelegend.com) and check out the noticeboard at `(V, 92)` game coordinate.

## Instructions

Check out [here](https://github.com/ByteLegendQuest/remember-brave-people/blob/master/docs/en/create-your-first-pull-request.md) for detailed instructions.

## Implementation details (Don't read this unless you want to understand how this repository works)

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
