#!/bin/bash

# Script to generate SSL certificates for ActiveMQ testing
# This creates both server and client certificates for mutual TLS authentication

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "Generating SSL certificates for ActiveMQ testing..."

# Clean up existing certificates
rm -f *.jks *.p12 *.pem *.key

# 1. Generate server keystore with self-signed certificate
echo "1. Creating server keystore..."
keytool -genkeypair -alias server -keyalg RSA -keysize 2048 \
    -dname "CN=localhost,OU=Test,O=Ballerina,L=Colombo,ST=Western,C=LK" \
    -validity 365 -keystore server-keystore.jks -storepass password -keypass password

# 2. Export server certificate (temporary, for creating truststores)
echo "2. Exporting server certificate..."
keytool -exportcert -alias server -keystore server-keystore.jks \
    -storepass password -file server.crt.tmp

# 3. Generate client keystore (temporary JKS, will convert to PKCS12)
echo "3. Creating client keystore..."
keytool -genkeypair -alias client -keyalg RSA -keysize 2048 \
    -dname "CN=client,OU=Test,O=Ballerina,L=Colombo,ST=Western,C=LK" \
    -validity 365 -keystore client-keystore.jks.tmp -storepass password -keypass password

# 4. Export client certificate (temporary, for creating truststores)
echo "4. Exporting client certificate..."
keytool -exportcert -alias client -keystore client-keystore.jks.tmp \
    -storepass password -file client.crt.tmp

# 5. Create server truststore and import client certificate
echo "5. Creating server truststore..."
keytool -importcert -alias client -file client.crt.tmp \
    -keystore server-truststore.jks -storepass password -noprompt

# 6. Create client truststore and import server certificate (temporary JKS)
echo "6. Creating client truststore..."
keytool -importcert -alias server -file server.crt.tmp \
    -keystore client-truststore.jks.tmp -storepass password -noprompt

# 7. Convert client keystore to PKCS12 format
echo "7. Converting client keystore to PKCS12..."
keytool -importkeystore -srckeystore client-keystore.jks.tmp -srcstorepass password \
    -destkeystore client-keystore.p12 -deststoretype PKCS12 -deststorepass password

# 8. Convert client truststore to PKCS12 format
echo "8. Converting client truststore to PKCS12..."
keytool -importkeystore -srckeystore client-truststore.jks.tmp -srcstorepass password \
    -destkeystore client-truststore.p12 -deststoretype PKCS12 -deststorepass password

# 9. Export server certificate to PEM format
echo "9. Exporting server certificate to PEM..."
keytool -exportcert -alias server -keystore server-keystore.jks \
    -storepass password -rfc -file server.pem

# 10. Export client certificate to PEM format (for Ballerina CertKey support)
echo "10. Exporting client certificate to PEM..."
keytool -exportcert -alias client -keystore client-keystore.p12 \
    -storepass password -rfc -file client-cert.pem

# 11. Extract client private key from PKCS12
echo "11. Extracting client private key..."
openssl pkcs12 -in client-keystore.p12 -passin pass:password \
    -nodes -nocerts -out client.key

# Clean up temporary files
rm -f *.tmp client-keystore.jks.tmp client-truststore.jks.tmp

echo ""
echo "Certificate generation complete!"
echo ""
echo "Generated files:"
echo "  Server (for ActiveMQ broker):"
echo "    - server-keystore.jks (JKS keystore with server cert+key)"
echo "    - server-truststore.jks (JKS truststore with client cert)"
echo "    - server.pem (PEM format certificate for CertKey tests)"
echo ""
echo "  Client (for Ballerina tests):"
echo "    - client-keystore.p12 (PKCS12 keystore with client cert+key)"
echo "    - client-truststore.p12 (PKCS12 truststore with server cert)"
echo "    - client-cert.pem (PEM format certificate for CertKey tests)"
echo "    - client.key (PEM format private key for CertKey tests)"
echo ""
echo "All keystores use password: 'password'"
