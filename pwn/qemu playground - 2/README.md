## qemu playground - 2

***Note: The source code will not be released in case of any unauthorized usage.***

### Description

This is an easy challenge of qemu escape. After you finish `qemu playground - 1` in Reverse, the `ptr` field after the encrypt buffer can be enabled. You can read from or write into it.

### Intended Solution

When writing into the encrypt buffer, the mmio handler has a wrong bound check, which leads to a 4 bytes overflow. Thus we can overwrite low 4 bytes of the ptr, and gain the ability to arbitrary address read / write. Just find leaks and do FSOP.

Check the file `exp.c` to see my solution : )

### Other solutions

There is an **rwx** page in recent version of qemu. Write shellcode into it and control flow will be hijacked, too.