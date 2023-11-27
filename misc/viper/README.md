# Viper

## 题目信息

* 难度 - 中等
* Flag - `ACTF{8EW@rE_0F_vEnom0us_sNaK3_81T3$_as_1t_HA$_nO_cOnSc1ENCe}`

## 题目描述

> When you encounter a viper, you need to be more careful to prevent injury

Once upon a time, there was a farmer who lived in a humble village.

One day, while working in his field, he came across a viper that had been injured.

Despite its venomous nature, the farmer felt compassion and decided to help it...

## 设计思路

2023 年 7 月 30 日，多个 Curve 的 Pools 遭受了攻击，造成了将近 $52 M 的损失。

一天后，[@vyperlang](https://twitter.com/vyperlang/status/1685692973051498497) 在其官推上宣布 Vyper 在 0.2.15、0.2.16 与 0.3.0 版本中的重入锁是有故障的。随后，[@CurveFinance](https://twitter.com/CurveFinance/status/1685693202722848768) 也在其官推上引用该推文，指出此次攻击事件的根本原因即为 Pool 合约所用到的 Vyper 编译器版本为 0.2.15。

这便是本道赛题的题目背景。眼见不一定为实，即使关键函数都加上了 `@nonreentrant('lock')` 这样看似同样的保护，实则不同函数中的 `lock` 并不在同一个 storage slot 中，原本应该生效的跨函数重入锁也就荡然无存了。

当确定了攻击手法是重入之后，漏洞点还是比较容易找出的。调用 swap 函数并在 else 分支中的 raw_call 时重入 deposit 函数存入 ETH 即可换出比 $\frac{amount}{ratio}$ 更多的ETH。为了构造这一漏洞，在 `swap` 函数中从 VETH 换出 ETH 的执行路径上，我特意加入了 `_after - _before` 这样多此一举的逻辑，灵感来源于今年年初的 Orion Protocol 攻击，同样是 reentrancy 相关，感兴趣的可以去了解一下。

延伸阅读: https://hackmd.io/@vyperlang/HJUgNMhs
