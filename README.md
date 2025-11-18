# Microservicio: Pedidos (ms-pedidos)

## 🎯 Propósito

Este servicio gestiona toda la lógica de negocio para la **creación y consulta de pedidos**. Es el "cerebro" de la operación de compra.

Es una API reactiva (construida con Spring WebFlux) que expone endpoints para crear y leer pedidos.

## 📡 Comunicación entre Servicios

Este es un servicio clave que **consume** a otro:
1.  Recibe la petición `POST /api/pedidos`.
2.  Antes de guardar, llama (usando WebClient) al servicio `ms-productos` (en la URL `http://ms-productos:8081`) para verificar el stock y obtener el precio real del producto.
3.  Si la validación es exitosa, guarda el pedido y los detalles en la base de datos.

## 🛠️ Configuración Clave

* **Puerto de Servicio:** `8082`
* **Tecnología de Datos:** `spring-boot-starter-data-r2dbc` (Reactivo).
* **Base de Datos:** Se conecta a la base de datos `sistema_pedidos_db` en el contenedor `postgres-db`.
* **Tablas que utiliza:** `pedidos` y `detalle_pedidos`.

## 🐳 Docker

* **Dependencias de Arranque:** En `docker-compose.yml`, este servicio espera a que `ms-config-server`, `postgres-db` y `registry-service` estén en estado `healthy` (saludable) antes de arrancar.
* **Registro de Servicios:** Al arrancar, se conecta a Eureka (en `registry-service:8099`) y se registra con el nombre `MS-PEDIDOS`.