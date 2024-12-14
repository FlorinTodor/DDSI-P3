-- Truncar tablas dependientes primero
TRUNCATE TABLE Gestion_Reseña;
TRUNCATE TABLE Realiza;
TRUNCATE TABLE GestionPago;

-- Truncar tablas principales después
TRUNCATE TABLE reseña;
TRUNCATE TABLE pedido;
TRUNCATE TABLE producto;
TRUNCATE TABLE pago;
TRUNCATE TABLE carrito;
TRUNCATE TABLE usuario;


-- Tablas que dependen de otras (deben eliminarse primero)
DROP TABLE Gestion_Reseña;
DROP TABLE Realiza;
DROP TABLE GestionPago;
DROP TABLE GestionCarrito;
DROP TABLE GestionPedido;
DROP TABLE modificaProducto;
DROP TABLE tiene;

-- Tablas base (ya sin referencias)
DROP TABLE reseña;
DROP TABLE pedido;
DROP TABLE producto;
DROP TABLE pago;
DROP TABLE carrito;
DROP TABLE usuario;

