/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.activemq.util;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.stdlib.crypto.nativeimpl.Decode;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.UUID;

import static io.ballerina.lib.activemq.util.ActiveMQConstants.CERT_FILE;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.CRYPTO_TRUSTSTORE_PASSWORD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.CRYPTO_TRUSTSTORE_PATH;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.KEY_FILE;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.KEY_PASSWORD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.KEY_STORE_PASSWORD;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.KEY_STORE_PATH;
import static io.ballerina.lib.activemq.util.ActiveMQConstants.PROTOCOL;

/**
 * SSL utility functions for ActiveMQ SSL/TLS configuration.
 *
 * @since 0.1.0
 */
public final class SslUtils {

    // Native data keys used by Ballerina crypto module
    private static final String NATIVE_DATA_PUBLIC_KEY_CERTIFICATE = "NATIVE_DATA_PUBLIC_KEY_CERTIFICATE";
    private static final String NATIVE_DATA_PRIVATE_KEY = "NATIVE_DATA_PRIVATE_KEY";

    private SslUtils() {
    }

    /**
     * Gets TrustManagers from the provided certificate or truststore configuration.
     *
     * @param bCert Certificate file path (BString) or TrustStore configuration (BMap)
     * @return Array of TrustManagers
     * @throws Exception If loading the certificate or truststore fails
     */
    @SuppressWarnings("unchecked")
    public static TrustManager[] getTrustmanagers(Object bCert) throws Exception {
        // Configure TrustManager (certificate for server verification)
        if (bCert instanceof BString cert) {
            // Single certificate file
            return getTrustManagerFactory(cert).getTrustManagers();
        }
        // TrustStore (JKS/PKCS12 file)
        BMap<BString, BString> trustStore = (BMap<BString, BString>) bCert;
        return getTrustManagerFactory(trustStore).getTrustManagers();
    }

    /**
     * Gets KeyManagers from the provided key configuration.
     *
     * @param keyRecord Key configuration with either cert/key files or keystore
     * @return Array of KeyManagers
     * @throws Exception If loading the certificate/key or keystore fails
     */
    public static KeyManager[] getKeyManagers(BMap<BString, BString> keyRecord) throws Exception {
        if (Objects.nonNull(keyRecord)) {
            if (keyRecord.containsKey(CERT_FILE)) {
                // Separate cert and key files
                BString certFile = keyRecord.get(CERT_FILE);
                BString keyFile = keyRecord.get(KEY_FILE);
                BString keyPassword = keyRecord.getStringValue(KEY_PASSWORD);
                return getKeyManagerFactory(certFile, keyFile, keyPassword).getKeyManagers();
            }
            // KeyStore (JKS/PKCS12 file)
            return getKeyManagerFactory(keyRecord).getKeyManagers();
        }
        return null;
    }

    public static SSLContext createSSLContext(String protocol, KeyManager[] keyManagers, TrustManager[] trustManagers)
            throws Exception {
        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(keyManagers, trustManagers, null);
        return sslContext;
    }



    /**
     * Creates a TrustManagerFactory from a single certificate file.
     *
     * @param cert Path to the certificate file
     * @return TrustManagerFactory configured with the certificate
     * @throws Exception If certificate loading fails
     */
    @SuppressWarnings("unchecked")
    private static TrustManagerFactory getTrustManagerFactory(BString cert) throws Exception {
        Object publicKeyMap = Decode.decodeRsaPublicKeyFromCertFile(cert);
        if (publicKeyMap instanceof BMap) {
            X509Certificate x509Certificate = (X509Certificate) ((BMap<BString, Object>) publicKeyMap)
                    .getNativeData(NATIVE_DATA_PUBLIC_KEY_CERTIFICATE);
            KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
            ts.load(null, "".toCharArray());
            ts.setCertificateEntry(UUID.randomUUID().toString(), x509Certificate);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            return tmf;
        } else {
            throw new Exception("Failed to get the public key from Crypto API. " +
                    ((BError) publicKeyMap).getErrorMessage().getValue());
        }
    }

    /**
     * Creates a TrustManagerFactory from a TrustStore file (JKS/PKCS12).
     *
     * @param trustStore TrustStore configuration with path and password
     * @return TrustManagerFactory configured with the truststore
     * @throws Exception If truststore loading fails
     */
    private static TrustManagerFactory getTrustManagerFactory(BMap<BString, BString> trustStore) throws Exception {
        BString trustStorePath = trustStore.getStringValue(CRYPTO_TRUSTSTORE_PATH);
        BString trustStorePassword = trustStore.getStringValue(CRYPTO_TRUSTSTORE_PASSWORD);
        KeyStore ts = getKeyStore(trustStorePath, trustStorePassword);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);
        return tmf;
    }

    /**
     * Creates a KeyManagerFactory from a KeyStore file (JKS/PKCS12).
     *
     * @param keyStore KeyStore configuration with path and password
     * @return KeyManagerFactory configured with the keystore
     * @throws Exception If keystore loading fails
     */
    private static KeyManagerFactory getKeyManagerFactory(BMap<BString, BString> keyStore) throws Exception {
        BString keyStorePath = keyStore.getStringValue(KEY_STORE_PATH);
        BString keyStorePassword = keyStore.getStringValue(KEY_STORE_PASSWORD);
        KeyStore ks = getKeyStore(keyStorePath, keyStorePassword);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyStorePassword.getValue().toCharArray());
        return kmf;
    }

    /**
     * Creates a KeyManagerFactory from separate certificate and private key files.
     *
     * @param certFile Path to the certificate file
     * @param keyFile Path to the private key file
     * @param keyPassword Password for the private key (if encrypted)
     * @return KeyManagerFactory configured with the certificate and key
     * @throws Exception If certificate or key loading fails
     */
    @SuppressWarnings("unchecked")
    private static KeyManagerFactory getKeyManagerFactory(BString certFile, BString keyFile, BString keyPassword)
            throws Exception {
        Object publicKey = Decode.decodeRsaPublicKeyFromCertFile(certFile);
        if (publicKey instanceof BMap) {
            X509Certificate publicCert = (X509Certificate) ((BMap<BString, Object>) publicKey).getNativeData(
                    NATIVE_DATA_PUBLIC_KEY_CERTIFICATE);
            Object privateKeyMap = Decode.decodeRsaPrivateKeyFromKeyFile(keyFile, keyPassword);
            if (privateKeyMap instanceof BMap) {
                PrivateKey privateKey = (PrivateKey) ((BMap<BString, Object>) privateKeyMap).getNativeData(
                        NATIVE_DATA_PRIVATE_KEY);
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(null, "".toCharArray());
                ks.setKeyEntry(UUID.randomUUID().toString(), privateKey, "".toCharArray(),
                        new X509Certificate[]{publicCert});
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, "".toCharArray());
                return kmf;
            } else {
                throw new Exception("Failed to get the private key from Crypto API. " +
                        ((BError) privateKeyMap).getErrorMessage().getValue());
            }
        } else {
            throw new Exception("Failed to get the public key from Crypto API. " +
                    ((BError) publicKey).getErrorMessage().getValue());
        }
    }

    /**
     * Loads a KeyStore from a file.
     *
     * @param path Path to the keystore file
     * @param password Password for the keystore
     * @return Loaded KeyStore
     * @throws Exception If keystore loading fails
     */
    private static KeyStore getKeyStore(BString path, BString password) throws Exception {
        try (FileInputStream is = new FileInputStream(path.getValue())) {
            char[] passphrase = password.getValue().toCharArray();
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(is, passphrase);
            return ks;
        }
    }
}
