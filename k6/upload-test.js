import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuração do teste
export const options = {
    vus: 10, // 10 uploads em paralelo
    iterations: 10, // total de execuções
};

const BASE_URL = 'http://localhost:8080/document-management/upload';

// Carrega o arquivo uma única vez (importante!)
const fileData = open('big.pdf', 'b');

export default function () {
    const payload = {
        file: http.file(fileData, 'big.pdf', 'application/pdf'),
        metadata: JSON.stringify({
            user: "galdao",
            name: `big_${__VU}_${__ITER}`, // evita conflito de nome
            tags: ["ingles", "big"]
        }),
    };

    const res = http.post(BASE_URL, payload);

    check(res, {
        'status is 200/201': (r) => r.status === 200 || r.status === 201,
    });

    sleep(1); // pequena pausa (opcional)
}