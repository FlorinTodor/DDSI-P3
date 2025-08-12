# 🛒 Plataforma de Comercio Electrónico – DDSI

## 📌 Descripción
Sistema desarrollado como parte de la asignatura **Diseño y Desarrollo de Sistemas de Información (DDSI)**.
Consiste en una **plataforma de comercio electrónico** que conecta compradores y vendedores, gestionando usuarios, productos, pedidos, métodos de pago y reseñas.
El sistema se conecta a una **base de datos Oracle** y cuenta con una interfaz desarrollada en **Java Swing**.

El acceso a la plataforma requiere **registro obligatorio**.

---

## 🗂️ Subsistemas

### 1. Gestión de Usuarios
- **Registro** con validación de correo único (cuentas deshabilitadas incluidas).
- **Inicio de sesión** con control de estado de cuenta.
- **Deshabilitación de cuenta** permanente (sin reactivación posible).
- **Recuperación de contraseña** con token (solo para cuentas activas).
- **Modificación de datos personales**.

### 2. Gestión de Productos
- Alta, edición y baja lógica (cantidad = 0).
- Visualización de productos sin stock pero no disponibles para compra.
- Control de stock automático al procesar pedidos.
- Asociación de productos a vendedores.

### 3. Gestión de Carrito de Compras
- Añadir, eliminar, modificar cantidades y vaciar carrito.
- Verificación de stock antes de confirmar pedidos.
- Cálculo automático de subtotal.

### 4. Gestión de Pedidos
- Creación de pedido con selección de **método de envío** y **pago**.
- Actualización de stock al confirmar compra.
- Historial de pedidos.
- Cancelación de pedidos con reposición de stock.
- Confirmación de recepción (manual o automática tras 3 días).

### 5. Gestión de Métodos de Pago
- Añadir, listar y eliminar métodos de pago (solo en proceso de compra).
- Validación de datos y propietario.
- Historial de transacciones.

### 6. Gestión de Reseñas
- Añadir reseñas tras confirmar recepción.
- Modificación y eliminación controlada.
- Listado de reseñas por pedido o usuario.

---

## 🛠️ Tecnologías utilizadas
- **Lenguaje**: Java 17
- **Interfaz**: Java Swing
- **Base de datos**: Oracle Database
- **Conector JDBC**: `ojdbc11.jar`
- **Arquitectura**: Cliente pesado con conexión directa a BD
- **Control de versiones**: Git

---

## 📂 Estructura del repositorio
```java
// Archivos principales del proyecto
Main.java         // Clase principal que inicia la aplicación
connection.java   // Gestiona la conexión y las transacciones con la BD Oracle
diseño.java       // Contiene la implementación de la interfaz gráfica con Java Swing
Producto.java     // Lógica de negocio para la gestión de productos
reseña.java       // Lógica de negocio para la gestión de reseñas
ojdbc11.jar       // Driver JDBC para la conexión con Oracle

```

---

## Extras
- Se mantiene una **conexión persistente** durante la ejecución de la aplicación.
- Se utiliza **`commit` manual** para asegurar la atomicidad de las transacciones complejas.
- Se implementa un `shutdown hook` para **cerrar de forma segura la conexión** al finalizar el programa, evitando recursos abiertos.

---

## 📈 Conclusiones
Este sistema proporciona una gestión integral de comercio electrónico, implementando todas las operaciones críticas:
- Alta, consulta y baja de usuarios y productos.
- Carrito y pedidos con control de stock en tiempo real.
- Gestión centralizada de métodos de pago.
- Sistema de reseñas que fomenta la confianza entre usuarios.

El diseño prioriza la **integridad de los datos**, la **usabilidad** de la interfaz y la **seguridad** en todas las operaciones transaccionales.
