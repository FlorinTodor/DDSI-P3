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
                WHERE ID_Usuario = :NEW.ID_Usuario;

            -- Si el usuario no existe, eliminar el producto correspondiente
                IF usuario_existe = 0 THEN
                DELETE FROM producto
                WHERE ID_Producto = :NEW.ID_Producto;

            -- Opcional: Lanzar un error si se requiere notificar el problema
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

        -- Si el producto no existe, eliminar la relación de modificaProducto
            IF producto_existe = 0 THEN
            DELETE FROM modificaProducto
            WHERE ID_Usuario = :NEW.ID_Usuario
              AND ID_Producto = :NEW.ID_Producto;

        -- Opcional: Lanzar un error para notificar el problema
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

        -- Si no hay una relación válida, eliminar el producto
            IF relacion_valida = 0 THEN
            DELETE FROM producto
            WHERE ID_Producto = :NEW.ID_Producto;

        -- Opcional: Lanzar un error para informar sobre la eliminación
            RAISE_APPLICATION_ERROR(-20004, 'El producto no tiene una relación válida en modificaProducto. Producto eliminado.');
    END IF;
END;
/





SELECT * FROM Usuario;
SELECT * FROM Producto;
SELECT * FROM Pedido;
SELECT * FROM Reseña;

COMMIT;