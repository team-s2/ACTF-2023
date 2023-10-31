FROM ubuntu:22.04

RUN sed -i "s/http:\/\/archive.ubuntu.com/http:\/\/mirrors.tuna.tsinghua.edu.cn/g" /etc/apt/sources.list && \
    apt-get update && apt-get -y dist-upgrade && \
    apt-get install -y lib32z1 xinetd \
                       libpixman-1-dev libepoxy-dev libpng16-16 libjpeg8-dev \
                       libfdt-dev libnuma-dev libglib2.0-dev \
                       libgtk-3-dev libasound2-dev libcurl4 hashcash qemu-system

WORKDIR /dist

COPY ./proof-of-work.py /proof-of-work.py
COPY ./ctf.xinetd /etc/xinetd.d/ctf
COPY ./start.sh /start.sh

CMD ["/start.sh"]

EXPOSE 9999
