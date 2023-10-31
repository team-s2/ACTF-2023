# MAnaGerfAker

## 题目信息

* 难度 - 中等

## 题目描述

This is a Crypto challenge in the Misc category.

It is known that using an unsafe hash algorithm is insecure.

Now, please prove this.

I hope you don’t use 100C server or 4090 to prove it.(doge)

Server can be access by `nc 120.46.65.156 32107`.

## 旧版题目描述

小明了解到了一款很火的Android root工具Kernel SU，并希望自己制作一个MAnaGerAPK来管理root应用。一番折腾后，小明很快注意到这需要改动内置在内核中APK签名摘要信息。而后小明看了看自己剩余容量5GB的硬盘，放弃了自己编译内核的想法。那么小明是否还有机会制作MAnaGerAPK呢？帮帮他，Mr. fAker！

## 原设计思路

下述内容已在新版 KernelSU 修复，见 [CVE-2023-5521](https://huntr.com/bounties/d438eff7-4e24-45e0-bc75-d3a5b3ab2ea1/)。

1. 见 https://android.googlesource.com/platform/tools/apksig/+/refs/tags/android-13.0.0_r59/src/main/java/com/android/apksig/internal/util/X509CertificateUtils.java
Android APK Signatures V2/V3 的验签机制允许附带的自签名 X509 证书使用 BER 格式，且允许使用 PEM 而非二进制格式。此时允许在末尾追加任意空白符。（这不是必要的，DER PEM格式用OpenSSL解析更宽松，<32的字符全是空白符）
1. 见 https://docs.oracle.com/javase/8/docs/api/java/lang/Character.html#isWhitespace-char-
显然，这样的空白符有很多。
1. 见 https://github.com/tiann/KernelSU/blob/0856b718defc558b0f0e6dfa423ea8f510a09c44/kernel/apk_sign.c#L111
KernelSU 使用 BKDR Hash 来校验证书内容。
1. 由于[APK Signature Scheme v3](https://source.android.com/docs/security/features/apksigning/v3)起支持密钥轮替，这使得这事实上常用程序可以发起实际攻击。

结论：

显然，通过制作 ASN.1 (BER 或 DER) PEM 格式的证书+追加特定空白符(可以考虑使用LLL)，可以快速伪造任意 BKDR Hash 的证书；但满足特定长度的证书，需要初始证书足够短，使用ECC而非RSA即可。

## 调整

与原设计思路相同，但不部署 Android 来校验证书。

具体解法见 [solver/cert_faker.py](./solver/cert_faker.py)。

## 关于对 apksigner.jar 的修改

附件和部署版本修改了 com.android.apksig.internal.util.X509CertificateUtils.java 使得它支持导出所有被读取过的证书用于验证。

solver的版本增加了对 com.android.apksig.internal.apk.ApkSigningBlockUtils.java 的修改，使得它支持导出初始欲写入签名块的证书和采用修改后的证书作为写入签名块的证书。
