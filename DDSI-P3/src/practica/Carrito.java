package practica;

import java.sql.*;
import java.util.ArrayList;



public class Carrito {


    /**
     * VARIABLES GLOBALES
     *
     */

    private Integer idCarritoActivo = null; // Carrito actual activo por usuario
    private Integer idUsuarioActual = null; // Usuario actual asociado al carrito

    /**
     * Método auxiliar para obtener o crear el ID del carrito asociado a un usuario.
     * Si no existe un carrito, se crea uno nuevo en la tabla CARRITO.
     */
    private int getOrCreateCarritoIdByUsuario(int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;

        // Si el usuario cambia, reiniciamos el carrito activo
        if (idUsuarioActual == null || !idUsuarioActual.equals(idUsuario)) {
            idCarritoActivo = null;
            idUsuarioActual = idUsuario;
        }

        // Reutilizar el carrito activo si existe
        if (idCarritoActivo != null) {
            return idCarritoActivo;
        }

        // Intentar obtener el carrito existente para el usuario desde la tabla TIENE
        Integer idCarrito = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT t.ID_Carrito FROM TIENE t " +
                        "JOIN CARRITO c ON t.ID_Carrito = c.ID_Carrito " +
                        "WHERE EXISTS (SELECT 1 FROM USUARIO u WHERE u.ID_Usuario = ?) FETCH FIRST 1 ROWS ONLY")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idCarrito = rs.getInt("ID_Carrito");
                }
            }
        }

        // Si no existe un carrito, crearlo en la tabla CARRITO
        if (idCarrito == null) {
            int nuevoIdCarrito = 1; // Iniciar en 1 si no hay registros

            // Obtener el máximo ID_Carrito existente
            try (PreparedStatement ps = conn.prepareStatement("SELECT MAX(ID_Carrito) FROM CARRITO")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        nuevoIdCarrito = rs.getInt(1) + 1;
                    }
                }
            }

            // Insertar el nuevo carrito en la tabla CARRITO
            try (PreparedStatement psInsert = conn.prepareStatement(
                    "INSERT INTO CARRITO (ID_Carrito) VALUES (?)")) {
                psInsert.setInt(1, nuevoIdCarrito);
                psInsert.executeUpdate();
            }

            idCarrito = nuevoIdCarrito; // Asignar el nuevo ID_Carrito
        }

        // Actualizar la variable global
        idCarritoActivo = idCarrito;

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
        int idCarrito = getOrCreateCarritoIdByUsuario(idUsuario);

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
        int idCarrito = getOrCreateCarritoIdByUsuario(idUsuario);

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

        int idCarrito = getOrCreateCarritoIdByUsuario(idUsuario);

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
        // Verificar si el usuario existe
        if (!Connection.doesUserExist(idUsuario)) {
            throw new Exception("El usuario no existe.");
        }

        int idCarrito = getOrCreateCarritoIdByUsuario(idUsuario);

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
        int idCarrito = getOrCreateCarritoIdByUsuario(idUsuario);

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
