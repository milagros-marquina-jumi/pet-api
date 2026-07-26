# Pet API

API REST que actúa como fachada sobre [Petstore Swagger](https://petstore.swagger.io/v2), desarrollada como prueba técnica para Ssr Backend Developer.

Java 17 · Spring Boot 3.2.7 · Gradle

## Ejecución

```bash
./gradlew bootRun
```

La aplicación queda disponible en `http://localhost:8080`.

Para ver el detalle de las llamadas al sistema externo:

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

## Endpoints

### GET /api/pet/{petId}

```bash
curl http://localhost:8080/api/pet/10000023
```

```json
{ "id": 10000023, "name": "testingPet1", "status": "available" }
```

### POST /api/pet

```bash
curl -X POST http://localhost:8080/api/pet \
  -H "Content-Type: application/json" \
  -d '{"id":10000023,"status":"available","name":"testingPet1"}'
```

```json
{
  "transactionId": "60cc5c22-3250-4e07-a519-a6dab99c6713",
  "dateCreated": "2026-07-25T18:50:19.513",
  "status": true,
  "name": "testingPet1"
}
```

`transactionId` (UUIDv4) y `dateCreated` se generan en la capa de servicio, no provienen del sistema externo.

### Códigos de respuesta

| Código | Situación |
|---|---|
| 400 | Parámetro o cuerpo inválido |
| 404 | La mascota no existe |
| 502 | El sistema externo respondió con error |
| 504 | El sistema externo no respondió a tiempo |

Los errores del sistema externo se devuelven como 502/504 en lugar de 500: el fallo está aguas arriba, no en este servicio.

## Estructura

```
controller/   Exposición REST
service/      Lógica de negocio: mapeo, transactionId, dateCreated
client/       Consumo de Petstore
model/dto     Contrato propio
model/external Modelo del tercero
exception/    Manejo centralizado de errores
config/       Configuración del cliente HTTP
```

El modelo externo se mantiene separado del contrato propio: si Petstore cambia su esquema, solo cambia el mapeo en el servicio.

## Configuración

Todos los valores se leen de variables de entorno con un valor por defecto para desarrollo.

| Variable | Por defecto |
|---|---|
| `SERVER_PORT` | `8080` |
| `PETSTORE_API_BASE_URL` | `https://petstore.swagger.io/v2` |
| `PETSTORE_API_CONNECT_TIMEOUT` | `3s` |
| `PETSTORE_API_READ_TIMEOUT` | `5s` |
| `LOG_LEVEL` | `INFO` |

Los timeouts son explícitos porque el valor por defecto de `RestTemplate` es infinito: un sistema externo lento bloquearía un hilo del contenedor de forma indefinida.

## Pruebas

```bash
./gradlew test
```

Pruebas unitarias del servicio, de capa web con `@WebMvcTest` y del cliente con `MockRestServiceServer`.

En `postman/` hay una colección con los dos endpoints y sus casos de error.

## Trazabilidad

Cada petición POST genera un `transactionId` que se incorpora al contexto de logging y se propaga al sistema externo como cabecera `X-Request-ID`, de modo que una operación reportada por un cliente pueda localizarse en los registros.

```
INFO [txId=05c138bd-b2b9-4f70-9a5e-9c09fe0cc32a] Mascota registrada | id=10000077
```

Petstore reinicia su almacén periódicamente, por lo que el `petId` del enunciado puede devolver 404. Crear la mascota con el POST antes de probar el GET.
