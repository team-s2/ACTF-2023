# obfuse

## 题目信息

* 难度 - 较难

## 题解

### 混淆部分

这题加了四种混淆分别为indirect jmp， indirect call， [enhanced ollvm](https://bbs.pediy.com/thread-274778.htm)（credit to R1mao） 以及 [global enc](https://github.com/bluesadi/Pluto-Obfuscator).

其实除了enhanced ollvm比较难去之外其他都还行，0ray的脚本是除了enhanced ollvm以外直接用模式匹配去去混淆，然后带着ollvm直接逆向，扎实的基本功也能直接逆出来.  
W&M的师傅直接用了binaryninja写了去混淆脚本完成了通杀，直接f5逆向（666）。 我的预期是用unicorn模拟执行去去混淆，可以通过bfs基本块也能完成通杀，总之姿势很多。

### 内部逻辑
程序本体是一个简单的 shell , 由于运行时 ```global enc``` 会自行解密（动调起来就没啥用了），可以发现在执行命令前存在一个函数 ```EvalCommand``` 判断命令内容并执行对应操作，其中存在两条指令分别为 save 与 check，这就是关键的输入与检测 flag 的位置。

其中 save 函数作用为将当前的后台进程数量作为字符保存到字符串中（不过要看明白这个可能需要逆 shell 才知道是后台进程数），较为简单的做法是直接 patch 一下程序手动输入 flag。

check 函数首先检验 flag 长度为25，并将 flag 分为三部分长度分别为 4、15、6。
1. 前 4 个字符为简单的 md5 加密，爆破即可
2. 中间 15 字符采用了 rc5 + AES，其中 rc5 修改了 $P_w$ 与 $Q_w$ ，AES 修改了行交换的顺序和 sbox，其中 sbox 并非预先存储，修改了初始值由算法生成，可以动调 dump 出来
3. 最后一段则生成了一颗 Trie 树并给每个节点随机赋值，需要字符串在树上路径值和节点编号根据规则运算后等于 flag 的运算值即通过校验
   （主打一个大杂烩）

### 部分源码
```cpp

#define ANS -1175156229
string md5hash="2aedfa0f134e41fa06a0dd4f8c6fba80";
int aes_flag[]={50,132,59,124,100,20,183,170,17,141,42,227,107,155,22,149,74,185,197,7,185,236,102,205,254,235,177,0,14,172,148,168};

void build_sbox(void)
{
    int i;
    for(i=0; i<256; i++)
        sbox[i] = aes_8bit_mul_mod_0x101(aes_8bit_inverse(i), 0x1F) ^ 0x37;
}

void gentree(int x,int dep)
{
    val[x]=rand();
    if(dep>=6)return;
    for(int i=0;i<=9;++i)
    {
        edge[x].push_back(++cnt_tree);
        gentree(cnt_tree,dep+1);
    }
}

void check(const vector<string>&cmd)
{
    srand(114514);
    cnt_tree=0;
    gentree(0,0);
    if(length!=25)throw "Wrong Flag";
    string s1,s2,s3;
    for(int i=0;i<4;++i)s1+=char(input[i]);
    for(int i=4;i<19;++i)s2+=char(input[i]);
    for(int i=19;i<25;++i)
    {
        s3+=char(input[i]);
        if(input[i]<'0' || input[i]>'9')throw "Wrong Flag";
    }
    // s1="W4@t";
    
    MD5 md5;
    string s4=md5.encode(s1);
    if(s4!=md5hash)throw "Wrong Flag1";

    // s2="_0_N41v3_$#4ll_";
    rc5 rc;
    int rc5_enc[110],aes_enc[110];
    rc.Init(s2,rc5_enc);
    for(int i=0;i<16;++i)
    {
        aes_1[i<<1]=rc5_enc[i]>>8;
        aes_1[i<<1|1]=rc5_enc[i]&0xff;
    }
    AES aes;
    aes.init(aes_1,aes_enc);
    for(int i=0;i<32;++i)
        if(aes_enc[i]!=aes_flag[i])throw "Wrong Flag2";
    
    // s3="120911";
    int pos=0,ans=0;
    for(int i=0;i<6;++i)
    {
        pos=edge[pos][s3[i]-'0'];
        ans^=val[pos]*pos;
    }
    if(ans!=ANS)throw "Wrong Flag3";
    throw "Correct Flag";
}
```