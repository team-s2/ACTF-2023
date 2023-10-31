# docker build -t mt_spg:latest .
# docker run -idt -p 21111:21111 mt_spg:latest
FROM ubuntu:22.04

RUN sed -i "s/http:\/\/archive.ubuntu.com/http:\/\/mirrors.tuna.tsinghua.edu.cn/g" /etc/apt/sources.list && \
    apt-get update && apt-get -y dist-upgrade && \
    apt-get install socat python3 python3-pip -y

WORKDIR /app
COPY ./server.py /app
COPY ./secret.py /app
RUN chmod -R 777 /app

EXPOSE 21111

CMD ["socat", "TCP-LISTEN:21111,reuseaddr,fork", "EXEC:python3 server.py,nofork,stderr"]