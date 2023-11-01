def rc4_encrypt(data, key):
    S = list(range(256))
    K = key.copy()
    key_len = len(K)

    j = 0
    for i in range(256):
        j = (j + S[i] + K[i % key_len]) & 0xff
        S[i], S[j] = S[j], S[i]

    i = k = 0
    cipher = [0] * len(data)
    for n in range(len(data)):
        i = (i + 1) & 0xff
        j = (j + S[i]) & 0xff
        S[i], S[j] = S[j], S[i]
        k = (S[i] + S[j]) & 0xff
        cipher[n] = data[n] ^ S[k]

    return cipher

key = [140, 136, 210, 238, 167, 102, 222, 38,]
cmp = [184,132,137,215,146,65,86,157,123,100,179,131,112,170,97,210,163,179,17,171,245,30,194,144,37,41,235,121,146,210,174,92,204,22]

a = rc4_encrypt(cmp,key)
print(bytes([0xff ^ b for b in a]))
