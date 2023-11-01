from Crypto.Util.number import (
    bytes_to_long, long_to_bytes
)
from pwn import *
from hashlib import md5
import os
import sys
import random
import time

BITS = 128


def transfer_frac(a):
    a = a[1:]
    if len(a) < 1: return 0, 0
    m = 1
    d = a[-1]
    for x in a[::-1][1:]:
        m, d = d, m + d*x
    return m, d

def fraction_range(p, q, upper, r):
    a = []
    while q:
        a.append(p // q)
        p, q = q, p % q
        m, d = transfer_frac(a)
        if d > upper: 
            break
    p, q = transfer_frac(a[:-1])
    return p, q

def find_seed(r, moves):
    def mapping(x):
        if x=='W': return "0"
        if x=='S': return "1"
        if x=='A': return "2"
        if x=='D': return "3"
    v = int("".join(map(mapping, moves)), 4)
    curTime = int(time.time())
    for i in range(100):
        t = curTime - i
        x = bytes_to_long(md5(
                    long_to_bytes((t + v) % pow(2,BITS))
                ).digest())
        if x == r:
            seed = (t + x + 1) % pow(2,BITS)
            return seed

def get_more_zero_md5(seed):
    for m in range(pow(2,32)):
        if md5(long_to_bytes((seed+m)%pow(2,128))).hexdigest().endswith('0'*7):
            return m

def trans_move(o):
    moves = ""
    while o:
        r = o%4
        if r==0: moves = "W" + moves
        if r==1: moves = "S" + moves
        if r==2: moves = "A" + moves
        if r==3: moves = "D" + moves
        o = o//4
    return 'W'*(64-len(moves))+moves

def to_target(m, pos):
    col, row = 0, 0
    for move in m:
        if move == "W":
            if row < 15: row += 1
        elif move == "S":
            if row > 0: row -= 1
        elif move == "A":
            if col > 0: col -= 1
        elif move == "D":
            if col < 15: col += 1
        else:
            return -1
    colDelta = pos[0] - col
    rowDelta = pos[1] - row
    if colDelta > 0:
        m += 'D'*colDelta
    elif colDelta < 0:
        m += 'A'*(-colDelta)
    if rowDelta > 0:
        m += 'W'*rowDelta
    elif rowDelta < 0:
        m += 'S'*(-rowDelta)
    
    return m

def step2num(s):
    def mapping(x):
        if x=='W': return "0"
        if x=='S': return "1"
        if x=='A': return "2"
        if x=='D': return "3"
    if type(s) == bytes:
        s = s.decode()
    v = int("".join(map(mapping, s)), 4)
    return v

def replay_attack(r, inp):
    return (inp-r-1) % pow(2,128)

# context.log_level = 'debug'      

for ii in range(2048):
    # io = remote("127.0.0.1", 19991)
    io = remote("120.46.65.156", 19991)
    choas = None
    inp = None
    sat = False
    score = 0
    for i in range(256):
        io.recvuntil(b"i am at ")
        pos = io.recvuntil(b", claw me").strip(b", claw me")
        pos = eval(pos)
        # print(pos)
        
        if i==0:
            moves = b"D"*pos[0] + b"W"*pos[1]
            inp = step2num(moves)
        else:
            inp = replay_attack(choas, inp)
            move = trans_move(inp)
            moves = to_target(move, pos)
        # moves = b"D"*pos[0] + b"W"*pos[1]
        
        io.sendlineafter(b"moves: ", moves)
        io.recvuntil(b"choas: ")
        choas = io.recvuntil(b"\n")
        choas = int(choas)
        # print(choas)
        p, q = fraction_range(choas, pow(2,128), pow(2,64), choas)
        p = p+pow(2,63)
        if p > pow(2,64):
            break
        delta = abs(choas*q - p*pow(2,BITS))
        bins = bin(delta)[2:]
        rate = bins.count('0') / len(bins)
        if rate < 220/256:
            break
        
        io.sendlineafter(b"(e.g.: 1,1): ", str(p)+","+str(q))
        io.recvuntil(b"your score:")
        score = int(io.recvuntil(b"\n").decode().strip())
        # print(i, score)

    # if score > 2000: 
    #     print(ii, score)
    #     input()
    
    if score >= 2220:
        print(ii, score)
        print(io.recvuntil(b"}"))
        io.close()
        break
    
    io.close()

# context.log_level = 'debug'      
        
# io = remote("127.0.0.1", 19991)
# choas = None
# for i in range(128):
#     io.recvuntil(b"i am at ")
#     pos = io.recvuntil(b", claw me").strip(b", claw me")
#     pos = eval(pos)
#     print(pos)
#     if i==1:
#         ctl = get_more_zero_md5(seed)
#         move = trans_move(ctl)
#         moves = to_target(move, pos)
#     if i>1:
#         ctl = (ctl - choas - 1) % pow(2,128)
#         move = trans_move(ctl)
#         moves = to_target(move, pos)
#     if i==0:
#         moves = b"D"*pos[0] + b"W"*pos[1]
#     io.sendlineafter(b"moves: ", moves)
#     io.recvuntil(b"choas: ")
#     choas = io.recvuntil(b"\n")
#     choas = int(choas)
#     print(choas)
#     if i==0:
#         seed = find_seed(choas, moves.decode())
#     p, q = fraction_range(choas, pow(2,128), pow(2,64))
#     io.sendlineafter(b"(e.g.: 1,1): ", str(p)+","+str(q))
#     io.recvuntil(b"your score:")
#     print(i, io.recvuntil(b"\n"))
    
# io.close()

    