package com.android.apksig.internal.apk;

import com.android.apksig.ApkVerifier;
import com.android.apksig.SigningCertificateLineage;
import com.android.apksig.apk.ApkFormatException;
import com.android.apksig.apk.ApkUtils;
import com.android.apksig.internal.apk.ApkSigningBlockUtils.ChunkDigester;
import com.android.apksig.internal.asn1.Asn1BerParser;
import com.android.apksig.internal.asn1.Asn1DecodingException;
import com.android.apksig.internal.asn1.Asn1DerEncoder;
import com.android.apksig.internal.asn1.Asn1EncodingException;
import com.android.apksig.internal.asn1.Asn1OpaqueObject;
import com.android.apksig.internal.pkcs7.AlgorithmIdentifier;
import com.android.apksig.internal.pkcs7.ContentInfo;
import com.android.apksig.internal.pkcs7.EncapsulatedContentInfo;
import com.android.apksig.internal.pkcs7.IssuerAndSerialNumber;
import com.android.apksig.internal.pkcs7.Pkcs7Constants;
import com.android.apksig.internal.pkcs7.SignedData;
import com.android.apksig.internal.pkcs7.SignerIdentifier;
import com.android.apksig.internal.pkcs7.SignerInfo;
import com.android.apksig.internal.util.ByteBufferDataSource;
import com.android.apksig.internal.util.ChainedDataSource;
import com.android.apksig.internal.util.Pair;
import com.android.apksig.internal.util.VerityTreeBuilder;
import com.android.apksig.internal.util.X509CertificateUtils;
import com.android.apksig.internal.x509.RSAPublicKey;
import com.android.apksig.internal.x509.SubjectPublicKeyInfo;
import com.android.apksig.internal.zip.ZipUtils;
import com.android.apksig.util.DataSink;
import com.android.apksig.util.DataSinks;
import com.android.apksig.util.DataSource;
import com.android.apksig.util.DataSources;
import com.android.apksig.util.RunnablesExecutor;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javax.security.auth.x500.X500Principal;
import java.util.Base64;

/* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils.class */
public class ApkSigningBlockUtils {
    private static final long CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES = 1048576;
    public static final int ANDROID_COMMON_PAGE_ALIGNMENT_BYTES = 4096;
    private static final int VERITY_PADDING_BLOCK_ID = 1114793335;
    public static final int VERSION_SOURCE_STAMP = 0;
    public static final int VERSION_JAR_SIGNATURE_SCHEME = 1;
    public static final int VERSION_APK_SIGNATURE_SCHEME_V2 = 2;
    public static final int VERSION_APK_SIGNATURE_SCHEME_V3 = 3;
    public static final int VERSION_APK_SIGNATURE_SCHEME_V4 = 4;
    private static final byte[] APK_SIGNING_BLOCK_MAGIC = {65, 80, 75, 32, 83, 105, 103, 32, 66, 108, 111, 99, 107, 32, 52, 50};
    private static final ContentDigestAlgorithm[] V4_CONTENT_DIGEST_ALGORITHMS = {ContentDigestAlgorithm.CHUNKED_SHA512, ContentDigestAlgorithm.VERITY_CHUNKED_SHA256, ContentDigestAlgorithm.CHUNKED_SHA256};

    /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$SignerConfig.class */
    public static class SignerConfig {
        public PrivateKey privateKey;
        public List<X509Certificate> certificates;
        public List<SignatureAlgorithm> signatureAlgorithms;
        public int minSdkVersion;
        public int maxSdkVersion;
        public SigningCertificateLineage mSigningCertificateLineage;
    }

    public static int compareSignatureAlgorithm(SignatureAlgorithm alg1, SignatureAlgorithm alg2) {
        return ApkSigningBlockUtilsLite.compareSignatureAlgorithm(alg1, alg2);
    }

    public static void verifyIntegrity(RunnablesExecutor executor, DataSource beforeApkSigningBlock, DataSource centralDir, ByteBuffer eocd, Set<ContentDigestAlgorithm> contentDigestAlgorithms, Result result) throws IOException, NoSuchAlgorithmException {
        if (contentDigestAlgorithms.isEmpty()) {
            throw new RuntimeException("No content digests found");
        }
        ByteBuffer modifiedEocd = ByteBuffer.allocate(eocd.remaining());
        int eocdSavedPos = eocd.position();
        modifiedEocd.order(ByteOrder.LITTLE_ENDIAN);
        modifiedEocd.put(eocd);
        modifiedEocd.flip();
        eocd.position(eocdSavedPos);
        ZipUtils.setZipEocdCentralDirectoryOffset(modifiedEocd, beforeApkSigningBlock.size());
        try {
            Map<ContentDigestAlgorithm, byte[]> actualContentDigests = computeContentDigests(executor, contentDigestAlgorithms, beforeApkSigningBlock, centralDir, new ByteBufferDataSource(modifiedEocd));
            if (actualContentDigests.containsKey(ContentDigestAlgorithm.VERITY_CHUNKED_SHA256)) {
                if (beforeApkSigningBlock.size() % 4096 != 0) {
                    throw new RuntimeException("APK Signing Block is not aligned on 4k boundary: " + beforeApkSigningBlock.size());
                }
                long centralDirOffset = ZipUtils.getZipEocdCentralDirectoryOffset(eocd);
                long signingBlockSize = centralDirOffset - beforeApkSigningBlock.size();
                if (signingBlockSize % 4096 != 0) {
                    throw new RuntimeException("APK Signing Block size is not multiple of page size: " + signingBlockSize);
                }
            }
            if (!contentDigestAlgorithms.equals(actualContentDigests.keySet())) {
                throw new RuntimeException("Mismatch between sets of requested and computed content digests . Requested: " + contentDigestAlgorithms + ", computed: " + actualContentDigests.keySet());
            }
            for (Result.SignerInfo signerInfo : result.signers) {
                for (Result.SignerInfo.ContentDigest expected : signerInfo.contentDigests) {
                    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.findById(expected.getSignatureAlgorithmId());
                    if (signatureAlgorithm != null) {
                        ContentDigestAlgorithm contentDigestAlgorithm = signatureAlgorithm.getContentDigestAlgorithm();
                        if (contentDigestAlgorithms.contains(contentDigestAlgorithm)) {
                            byte[] expectedDigest = expected.getValue();
                            byte[] actualDigest = actualContentDigests.get(contentDigestAlgorithm);
                            if (!Arrays.equals(expectedDigest, actualDigest)) {
                                if (result.signatureSchemeVersion == 2) {
                                    signerInfo.addError(ApkVerifier.Issue.V2_SIG_APK_DIGEST_DID_NOT_VERIFY, contentDigestAlgorithm, toHex(expectedDigest), toHex(actualDigest));
                                } else if (result.signatureSchemeVersion == 3) {
                                    signerInfo.addError(ApkVerifier.Issue.V3_SIG_APK_DIGEST_DID_NOT_VERIFY, contentDigestAlgorithm, toHex(expectedDigest), toHex(actualDigest));
                                }
                            } else {
                                signerInfo.verifiedContentDigests.put(contentDigestAlgorithm, actualDigest);
                            }
                        }
                    }
                }
            }
        } catch (DigestException e) {
            throw new RuntimeException("Failed to compute content digests", e);
        }
    }

