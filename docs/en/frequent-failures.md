# Common Failures

This documentation collects the common failures when newbies submit their first pull requests.
It's totally FINE, people make mistakes. Please don't be frustrated. Keep the passion, keep going forward.

Because **every expert in the world was an average people, like you and me.**

If your problem is not resolved after finishing this documentation, I highly suggest you delete the forked repository
and retry (see the bottom of this documentation for details). This doesn't sound like a good suggestion,
but it helps you a lot to be familiar with pull request and consolidate your skills.

## Check the Changed Files in Your Pull Request

Click `Files changed` tab to check the code changes you made. Remember this tab and you'll use it a million times in the future.

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/pr-changes-tab.png)

### Bad JSON

JSON is the most popular data exchange format in the Internet. It's fine if you don't know it - we'll learn it
in the future. Please make sure you add a pair of curly bracket, and there is a comma outside your curly brackets.

Correct Example:

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/json-correct-1.png)

[Incorrect Example (missing right curly bracket)](https://github.com/ByteLegendQuest/remember-brave-people/pull/183/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/json-error-1.png)

[Incorrect Example (missing comma)](https://github.com/ByteLegendQuest/remember-brave-people/pull/158/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/json-error-2.png)

### Change Other One's Tile

You are only allowed to add or modify your own avatar. You can't change other people's avatar. You don't like other people to change yours, right?

[Wrong Example (change other one's avatar and color)](https://github.com/ByteLegendQuest/remember-brave-people/pull/180/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/other-people-error-1.png)

[Wrong Example (add two avatars, one for yourself, one for others)](https://github.com/ByteLegendQuest/remember-brave-people/pull/178/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/other-people-error-2.png)

### Use Real Name or Typo in GitHub ID

[Wrong Example (real name)](https://github.com/ByteLegendQuest/remember-brave-people/pull/172/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/username-error-1.png)

[Wrong Example (weird name)](https://github.com/ByteLegendQuest/remember-brave-people/pull/166/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/username-error-2.png)

[Wrong Example (typo in GitHub ID)](https://github.com/ByteLegendQuest/remember-brave-people/pull/150/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/username-error-3.png)

## Code Conflict

If you're sure you're right, please check if there is a code conflict warning in PR page:

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/code-conflict.png)

Code conflict is usually caused by many people submitting code at the same time, and other people are faster.
This is a really hard problem for newbies, I suggest your delete your forked repository and retry.

## Delete Forked Repository

When you create your first pull request, GitHub creates a "forked repository" under your name
(because you don't have write permission to original repository, obviously):

`https://github.com/<Your Own GitHub ID>/remember-brave-people`

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/delete-repo-0.png)

Click `Settings` and scroll down to the bottom:

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/delete-repo-1.png)

Click `Delete this repository`：

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/delete-repo-2.png)

Copy the bold words in the popup and paste into the input box, click the button:

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/delete-repo-3.png)
