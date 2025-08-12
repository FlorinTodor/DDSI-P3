-- *************************
-- 3) Crear tablas base
-- *************************

-- Tabla: USUARIO
CREATE TABLE usuario (
                         ID_Usuario          INTEGER      NOT NULL,
                         Correo              VARCHAR2(30),
                         Nombre              VARCHAR2(30),
                         Estado              CHAR(1) CHECK (Estado IN ('A', 'I')),
                         Direccion           VARCHAR2(50),
                         Contraseña          VARCHAR2(50),
                         Fecha_Registro      TIMESTAMP DEFAULT SYSTIMESTAMP,
                         Fecha_Desactivacion TIMESTAMP DEFAULT NULL,
                         CONSTRAINT PK_usuario PRIMARY KEY (ID_Usuario),
                         CONSTRAINT UK_usuario_correo UNIQUE (Correo)
);

-- Tabla: PRODUCTO
CREATE TABLE producto (
                          ID_Producto   INTEGER,
                          NombreProducto VARCHAR2(30),
                          Cantidad       INTEGER      NOT NULL CHECK (Cantidad >= 0),
                          Precio         FLOAT        NOT NULL CHECK (Precio >= 0),
                          CONSTRAINT PK_producto PRIMARY KEY (ID_Producto)
);

-- Tabla: CARRITO
CREATE TABLE carrito (
                         ID_Carrito    INTEGER NOT NULL,
                         CONSTRAINT PK_carrito PRIMARY KEY (ID_Carrito)
);

-- Tabla: PEDIDO
CREATE TABLE pedido (
                        ID_Pedido     INTEGER NOT NULL,
                        Direccion     VARCHAR2(30),
                        Estado_Pedido VARCHAR2(10),
                        Tipo_Pago     INTEGER,
                        Metodo_Envio  VARCHAR2(10),
                        ID_Usuario    INTEGER REFERENCES usuario(ID_Usuario),
                        CONSTRAINT PK_pedido PRIMARY KEY (ID_Pedido)
);

-- Tabla: RESEÑA
CREATE TABLE reseña (
                        ID_Resena   INTEGER PRIMARY KEY,
                        Comentario  VARCHAR2(500),
                        Valoracion  INTEGER
);

-- Tabla: PAGO
CREATE TABLE pago (
                      ID_metodoPago     INTEGER PRIMARY KEY,
                      ID_Usuario        INTEGER,
                      Tipo_MetodoPago   VARCHAR2(50) NOT NULL,
                      Numero_Tarjeta    VARCHAR2(16),
                      Fecha_Expiracion  VARCHAR2(5),
                      Codigo_CVV        VARCHAR2(4),
                      Nombre_Titular    VARCHAR2(100),
                      Correo_PayPal     VARCHAR2(100),
                      Fecha             DATE,
                      FOREIGN KEY (ID_Usuario) REFERENCES usuario(ID_Usuario)
);

-- Crear secuencia para tabla pago
CREATE SEQUENCE PAGO_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE;

-- *************************
-- 4) Crear tablas RELACIONADAS (foráneas)
-- *************************

-- Tabla: GestionPago
CREATE TABLE GestionPago (
                             ID_Usuario     INTEGER,
                             ID_metodoPago  INTEGER,
                             CONSTRAINT PK_GestionPago PRIMARY KEY (ID_Usuario),
                             CONSTRAINT UK_GestionPago_metodo UNIQUE (ID_metodoPago),
                             CONSTRAINT FK_GestionPago_usuario FOREIGN KEY (ID_Usuario) REFERENCES usuario(ID_Usuario),
                             CONSTRAINT FK_GestionPago_pago    FOREIGN KEY (ID_metodoPago) REFERENCES pago(ID_metodoPago)
);

-- Tabla: Realiza
CREATE TABLE Realiza (
                         ID_metodoPago  INTEGER,
                         ID_Pedido      INTEGER,
                         Metodo_Pago    VARCHAR2(30),
                         CONSTRAINT PK_Realiza PRIMARY KEY (ID_Pedido),
                         CONSTRAINT UK_Realiza_metodoPago UNIQUE (ID_metodoPago),
                         CONSTRAINT FK_Realiza_pago    FOREIGN KEY (ID_metodoPago) REFERENCES pago(ID_metodoPago),
                         CONSTRAINT FK_Realiza_pedido  FOREIGN KEY (ID_Pedido)     REFERENCES pedido(ID_Pedido)
);

-- Tabla: Gestion_Reseña
CREATE TABLE Gestion_Reseña (
                                ID_Resena INTEGER,
                                ID_Pedido INTEGER,
                                CONSTRAINT PK_GestionReseña PRIMARY KEY (ID_Resena),
                                CONSTRAINT FK_GestionReseña_resena FOREIGN KEY (ID_Resena) REFERENCES reseña(ID_Resena),
                                CONSTRAINT FK_GestionReseña_pedido FOREIGN KEY (ID_Pedido) REFERENCES pedido(ID_Pedido)
);

-- Tabla: modificaProducto
CREATE TABLE modificaProducto (
                                  ID_Usuario  INTEGER REFERENCES usuario(ID_Usuario),
                                  ID_Producto INTEGER REFERENCES producto(ID_Producto),
                                  CONSTRAINT PK_modificaProducto PRIMARY KEY (ID_Producto)
);

-- Tabla: tiene
CREATE TABLE tiene (
                       ID_Carrito  INTEGER REFERENCES carrito(ID_Carrito),
                       ID_Producto INTEGER REFERENCES producto(ID_Producto),
                       Cantidad    INTEGER CHECK (Cantidad > 0),
                       CONSTRAINT PK_tiene PRIMARY KEY (ID_Carrito, ID_Producto)
);

-- Tabla: GestionCarrito
CREATE TABLE GestionCarrito (
                                ID_Carrito INTEGER REFERENCES carrito(ID_Carrito),
                                ID_Pedido  INTEGER REFERENCES pedido(ID_Pedido),
                                CONSTRAINT PK_GestionCarrito PRIMARY KEY (ID_Carrito, ID_Pedido)
);

-- Tabla: GestionPedido
CREATE TABLE GestionPedido (
                               ID_Usuario INTEGER REFERENCES usuario(ID_Usuario),
                               ID_Pedido  INTEGER REFERENCES pedido(ID_Pedido),
                               CONSTRAINT PK_GestionPedido PRIMARY KEY (ID_Usuario, ID_Pedido),
                               CONSTRAINT UK_GestionPedido_pedido UNIQUE (ID_Pedido)
);

COMMIT;
/

--
-- SCRIPT: Crear secuencias
--

-- Secuencia de pedido

/
CREATE SEQUENCE seq_id_pedido
    START WITH 1
    INCREMENT BY 1
    NOCACHE;
/

-- Secuencia de carrito

/
CREATE SEQUENCE seq_id_carrito
    START WITH 1
    INCREMENT BY 1
    NOCACHE;
/
COMMIT;
/