    public static ByteBuffer findApkSignatureSchemeBlock(ByteBuffer apkSigningBlock, int blockId, Result result) throws SignatureNotFoundException {
        try {
            return ApkSigningBlockUtilsLite.findApkSignatureSchemeBlock(apkSigningBlock, blockId);
        } catch (com.android.apksig.internal.apk.SignatureNotFoundException e) {
            throw new SignatureNotFoundException(e.getMessage());
        }
    }

    public static void checkByteOrderLittleEndian(ByteBuffer buffer) {
        ApkSigningBlockUtilsLite.checkByteOrderLittleEndian(buffer);
    }

    public static ByteBuffer getLengthPrefixedSlice(ByteBuffer source) throws ApkFormatException {
        return ApkSigningBlockUtilsLite.getLengthPrefixedSlice(source);
    }

    public static byte[] readLengthPrefixedByteArray(ByteBuffer buf) throws ApkFormatException {
        return ApkSigningBlockUtilsLite.readLengthPrefixedByteArray(buf);
    }

    public static String toHex(byte[] value) {
        return ApkSigningBlockUtilsLite.toHex(value);
    }

    public static Map<ContentDigestAlgorithm, byte[]> computeContentDigests(RunnablesExecutor executor, Set<ContentDigestAlgorithm> digestAlgorithms, DataSource beforeCentralDir, DataSource centralDir, DataSource eocd) throws IOException, NoSuchAlgorithmException, DigestException {
        Map<ContentDigestAlgorithm, byte[]> contentDigests = new HashMap<>();
        Set<ContentDigestAlgorithm> oneMbChunkBasedAlgorithm = new HashSet<>();
        for (ContentDigestAlgorithm digestAlgorithm : digestAlgorithms) {
            if (digestAlgorithm == ContentDigestAlgorithm.CHUNKED_SHA256 || digestAlgorithm == ContentDigestAlgorithm.CHUNKED_SHA512) {
                oneMbChunkBasedAlgorithm.add(digestAlgorithm);
            }
        }
        computeOneMbChunkContentDigests(executor, oneMbChunkBasedAlgorithm, new DataSource[]{beforeCentralDir, centralDir, eocd}, contentDigests);
        if (digestAlgorithms.contains(ContentDigestAlgorithm.VERITY_CHUNKED_SHA256)) {
            computeApkVerityDigest(beforeCentralDir, centralDir, eocd, contentDigests);
        }
        return contentDigests;
    }

    /* JADX WARN: Multi-variable type inference failed */
    static void computeOneMbChunkContentDigests(Set<ContentDigestAlgorithm> digestAlgorithms, DataSource[] contents, Map<ContentDigestAlgorithm, byte[]> outputContentDigests) throws IOException, NoSuchAlgorithmException, DigestException {
        long chunkCountLong = 0;
        for (DataSource input : contents) {
            chunkCountLong += getChunkCount(input.size(), CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES);
        }
        if (chunkCountLong > 2147483647L) {
            throw new DigestException("Input too long: " + chunkCountLong + " chunks");
        }
        int chunkCount = (int) chunkCountLong;
        ContentDigestAlgorithm[] digestAlgorithmsArray = (ContentDigestAlgorithm[]) digestAlgorithms.toArray(new ContentDigestAlgorithm[digestAlgorithms.size()]);
        MessageDigest[] mds = new MessageDigest[digestAlgorithmsArray.length];
        byte[][] bArr = new byte[digestAlgorithmsArray.length][];
        int[] digestOutputSizes = new int[digestAlgorithmsArray.length];
        for (int i = 0; i < digestAlgorithmsArray.length; i++) {
            ContentDigestAlgorithm digestAlgorithm = digestAlgorithmsArray[i];
            int digestOutputSizeBytes = digestAlgorithm.getChunkDigestOutputSizeBytes();
            digestOutputSizes[i] = digestOutputSizeBytes;
            byte[] concatenationOfChunkCountAndChunkDigests = new byte[5 + (chunkCount * digestOutputSizeBytes)];
            concatenationOfChunkCountAndChunkDigests[0] = 90;
            setUnsignedInt32LittleEndian(chunkCount, concatenationOfChunkCountAndChunkDigests, 1);
            bArr[i] = concatenationOfChunkCountAndChunkDigests;
            String jcaAlgorithm = digestAlgorithm.getJcaMessageDigestAlgorithm();
            mds[i] = MessageDigest.getInstance(jcaAlgorithm);
        }
        DataSink mdSink = DataSinks.asDataSink(mds);
        byte[] chunkContentPrefix = new byte[5];
        chunkContentPrefix[0] = -91;
        int chunkIndex = 0;
        for (DataSource input2 : contents) {
            long inputOffset = 0;
            long inputRemaining = input2.size();
            while (inputRemaining > 0) {
                int chunkSize = (int) Math.min(inputRemaining, (long) CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES);
                setUnsignedInt32LittleEndian(chunkSize, chunkContentPrefix, 1);
                for (MessageDigest messageDigest : mds) {
                    messageDigest.update(chunkContentPrefix);
                }
                try {
                    input2.feed(inputOffset, chunkSize, mdSink);
                    for (int i2 = 0; i2 < digestAlgorithmsArray.length; i2++) {
                        MessageDigest md = mds[i2];
                        byte[] concatenationOfChunkCountAndChunkDigests2 = bArr[i2];
                        int expectedDigestSizeBytes = digestOutputSizes[i2];
                        int actualDigestSizeBytes = md.digest(concatenationOfChunkCountAndChunkDigests2, 5 + (chunkIndex * expectedDigestSizeBytes), expectedDigestSizeBytes);
                        if (actualDigestSizeBytes != expectedDigestSizeBytes) {
                            throw new RuntimeException("Unexpected output size of " + md.getAlgorithm() + " digest: " + actualDigestSizeBytes);
                        }
                    }
                    inputOffset += chunkSize;
                    inputRemaining -= chunkSize;
                    chunkIndex++;
                } catch (IOException e) {
                    throw new IOException("Failed to read chunk #" + chunkIndex, e);
                }
            }
        }
        for (int i3 = 0; i3 < digestAlgorithmsArray.length; i3++) {
            ContentDigestAlgorithm digestAlgorithm2 = digestAlgorithmsArray[i3];
            byte[] concatenationOfChunkCountAndChunkDigests3 = bArr[i3];
            byte[] digest = mds[i3].digest(concatenationOfChunkCountAndChunkDigests3);
            outputContentDigests.put(digestAlgorithm2, digest);
        }
    }

