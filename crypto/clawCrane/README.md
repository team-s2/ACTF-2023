## claw crane

### 题目信息

- 难度 - 中
- Flag: ACTF{C0Nt1nu3d_Fract1on&Mor3_zer0s&Replay_it}

### 设计思路

以夹娃娃为背景，要求在256次夹娃娃的过程中成功222次（0.867）及以上。

题目主要分为两部分：

第一部分是根据输入的moves随机生成r，并要求输入p，q使得`abs(r*q - p*pow(2,BITS))`中包含的 0 bit尽可能小。这里可以使用连分数，即把这个公式看成$\frac{r}{p}-\frac{2^{BITS}}{q}$，使得这个值尽可能小，相关资料可以看https://oi-wiki.org/math/number-theory/continued-fraction/。同时也可以用LLL格基规约找到最小的值。这里能找到的最小的值的位数可以预见为64bit，根据代码中的扩展`bits += "0"*(128+self.bless - len(bits))`,这里0的概率大约为96/128，即0.75，显然概率是不够的（但是居然有队伍就这么暴力的做出来了？）。

因此这里要使用第一个trick，将找到的p进行+pow(2,63)操作，此时得到的x = r\*q - p*pow(2,BITS) + pow(2,63+BITS)，相当于把这个数值加上了2^191次，但是高位除了最高位均为0，此时的概率为(96+63-1)/(128+63)=0.812。还是差一点。

接下来就是第二部分，在生成r的过程中，即代码片段：

```python
        chaos = bytes_to_long(md5(
                    long_to_bytes((self.seed + vs) % pow(2,BITS))
                ).digest())
        self.seed = (self.seed + chaos + 1) % pow(2,BITS)
        return chaos
```

是可以通过输入的vs进行重放攻击的，由于我们知道上一次输出的chaos，记为c0，那么有：

​	c0 = md5(seed0+vs0)								(1)

​	seed1 = seed0 + c0 + 1							（2）

​	c1 = md5(seed1+vs1)								（3）

因此只要使得seed1+vs1 = seed0+vs0，即seed0 + c0 + 1 + vs1 = seed0 + vs0，也就是vs1 = vs0 - c0 - 1即可，并不需要知道seed的值。

同时，尽管vs的值控制着爪子的位置，但是由于有100次移动的机会，而我们只需要32步就能到达任意位置，因此前64次可以任意移动，通过后32次可以将其移动到正确的位置上。

最终，我们的想法是通过多次连接，找到一个r，其中0的位数的概率在0.867，然后进行重放。这是合理的，实验证明可以在64次以内稳定找到。

