# Práctica de Socket con Hilos - Programación Avanzada 2026

**Alumno:** Andres Fernando RIVEROS  
**Materia:** Programación Avanzada - FCSAIDSPA  
**Universidad:** UDA - Universidad del Aconcagua  
**Fecha de entrega:** Abril 2026  

---

## Descripción

Segunda parte de la práctica de Sockets en Java. Toma como base el servidor de socket TCP simple (Práctica anterior, aprobada) y lo extiende para **atender múltiples clientes de forma concurrente mediante hilos (threads)**.

El sistema implementa un **servidor de chat** donde los clientes pueden conectarse, identificarse con un nombre de usuario, enviarse mensajes entre sí y ejecutar comandos del servidor.

---

## Archivos del proyecto

| Archivo | Descripción |
|---|---|
| `Servidor.java` | Clase principal del servidor. Gestiona el mapa de clientes y acepta conexiones. |
| `ManejadorCliente.java` | Hilo que atiende a cada cliente individual. Procesa todos los comandos. |
| `Cliente.java` | Cliente de consola con hilo receptor concurrente para envío/recepción simultánea. |

---

## Arquitectura

```
+----------+        TCP        +------------------+
| Cliente1 | <--------------> |                  |
+----------+                  |   Servidor.java  |
                               |   (puerto 5000) |
+----------+        TCP        |                  |
| Cliente2 | <--------------> |  ManejadorCliente|  <- Hilo por cliente
+----------+                  |  ManejadorCliente|  <- Hilo por cliente
                               +------------------+
```

El servidor crea un nuevo hilo (`ManejadorCliente`) por cada cliente que se conecta. Esto permite atender múltiples clientes simultáneamente sin que se bloqueen entre sí.

---

## Comandos disponibles

| Comando | Descripción | Ejemplo |
|---|---|---|
| `FECHA` | Muestra fecha y hora del servidor | `FECHA` |
| `LISTA` | Lista los clientes conectados | `LISTA` |
| `CALC <expr>` | Resuelve una expresión matemática | `CALC 5+3*2` |
| `*<nombre> <msg>` | Mensaje privado a un cliente | `*Juan Hola!` |
| `*ALL <msg>` | Mensaje a todos los clientes | `*ALL Como están todos?` |
| `SALIR` | Desconecta al cliente | `SALIR` |

---

## Características implementadas

- ✅ Atención de múltiples clientes **mediante hilos** (`Thread`)
- ✅ Nombres de usuario únicos (con sufijo numérico automático si hay duplicados)
- ✅ **Log completo** en consola del servidor con timestamp
- ✅ Menú de bienvenida con lista de comandos
- ✅ Consulta de fecha y hora del servidor
- ✅ Resolución de expresiones matemáticas (ScriptEngine)
- ✅ Listado de clientes conectados
- ✅ Mensajes privados entre clientes
- ✅ Mensajes broadcast (a todos)
- ✅ Notificación cuando un cliente se conecta/desconecta
- ✅ Manejo de errores en comandos inválidos
- ✅ Mapa de clientes thread-safe (`ConcurrentHashMap`)

---

## Cómo ejecutar

### 1. Compilar
```bash
javac Servidor.java ManejadorCliente.java Cliente.java
```

### 2. Iniciar el servidor
```bash
java Servidor
```

### 3. Conectar clientes (en terminales separadas)
```bash
java Cliente
```

---

## Relación con la práctica anterior

Esta práctica extiende directamente la **Práctica de Socket base - 2026** (aprobada), que implementaba un servidor TCP simple de un solo cliente. Los archivos `Servidor.java` y `Cliente.java` fueron refactorizados para soportar concurrencia mediante hilos.
