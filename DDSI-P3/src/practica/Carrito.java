package practica;

import java.sql.*;
import java.util.ArrayList;



public class Carrito {

    /**
     * Método auxiliar para obtener el ID del carrito asociado a un usuario.
     * Asume que existe una relación entre Usuario -> Pedido -> Carrito
     * a través de las tablas GestionPedido y GestionCarrito.
     */
    private int getCarritoIdByUsuario(int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn =  Connection.connection;

        // Verificar que el usuario existe
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE ID_USUARIO = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new Exception("El usuario no existe.");
                }
            }
        }

        // Obtener el pedido del usuario a través de GestionPedido
        Integer idPedido = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT ID_Pedido FROM GestionPedido WHERE ID_Usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idPedido = rs.getInt("ID_Pedido");
                } else {
                    throw new Exception("No se encontró un pedido asociado al usuario.");
                }
            }
        }

        // Obtener el ID del carrito asociado al pedido a través de GestionCarrito
        Integer idCarrito = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT ID_Carrito FROM GestionCarrito WHERE ID_Pedido = ?")) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idCarrito = rs.getInt("ID_Carrito");
                } else {
                    throw new Exception("No se encontró un carrito asociado al pedido del usuario.");
                }
            }
        }

        return idCarrito;
    }

    /**
     * RF3.1: Añadir producto al carrito
     * Permite agregar productos al carrito especificando la cantidad.
     * Verifica stock, verifica que el producto no exista ya en el carrito.
     */
    public void addProductToCart(int idUsuario, int idProducto, int cantidad) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn =  Connection.connection;

        // Comprobar que el producto existe y obtener stock
        int stockDisponible = 0;
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS Cnt, Cantidad FROM PRODUCTO WHERE ID_Producto = ?")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("Cnt") == 0) {
                        throw new Exception("El producto no existe.");
                    }
                    stockDisponible = rs.getInt("Cantidad");
                }
            }
        }

        // Verificar que la cantidad solicitada sea menor o igual al stock disponible
        if (cantidad > stockDisponible) {
            throw new Exception("La cantidad solicitada excede el stock disponible.");
        }

        // Obtener el ID_Carrito asociado al usuario
        int idCarrito = getCarritoIdByUsuario(idUsuario);

        // Verificar que el producto no esté ya en el carrito
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM TIENE WHERE ID_Carrito = ? AND ID_Producto = ?")) {
            ps.setInt(1, idCarrito);
            ps.setInt(2, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Según las restricciones, no se puede agregar un producto ya existente, en vez de ello se debería modificar.
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
    }

    /**
     * RF3.2: Ver carrito de compras
     * Muestra los productos añadidos al carrito y el subtotal.
     * Retorna una lista de cadenas con el detalle y el subtotal al final.
     */
    public ArrayList<String> viewCart(int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn =  Connection.connection;
        int idCarrito = getCarritoIdByUsuario(idUsuario);

        ArrayList<String> resultado = new ArrayList<>();
        double subtotal = 0.0;

        // Obtener productos del carrito
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
                    int idProd = rs.getInt("ID_Producto");
                    String nombre = rs.getString("NombreProducto");
                    double precio = rs.getDouble("Precio");
                    int cantidad = rs.getInt("Cantidad");
                    double totalProducto = precio * cantidad;
                    subtotal += totalProducto;
                    resultado.add("Producto: " + nombre + " | ID: " + idProd + " | Cantidad: " + cantidad + " | Precio: " + precio + " | Total: " + totalProducto);
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

        // Comprobar producto
        int stockDisponible = 0;
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS Cnt, Cantidad FROM PRODUCTO WHERE ID_Producto = ?")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("Cnt") == 0) {
                        throw new Exception("El producto no existe.");
                    }
                    stockDisponible = rs.getInt("Cantidad");
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

        int idCarrito = getCarritoIdByUsuario(idUsuario);

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
        int idCarrito = getCarritoIdByUsuario(idUsuario);

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
        int idCarrito = getCarritoIdByUsuario(idUsuario);

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
    }

}