    static void computeOneMbChunkContentDigests(RunnablesExecutor executor, Set<ContentDigestAlgorithm> digestAlgorithms, DataSource[] contents, Map<ContentDigestAlgorithm, byte[]> outputContentDigests) throws NoSuchAlgorithmException, DigestException {
        long chunkCountLong = 0;
        for (DataSource input : contents) {
            chunkCountLong += getChunkCount(input.size(), CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES);
        }
        if (chunkCountLong > 2147483647L) {
            throw new DigestException("Input too long: " + chunkCountLong + " chunks");
        }
        int chunkCount = (int) chunkCountLong;
        List<ChunkDigests> chunkDigestsList = new ArrayList<>(digestAlgorithms.size());
        for (ContentDigestAlgorithm algorithms : digestAlgorithms) {
            chunkDigestsList.add(new ChunkDigests(algorithms, chunkCount));
        }
        ChunkSupplier chunkSupplier = new ChunkSupplier(contents);
        executor.execute(() -> {
            return new ChunkDigester(chunkSupplier, chunkDigestsList);
        });
        for (ChunkDigests chunkDigests : chunkDigestsList) {
            MessageDigest messageDigest = chunkDigests.createMessageDigest();
            outputContentDigests.put(chunkDigests.algorithm, messageDigest.digest(chunkDigests.concatOfDigestsOfChunks));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$ChunkDigests.class */
    public static class ChunkDigests {
        private final ContentDigestAlgorithm algorithm;
        private final int digestOutputSize;
        private final byte[] concatOfDigestsOfChunks;

        private ChunkDigests(ContentDigestAlgorithm algorithm, int chunkCount) {
            this.algorithm = algorithm;
            this.digestOutputSize = this.algorithm.getChunkDigestOutputSizeBytes();
            this.concatOfDigestsOfChunks = new byte[5 + (chunkCount * this.digestOutputSize)];
            this.concatOfDigestsOfChunks[0] = 90;
            ApkSigningBlockUtils.setUnsignedInt32LittleEndian(chunkCount, this.concatOfDigestsOfChunks, 1);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public MessageDigest createMessageDigest() throws NoSuchAlgorithmException {
            return MessageDigest.getInstance(this.algorithm.getJcaMessageDigestAlgorithm());
        }

        /* JADX INFO: Access modifiers changed from: private */
        public int getOffset(int chunkIndex) {
            return 5 + (chunkIndex * this.digestOutputSize);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$ChunkDigester.class */
    public static class ChunkDigester implements Runnable {
        private final ChunkSupplier dataSupplier;
        private final List<ChunkDigests> chunkDigests;
        private final List<MessageDigest> messageDigests;
        private final DataSink mdSink;

        private ChunkDigester(ChunkSupplier dataSupplier, List<ChunkDigests> chunkDigests) {
            this.dataSupplier = dataSupplier;
            this.chunkDigests = chunkDigests;
            this.messageDigests = new ArrayList<>(chunkDigests.size());
            for (ChunkDigests chunkDigest : chunkDigests) {
                try {
                    this.messageDigests.add(chunkDigest.createMessageDigest());
                } catch (NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                }
            }
            this.mdSink = DataSinks.asDataSink((MessageDigest[]) this.messageDigests.toArray(new MessageDigest[0]));
        }

        @Override // java.lang.Runnable
        public void run() {
            byte[] chunkContentPrefix = new byte[5];
            chunkContentPrefix[0] = -91;
            try {
                ChunkSupplier.Chunk chunk = this.dataSupplier.get();
                while (chunk != null) {
                    int size = chunk.size;
                    if (size <= ApkSigningBlockUtils.CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES) {
                        ApkSigningBlockUtils.setUnsignedInt32LittleEndian(size, chunkContentPrefix, 1);
                        this.mdSink.consume(chunkContentPrefix, 0, chunkContentPrefix.length);
                        this.mdSink.consume(chunk.data);
                        for (int i = 0; i < this.chunkDigests.size(); i++) {
                            ChunkDigests chunkDigest = this.chunkDigests.get(i);
                            int actualDigestSize = this.messageDigests.get(i).digest(chunkDigest.concatOfDigestsOfChunks, chunkDigest.getOffset(chunk.chunkIndex), chunkDigest.digestOutputSize);
                            if (actualDigestSize != chunkDigest.digestOutputSize) {
                                throw new RuntimeException("Unexpected output size of " + chunkDigest.algorithm + " digest: " + actualDigestSize);
                            }
                        }
                        chunk = this.dataSupplier.get();
                    } else {
                        throw new RuntimeException("Chunk size greater than expected: " + size);
                    }
                }
            } catch (IOException | DigestException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$ChunkSupplier.class */
    public static class ChunkSupplier implements Supplier<ChunkSupplier.Chunk> {
        private final DataSource[] dataSources;
        private final int[] chunkCounts;
        private final int totalChunkCount;
        private final AtomicInteger nextIndex;

        private ChunkSupplier(DataSource[] dataSources) {
            this.dataSources = dataSources;
            this.chunkCounts = new int[dataSources.length];
            int totalChunkCount = 0;
            for (int i = 0; i < dataSources.length; i++) {
                long chunkCount = ApkSigningBlockUtils.getChunkCount(dataSources[i].size(), ApkSigningBlockUtils.CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES);
                if (chunkCount > 2147483647L) {
                    throw new RuntimeException(String.format("Number of chunks in dataSource[%d] is greater than max int.", Integer.valueOf(i)));
                }
                this.chunkCounts[i] = (int) chunkCount;
                totalChunkCount = (int) (totalChunkCount + chunkCount);
            }
            this.totalChunkCount = totalChunkCount;
            this.nextIndex = new AtomicInteger(0);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // java.util.function.Supplier
        public Chunk get() {
            int index = this.nextIndex.getAndIncrement();
            if (index < 0 || index >= this.totalChunkCount) {
                return null;
            }
            int dataSourceIndex = 0;
            long dataSourceChunkOffset = index;
            while (dataSourceIndex < this.dataSources.length && dataSourceChunkOffset >= this.chunkCounts[dataSourceIndex]) {
                dataSourceChunkOffset -= this.chunkCounts[dataSourceIndex];
                dataSourceIndex++;
            }
            long remainingSize = Math.min(this.dataSources[dataSourceIndex].size() - (dataSourceChunkOffset * ApkSigningBlockUtils.CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES), (long) ApkSigningBlockUtils.CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES);
            int size = (int) remainingSize;
            ByteBuffer buffer = ByteBuffer.allocate(size);
            try {
                this.dataSources[dataSourceIndex].copyTo(dataSourceChunkOffset * ApkSigningBlockUtils.CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES, size, buffer);
                buffer.rewind();
                return new Chunk(index, buffer, size);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read chunk", e);
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$ChunkSupplier$Chunk.class */
        public static class Chunk {
            private final int chunkIndex;
            private final ByteBuffer data;
            private final int size;

            private Chunk(int chunkIndex, ByteBuffer data, int size) {
                this.chunkIndex = chunkIndex;
                this.data = data;
                this.size = size;
            }
        }
    }

    private static void computeApkVerityDigest(DataSource beforeCentralDir, DataSource centralDir, DataSource eocd, Map<ContentDigestAlgorithm, byte[]> outputContentDigests) throws IOException, NoSuchAlgorithmException {
        ByteBuffer encoded = createVerityDigestBuffer(true);
        VerityTreeBuilder builder = new VerityTreeBuilder(new byte[8]);
        try {
            byte[] rootHash = builder.generateVerityTreeRootHash(beforeCentralDir, centralDir, eocd);
            encoded.put(rootHash);
            encoded.putLong(beforeCentralDir.size() + centralDir.size() + eocd.size());
            outputContentDigests.put(ContentDigestAlgorithm.VERITY_CHUNKED_SHA256, encoded.array());
            builder.close();
        } catch (Throwable th) {
            try {
                builder.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    private static ByteBuffer createVerityDigestBuffer(boolean includeSourceDataSize) {
        int backBufferSize = ContentDigestAlgorithm.VERITY_CHUNKED_SHA256.getChunkDigestOutputSizeBytes();
        if (includeSourceDataSize) {
            backBufferSize += 8;
        }
        ByteBuffer encoded = ByteBuffer.allocate(backBufferSize);
        encoded.order(ByteOrder.LITTLE_ENDIAN);
        return encoded;
    }

    /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$VerityTreeAndDigest.class */
    public static class VerityTreeAndDigest {
        public final ContentDigestAlgorithm contentDigestAlgorithm;
        public final byte[] rootHash;
        public final byte[] tree;

        VerityTreeAndDigest(ContentDigestAlgorithm contentDigestAlgorithm, byte[] rootHash, byte[] tree) {
            this.contentDigestAlgorithm = contentDigestAlgorithm;
            this.rootHash = rootHash;
            this.tree = tree;
        }
    }

    public static VerityTreeAndDigest computeChunkVerityTreeAndDigest(DataSource dataSource) throws IOException, NoSuchAlgorithmException {
        ByteBuffer encoded = createVerityDigestBuffer(false);
        VerityTreeBuilder builder = new VerityTreeBuilder(null);
        try {
            ByteBuffer tree = builder.generateVerityTree(dataSource);
            byte[] rootHash = builder.getRootHashFromTree(tree);
            encoded.put(rootHash);
            VerityTreeAndDigest verityTreeAndDigest = new VerityTreeAndDigest(ContentDigestAlgorithm.VERITY_CHUNKED_SHA256, encoded.array(), tree.array());
            builder.close();
            return verityTreeAndDigest;
        } catch (Throwable th) {
            try {
                builder.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static long getChunkCount(long inputSize, long chunkSize) {
        return ((inputSize + chunkSize) - 1) / chunkSize;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void setUnsignedInt32LittleEndian(int value, byte[] result, int offset) {
        result[offset] = (byte) (value & 255);
        result[offset + 1] = (byte) ((value >> 8) & 255);
        result[offset + 2] = (byte) ((value >> 16) & 255);
        result[offset + 3] = (byte) ((value >> 24) & 255);
    }

    public static byte[] encodePublicKey(PublicKey publicKey) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] encodedPublicKey = null;
        if ("X.509".equals(publicKey.getFormat())) {
            encodedPublicKey = publicKey.getEncoded();
            if ("RSA".equals(publicKey.getAlgorithm())) {
                try {
                    ByteBuffer encodedPublicKeyBuffer = ByteBuffer.wrap(encodedPublicKey);
                    SubjectPublicKeyInfo subjectPublicKeyInfo = (SubjectPublicKeyInfo) Asn1BerParser.parse(encodedPublicKeyBuffer, SubjectPublicKeyInfo.class);
                    ByteBuffer subjectPublicKeyBuffer = subjectPublicKeyInfo.subjectPublicKey;
                    byte padding = subjectPublicKeyBuffer.get();
                    RSAPublicKey rsaPublicKey = (RSAPublicKey) Asn1BerParser.parse(subjectPublicKeyBuffer, RSAPublicKey.class);
                    if (rsaPublicKey.modulus.compareTo(BigInteger.ZERO) < 0) {
                        byte[] encodedModulus = rsaPublicKey.modulus.toByteArray();
                        byte[] reencodedModulus = new byte[encodedModulus.length + 1];
                        reencodedModulus[0] = 0;
                        System.arraycopy(encodedModulus, 0, reencodedModulus, 1, encodedModulus.length);
                        rsaPublicKey.modulus = new BigInteger(reencodedModulus);
                        byte[] reencodedRSAPublicKey = Asn1DerEncoder.encode(rsaPublicKey);
                        byte[] reencodedSubjectPublicKey = new byte[reencodedRSAPublicKey.length + 1];
                        reencodedSubjectPublicKey[0] = padding;
                        System.arraycopy(reencodedRSAPublicKey, 0, reencodedSubjectPublicKey, 1, reencodedRSAPublicKey.length);
                        subjectPublicKeyInfo.subjectPublicKey = ByteBuffer.wrap(reencodedSubjectPublicKey);
                        encodedPublicKey = Asn1DerEncoder.encode(subjectPublicKeyInfo);
                    }
                } catch (Asn1DecodingException | Asn1EncodingException e) {
                    System.out.println("Caught a exception encoding the public key: " + e);
                    e.printStackTrace();
                    encodedPublicKey = null;
                }
            }
        }
        if (encodedPublicKey == null) {
            try {
                encodedPublicKey = ((X509EncodedKeySpec) KeyFactory.getInstance(publicKey.getAlgorithm()).getKeySpec(publicKey, X509EncodedKeySpec.class)).getEncoded();
            } catch (InvalidKeySpecException e2) {
                throw new InvalidKeyException("Failed to obtain X.509 encoded form of public key " + publicKey + " of class " + publicKey.getClass().getName(), e2);
            }
        }
        if (encodedPublicKey == null || encodedPublicKey.length == 0) {
            throw new InvalidKeyException("Failed to obtain X.509 encoded form of public key " + publicKey + " of class " + publicKey.getClass().getName());
        }
        return encodedPublicKey;
    }

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    public final static String LINE_SEPARATOR = "\n";

    public static String DERToPEM(byte[] bytes) {
        final Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());
        final String encodedCertText = new String(encoder.encode(bytes));
        final String prettified_cert = BEGIN_CERT + LINE_SEPARATOR + encodedCertText + LINE_SEPARATOR + END_CERT;
        return prettified_cert;
    }

    public static int counter = 0;

    public static List<byte[]> encodeCertificates(List<X509Certificate> certificates) throws CertificateEncodingException {
        List<byte[]> result = new ArrayList<>(certificates.size());
        for (X509Certificate certificate : certificates) {
            byte[] encoded = certificate.getEncoded();
            try {
                System.out.printf("dumped %d\n", counter);
                Files.write(Paths.get("x509_dumped_" + counter + ".cer"), encoded);
                Files.write(Paths.get("x509_dumped_" + counter + ".pem"), DERToPEM(encoded).getBytes());
                byte[] draft = null;
                if (new File("x509_faked_" + counter + ".pem").exists()) {
                    System.out.printf("replaced %d\n", counter);
                    draft = Files.readAllBytes(Paths.get("x509_faked_" + counter + ".pem"));
                } else if (new File("x509_faked.pem").exists()) {
                    System.out.printf("replaced %d[global]\n", counter);
                    draft = Files.readAllBytes(Paths.get("x509_faked.pem"));
                }
                if (draft != null) {
                    CertificateFactory certFactory;
                    try {
                        certFactory = CertificateFactory.getInstance("X.509");
                    } catch (CertificateException e) {
                        throw new RuntimeException("Failed to obtain X.509 CertificateFactory", e);
                    }
                    try {
                        X509CertificateUtils.enableExport = false;
                        byte[] draftReencoded = X509CertificateUtils.generateCertificate(draft, certFactory).getEncoded();
                        if (!Arrays.equals(encoded, draftReencoded)) {
                            throw new CertificateEncodingException("draft and original not equal");
                        }
                        encoded = draft;
                    } catch (CertificateException e) {
                        System.out.println("replace failed due to " + e);
                    } finally {
                        X509CertificateUtils.enableExport = true;
                    }
                }
                counter += 1;
            } catch (IOException err) {
                err.printStackTrace();
            }
            result.add(encoded);
        }
        return result;
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [byte[], byte[][]] */
    public static byte[] encodeAsLengthPrefixedElement(byte[] bytes) {
        byte[][] adapterBytes = new byte[1][];
        adapterBytes[0] = bytes;
        return encodeAsSequenceOfLengthPrefixedElements(adapterBytes);
    }

    public static byte[] encodeAsSequenceOfLengthPrefixedElements(List<byte[]> sequence) {
        return encodeAsSequenceOfLengthPrefixedElements(
                sequence.toArray(new byte[sequence.size()][]));
    }

    public static byte[] encodeAsSequenceOfLengthPrefixedElements(byte[][] sequence) {
        int payloadSize = 0;
        for (byte[] element : sequence) {
            payloadSize += 4 + element.length;
        }
        ByteBuffer result = ByteBuffer.allocate(payloadSize);
        result.order(ByteOrder.LITTLE_ENDIAN);
        for (byte[] element2 : sequence) {
            result.putInt(element2.length);
            result.put(element2);
        }
        return result.array();
    }

    public static byte[] encodeAsSequenceOfLengthPrefixedPairsOfIntAndLengthPrefixedBytes(List<Pair<Integer, byte[]>> sequence) {
        return ApkSigningBlockUtilsLite.encodeAsSequenceOfLengthPrefixedPairsOfIntAndLengthPrefixedBytes(sequence);
    }

    public static SignatureInfo findSignature(DataSource apk, ApkUtils.ZipSections zipSections, int blockId, Result result) throws IOException, SignatureNotFoundException {
        try {
            return ApkSigningBlockUtilsLite.findSignature(apk, zipSections, blockId);
        } catch (com.android.apksig.internal.apk.SignatureNotFoundException e) {
            throw new SignatureNotFoundException(e.getMessage());
        }
    }

    public static Pair<DataSource, Integer> generateApkSigningBlockPadding(DataSource beforeCentralDir, boolean apkSigningBlockPaddingSupported) {
        int padSizeBeforeSigningBlock = 0;
        if (apkSigningBlockPaddingSupported && beforeCentralDir.size() % 4096 != 0) {
            padSizeBeforeSigningBlock = (int) (4096 - (beforeCentralDir.size() % 4096));
            beforeCentralDir = new ChainedDataSource(beforeCentralDir, DataSources.asDataSource(ByteBuffer.allocate(padSizeBeforeSigningBlock)));
        }
        return Pair.of(beforeCentralDir, Integer.valueOf(padSizeBeforeSigningBlock));
    }

    public static DataSource copyWithModifiedCDOffset(DataSource beforeCentralDir, DataSource eocd) throws IOException {
        long centralDirOffsetForDigesting = beforeCentralDir.size();
        ByteBuffer eocdBuf = ByteBuffer.allocate((int) eocd.size());
        eocdBuf.order(ByteOrder.LITTLE_ENDIAN);
        eocd.copyTo(0L, (int) eocd.size(), eocdBuf);
        eocdBuf.flip();
        ZipUtils.setZipEocdCentralDirectoryOffset(eocdBuf, centralDirOffsetForDigesting);
        return DataSources.asDataSource(eocdBuf);
    }

    public static byte[] generateApkSigningBlock(List<Pair<byte[], Integer>> apkSignatureSchemeBlockPairs) {
        int blocksSize = 0;
        for (Pair<byte[], Integer> schemeBlockPair : apkSignatureSchemeBlockPairs) {
            blocksSize += 12 + schemeBlockPair.getFirst().length;
        }
        int resultSize = 8 + blocksSize + 8 + 16;
        ByteBuffer paddingPair = null;
        if (resultSize % ANDROID_COMMON_PAGE_ALIGNMENT_BYTES != 0) {
            int padding = ANDROID_COMMON_PAGE_ALIGNMENT_BYTES - (resultSize % ANDROID_COMMON_PAGE_ALIGNMENT_BYTES);
            if (padding < 12) {
                padding += ANDROID_COMMON_PAGE_ALIGNMENT_BYTES;
            }
            paddingPair = ByteBuffer.allocate(padding).order(ByteOrder.LITTLE_ENDIAN);
            paddingPair.putLong(padding - 8);
            paddingPair.putInt(VERITY_PADDING_BLOCK_ID);
            paddingPair.rewind();
            resultSize += padding;
        }
        ByteBuffer result = ByteBuffer.allocate(resultSize);
        result.order(ByteOrder.LITTLE_ENDIAN);
        long blockSizeFieldValue = resultSize - 8;
        result.putLong(blockSizeFieldValue);
        for (Pair<byte[], Integer> schemeBlockPair2 : apkSignatureSchemeBlockPairs) {
            byte[] apkSignatureSchemeBlock = schemeBlockPair2.getFirst();
            int apkSignatureSchemeId = schemeBlockPair2.getSecond().intValue();
            long pairSizeFieldValue = 4 + apkSignatureSchemeBlock.length;
            result.putLong(pairSizeFieldValue);
            result.putInt(apkSignatureSchemeId);
            result.put(apkSignatureSchemeBlock);
        }
        if (paddingPair != null) {
            result.put(paddingPair);
        }
        result.putLong(blockSizeFieldValue);
        result.put(APK_SIGNING_BLOCK_MAGIC);
        return result.array();
    }

    public static Pair<List<SignerConfig>, Map<ContentDigestAlgorithm, byte[]>> computeContentDigests(RunnablesExecutor executor, DataSource beforeCentralDir, DataSource centralDir, DataSource eocd, List<SignerConfig> signerConfigs) throws IOException, NoSuchAlgorithmException, SignatureException {
        if (signerConfigs.isEmpty()) {
            throw new IllegalArgumentException("No signer configs provided. At least one is required");
        }
        Set<ContentDigestAlgorithm> contentDigestAlgorithms = new HashSet<>(1);
        for (SignerConfig signerConfig : signerConfigs) {
            for (SignatureAlgorithm signatureAlgorithm : signerConfig.signatureAlgorithms) {
                contentDigestAlgorithms.add(signatureAlgorithm.getContentDigestAlgorithm());
            }
        }
        try {
            Map<ContentDigestAlgorithm, byte[]> contentDigests = computeContentDigests(executor, contentDigestAlgorithms, beforeCentralDir, centralDir, eocd);
            return Pair.of(signerConfigs, contentDigests);
        } catch (IOException e) {
            throw new IOException("Failed to read APK being signed", e);
        } catch (DigestException e2) {
            throw new SignatureException("Failed to compute digests of APK", e2);
        }
    }

    public static <T extends ApkSupportedSignature> List<T> getSignaturesToVerify(List<T> signatures, int minSdkVersion, int maxSdkVersion) throws NoSupportedSignaturesException {
        return getSignaturesToVerify(signatures, minSdkVersion, maxSdkVersion, false);
    }

    public static <T extends ApkSupportedSignature> List<T> getSignaturesToVerify(List<T> signatures, int minSdkVersion, int maxSdkVersion, boolean onlyRequireJcaSupport) throws NoSupportedSignaturesException {
        try {
            return ApkSigningBlockUtilsLite.getSignaturesToVerify(signatures, minSdkVersion, maxSdkVersion, onlyRequireJcaSupport);
        } catch (NoApkSupportedSignaturesException e) {
            throw new NoSupportedSignaturesException(e.getMessage());
        }
    }

    /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$NoSupportedSignaturesException.class */
    public static class NoSupportedSignaturesException extends NoApkSupportedSignaturesException {
        public NoSupportedSignaturesException(String message) {
            super(message);
        }
    }

    /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$SignatureNotFoundException.class */
    public static class SignatureNotFoundException extends Exception {
        private static final long serialVersionUID = 1;

        public SignatureNotFoundException(String message) {
            super(message);
        }

        public SignatureNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static List<Pair<Integer, byte[]>> generateSignaturesOverData(SignerConfig signerConfig, byte[] data) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        List<Pair<Integer, byte[]>> signatures = new ArrayList<>(signerConfig.signatureAlgorithms.size());
        PublicKey publicKey = signerConfig.certificates.get(0).getPublicKey();
        for (SignatureAlgorithm signatureAlgorithm : signerConfig.signatureAlgorithms) {
            Pair<String, ? extends AlgorithmParameterSpec> sigAlgAndParams = signatureAlgorithm.getJcaSignatureAlgorithmAndParams();
            String jcaSignatureAlgorithm = sigAlgAndParams.getFirst();
            AlgorithmParameterSpec jcaSignatureAlgorithmParams = sigAlgAndParams.getSecond();
            try {
                Signature signature = Signature.getInstance(jcaSignatureAlgorithm);
                signature.initSign(signerConfig.privateKey);
                if (jcaSignatureAlgorithmParams != null) {
                    signature.setParameter(jcaSignatureAlgorithmParams);
                }
                signature.update(data);
                byte[] signatureBytes = signature.sign();
                try {
                    Signature signature2 = Signature.getInstance(jcaSignatureAlgorithm);
                    signature2.initVerify(publicKey);
                    if (jcaSignatureAlgorithmParams != null) {
                        signature2.setParameter(jcaSignatureAlgorithmParams);
                    }
                    signature2.update(data);
                    if (!signature2.verify(signatureBytes)) {
                        throw new SignatureException("Failed to verify generated " + jcaSignatureAlgorithm + " signature using public key from certificate");
                    }
                    signatures.add(Pair.of(Integer.valueOf(signatureAlgorithm.getId()), signatureBytes));
                } catch (InvalidAlgorithmParameterException | SignatureException e) {
                    throw new SignatureException("Failed to verify generated " + jcaSignatureAlgorithm + " signature using public key from certificate", e);
                } catch (InvalidKeyException e2) {
                    throw new InvalidKeyException("Failed to verify generated " + jcaSignatureAlgorithm + " signature using public key from certificate", e2);
                }
            } catch (InvalidAlgorithmParameterException | SignatureException e3) {
                throw new SignatureException("Failed to sign using " + jcaSignatureAlgorithm, e3);
            } catch (InvalidKeyException e4) {
                throw new InvalidKeyException("Failed to sign using " + jcaSignatureAlgorithm, e4);
            }
        }
        return signatures;
    }

    public static byte[] generatePkcs7DerEncodedMessage(byte[] signatureBytes, ByteBuffer data, List<X509Certificate> signerCerts, AlgorithmIdentifier digestAlgorithmId, AlgorithmIdentifier signatureAlgorithmId) throws Asn1EncodingException, CertificateEncodingException {
        SignerInfo signerInfo = new SignerInfo();
        signerInfo.version = 1;
        X509Certificate signingCert = signerCerts.get(0);
        X500Principal signerCertIssuer = signingCert.getIssuerX500Principal();
        signerInfo.sid = new SignerIdentifier(new IssuerAndSerialNumber(new Asn1OpaqueObject(signerCertIssuer.getEncoded()), signingCert.getSerialNumber()));
        signerInfo.digestAlgorithm = digestAlgorithmId;
        signerInfo.signatureAlgorithm = signatureAlgorithmId;
        signerInfo.signature = ByteBuffer.wrap(signatureBytes);
        SignedData signedData = new SignedData();
        signedData.certificates = new ArrayList<>(signerCerts.size());
        for (X509Certificate cert : signerCerts) {
            signedData.certificates.add(new Asn1OpaqueObject(cert.getEncoded()));
        }
        signedData.version = 1;
        signedData.digestAlgorithms = Collections.singletonList(digestAlgorithmId);
        signedData.encapContentInfo = new EncapsulatedContentInfo(Pkcs7Constants.OID_DATA);
        signedData.encapContentInfo.content = data;
        signedData.signerInfos = Collections.singletonList(signerInfo);
        ContentInfo contentInfo = new ContentInfo();
        contentInfo.contentType = Pkcs7Constants.OID_SIGNED_DATA;
        contentInfo.content = new Asn1OpaqueObject(Asn1DerEncoder.encode(signedData));
        return Asn1DerEncoder.encode(contentInfo);
    }

    public static byte[] pickBestDigestForV4(Map<ContentDigestAlgorithm, byte[]> contentDigests) {
        ContentDigestAlgorithm[] contentDigestAlgorithmArr;
        for (ContentDigestAlgorithm algo : V4_CONTENT_DIGEST_ALGORITHMS) {
            if (contentDigests.containsKey(algo)) {
                return contentDigests.get(algo);
            }
        }
        return null;
    }

    /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$Result.class */
    public static class Result extends ApkSigResult {
        public SigningCertificateLineage signingCertificateLineage;
        public final List<SignerInfo> signers;
        private final List<ApkVerifier.IssueWithParams> mWarnings;
        private final List<ApkVerifier.IssueWithParams> mErrors;

        public Result(int signatureSchemeVersion) {
            super(signatureSchemeVersion);
            this.signingCertificateLineage = null;
            this.signers = new ArrayList<>();
            this.mWarnings = new ArrayList<>();
            this.mErrors = new ArrayList<>();
        }

        @Override // com.android.apksig.internal.apk.ApkSigResult
        public boolean containsErrors() {
            if (!this.mErrors.isEmpty()) {
                return true;
            }
            if (!this.signers.isEmpty()) {
                for (SignerInfo signer : this.signers) {
                    if (signer.containsErrors()) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }

        @Override // com.android.apksig.internal.apk.ApkSigResult
        public boolean containsWarnings() {
            if (!this.mWarnings.isEmpty()) {
                return true;
            }
            if (!this.signers.isEmpty()) {
                for (SignerInfo signer : this.signers) {
                    if (signer.containsWarnings()) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }

        public void addError(ApkVerifier.Issue msg, Object... parameters) {
            this.mErrors.add(new ApkVerifier.IssueWithParams(msg, parameters));
        }

        public void addWarning(ApkVerifier.Issue msg, Object... parameters) {
            this.mWarnings.add(new ApkVerifier.IssueWithParams(msg, parameters));
        }

        @Override // com.android.apksig.internal.apk.ApkSigResult
        public List<ApkVerifier.IssueWithParams> getErrors() {
            return this.mErrors;
        }

        @Override // com.android.apksig.internal.apk.ApkSigResult
        public List<ApkVerifier.IssueWithParams> getWarnings() {
            return this.mWarnings;
        }

        /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$Result$SignerInfo.class */
        public static class SignerInfo extends ApkSignerInfo {
            public byte[] signedData;
            public int minSdkVersion;
            public int maxSdkVersion;
            public SigningCertificateLineage signingCertificateLineage;
            public List<ContentDigest> contentDigests = new ArrayList<>();
            public Map<ContentDigestAlgorithm, byte[]> verifiedContentDigests = new HashMap<>();
            public List<Signature> signatures = new ArrayList<>();
            public Map<SignatureAlgorithm, byte[]> verifiedSignatures = new HashMap<>();
            public List<AdditionalAttribute> additionalAttributes = new ArrayList<>();
            private final List<ApkVerifier.IssueWithParams> mWarnings = new ArrayList<>();
            private final List<ApkVerifier.IssueWithParams> mErrors = new ArrayList<>();

            public void addError(ApkVerifier.Issue msg, Object... parameters) {
                this.mErrors.add(new ApkVerifier.IssueWithParams(msg, parameters));
            }

            public void addWarning(ApkVerifier.Issue msg, Object... parameters) {
                this.mWarnings.add(new ApkVerifier.IssueWithParams(msg, parameters));
            }

            @Override // com.android.apksig.internal.apk.ApkSignerInfo
            public boolean containsErrors() {
                return !this.mErrors.isEmpty();
            }

            @Override // com.android.apksig.internal.apk.ApkSignerInfo
            public boolean containsWarnings() {
                return !this.mWarnings.isEmpty();
            }

            @Override // com.android.apksig.internal.apk.ApkSignerInfo
            public List<ApkVerifier.IssueWithParams> getErrors() {
                return this.mErrors;
            }

            @Override // com.android.apksig.internal.apk.ApkSignerInfo
            public List<ApkVerifier.IssueWithParams> getWarnings() {
                return this.mWarnings;
            }

            /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$Result$SignerInfo$ContentDigest.class */
            public static class ContentDigest {
                private final int mSignatureAlgorithmId;
                private final byte[] mValue;

                public ContentDigest(int signatureAlgorithmId, byte[] value) {
                    this.mSignatureAlgorithmId = signatureAlgorithmId;
                    this.mValue = value;
                }

                public int getSignatureAlgorithmId() {
                    return this.mSignatureAlgorithmId;
                }

                public byte[] getValue() {
                    return this.mValue;
                }
            }

            /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$Result$SignerInfo$Signature.class */
            public static class Signature {
                private final int mAlgorithmId;
                private final byte[] mValue;

                public Signature(int algorithmId, byte[] value) {
                    this.mAlgorithmId = algorithmId;
                    this.mValue = value;
                }

                public int getAlgorithmId() {
                    return this.mAlgorithmId;
                }

                public byte[] getValue() {
                    return this.mValue;
                }
            }

            /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$Result$SignerInfo$AdditionalAttribute.class */
            public static class AdditionalAttribute {
                private final int mId;
                private final byte[] mValue;

                public AdditionalAttribute(int id, byte[] value) {
                    this.mId = id;
                    this.mValue = (byte[]) value.clone();
                }

                public int getId() {
                    return this.mId;
                }

                public byte[] getValue() {
                    return (byte[]) this.mValue.clone();
                }
            }
        }
    }

    /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$SupportedSignature.class */
    public static class SupportedSignature extends ApkSupportedSignature {
        public SupportedSignature(SignatureAlgorithm algorithm, byte[] signature) {
            super(algorithm, signature);
        }
    }

    /* loaded from: apksigner.jar:com/android/apksig/internal/apk/ApkSigningBlockUtils$SigningSchemeBlockAndDigests.class */
    public static class SigningSchemeBlockAndDigests {
        public final Pair<byte[], Integer> signingSchemeBlock;
        public final Map<ContentDigestAlgorithm, byte[]> digestInfo;

        public SigningSchemeBlockAndDigests(Pair<byte[], Integer> signingSchemeBlock, Map<ContentDigestAlgorithm, byte[]> digestInfo) {
            this.signingSchemeBlock = signingSchemeBlock;
            this.digestInfo = digestInfo;
        }
    }
}