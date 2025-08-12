CREATE OR REPLACE TRIGGER TRIG_VerificaPedidoYUsuario
BEFORE INSERT OR UPDATE ON Gestion_Reseña
                            FOR EACH ROW
DECLARE
v_contador NUMBER;
BEGIN
   -- Verifica que el ID_Pedido exista en la tabla Pedido
SELECT COUNT(*)
INTO v_contador
FROM pedido
WHERE ID_Pedido = :NEW.ID_Pedido;

IF v_contador = 0 THEN
      RAISE_APPLICATION_ERROR(
          -20000,
          'Error RS5.1.1: El ID_Pedido ' || :NEW.ID_Pedido || ' no existe en la tabla Pedido.'
      );
END IF;

   -- Verifica que el Pedido tenga un Usuario asociado
SELECT COUNT(*)
INTO v_contador
FROM pedido p
         JOIN usuario u ON p.ID_Usuario = u.ID_Usuario
WHERE p.ID_Pedido = :NEW.ID_Pedido;

IF v_contador = 0 THEN
      RAISE_APPLICATION_ERROR(
          -20001,
          'Error RS5.1.1: El Pedido ' || :NEW.ID_Pedido || ' no tiene un Usuario válido asociado.'
      );
END IF;
END;


/
CREATE OR REPLACE TRIGGER validar_usuario_en_modificaProducto
AFTER INSERT OR UPDATE ON modificaProducto
                           FOR EACH ROW
DECLARE
usuario_existe INTEGER;
BEGIN
SELECT COUNT(*)
INTO usuario_existe
FROM usuario
WHERE ID_Usuario = :NEW.ID_Usuario;

IF usuario_existe = 0 THEN
       RAISE_APPLICATION_ERROR(
           -20002,
           'El usuario asociado al producto no existe.'
       );
END IF;
END;
/

CREATE OR REPLACE TRIGGER validar_usuario_en_modificaProducto
AFTER INSERT OR UPDATE ON modificaProducto
                           FOR EACH ROW
DECLARE
usuario_existe INTEGER;
BEGIN
SELECT COUNT(*)
INTO usuario_existe
FROM usuario
WHERE ID_Usuario = :NEW.ID_Usuario;

IF usuario_existe = 0 THEN
       RAISE_APPLICATION_ERROR(
           -20002,
           'El usuario asociado al producto no existe.'
       );
END IF;
END;
/

CREATE OR REPLACE TRIGGER TRIG_VERIFICAR_USUARIO_EXISTE
BEFORE INSERT ON pago
FOR EACH ROW
DECLARE
usuario_existe NUMBER;
BEGIN
SELECT COUNT(*)
INTO usuario_existe
FROM usuario
WHERE ID_Usuario = :NEW.ID_Usuario;

IF usuario_existe = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'Error: El usuario asociado al método de pago no existe.'
        );
END IF;
END;
/

CREATE OR REPLACE TRIGGER TRIG_VERIFICAR_METODO_PAGO_EXISTE
BEFORE INSERT ON Realiza
FOR EACH ROW
DECLARE
metodo_pago_existe NUMBER;
BEGIN
SELECT COUNT(*)
INTO metodo_pago_existe
FROM pago
WHERE ID_metodoPago = :NEW.ID_metodoPago;

IF metodo_pago_existe = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20003,
            'Error: El método de pago especificado no existe.'
        );
END IF;
END;
/

CREATE OR REPLACE TRIGGER verificar_cantidad_producto
BEFORE INSERT OR UPDATE ON tiene
                            FOR EACH ROW
DECLARE
cantidad_producto  INTEGER;
    producto_existe    INTEGER;
BEGIN
    -- Verificar que el producto exista
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

    -- Verificar que la cantidad solicitada no supere la cantidad disponible
SELECT Cantidad
INTO cantidad_producto
FROM producto
WHERE ID_Producto = :NEW.ID_Producto;

IF :NEW.Cantidad > cantidad_producto THEN
        RAISE_APPLICATION_ERROR(
            -20005,
            'Error: La cantidad a insertar es mayor que la cantidad disponible del producto.'
        );
END IF;
END;
/

CREATE OR REPLACE TRIGGER evitar_carrito_duplicado
BEFORE INSERT ON GestionCarrito
FOR EACH ROW
DECLARE
carrito_duplicado INTEGER;
BEGIN
SELECT COUNT(*)
INTO carrito_duplicado
FROM GestionCarrito
WHERE ID_Carrito = :NEW.ID_Carrito
  AND ID_Pedido  = :NEW.ID_Pedido;

IF carrito_duplicado > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20007,
            'Error: Ya existe una relación entre este carrito y pedido.'
        );
END IF;
END;
/

CREATE OR REPLACE TRIGGER verificar_usuario_en_pedido
BEFORE INSERT OR UPDATE ON pedido
                            FOR EACH ROW
DECLARE
usuario_existe INTEGER;
BEGIN
SELECT COUNT(*)
INTO usuario_existe
FROM usuario
WHERE ID_Usuario = :NEW.ID_Usuario;

IF usuario_existe = 0 THEN
      RAISE_APPLICATION_ERROR(
          -20007,
          'Error: El usuario asociado al pedido no existe.'
      );
END IF;
END;
/

