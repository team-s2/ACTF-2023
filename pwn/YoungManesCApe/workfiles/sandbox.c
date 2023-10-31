#include <errno.h>
#include <linux/audit.h>
#include <linux/bpf.h>
#include <linux/filter.h>
#include <linux/seccomp.h>
#include <linux/unistd.h>
#include <stddef.h>
#include <stdio.h>
#include <sys/prctl.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>

#define add_filter_stmt(iter, defination) do { struct sock_filter stmt = defination; memcpy(filter+iter, &stmt, sizeof(stmt)); } while(0)

static void install_seccomp() {
  int syscalls[] = {__NR_chroot, __NR_mount, __NR_unshare, __NR_reboot, __NR_ptrace, __NR_process_vm_readv, __NR_process_vm_writev, __NR_open_by_handle_at, __NR_pivot_root, __NR_acct, __NR_add_key, __NR_bpf, __NR_clock_adjtime, __NR_clock_settime, __NR_clone, __NR_create_module, __NR_delete_module, __NR_finit_module, __NR_get_kernel_syms, __NR_get_mempolicy, __NR_init_module, __NR_ioperm, __NR_iopl, __NR_kcmp, __NR_kexec_file_load, __NR_kexec_load, __NR_keyctl, __NR_lookup_dcookie, __NR_mbind, __NR_move_pages, __NR_name_to_handle_at, __NR_nfsservctl, __NR_perf_event_open, __NR_personality, __NR_query_module, __NR_quotactl, __NR_request_key, __NR_set_mempolicy, __NR_setns, __NR_settimeofday, __NR_swapon, __NR_swapoff, __NR_sysfs, __NR__sysctl, __NR_umount2, __NR_uselib, __NR_userfaultfd, __NR_ustat, __NR_clone3, __NR_pidfd_open, __NR_pidfd_getfd, __NR_pidfd_send_signal, __NR_seccomp, __NR_prctl, __NR_arch_prctl, __NR_create_module, __NR_init_module, __NR_delete_module, __NR_get_kernel_syms, __NR_query_module, __NR_epoll_create, __NR_epoll_create1, __NR_io_uring_setup, __NR_io_uring_enter, __NR_io_uring_register};
  struct sock_filter filter[sizeof(syscalls) / sizeof(syscalls[0]) + 6];
  add_filter_stmt(0, BPF_STMT(BPF_LD | BPF_W | BPF_ABS, (offsetof(struct seccomp_data, arch))));
  add_filter_stmt(1, BPF_JUMP(BPF_JMP | BPF_JEQ | BPF_K, AUDIT_ARCH_X86_64, 0, sizeof(syscalls) / sizeof(syscalls[0]) + 3));
  add_filter_stmt(2, BPF_STMT(BPF_LD | BPF_W | BPF_ABS, (offsetof(struct seccomp_data, nr))));
  add_filter_stmt(3, BPF_JUMP(BPF_JMP | BPF_JGE | BPF_K, __X32_SYSCALL_BIT, sizeof(syscalls) / sizeof(syscalls[0]) + 1, 0));
  for (int i = 0; i < sizeof(syscalls) / sizeof(syscalls[0]); i++) {
	  add_filter_stmt(i + 4, BPF_JUMP(BPF_JMP | BPF_JEQ | BPF_K, syscalls[i], sizeof(syscalls) / sizeof(syscalls[0]) - i, 0));
  }
  add_filter_stmt(sizeof(syscalls) / sizeof(syscalls[0]) + 4, BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_ALLOW));
  add_filter_stmt(sizeof(syscalls) / sizeof(syscalls[0]) + 5, BPF_STMT(BPF_RET | BPF_K, SECCOMP_RET_KILL));
  struct sock_fprog rule = {
      .len = (unsigned short)(sizeof(filter) / sizeof(filter[0])),
      .filter = filter,
  };
  if (prctl(PR_SET_NO_NEW_PRIVS, 1, 0, 0, 0) < 0) _exit(1);
  if (prctl(PR_SET_SECCOMP, SECCOMP_MODE_FILTER, &rule) < 0) _exit(1);
}

static unsigned char hex2num(unsigned char chr) {
  if (chr >= '0' && chr <= '9') return chr - '0';
  else if (chr >= 'a' && chr <= 'f') return chr - 'a' + 10;
  else if (chr >= 'A' && chr <= 'F') return chr - 'A' + 10;
  return 0xff;
}

int main(void) {
  char *args[] = {"/elf", NULL};

  if (chroot("sandbox")) {
    write(STDERR_FILENO, "chroot failed\n", 14);
    _exit(1);
  }
  if (chdir("sandbox")) {
    write(STDERR_FILENO, "chdir failed\n", 13);
    _exit(1);
  }

  install_seccomp();

  int fd = open(args[0], O_RDWR | O_CREAT | O_TRUNC, 0700);
  if (fd < 0) {
    puts(strerror(errno));
    write(STDERR_FILENO, "open elf failed\n", 16);
    _exit(1);
  }
  write(STDOUT_FILENO, "Please input your static executable file (in HEX, ends with empty line): ", 73);
  unsigned char buf[1];
  int is_end_of_line = 0, has_prefix = 0;
  int prefix = 0;
  while (1) {
    int len = (int)read(STDIN_FILENO, buf, 1);
    if (len <= 0) break;
    if (buf[0] == '\n' || buf[0] == '\r') {
      if (is_end_of_line) {
        break;
      }
      is_end_of_line = buf[0] == '\n';
      continue;
    }
    is_end_of_line = 0;
    unsigned char cur = hex2num(buf[0]);
    if (cur == 0xff) {
      write(STDERR_FILENO, "invalid input\n", 14);
      printf("%d", buf[0]);
      _exit(1);
    }
    if (has_prefix) {
      prefix |= cur;
      has_prefix = 0;
      if (write(fd, &prefix, 1) < 0) {
        write(STDERR_FILENO, "write failed\n", 13);
        _exit(1);
      }
    } else {
      has_prefix = 1;
      prefix = cur << 4;
    }
  }
  if (has_prefix) {
    write(STDERR_FILENO, "odd length\n", 11);
    _exit(1);
  }
  close(fd);

  execve(args[0], args, NULL);
  return -1;
}