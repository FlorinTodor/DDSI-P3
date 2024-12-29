-- Subsistema Usuario
CREATE TABLE usuario (
    ID_Usuario integer NOT NULL,
    Correo varchar(30),
    Nombre varchar(30),
    Estado CHAR(1) CHECK (Estado IN ('A', 'I')),
    Direccion varchar(50),
    Contraseña varchar(50),
    Fecha_Registro TIMESTAMP DEFAULT SYSTIMESTAMP,
    Fecha_Desactivacion TIMESTAMP,
    PRIMARY KEY(ID_Usuario),
    UNIQUE(Correo)
);

CREATE SEQUENCE pago_seq
START WITH 1      -- Valor inicial
INCREMENT BY 1    -- De cuánto en cuánto incrementa
NOCACHE           -- Opcional
NOCYCLE; 

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
    ID_Usuario int,
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
    ID_Carrito INTEGER NOT NULL,
    ID_Pedido INTEGER NOT NULL,
    PRIMARY KEY (ID_Carrito, ID_Pedido),
    FOREIGN KEY (ID_Carrito) REFERENCES CARRITO(ID_Carrito),
    FOREIGN KEY (ID_Pedido) REFERENCES PEDIDO(ID_Pedido)
);

-- Relación: Gestión de Pedido
CREATE TABLE GestionPedido (
    ID_Usuario integer REFERENCES usuario(ID_Usuario),
    ID_Pedido integer REFERENCES pedido(ID_Pedido),
    PRIMARY KEY(ID_Pedido),
    UNIQUE(ID_Usuario)
);

--Secuencia para los id_pedido
CREATE SEQUENCE seq_id_pedido START WITH 1 INCREMENT BY 1;

-- DISPARADORES --

CREATE OR REPLACE TRIGGER TRIG_VerificaPedidoYUsuario
BEFORE INSERT OR UPDATE ON Gestion_Reseña
                            FOR EACH ROW
DECLARE
v_contador NUMBER;
BEGIN
   ------------------------------------------------------------------
   -- 1) Verificar que el ID_Pedido exista en la tabla Pedido
   ------------------------------------------------------------------
SELECT COUNT(*)
INTO v_contador
FROM pedido
WHERE ID_Pedido = :NEW.ID_Pedido;

IF v_contador = 0 THEN
      RAISE_APPLICATION_ERROR(
         -20000,
         'Error RS5.1.1: El ID_Pedido ' || :NEW.ID_Pedido ||
         ' no existe en la tabla Pedido.'
      );
END IF;

   ------------------------------------------------------------------
   -- 2) Verificar que el ID_Usuario asociado a ese Pedido exista
   ------------------------------------------------------------------
SELECT COUNT(*)
INTO v_contador
FROM pedido p
         JOIN usuario u ON p.ID_Usuario = u.ID_Usuario
WHERE p.ID_Pedido = :NEW.ID_Pedido;

IF v_contador = 0 THEN
      RAISE_APPLICATION_ERROR(
         -20001,
         'Error RS5.1.1: El Pedido ' || :NEW.ID_Pedido ||
         ' no tiene un Usuario válido asociado.'
      );
END IF;
END;
/
--Disparador que comprueba que usuario asociado a producto en modificaProducto existe

CREATE OR REPLACE TRIGGER validar_usuario_en_modificaProducto
AFTER INSERT OR UPDATE ON modificaProducto
                           FOR EACH ROW
DECLARE
usuario_existe INTEGER;
    BEGIN
        -- Verificar si el usuario asociado al producto existe
        SELECT COUNT(*)
        INTO usuario_existe
        FROM usuario
        WHERE ID_Usuario = : NEW.ID_Usuario;

        -- Opcional: Lanzar un error si se requiere notificar el problema
        IF usuario_existe = 0 THEN
            RAISE_APPLICATION_ERROR(-20002, 'El usuario asociado al producto no existe. Producto eliminado.');
        END IF;
END;
/

--Disparador que verifica que el producto en tabla ModificaProducto existe
CREATE OR REPLACE TRIGGER validar_producto_en_modificaProducto
AFTER INSERT OR UPDATE ON modificaProducto
                           FOR EACH ROW
DECLARE
producto_existe INTEGER;
    BEGIN
        -- Verificar si el producto existe en la tabla producto
            SELECT COUNT(*)
            INTO producto_existe
            FROM producto
            WHERE ID_Producto = :NEW.ID_Producto;

        -- Opcional: Lanzar un error para notificar el problema
        IF producto_existe = 0 THEN
            RAISE_APPLICATION_ERROR(-20003, 'El producto asociado no existe. Relación eliminada de modificaProducto.');
        END IF;
END;
/

--Disparador que comprueba que el producto agregado haya sido subido por un usuario
CREATE OR REPLACE TRIGGER validar_relacion_producto_modificaProducto
AFTER INSERT OR UPDATE ON producto
                           FOR EACH ROW
DECLARE
relacion_valida INTEGER;
    BEGIN
        -- Verificar si el producto tiene una relación válida en modificaProducto
            SELECT COUNT(*)
            INTO relacion_valida
            FROM modificaProducto
            WHERE ID_Producto = :NEW.ID_Producto;

        -- Opcional: Lanzar un error para informar sobre la eliminación
        IF relacion_valida = 0 THEN
            RAISE_APPLICATION_ERROR(-20004, 'El producto no tiene una relación válida en modificaProducto. Producto eliminado.');
        END IF;
END;
/

-- Disparador que comprueba si el producto que queremos agregar al carrito existe
-- y si la cantidad a agregar es menor o igual que la cantidad en stock
CREATE OR REPLACE TRIGGER verificar_cantidad_producto
    BEFORE INSERT OR UPDATE ON tiene
    FOR EACH ROW
DECLARE
    cantidad_producto INTEGER;
    producto_existe INTEGER;
BEGIN
    -- Verificar si el producto existe en la tabla Producto
    SELECT COUNT(*)
    INTO producto_existe
    FROM producto
    WHERE ID_Producto = :NEW.ID_Producto;

    IF producto_existe = 0 THEN
        RAISE_APPLICATION_ERROR(
                -20006,
                'Error: El producto no existe en la tabla Producto.'
        );
    END IF;

    -- Obtener la cantidad disponible del producto en la tabla Producto
    SELECT Cantidad
    INTO cantidad_producto
    FROM producto
    WHERE ID_Producto = :NEW.ID_Producto;

    -- Verificar que la cantidad a insertar sea menor que la cantidad disponible
    IF :NEW.Cantidad > cantidad_producto THEN
        RAISE_APPLICATION_ERROR(
                -20005,
                'Error: La cantidad a insertar es mayor que la cantidad disponible del producto.'
        );
    END IF;
END;
/

SELECT * FROM Usuario;
SELECT * FROM Producto;
SELECT * FROM Pedido;
SELECT * FROM Reseña;

COMMIT;