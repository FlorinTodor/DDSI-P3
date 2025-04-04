package practica;

import java.sql.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class Carrito {

    /**
     * Obtener productos del carrito
     */
    public Map<Integer, Integer> getProductosDelCarrito(int idCarrito) throws SQLException {
        Map<Integer, Integer> productos = new HashMap<>();
        String sqlProductos = "SELECT ID_Producto, Cantidad FROM tiene WHERE ID_Carrito = ?";
        try (PreparedStatement ps = Connection.connection.prepareStatement(sqlProductos)) {
            ps.setInt(1, idCarrito);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idProducto = rs.getInt("ID_Producto");
                    int cantidad = rs.getInt("Cantidad");
                    productos.put(idProducto, cantidad);
                }
            }
        }
        return productos;
    }

    private void crearRelacionCarritoPedido(int idUsuario, int idCarrito) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;

        try {
            // Crear un nuevo ID de Pedido
            int idPedido = -1;

            // Obtener el valor actual de la secuencia
            try (PreparedStatement psSequence = conn.prepareStatement("SELECT seq_id_pedido.CURRVAL FROM DUAL")) { // DUAL es una tabla dummy en Oracle. En PostgreSQL sería simplemente "SELECT currval('seq_id_pedido')")
                try (ResultSet rs = psSequence.executeQuery()) {
                    if (rs.next()) {
                        idPedido = rs.getInt(1);
                    } else {
                        // Manejar el caso en el que no se puede obtener el valor actual de la secuencia (poco probable)
                        throw new SQLException("No se pudo obtener el valor actual de la secuencia");
                    }
                } catch (SQLException e) {
                    // Si CURRVAL falla, inicializar la secuencia con NEXTVAL
                    try (PreparedStatement psInitSequence = conn.prepareStatement("SELECT seq_id_pedido.NEXTVAL FROM DUAL")) {
                        try (ResultSet rsInit = psInitSequence.executeQuery()) {
                            if (rsInit.next()) {
                                idPedido = rsInit.getInt(1);
                            } else {
                                throw new SQLException("No se pudo inicializar la secuencia");
                            }
                        }
                    }
                }
            }

            try (PreparedStatement psIncrementSequence = conn.prepareStatement("SELECT seq_id_pedido.NEXTVAL FROM DUAL")) { // En PostgreSQL sería simplemente "SELECT nextval('seq_id_pedido')")
                psIncrementSequence.executeQuery();
            }

            System.out.println("currentIdPedido: " + idPedido + "idCarrito: " + idCarrito);

            // Insertar el pedido usando el valor actual de la secuencia
            String insertPedidoSQL = "INSERT INTO PEDIDO (ID_PEDIDO, ID_USUARIO) VALUES (?, ?)";
            try (PreparedStatement psPedido = conn.prepareStatement(insertPedidoSQL)) {
                psPedido.setInt(1, idPedido);
                psPedido.setInt(2, idUsuario);
                psPedido.executeUpdate();
            }

            // Insertar la entrada en la tabla GESTIONCARRITO
            String insertGestionCarritoSQL = "INSERT INTO GESTIONCARRITO (ID_CARRITO, ID_PEDIDO) VALUES (?, ?)";
            try (PreparedStatement psGestionCarrito = conn.prepareStatement(insertGestionCarritoSQL)) {
                psGestionCarrito.setInt(1, idCarrito);
                psGestionCarrito.setInt(2, idPedido);
                psGestionCarrito.executeUpdate();
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    }

    public int addCarritoEntry(int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;

        // Verificar que el usuario existe
        if (!Connection.doesUserExist(idUsuario)) {
            throw new Exception("El usuario no existe.");
        }

        try {
            // Crear un nuevo ID de carrito
            int idCarrito = -1;

            // Obtener el valor actual de la secuencia
            try (PreparedStatement psSequence = conn.prepareStatement("SELECT seq_id_carrito.CURRVAL FROM DUAL")) { // DUAL es una tabla dummy en Oracle. En PostgreSQL sería simplemente "SELECT currval('seq_id_pedido')")
                try (ResultSet rs = psSequence.executeQuery()) {
                    if (rs.next()) {
                        idCarrito = rs.getInt(1);
                    } else {
                        // Manejar el caso en el que no se puede obtener el valor actual de la secuencia (poco probable)
                        throw new SQLException("No se pudo obtener el valor actual de la secuencia");
                    }
                } catch (SQLException e) {
                    // Si CURRVAL falla, inicializar la secuencia con NEXTVAL
                    try (PreparedStatement psInitSequence = conn.prepareStatement("SELECT seq_id_carrito.NEXTVAL FROM DUAL")) {
                        try (ResultSet rsInit = psInitSequence.executeQuery()) {
                            if (rsInit.next()) {
                                idCarrito = rsInit.getInt(1);
                            } else {
                                throw new SQLException("No se pudo inicializar la secuencia");
                            }
                        }
                    }
                }
            }

            try (PreparedStatement psIncrementSequence = conn.prepareStatement("SELECT seq_id_carrito.NEXTVAL FROM DUAL")) { // En PostgreSQL sería simplemente "SELECT nextval('seq_id_pedido')")
                psIncrementSequence.executeQuery();
            }

            // Insertar el nuevo carrito en la tabla CARRITO
            String insertCarritoSQL = "INSERT INTO CARRITO (ID_CARRITO) VALUES (?)";
            try (PreparedStatement psCarrito = conn.prepareStatement(insertCarritoSQL)) {
                psCarrito.setInt(1, idCarrito);
                psCarrito.executeUpdate();
            }

            crearRelacionCarritoPedido(idUsuario, idCarrito);

            conn.commit();

            return idCarrito;
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }

    /**
     * Método auxiliar para obtener o crear el ID del carrito activo asociado a un usuario.
     * Si no existe un carrito activo, se crea uno nuevo.
     */
    public int getCarritoId(int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;

        // Verificar que el usuario existe
        if (!Connection.doesUserExist(idUsuario)) {
            throw new Exception("El usuario no existe.");
        }

        int idCarrito = -1;

        // 1. Buscar el máximo ID_Pedido para el usuario en la tabla PEDIDO
        String maxPedidoQuery = "SELECT MAX(ID_Pedido) FROM pedido WHERE ID_Usuario = ?";
        try (PreparedStatement ps = conn.prepareStatement(maxPedidoQuery)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int maxIdPedido = rs.getInt(1);
                    if (!rs.wasNull()) {
                        // 2. Buscar el ID_Carrito en la tabla GestionCarrito usando el ID_Pedido
                        String carritoQuery = "SELECT ID_Carrito FROM GestionCarrito WHERE ID_Pedido = ?";
                        try (PreparedStatement ps2 = conn.prepareStatement(carritoQuery)) {
                            ps2.setInt(1, maxIdPedido);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next()) {
                                    idCarrito = rs2.getInt("ID_Carrito");
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("idCarrito: " + idCarrito);

        return idCarrito;
    }

    /**
     * RF3.1: Añadir producto al carrito
     * Permite agregar productos al carrito especificando la cantidad.
     */
    public void addProductToCart(int idUsuario, int idProducto, int cantidad) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;
        // Verificar si el usuario existe
        if (!Connection.doesUserExist(idUsuario)) {
            throw new Exception("El usuario no existe.");
        }

        try {
            // Comprobar que el producto existe
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM PRODUCTO WHERE ID_Producto = ?")) {
                ps.setInt(1, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new Exception("El producto no existe.");
                    }
                }
            }

            // Obtener el stock disponible
            int stockDisponible = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT Cantidad FROM PRODUCTO WHERE ID_Producto = ?")) {
                ps.setInt(1, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stockDisponible = rs.getInt("Cantidad");
                    }
                }
            }

            // Verificar que la cantidad solicitada sea menor o igual al stock disponible
            if (cantidad > stockDisponible) {
                throw new Exception("La cantidad solicitada excede el stock disponible.");
            }

            // Obtener o crear el carrito asociado al usuario
            int idCarrito = getCarritoId(idUsuario);
            if (idCarrito == -1)
                idCarrito = addCarritoEntry(idUsuario);

            // Verificar que el producto no esté ya en el carrito
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM TIENE WHERE ID_Carrito = ? AND ID_Producto = ?")) {
                ps.setInt(1, idCarrito);
                ps.setInt(2, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new Exception("El producto ya está en el carrito. Utiliza la función de modificar cantidad.");
                    }
                }
            }

            // Insertar el producto en el carrito
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO TIENE (ID_Carrito, ID_Producto, Cantidad) VALUES (?, ?, ?)")) {
                ps.setInt(1, idCarrito);
                ps.setInt(2, idProducto);
                ps.setInt(3, cantidad);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }

    /**
     * RF3.2: Ver carrito de compras
     * Muestra los productos añadidos al carrito y el subtotal.
     */
    public ArrayList<String> viewCart(int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        // Verificar si el usuario existe
        if (!Connection.doesUserExist(idUsuario)) {
            throw new Exception("El usuario no existe.");
        }

        java.sql.Connection conn = Connection.connection;
        int idCarrito = getCarritoId(idUsuario);

        ArrayList<String> resultado = new ArrayList<>();
        double subtotal = 0.0;

        // Obtener productos del carrito desde la tabla TIENE
        String query = "SELECT p.ID_Producto, p.NombreProducto, p.Precio, t.Cantidad " +
                "FROM TIENE t " +
                "JOIN PRODUCTO p ON t.ID_Producto = p.ID_Producto " +
                "WHERE t.ID_Carrito = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idCarrito);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hayProductos = false;
                while (rs.next()) {
                    hayProductos = true;
                    int idProducto = rs.getInt("ID_Producto");
                    String nombre = rs.getString("NombreProducto");
                    double precio = rs.getDouble("Precio");
                    int cantidad = rs.getInt("Cantidad");
                    double totalProducto = precio * cantidad;
                    subtotal += totalProducto;

                    resultado.add("Producto: " + nombre + " | ID: " + idProducto +
                            " | Cantidad: " + cantidad + " | Precio: " + precio +
                            " | Total: " + totalProducto);
                }
                if (!hayProductos) {
                    resultado.add("El carrito está vacío.");
                } else {
                    resultado.add("Subtotal: " + subtotal);
                }
            }
        }

        return resultado;
    }

    /**
     * RF3.3: Modificar cantidad de un producto en el carrito
     * Ajusta la cantidad de un producto en el carrito.
     */
    public void modifyCartQuantity(int idUsuario, int idProducto, int nuevaCantidad) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn =  Connection.connection;
        // Verificar si el usuario existe
        if (!Connection.doesUserExist(idUsuario)) {
            throw new Exception("El usuario no existe.");
        }

        try {

            // Verificar si el producto existe
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM PRODUCTO WHERE ID_Producto = ?")) {
                ps.setInt(1, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new Exception("El producto no existe.");
                    }
                }
            }

            // Obtener el stock disponible del producto
            int stockDisponible = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT Cantidad FROM PRODUCTO WHERE ID_Producto = ?")) {
                ps.setInt(1, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stockDisponible = rs.getInt("Cantidad");
                    } else {
                        throw new Exception("Error al obtener el stock del producto.");
                    }
                }
            }

            // Comprobar que la nueva cantidad es válida
            if (nuevaCantidad < 0) {
                throw new Exception("La cantidad no puede ser negativa.");
            }
            if (nuevaCantidad > stockDisponible) {
                throw new Exception("La cantidad solicitada excede el stock disponible.");
            }

            int idCarrito = getCarritoId(idUsuario);

            // Verificar si el producto está en el carrito
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM TIENE WHERE ID_Carrito = ? AND ID_Producto = ?")) {
                ps.setInt(1, idCarrito);
                ps.setInt(2, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new Exception("El producto no está en el carrito.");
                    }
                }
            }

            // Actualizar la cantidad
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE TIENE SET Cantidad = ? WHERE ID_Carrito = ? AND ID_Producto = ?")) {
                ps.setInt(1, nuevaCantidad);
                ps.setInt(2, idCarrito);
                ps.setInt(3, idProducto);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }

    /**
     * RF3.4: Eliminar producto del carrito
     * Elimina un producto específico del carrito.
     */
    public void removeProductFromCart(int idUsuario, int idProducto) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn =  Connection.connection;
        // Verificar si el usuario existe
        if (!Connection.doesUserExist(idUsuario)) {
            throw new Exception("El usuario no existe.");
        }

        try {
            int idCarrito = getCarritoId(idUsuario);

            // Verificar si el producto está en el carrito
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM TIENE WHERE ID_Carrito = ? AND ID_Producto = ?")) {
                ps.setInt(1, idCarrito);
                ps.setInt(2, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new Exception("El producto no está en el carrito.");
                    }
                }
            }

            // Eliminar el producto del carrito
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM TIENE WHERE ID_Carrito = ? AND ID_Producto = ?")) {
                ps.setInt(1, idCarrito);
                ps.setInt(2, idProducto);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }

    /**
     * RF3.5: Vaciar carrito de compras
     * Elimina todos los productos del carrito.
     */
    public void emptyCart(int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn =  Connection.connection;

        try {
            int idCarrito = getCarritoId(idUsuario);

            // Verificar si el carrito ya está vacío
            boolean estaVacio = false;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM TIENE WHERE ID_Carrito = ?")) {
                ps.setInt(1, idCarrito);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        estaVacio = true;
                    }
                }
            }

            if (estaVacio) {
                throw new Exception("El carrito ya está vacío.");
            }

            // Vaciar el carrito
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM TIENE WHERE ID_Carrito = ?")) {
                ps.setInt(1, idCarrito);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }

}
