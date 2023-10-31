from sage.all import *
from pwn import *
import os
from base64 import b64encode

rr = remote('120.46.65.156', 32107)#process(['python3', 'verifier.py'], preexec_fn=lambda:os.chdir('../deploy'))
rr.recvuntil(b'Wanted hash:')
target_length = 0x33b
target_value = int(rr.recvline().strip().decode(), 16)
print('target_hash =', hex(target_value))
white_space_default = ord(' ')
white_space_base = 0x1d
with open('original.pem', 'rb') as f:
    cert = f.read()

orig_len = len(cert)
assert orig_len < target_length, 'Certificate too long'

hash_mod = 1 << 32
hash_mask = hash_mod - 1
cert += b'\x00' * (target_length - orig_len)
cert = list(cert)
def sign_ext(val):
    if val >= 0x80:
        return val - 256 + hash_mod
    return val

base_hash = 1
for i in range(target_length):
    base_hash = (base_hash * 31 + sign_ext(cert[i])) & hash_mask

selections = []
power = 1
choices_cnt = 1
for i in range(orig_len, target_length)[::-1]:
    if choices_cnt > (hash_mod << 8):
        base_hash += (sign_ext(white_space_default) * power) & hash_mask
        base_hash &= hash_mask
        cert[i] = white_space_default
    else:
        base_hash += (sign_ext(white_space_base) * power) & hash_mask
        base_hash &= hash_mask
        cert[i] = white_space_base
        selections.append(power)
    choices_cnt *= 3
    power *= 31
    power &= hash_mask

slen = len(selections)
A = matrix(ZZ, slen + 2, slen + 2)
B = 1 << 48

for (i, power) in enumerate(selections):
    A[i, i] = 1
    A[i, slen] = power * B

A[slen, slen] = -((target_value - base_hash) % hash_mod) * B
print('required_diff =', hex(((target_value - base_hash) % hash_mod)))
A[slen, slen + 1] = B
A[slen + 1, slen] = hash_mod * B
L = A.LLL()
ans = None
for line in L:
    if line[-1] == B and line[-2] == 0:
        ans = line[:-2]
        print('LLL ans =', ans)
        break

assert ans is not None, "no solution"

for i, (j, power) in enumerate(zip(range(orig_len, target_length)[::-1], selections)):
    cert[j] += ans[i]
    base_hash += ans[i] * power
    base_hash &= hash_mask

print('expected_hash =', hex(base_hash))
base_hash = 1
for i in range(target_length):
    base_hash = (base_hash * 31 + sign_ext(cert[i])) & hash_mask
print('calc_hash =', hex(base_hash))
with open('x509_faked.pem', 'wb') as f:
    f.write(bytes(cert))

os.system('java -jar apksigner.jar sign --verbose --v1-signer-name CERT --ks "test.keystore" --ks-pass pass:123456 app-test.apk')
rr.recvuntil(b'new line):')
data = b64encode(open('app-test.apk', 'rb').read())
for i in range(0, len(data), 1024):
    rr.sendline(data[i:i+1024])
rr.sendline(b'EOF')
os.system('rm x509*')
rr.interactive()