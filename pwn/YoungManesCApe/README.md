# YoungManesCApe

## 题目信息

* 难度 - 中等
* Flag - `ACTF{n3vEr_u5e_d3n$1lsT_0n1y_!n_secc0mp}`

## 设计思路

SECCON 22 CTF Finals 中的 babyescape 的翻版，原题预期为使用 `kexec_load` 与 `kexec_file_load` （也就是 busybox insmod）加载内核模块，这里改为使用拆解版 mount syscall(fsopen+fsconfig+fsmount+move_mount)，然后读 `/proc/1/root/flag`。

## 题目描述

> Young man apply all the recommendations in a [document](https://docs.docker.com/engine/security/seccomp/). Does this mean that his sandbox secure enough? 

This program is running that you can access by `nc 127.0.0.1 9999`.

## 考点

fsopen+fsconfig+fsmount+move_mount syscall
