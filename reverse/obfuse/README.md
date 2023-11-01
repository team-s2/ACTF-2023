# obfuse

## 题目信息

* 难度 - 较难

## 题解

### 混淆部分

这题加了四种混淆分别为indirect jmp， indirect call， [enhanced ollvm](https://bbs.pediy.com/thread-274778.htm)（credit to R1mao） 以及 [global enc](https://github.com/bluesadi/Pluto-Obfuscator).

其实除了enhanced ollvm比较难去之外其他都还行，0ray的脚本是除了enhanced ollvm以外直接用模式匹配去去混淆，然后带着ollvm直接逆向，扎实的基本功也能直接逆出来.  
W&M的师傅直接用了binaryninja写了去混淆脚本完成了通杀，直接f5逆向（666）。 我的预期是用unicorn模拟执行去去混淆，可以通过bfs基本块也能完成通杀，总之姿势很多。

### 内部逻辑
