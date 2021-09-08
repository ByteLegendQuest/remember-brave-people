# 常见错误

本文档整理了新手第一次提交Pull Request的常见错误。这没什么，是个人就会犯错，请不要气馁，更不要否定自己，继续自己对编程的热情。

**这世界上的每个牛人，都曾经是和你我一样的普通人。**

## 检查本次Pull Request提交的文件变更

请点击`files`标签页，查看你所做修改的代码。请记住它的位置，未来你会无数次用到这个技巧。

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/pr-changes-tab.png)

## JSON格式错误

JSON是互联网常用的数据交换格式，如果你还不了解，没关系，我们会在稍后的课程里学习。请确保你增加了一对花括号，且你的花括号外有一个逗号。

正确的例子：

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/json-correct-1.png)

[错误的例子（少了右花括号）](https://github.com/ByteLegendQuest/remember-brave-people/pull/183/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/json-error-1.png)

[错误的例子（少了逗号）](https://github.com/ByteLegendQuest/remember-brave-people/pull/158/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/json-error-2.png)

## 修改了别人的格子

你只能新增或修改自己的头像，不能修改别人的头像。你也不会喜欢别人修改你的头像，对吧？

[错误的例子（修改了别人的头像和颜色）](https://github.com/ByteLegendQuest/remember-brave-people/pull/180/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/other-people-error-1.png)

[错误的例子（新增了两个头像，一个自己的，一个别人的）](https://github.com/ByteLegendQuest/remember-brave-people/pull/178/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/other-people-error-2.png)

## 使用真实名字，或者把自己的GitHub ID拼写错误

[错误的例子（使用了自己的真实姓名）](https://github.com/ByteLegendQuest/remember-brave-people/pull/172/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/username-error-1.png)

[错误的例子（使用了奇怪的名字）](https://github.com/ByteLegendQuest/remember-brave-people/pull/166/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/username-error-2.png)

[错误的例子（自己的GitHub ID拼写错误）](https://github.com/ByteLegendQuest/remember-brave-people/pull/150/files)

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/username-error-3.png)

### 代码冲突

如果你确定你没有做错，请检查PR页面是否包含代码冲突警告：

![1](https://raw.githubusercontent.com/ByteLegendQuest/remember-brave-people/master/docs/code-conflict.png)

代码冲突通常是由于多人同时尝试提交，且其他人的代码比你先通过。这对于初学者是一个很难解决的问题，我建议删除你fork的仓库然后重来一次。
虽然这听上去不怎么优雅，但是对于熟悉Pull Request的基本操作，巩固自己的技能很有好处。

