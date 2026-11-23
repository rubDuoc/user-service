# user-service

Microservicio de **control de usuarios e inicio de sesión** del Sistema de Foros en Línea
(Desarrollo Full Stack III - DSY2205). Generado con el arquetipo propio
[foro-arquetipo](https://github.com/rubDuoc/foro-arquetipo).

- Spring Boot 4 · Java 21 · Spring Data JPA · Oracle Autonomous Database (Oracle Cloud)
- Puerto local: **8083**
- Contraseñas cifradas con **BCrypt**
- Patrón de diseño: **Builder** en las respuestas de error y de estado (`ErrorResponse`, `EstadoResponse`)

## Roles y privilegios

El header **`X-Usuario-Id`** indica qué usuario realiza la operación.

| Operación | USUARIO | ADMIN |
|---|---|---|
| Registrarse (`POST /api/usuarios`) | Sí, siempre como USUARIO | Puede crear otros ADMIN |
| Editar perfil (`PUT /api/usuarios/{id}`) | Solo el propio | Cualquiera |
| Cambiar rol | No | Sí |
| Eliminar usuario (`DELETE /api/usuarios/{id}`) | No | Sí |
| Consultas `GET` e inicio de sesión | Sí | Sí |

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/estado` | Estado del servicio |
| GET | `/api/usuarios` | Listar usuarios |
| GET | `/api/usuarios/{id}` | Obtener un usuario (lo usan forum-service y moderation-service para validar roles) |
| POST | `/api/usuarios` | Registrar usuario |
| PUT | `/api/usuarios/{id}` | Modificar usuario (header `X-Usuario-Id`) |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario (header `X-Usuario-Id`, solo ADMIN) |
| POST | `/api/usuarios/login` | Inicio de sesión (401 si las credenciales son incorrectas) |
| GET | `/api/usuarios/rol/{rol}` | Usuarios por rol (`USUARIO` o `ADMIN`) |
| GET | `/api/usuarios/buscar?nombre=` | Buscar por nombre |

Errores en JSON: 400 (validación, header o parámetro faltante), 401, 403, 404, 409 y 500.

## Base de datos

Script: `src/main/resources/db/script.sql` (tabla `USUARIOS` con 2 ADMIN y 3 USUARIO).
Orden de ejecución de los scripts del sistema: **user-service → forum-service → moderation-service**.

| Usuario | Contraseña | Rol |
|---|---|---|
| admin_foro, carla_mod | Admin1234 | ADMIN |
| maria_dev, juan_gamer, sofia_music | Usuario1234 | USUARIO |

## Ejecutar

Copiar el wallet de Oracle en la carpeta `wallet/` (no se sube a Git) y en CMD:

```
set DB_PASSWORD=clave_de_oracle
mvnw spring-boot:run
```

Variables opcionales: `DB_USERNAME`, `DB_SERVICE`, `TNS_ADMIN`, `PORT`, `CORS_ORIGENES`.

## Despliegue en la nube (Render)

URL publica: **https://foro-user-service-1-0.onrender.com** (por ejemplo, `/api/estado`).
Imagen Docker publicada en Docker Hub y desplegada en Render; el wallet de Oracle llega como archivo secreto.

## Postman

Colección con tests automáticos en `postman/user-service.postman_collection.json`
(importar en Postman junto con el entorno `postman/render.postman_environment.json` para probar contra Render → *Run collection*). Crea y elimina sus propios datos de prueba.

## Docker

```
docker build -t user-service .
docker run -p 8083:8083 -e DB_PASSWORD=clave -v %cd%/wallet:/app/wallet user-service
```
