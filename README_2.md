Comando para conectar no docker minio
docker exec -it minio /bin/bash

Caso não permita rodar comandos com as AccessKeys deve-se atribuir as permissões
mc alias set local http://localhost:9000 ADMIN_USER ADMIN_PASSWORD

Para criar a Access Key e Secret Key
mc admin accesskey create local/

Pegar estas chaves e adicionar nas variáveis de ambiente do docker-compose.yml
MINIO_ACCESS_KEY: "X3TRXWWFAN71CU01TQ0Z"
MINIO_SECRET_KEY: "X6R4z4oDUhF14S4TIFe4mAVVw7qYyTj+j8pkZ167"


// TODO
- Revisar código se está aderente ao SOLID
- Criar testes unitários
- Rodar Jacoco para verificar a cobertura dos testes
- Criar DockerFile
- Verificar como que vai rodar este DockerFile e DockerCompose
- Redigir explicações no README.md
- Como que vai criar o Access Key e Secret Key para entregar?
- 