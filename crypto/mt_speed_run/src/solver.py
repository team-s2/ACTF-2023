from pwn import *
import os, time, base64, hashlib

NN = 30000
N = 624

rr = remote('120.46.65.156', 21111)#process(['python3', 'chall.py'])

rr.recvuntil(b'RANDS = ')
my_rands = bin(int.from_bytes(base64.b64decode(rr.recvline().strip()), 'big'))[2:].zfill(NN)
my_rands_values = [{'0': 0, '1': 1}[v] for v in my_rands]
rr.recvuntil(b'MAGIC_A = '); MAGIC_A = int(rr.recvline().strip(), 0)
rr.recvuntil(b'MAGIC_B = '); MAGIC_B = int(rr.recvline().strip(), 0)
rr.recvuntil(b'MAGIC_C = '); MAGIC_C = int(rr.recvline().strip(), 0)
rr.recvuntil(b'MAGIC_D = '); MAGIC_D = int(rr.recvline().strip(), 0)
rr.recvuntil(b'MAGIC_E = '); MAGIC_E = int(rr.recvline().strip(), 0)

start = time.time()
with open('test_my_rands.txt', 'w') as f:
    f.write(f'{my_rands}\n{MAGIC_A} {MAGIC_B} {MAGIC_C} {MAGIC_D} {MAGIC_E}\n')
os.system('./mtsolver > test_my_state.txt')
with open('test_my_state.txt', 'r') as f:
    state = list(map(int, f.read().strip().split(' ')))
os.remove('test_my_rands.txt')
os.remove('test_my_state.txt')
ans = hashlib.sha256(','.join(map(str, state)).encode()).hexdigest()
print(f'cost = {time.time() - start}s')

ans = hashlib.sha256(','.join(map(str, state)).encode()).hexdigest()
open('test_state.txt', 'w').write('\n'.join(map(str, state)))

print(f'{ans = }')
rr.sendlineafter(b'ANS = ', ans.encode())
print(rr.recvline().decode())
rr.close()
