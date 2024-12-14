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




SELECT * FROM Usuario;
SELECT * FROM Producto;
SELECT * FROM Pedido;
SELECT * FROM Reseña;

COMMIT;