-- Subsistema Usuario
CREATE TABLE usuario (
    ID_Usuario integer NOT NULL,
    Correo varchar(30),
    Nombre varchar(30),
    Estado CHAR(1) CHECK (Estado IN ('A', 'I')),
    Direccion varchar(50),
    PRIMARY KEY(ID_Usuario)
);

-- Subsistema Producto
CREATE TABLE producto (
    ID_Producto integer NOT NULL,
    NombreProducto varchar(30),
    ID_Usuario integer REFERENCES usuario(ID_Usuario),
    Cantidad integer,
    Precio float,
    PRIMARY KEY(ID_Producto)
);

-- Subsistema Carrito
CREATE TABLE carrito (
    ID_Carrito integer NOT NULL,
    PRIMARY KEY(ID_Carrito)
);

-- Subsistema Pedido
CREATE TABLE pedido (
    ID_Pedido integer NOT NULL,
    Direccion varchar(30),
    Estado_Pedido varchar(10),
    Tipo_Pago integer,
    Metodo_Envio varchar(10),
    ID_Usuario integer REFERENCES usuario(ID_Usuario),
    PRIMARY KEY(ID_Pedido)
);

-- Subsistema Reseña
CREATE TABLE reseña (
    ID_Resena INT PRIMARY KEY, -- Evitamos caracteres especiales
    Comentario VARCHAR2(500),
    Valoracion INT
);

-- Subsistema Pagos
CREATE TABLE pago (
    ID_metodoPago INT PRIMARY KEY,
    Fecha DATE
);

-- Relación: Gestión de Pagos
CREATE TABLE GestionPago (
    ID_Usuario INT,
    ID_metodoPago INT,
    PRIMARY KEY (ID_Usuario),
    UNIQUE (ID_metodoPago),
    FOREIGN KEY (ID_Usuario) REFERENCES usuario(ID_Usuario),
    FOREIGN KEY (ID_metodoPago) REFERENCES pago(ID_metodoPago)
);

-- Relación: Realiza
CREATE TABLE Realiza (
    ID_metodoPago INT,
    ID_Pedido INT,
    Metodo_Pago VARCHAR(30),
    PRIMARY KEY (ID_Pedido),
    UNIQUE (ID_metodoPago),
    FOREIGN KEY (ID_Pedido) REFERENCES pedido(ID_Pedido),
    FOREIGN KEY (ID_metodoPago) REFERENCES pago(ID_metodoPago)
);

-- Relación: Gestión de Reseñas
CREATE TABLE Gestion_Reseña (
    ID_Resena INT, -- Evitamos caracteres especiales
    ID_Pedido INT,
    PRIMARY KEY (ID_Resena),
    FOREIGN KEY (ID_Resena) REFERENCES reseña(ID_Resena),
    FOREIGN KEY (ID_Pedido) REFERENCES pedido(ID_Pedido)
);

-- Relación: Modificación de Productos
CREATE TABLE modificaProducto (
    ID_Usuario integer REFERENCES usuario(ID_Usuario),
    ID_Producto integer REFERENCES producto(ID_Producto),
    PRIMARY KEY(ID_Usuario)
);

-- Relación: Productos en Carrito
CREATE TABLE tiene (
    ID_Carrito integer REFERENCES carrito(ID_Carrito),
    ID_Producto integer REFERENCES producto(ID_Producto),
    Cantidad integer,
    PRIMARY KEY(ID_Carrito, ID_Producto)
);

-- Relación: Gestión de Carrito
CREATE TABLE GestionCarrito (
    ID_Carrito integer REFERENCES carrito(ID_Carrito),
    ID_Pedido integer REFERENCES pedido(ID_Pedido),
    PRIMARY KEY(ID_Carrito)
);

-- Relación: Gestión de Pedido
CREATE TABLE GestionPedido (
    ID_Usuario integer REFERENCES usuario(ID_Usuario),
    ID_Pedido integer REFERENCES pedido(ID_Pedido),
    PRIMARY KEY(ID_Pedido),
    UNIQUE(ID_Usuario)
);



-- PRUEBAS
/*
-- Insertar Usuarios
INSERT INTO usuario (ID_Usuario, Correo, Nombre, Estado, Direccion)
VALUES (1, 'user1@example.com', 'Juan', 'A', 'Calle Falsa 123');

INSERT INTO usuario (ID_Usuario, Correo, Nombre, Estado, Direccion)
VALUES (2, 'user2@example.com', 'Maria', 'A', 'Avenida Principal 456');

-- Insertar Productos
INSERT INTO producto (ID_Producto, NombreProducto, ID_Usuario, Cantidad, Precio)
VALUES (10, 'Camisa', 1, 50, 19.99);

INSERT INTO producto (ID_Producto, NombreProducto, ID_Usuario, Cantidad, Precio)
VALUES (11, 'Pantalón', 2, 20, 29.99);

-- Insertar Pedidos
INSERT INTO pedido (ID_Pedido, Direccion, Estado_Pedido, Tipo_Pago, Metodo_Envio, ID_Usuario)
VALUES (100, 'Calle Falsa 123', 'Entregado', 1, 'Correo', 1);

INSERT INTO pedido (ID_Pedido, Direccion, Estado_Pedido, Tipo_Pago, Metodo_Envio, ID_Usuario)
VALUES (101, 'Avenida Principal 456', 'Entregado', 2, 'Mensajeria', 2);

-- Insertar Reseñas
INSERT INTO reseña (ID_Resena, Comentario, Valoracion)
VALUES (1, 'Muy buen producto', 5);

INSERT INTO reseña (ID_Resena, Comentario, Valoracion)
VALUES (2, 'Rápida entrega, producto aceptable', 4);

-- Asociar Reseñas con Pedidos
INSERT INTO Gestion_Reseña (ID_Resena, ID_Pedido)
VALUES (1, 100);

INSERT INTO Gestion_Reseña (ID_Resena, ID_Pedido)
VALUES (2, 101);

-- Insertar Pagos
INSERT INTO pago (ID_metodoPago, Fecha)
VALUES (1, SYSDATE);

INSERT INTO pago (ID_metodoPago, Fecha)
VALUES (2, SYSDATE);

INSERT INTO GestionPago (ID_Usuario, ID_metodoPago)
VALUES (1, 1);

INSERT INTO GestionPago (ID_Usuario, ID_metodoPago)
VALUES (2, 2);

INSERT INTO Realiza (ID_metodoPago, ID_Pedido, Metodo_Pago)
VALUES (1, 100, 'Tarjeta');

INSERT INTO Realiza (ID_metodoPago, ID_Pedido, Metodo_Pago)
VALUES (2, 101, 'PayPal');

*/


SELECT * FROM Usuario;
SELECT * FROM Producto;
SELECT * FROM Pedido;
SELECT * FROM Reseña;

COMMIT;