# Tree

## 题目信息

* 难度 - 较难

## 题解

这个题目我完全没想到会0解，尤其是之后给了hint：The name of this challenge means “Abstract Syntax Tree of clang”, maybe you need learn something about clang ASTMatcher。 

唯一一个来问我的队伍似乎错误的用clang本身去做bindiff的符号还原了。clang作为编译过程中最开始的入口，是一定会缺失一部分AST相关的symbol的。因此这题如果自己写一个ASTMatcher进行bindiff应该不会很难。

