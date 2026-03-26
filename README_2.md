Comando para conectar no docker minio
docker exec -it minio /bin/bash

Caso não permita rodar comandos com as AccessKeys deve-se atribuir as permissões
mc alias set local http://localhost:9000 ADMIN_USER ADMIN_PASSWORD

Para criar a Access Key e Secret Key
mc admin accesskey create local/

Pegar estas chaves e adicionar nas variáveis de ambiente do docker-compose.yml
MINIO_ACCESS_KEY: "RL9HRH51KWYNN50XBP24"
MINIO_SECRET_KEY: "KlGFbvWRec+OcV8zPt3iFSuxOTeIq5Bab39Nh4Us"


## Load Test

```bash
k6 run k6/upload-test.js
```

// TODO
- Revisar código se está aderente ao SOLID
- Criar testes unitários
- Rodar Jacoco para verificar a cobertura dos testes
- Criar DockerFile
- Verificar como que vai rodar este DockerFile e DockerCompose
- Redigir explicações no README.md
- Como que vai criar o Access Key e Secret Key para entregar?