# sudo docker build -t clawcrane:latest .
# sudo docker run -p 19991:19991 clawcrane:latest
FROM ubuntu:20.04

RUN apt-get -qq update && apt-get install -qq --no-install-recommends xinetd python3-pip
RUN pip3 install pycryptodome
RUN apt-get install -qq --no-install-recommends netcat

COPY task.py /
COPY service.conf /
COPY wrapper /
COPY flag.txt /


RUN chmod +x /task.py
RUN chmod +x /service.conf /wrapper

EXPOSE 19991

CMD ["/usr/sbin/xinetd", "-filelog", "/dev/stderr", "-dontfork", "-f", "/service.conf"]
