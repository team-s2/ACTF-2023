

# Challenge design 

The version of QEMU that I used is qemu-8.0.0-rc2. This challenge only loads virtio-crypto device, which exists an heap-based overflow vulnerability with CVE-2023-3180 assigned. In order to simplify the exploitation, I add two patches ：

1、The first patch is as below. This patch makes virtio-crypto copy dst data to guest by call iov_from_buf even though the encryption/decrytion operation is failed, which can lead to heap-based overflow read. So you can leverage this to do some information leak.

```c
/* hw/virtio/virtio-crypto.c */

static void 
virtio_crypto_sym_input_data_helper(VirtIODevice *vdev,
                VirtIOCryptoReq *req,
                uint32_t status,
                CryptoDevBackendSymOpInfo *sym_op_info)
{
    size_t s, len;
    struct iovec *in_iov = req->in_iov;

//    if (status != VIRTIO_CRYPTO_OK) {
//        return;
//    }

    len = sym_op_info->src_len;
    /* Save the cipher result */
    s = iov_from_buf(in_iov, req->in_num, 0, sym_op_info->dst, len); //leak
    ...
}
```

2、The second patch is adding a g_free pointer in the end of CryptoDevBackendSymOpInfo structure, which will be called in the virtio_crypto_free_request function.

```c
/* hw/virtio/virtio-crypto.c */

static CryptoDevBackendSymOpInfo *
virtio_crypto_sym_op_helper(VirtIODevice *vdev,
           struct virtio_crypto_cipher_para *cipher_para,
           struct virtio_crypto_alg_chain_data_para *alg_chain_para,
           struct iovec *iov, unsigned int out_num)
{
		...
		*(uint64_t *)(op_info->data + curr_size) = g_free;
		...
}

static void virtio_crypto_free_request(VirtIOCryptoReq *req)
{
    if (!req) {
        return;
    }

    if (req->flags == QCRYPTODEV_BACKEND_ALG_SYM) {
        size_t max_len;
        CryptoDevBackendSymOpInfo *op_info = req->op_info.u.sym_op_info;

        max_len = op_info->iv_len +
                  op_info->aad_len +
                  op_info->src_len +
                  op_info->dst_len +
                  op_info->digest_result_len;

        /* Zeroize and free request data structure */
        memset(op_info, 0, sizeof(*op_info) + max_len);
        ((void (*)(void *))(*(uint64_t *)(op_info->data + max_len)))(op_info);
}
```

# Exploitation

So, this challenge is very simple by adding the two patches , first you can get information leak by using the heap-based overflow read, then you can overwrite the g_free pointer to control PC by using heap-based overflow write. 

Of course, you need to kown the interaction of virtio in QEMU.