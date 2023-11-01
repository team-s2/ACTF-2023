# CTFer simulator

## 题目信息

* 难度: 简单

## 题目描述

Play CTF and win flag.

## 题目设计

来自于（也给了 credit）近期很火的 [phd-game](https://research.wmz.ninja/projects/phd/index.html)，添加了后端，设计成了要 48 小时内获得 8 个 flag 即可返回 flag

没有特别的预期做法，因为 8 这个数字其实当时也是画了一个表觉得运气足够欧的时候可以玩出来的；反正如果一直没有人做出来的话也会放出后端代码 app.js，看了后就会发现没有校验体力值以及时间等等

好在师傅们强的很，看了 wp 做法包括但不限于爆破优秀随机数种子、改体力和时间 blabla

这里给出一个简单的（考虑到后台逻辑没有严格做事件顺序）的解法，其实如果多尝试几次是可能发现 action 并不一定符合前端逻辑的，甚至其实后台也没有校验考试到底通没通过 :D

见 [solver/exp.js](./solver/exp.js)
