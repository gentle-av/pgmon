#!/bin/bash
# generate-ssl.sh

# Генерация keystore для сервера (self-signed)
keytool -genkeypair -alias tomcat \
    -keyalg RSA \
    -keysize 2048 \
    -keystore src/main/resources/keystore.p12 \
    -storepass changeit \
    -validity 3650 \
    -dname "CN=localhost, OU=Dev, O=Company, L=City, ST=State, C=RU"

# Для клиентских сертификатов к PostgreSQL (опционально)
openssl genrsa -out src/main/resources/client-key.pem 2048
openssl req -new -key src/main/resources/client-key.pem \
    -out src/main/resources/client-cert.csr \
    -subj "/CN=postgres-client"
openssl x509 -req -in src/main/resources/client-cert.csr \
    -signkey src/main/resources/client-key.pem \
    -out src/main/resources/client-cert.pem

echo "SSL сертификаты созданы в src/main/resources/"
