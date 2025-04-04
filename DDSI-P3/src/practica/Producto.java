package practica;

import java.sql.*;
import java.util.ArrayList;

public class Producto{

    /**
     * RF2.1: Insertar Producto
     */
    public void addProduct(int idProducto, String nombre, double precio, int cantidad, int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;

    try {


        // Verificar si el usuario existe
        if (!Connection.doesUserExist(idUsuario)) {
            throw new Exception("El usuario no existe.");
        }


        // Validar que el precio y la cantidad sean mayores que 0
        if (precio <= 0) {
            throw new Exception("El precio debe ser mayor que 0.");
        }
        if (cantidad <= 0) {
            throw new Exception("La cantidad debe ser mayor que 0.");
        }

        // Verificar que el idProducto no exista ya en la tabla Producto
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM PRODUCTO WHERE ID_PRODUCTO = ?")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new Exception("El ID del producto ya existe.");
                }
            }
        }

        // Insertar el nuevo producto en la tabla Producto
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO PRODUCTO (ID_PRODUCTO, NOMBREPRODUCTO, CANTIDAD, PRECIO) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, idProducto);
            ps.setString(2, nombre);
            ps.setInt(3, cantidad);
            ps.setDouble(4, precio);
            ps.executeUpdate();
        }

        // Asociar el producto con el usuario en la tabla Modifica_Producto
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO MODIFICAPRODUCTO (ID_PRODUCTO, ID_USUARIO) VALUES (?, ?)")) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }

        conn.commit(); // Confirmar los cambios
    } catch (Exception e) {
        conn.rollback();
        throw e;
    }
    }

    /**
     * RF2.2: Editar producto
     */
    public void editProduct(int idProducto, String nuevoNombre, Integer nuevaCantidad, Double nuevoPrecio, int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;
        try {
            // Verificar si el usuario existe
            if (!Connection.doesUserExist(idUsuario)) {
                throw new Exception("El usuario no existe.");
            }

            // Comprobar si el usuario está autorizado para editar el producto
            String verificarPermisoQuery =
                    "SELECT COUNT(*) " +
                            "FROM modificaProducto " +
                            "WHERE ID_PRODUCTO = ? AND ID_USUARIO = ?";
            try (PreparedStatement ps = conn.prepareStatement(verificarPermisoQuery)) {
                ps.setInt(1, idProducto);
                ps.setInt(2, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new Exception("El usuario no está autorizado para editar este producto.");
                    }
                }
            }

            // Comprobar existencia del producto y obtener detalles actuales
            String nombreActual = null;
            Integer cantidadActual = null;
            Double precioActual = null;

            String verificarProductoQuery =
                    "SELECT NOMBREPRODUCTO, CANTIDAD, PRECIO " +
                            "FROM PRODUCTO " +
                            "WHERE ID_PRODUCTO = ?";
            try (PreparedStatement ps = conn.prepareStatement(verificarProductoQuery)) {
                ps.setInt(1, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception("El producto no existe.");
                    }
                    nombreActual = rs.getString("NOMBREPRODUCTO");
                    cantidadActual = rs.getInt("CANTIDAD");
                    precioActual = rs.getDouble("PRECIO");
                }
            }

            if (cantidadActual == null || precioActual == null) {
                throw new Exception("No se puede editar un producto que tiene campos inválidos.");
            }

            if (nuevaCantidad != null && nuevaCantidad <= 0) {
                throw new Exception("La nueva cantidad debe ser mayor que 0.");
            }

            if (nuevoPrecio != null && nuevoPrecio <= 0) {
                throw new Exception("El nuevo precio debe ser mayor que 0.");
            }

            // Usar valores actuales si no se proporcionan nuevos
            if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
                nuevoNombre = nombreActual;
            }
            if (nuevaCantidad == null) {
                nuevaCantidad = cantidadActual;
            }
            if (nuevoPrecio == null) {
                nuevoPrecio = precioActual;
            }

            // Actualizar el producto
            String actualizarProductoQuery =
                    "UPDATE PRODUCTO " +
                            "SET NOMBREPRODUCTO = ?, CANTIDAD = ?, PRECIO = ? " +
                            "WHERE ID_PRODUCTO = ?";
            try (PreparedStatement ps = conn.prepareStatement(actualizarProductoQuery)) {
                ps.setString(1, nuevoNombre);
                ps.setInt(2, nuevaCantidad);
                ps.setDouble(3, nuevoPrecio);
                ps.setInt(4, idProducto);
                ps.executeUpdate();
            }

            conn.commit(); // Confirmar cambios

        }catch (Exception e){
            conn.rollback();
            throw e;
        }
    }

    /**
     * RF2.3 Eliminar cantidad producto
     * Eliminar producto (cambiar cantidad a 0)
     * @param idProducto ID del producto
     * @throws Exception si el producto no existe o ya tiene cantidad 0
     */
    public void deleteProduct(int idProducto, int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;
        try {

            // Verificar si el usuario existe
            if (!Connection.doesUserExist(idUsuario)) {
                throw new Exception("El usuario no existe.");
            }
            // Comprobar si el usuario está autorizado para editar el producto
            String verificarPermisoQuery =
                    "SELECT COUNT(*) " +
                            "FROM modificaProducto " +
                            "WHERE ID_PRODUCTO = ? AND ID_USUARIO = ?";
            try (PreparedStatement ps = conn.prepareStatement(verificarPermisoQuery)) {
                ps.setInt(1, idProducto);
                ps.setInt(2, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new Exception("El usuario no está autorizado para editar este producto.");
                    }
                }
            }

            // Comprobar existencia y cantidad del producto
            Integer cantidadActual = null;
            String verificarProductoQuery =
                    "SELECT CANTIDAD FROM PRODUCTO WHERE ID_PRODUCTO = ?";
            try (PreparedStatement ps = conn.prepareStatement(verificarProductoQuery)) {
                ps.setInt(1, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception("El producto no existe.");
                    }
                    cantidadActual = rs.getInt("CANTIDAD");
                }
            }

            if (cantidadActual == null || cantidadActual == 0) {
                throw new Exception("El producto ya está eliminado (cantidad 0) o tiene un estado inválido.");
            }

            // Actualizar el producto: cantidad a 0
            String eliminarProductoQuery =
                    "UPDATE PRODUCTO SET CANTIDAD = 0 WHERE ID_PRODUCTO = ?";
            try (PreparedStatement ps = conn.prepareStatement(eliminarProductoQuery)) {
                ps.setInt(1, idProducto);
                ps.executeUpdate();
            }

            conn.commit(); // Confirmar cambios

        } catch (Exception e) {
            // Revertir cambios en caso de error
            conn.rollback();
            throw e;
        }
    }

    /**
     * RF2.4 Mostrar productos de un usuario
     * Obtener todos los productos subidos o modificados por un usuario
     * @param idUsuario ID del usuario
     * @return Lista de productos del usuario (formato "ID_Producto, Nombre, Cantidad, Precio")
     * @throws Exception si el usuario no existe o no tiene productos asociados
     */
    public ArrayList<String> getProductsByUser(int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }
        System.out.println("ID Usuario recibido: " + idUsuario);
        ArrayList<String> productos = new ArrayList<>();
        java.sql.Connection conn = Connection.connection;
        try {

            // Verificar si el usuario existe
            if (!Connection.doesUserExist(idUsuario)) {
                throw new Exception("El usuario no existe.");
            }


            // Obtener productos asociados al usuario desde la tabla MODIFICA_PRODUCTO
            String obtenerProductosQuery =
                    "SELECT p.ID_PRODUCTO, p.NOMBREPRODUCTO, p.CANTIDAD, p.PRECIO " +
                            "FROM PRODUCTO p " +
                            "JOIN MODIFICAPRODUCTO mp ON p.ID_PRODUCTO = mp.ID_PRODUCTO " +
                            "WHERE mp.ID_USUARIO = ?";
            try (PreparedStatement ps = conn.prepareStatement(obtenerProductosQuery)) {
                ps.setInt(1, idUsuario); //establecer el usuario
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idProducto = rs.getInt("ID_PRODUCTO");
                        String nombreProducto = rs.getString("NOMBREPRODUCTO");
                        int cantidad = rs.getInt("CANTIDAD");
                        double precio = rs.getDouble("PRECIO");
                        productos.add("ID_Producto: " + idProducto +
                                ", Nombre: " + nombreProducto +
                                ", Cantidad: " + cantidad +
                                ", Precio: " + precio);
                    }
                }
            }

            if (productos.isEmpty()) {
                throw new Exception("El usuario no tiene productos asociados.");
            }
        }catch (Exception e) {
            // Revertir cambios en caso de error
            conn.rollback();
            throw e;
        }
        return productos;
    }

    /**
     * Obtener todos los productos con un precio específico y cantidad mayor a 0
     * @param precio Precio de los productos a buscar
     * @return Lista de productos con el precio especificado y cantidad mayor a 0 (formato "ID_Producto, Nombre, Cantidad, Precio")
     * @throws Exception si no hay productos con ese precio o si ocurre un error
     */
    public ArrayList<String> getProductsByPrice(double precio) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        ArrayList<String> productos = new ArrayList<>();
        java.sql.Connection conn = Connection.connection;
        try {
            // Consulta para obtener productos con el precio especificado y cantidad mayor a 0
            String obtenerProductosPorPrecioQuery =
                    "SELECT ID_PRODUCTO, NOMBREPRODUCTO, CANTIDAD, PRECIO " +
                            "FROM PRODUCTO " +
                            "WHERE PRECIO = ? AND CANTIDAD > 0";

            try (PreparedStatement ps = conn.prepareStatement(obtenerProductosPorPrecioQuery)) {
                ps.setDouble(1, precio); // Establecer el parámetro del precio
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idProducto = rs.getInt("ID_PRODUCTO");
                        String nombreProducto = rs.getString("NOMBREPRODUCTO");
                        int cantidad = rs.getInt("CANTIDAD");
                        double precioProducto = rs.getDouble("PRECIO");
                        productos.add("ID_Producto: " + idProducto +
                                ", Nombre: " + nombreProducto +
                                ", Cantidad: " + cantidad +
                                ", Precio: " + precioProducto);
                    }
                }
            }

            // Si no se encuentran productos, lanzar una excepción
            if (productos.isEmpty()) {
                throw new Exception("No hay productos con el precio especificado y cantidad mayor a 0.");
            }
        }catch (Exception e) {
            // Revertir cambios en caso de error
            conn.rollback();
            throw e;
        }
        return productos;
    }





}
