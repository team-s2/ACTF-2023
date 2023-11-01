from Crypto.Util.strxor import strxor

l = bytes([154, 216, 141, 169, 194, 158, 162, 171, 212, 169, 180, 129, 171, 180, 241, 187, 168, 255, 134, 179, 141, 164, 146, 251, 211, 171, 180, 175, 184, 145, 180, 239])
r = bytes([151, 3, 208, 203, 241, 105, 239, 128, 211, 82, 218, 156, 133, 7, 235, 178, 163, 127, 160, 165, 245, 34, 158, 236, 132, 90, 101, 91, 123, 223, 54, 75])
k = p32(0x1234ac7f) * (0x20 // 4)
for i in range(10):
    xorstr = bytes([i * 0x11 + j for j in range(len(k))])
    k = strxor(k, xorstr)
for i in range(9, -1, -1):
    xorstr = bytes([i * 0x11 + j for j in range(len(k))])
    l, r, k = strxor(strxor(l, r), k), l, strxor(k, xorstr)
print(l + r)
