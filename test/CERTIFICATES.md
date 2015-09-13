openssl genrsa -aes128 -out auth_privkey.pem 2048
openssl rsa -pubout -in auth_privkey.pem -out auth_pubkey.pem

- passphrase: dummy
