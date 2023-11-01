CUR=$(pwd)
mkdir /tmp/cpiomake
cd /tmp/cpiomake
sudo cpio -idmv < $CUR/rootfs.cpio
cd $CUR
sudo gcc sandbox.c -o /tmp/cpiomake/bin/sandbox -static
cd /tmp/cpiomake
sudo find . | sudo cpio -o --format=newc > $CUR/rootfs.cpio