#/bin/bash
gcc -S src.c -fno-asynchronous-unwind-tables -Os -mavx -msse -mavx2 -ffast-math -fno-stack-protector -fomit-frame-pointer -fsingle-precision-constant  -fno-verbose-asm -fno-unroll-loops -nodefaultlibs 
cp src.s shim.s
sed -i -e "s/main/_start/g" shim.s
sed -i -e "s/\.size.*//g" shim.s
sed -i -e "s/\.ident.*//g" shim.s
sed -i -e "s/\.section.*//g" shim.s
#sed -i "s/pushq.*//g" shim.s
sed -i "s/.align 4//g" shim.s
sed -i "s/.align 16//g" shim.s
sed -i "s/.align 32//g" shim.s

sed -i "s/.size	main, .-main//g" shim.s
sed -i "s/movl	\$1, %edx/mov \$1, %dl/g" shim.s
sed -i "s/vmovdqa/vmovdqu/g" shim.s
sed -i "s/endbr64//g" shim.s
sed -i "s/movsbq	%r11b, %r11//g" shim.s
sed -i "s/movslq	%ecx, %rcx//g" shim.s
# sed -i -e "s/\.string.*//g" shim.s
as --64 -ac -ad -an --statistics -o shim.o shim.s # syscall.s 
ld -N --no-demangle -x -s -Os --cref -o shim shim.o
wc -c shim
strip shim
wc -c shim
sstrip  shim
wc -c shim
python -c "import os; f = os.popen('wc -c shim'); fsize = int(f.read().split(' ')[0]); print('new_len:',((((fsize + 8) / 64) + 1) * 64) - 8);print('fsize<<3:',fsize << 3);"
sha256sum shim
rm -rf ./*.o
# ./shim