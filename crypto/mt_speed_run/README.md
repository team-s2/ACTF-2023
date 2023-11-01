# mt_speed_run

## 题目信息

* 难度 - 中等
* Flag - `ACTF{482A17FD-17FA-872A-9DBB-41D715C0B266}`

## 题目描述

> 你是否也有很多想要完成、调试、优化但因为时间不够而未能完成的程序？
也许因为生命有限，最终你选择了低效的语言和框架，写出了难看的代码，并告诉自己这就是你所需要的代码。
它真的是你脑海里构思无数遍的形象吗？
明明在梦中的程序如此丝滑、流畅，轻松解决你预设的问题，而现实中它却慢如蜗牛、调试不良、无法编译，甚至连新建文件夹都没有！
来吧，将你的梦想变为现实，不留遗憾！
—— Karp de Chant

> Do you also have a lot of programs that you wanted to write, debug, and optimize but haven't done due to lack of time?
Perhaps due to the limited life, you ended up choosing inefficient languages and frameworks, resulting in ugly code and UI.
Then you convinced yourself that was the code you needed.
Was it really the code that you had envisioned in your mind after countless iterations?
In your dreams, the program is so smooth and effortless, solving all the problems as expected, but in reality, it runs as slow as a snail, poorly debugged, unable to compile, even without creating a new folder!
Come on, turn your dreams into reality without leaving any regrets!
—— Karp de Chant

## 设计思路

去年很火的MT19937 + 追加时间限制≤10s。

目前的C++ m4ri实现在无预计算的情况下对getrandbit(1)的求解小于10s。

网上有预计算getrandbit(4) [https://github.com/JuliaPoo/MT19937-Symbolic-Execution-and-Solver/blob/master/Demo of Features.ipynb](https://github.com/JuliaPoo/MT19937-Symbolic-Execution-and-Solver/blob/master/Demo%20of%20Features.ipynb) 消耗2s的版本，为此将 0x9908b0df 0x9D2C5680 0xEFC60000 这几个常数改掉阻止预计算。

由于题目是很久之前出的，不知道最近有无新板子。
