package practica;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import practica.Carrito;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public class Pedido {
    private int idPedido;
    private Carrito carrito;
    private List<Integer> productos;
    private String estadoPedido;
    private int idUsuario;
    private String metodoEnvio;
    private String direccion;

    //constructor sin parámetros
    public Pedido() {
    }

    public Pedido(int idPedido, Carrito carrito, String estadoPedido, int idUsuario, String metodoEnvio, String direccion) {
        this.idPedido = idPedido;
        this.carrito = carrito;
        this.estadoPedido = estadoPedido;
        this.idUsuario = idUsuario;
        this.metodoEnvio = metodoEnvio;
        this.direccion = direccion;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "idPedido=" + idPedido +
                ", idUsuario=" + idUsuario +
                ", estadoPedido='" + estadoPedido + '\'' +
                ", metodoEnvio='" + metodoEnvio + '\'' +
                ", direccion='" + direccion + '\'' +
                '}';
    }
    /**
     * RF4.1: Añadir reseña sobre un pedido
     */
    public void realizarPedido(String direccion, Carrito carrito, int tipoPago, String metodoEnvio, int idUsuario) throws SQLException {
        java.sql.Connection  conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // Estados validos para metodo de  un envio
        String[] validStates = {"express", "normal", "frágil"};

        // Comprobar si es valido el estado del pedido
        if (!Arrays.asList(validStates).contains(metodoEnvio)) {
            throw new IllegalArgumentException("El método del envío no es válido.");
        }

        try {
            conn = Connection.connection;
            conn.setAutoCommit(false); // Iniciar transacción

            // Verificar si el usuario existe
            try {
                if (!Connection.doesUserExist(idUsuario)) {
                    throw new Exception("El usuario no existe.");
                }
            } catch (Exception e) {
                throw new SQLException("Error al verificar si el usuario existe.", e);
            }

            // Verificar si el carrito existe para el usuario
            int idCarrito = -1;
            try {
                idCarrito = carrito.getCarritoId(idUsuario);
                if (idCarrito == -1) {
                    throw new SQLException("Error al verificar la existencia del carrito: El carrito no existe para el usuario.");
                }
            } catch (Exception e) {
                throw new SQLException("Error al obtener el ID del carrito: " + e.getMessage(), e);
            }

            // Verificar si el carrito está vacío
           try {
                ArrayList<String> productosEnCarrito = carrito.viewCart(idUsuario);
                if (productosEnCarrito.isEmpty() || productosEnCarrito.get(0).equals("El carrito está vacío.")) {
                    throw new Exception("El carrito está vacío. No se puede realizar el pedido.");
                }
            } catch (Exception e) {
                throw new SQLException("Error al verificar el contenido del carrito: " + e.getMessage(), e);
            }
            try {
                idCarrito = carrito.getCarritoId(idUsuario);
                // Your code here
            } catch (Exception e) {
                e.printStackTrace();
                // Handle the exception appropriately
            }

            Map<Integer, Integer> productos = carrito.getProductosDelCarrito(idCarrito);
            // Verificar stock y estado de los productos en el carrito
            for (Map.Entry<Integer, Integer> producto : productos.entrySet()) {
                int idProducto = producto.getKey();
                int cantidad = producto.getValue();
                String sqlProducto = "SELECT Cantidad FROM producto WHERE ID_Producto = ?";
                pstmt = conn.prepareStatement(sqlProducto);
                pstmt.setInt(1, idProducto);
                rs = pstmt.executeQuery();
                if (!rs.next() || rs.getInt("Cantidad") <= 0) {
                    throw new SQLException("El producto con ID " + idProducto + " no tiene stock o no está habilitado.");
                }
            }

            // Buscar máximo id pedido asociado al usuario
            int idPedido = -1;
            String maxIdQuery = "SELECT MAX(ID_Pedido) FROM pedido WHERE ID_Usuario = ?";
            pstmt = conn.prepareStatement(maxIdQuery);
            pstmt.setInt(1, idUsuario); // Establecer el parámetro del ID de usuario
            rs = pstmt.executeQuery();
            if (rs.next()) {
                idPedido = rs.getInt(1); // Obtener el mayor ID_Pedido
                if (rs.wasNull()) {
                    idPedido = 0; // Si el resultado es NULL, asignar 0
                }
            }

            // Crear pedido
            String sqlPedido = "UPDATE PEDIDO SET Direccion = ?, Estado_Pedido = 'procesando', Tipo_Pago = ?, Metodo_Envio = ? WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlPedido);
            pstmt.setString(1, direccion);
            pstmt.setInt(2, tipoPago);
            pstmt.setString(3, metodoEnvio);
            pstmt.setInt(4, idPedido);
            pstmt.executeUpdate();

            // Insertar en la tabla GestionPedido
            String sqlGestionPedido = "INSERT INTO GestionPedido (ID_Usuario, ID_Pedido) VALUES (?, ?)";
            try (PreparedStatement psGestionPedido = conn.prepareStatement(sqlGestionPedido)) {
                psGestionPedido.setInt(1, idUsuario);
                psGestionPedido.setInt(2, idPedido);
                psGestionPedido.executeUpdate();
            } catch (SQLException e) {
                throw new SQLException("Error al insertar en la tabla GestionPedido: " + e.getMessage(), e);
            }

            // Actualizar el stock de los productos
           for (Map.Entry<Integer, Integer> producto : productos.entrySet()) {
               int idProducto = producto.getKey();
               String sqlUpdateStock = "UPDATE producto SET Cantidad = Cantidad - ? WHERE ID_Producto = ?";
               pstmt = conn.prepareStatement(sqlUpdateStock);
               pstmt.setInt(1, producto.getValue());
               pstmt.setInt(2, idProducto);
               pstmt.executeUpdate();
           }

            try{
                carrito.addCarritoEntry(idUsuario);
            } catch (Exception e) {
                e.printStackTrace();
            }

            conn.commit(); // Confirmar transacción
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Revertir transacción en caso de error
            }
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }

    /**
     * RF4.2: Ver historial de pedidos
     */
    public List<Pedido> verHistorialPedidos(int idUsuario) throws SQLException {
        java.sql.Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Pedido> pedidos = new ArrayList<>();

        try {
            conn = Connection.connection;

            // Consultar los pedidos del usuario
            String sqlPedidos = "SELECT ID_Pedido, Estado_Pedido, Metodo_Envio, Direccion FROM pedido WHERE ID_Usuario = ?";
            pstmt = conn.prepareStatement(sqlPedidos);
            pstmt.setInt(1, idUsuario);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int idPedido = rs.getInt("ID_Pedido");
                String estadoPedido = rs.getString("Estado_Pedido");
                String metodoEnvio = rs.getString("Metodo_Envio");
                String direccion = rs.getString("Direccion");

                // Consultar los productos asociados a cada pedido
                List<Integer> productos = new ArrayList<>();
                String sqlProductos = "SELECT ID_Producto FROM tiene WHERE ID_Carrito IN (SELECT ID_Carrito FROM GestionCarrito WHERE ID_Pedido = ?)";
                PreparedStatement pstmtProductos = conn.prepareStatement(sqlProductos);
                pstmtProductos.setInt(1, idPedido);
                ResultSet rsProductos = pstmtProductos.executeQuery();

                while (rsProductos.next()) {
                    productos.add(rsProductos.getInt("ID_Producto"));
                }

                rsProductos.close();
                pstmtProductos.close();

                // Crear un objeto Pedido y agregarlo a la lista
                Pedido pedido = new Pedido(idPedido, carrito, estadoPedido, idUsuario, metodoEnvio, direccion);
                pedidos.add(pedido);
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }

        return pedidos;
    }

    /**
     * RF4.3: Cancelar pedido
     */
    public void cancelarPedido(int idPedido, int idUsuario) throws SQLException {
        java.sql.Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connection.connection;
            conn.setAutoCommit(false); // Iniciar transacción

            // Verificar que el pedido pertenece al usuario y obtener su estado actual
            String sqlVerificarPedido = "SELECT ID_Usuario, Estado_Pedido FROM pedido WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlVerificarPedido);
            pstmt.setInt(1, idPedido);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("El pedido no existe.");
            }

            if (rs.getInt("ID_Usuario") != idUsuario) {
                throw new SQLException("El pedido no pertenece al usuario.");
            }

            // Verificar si el pedido ya está entregado
            String estadoPedido = rs.getString("Estado_Pedido");
            if ("entregado".equalsIgnoreCase(estadoPedido)) {
                throw new SQLException("No se puede cancelar un pedido que ya ha sido entregado.");
            }

            //Si está cancelado no se puede volver a cancelar
            if ("cancelado".equalsIgnoreCase(estadoPedido)) {
                throw new SQLException("El pedido ya está cancelado.");
            }

            // Actualizar el estado del pedido a "cancelado"
            String sqlActualizarEstado = "UPDATE pedido SET Estado_Pedido = 'cancelado' WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlActualizarEstado);
            pstmt.setInt(1, idPedido);
            pstmt.executeUpdate();

            // Obtener los productos asociados al pedido y sus cantidades
            Map<Integer, Integer> productos = new HashMap<>();
            String sqlProductos = "SELECT ID_Producto, Cantidad FROM tiene WHERE ID_Carrito IN (SELECT ID_Carrito FROM GestionCarrito WHERE ID_Pedido = ?)";
            pstmt = conn.prepareStatement(sqlProductos);
            pstmt.setInt(1, idPedido);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int idProducto = rs.getInt("ID_Producto");
                int cantidad = rs.getInt("Cantidad");
                productos.put(idProducto, cantidad);
            }

            // Restablecer el stock de los productos
            for (Map.Entry<Integer, Integer> entry : productos.entrySet()) {
                int idProducto = entry.getKey();
                int cantidad = entry.getValue();
                String sqlActualizarStock = "UPDATE producto SET Cantidad = Cantidad + ? WHERE ID_Producto = ?";
                pstmt = conn.prepareStatement(sqlActualizarStock);
                pstmt.setInt(1, cantidad);
                pstmt.setInt(2, idProducto);
                pstmt.executeUpdate();
            }

            conn.commit(); // Confirmar transacción
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Revertir transacción en caso de error
            }
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }


    /*
     /RF4.4: Opción para elegir el metodo de envío
     */
    public void elegirMetodoEnvio(String metodoEnvio, int idUsuario, int idPedido) throws SQLException {
        java.sql.Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connection.connection;
            conn.setAutoCommit(false); // Iniciar transacción

            // Verificar que el pedido pertenece al usuario
            String sqlVerificarPedido = "SELECT ID_Usuario FROM pedido WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlVerificarPedido);
            pstmt.setInt(1, idPedido);
            rs = pstmt.executeQuery();
            if (!rs.next() || rs.getInt("ID_Usuario") != idUsuario) {
                throw new SQLException("El pedido no pertenece al usuario.");
            }

            // Actualizar el metodo de envío del pedido
            String sqlActualizarMetodoEnvio = "UPDATE pedido SET Metodo_Envio = ? WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlActualizarMetodoEnvio);
            pstmt.setString(1, metodoEnvio);
            pstmt.setInt(2, idPedido);
            pstmt.executeUpdate();

            conn.commit(); // Confirmar transacción
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Revertir transacción en caso de error
            }
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }

    /*
     * RF4.5: Confirmación recepción del pedido
     */
    public void confirmarRecepcionPedido(int idUsuario, int idPedido) throws SQLException {
        java.sql.Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connection.connection;
            conn.setAutoCommit(false); // Iniciar transacción

            // Verificar que el pedido pertenece al usuario Y obtener su estado
            String sqlVerificarPedido = "SELECT ID_Usuario, Estado_Pedido FROM pedido WHERE ID_Pedido = ?"; // Se añade Estado_Pedido
            pstmt = conn.prepareStatement(sqlVerificarPedido);
            pstmt.setInt(1, idPedido);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("El pedido no existe.");
            }

            if (rs.getInt("ID_Usuario") != idUsuario) {
                throw new SQLException("El pedido no pertenece al usuario.");
            }

            // Verificar si el pedido está cancelado DESPUÉS de rs.next()
            String estadoPedido = rs.getString("Estado_Pedido");
            if ("cancelado".equalsIgnoreCase(estadoPedido)) {
                throw new SQLException("No se puede confirmar la recepción de un pedido cancelado.");
            }

            // Actualizar Estado_Pedido a 'entregado'
            String sqlActualizarEstado = "UPDATE pedido SET Estado_Pedido = 'entregado' WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlActualizarEstado);
            pstmt.setInt(1, idPedido);
            pstmt.executeUpdate();

            conn.commit(); // Confirmar transacción
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Revertir transacción en caso de error
            }
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }
    /**
     * RF4.6: Opción para elegir el metodo de pago
     */
    public void elegirMetodoPago( String tipoMetodoPago, int idPedido, int idUsuario) throws SQLException {
        java.sql.Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connection.connection;
            conn.setAutoCommit(false); // Iniciar transacción

            // Verificar que el pedido pertenece al usuario
            String sqlVerificarPedido = "SELECT ID_Usuario FROM pedido WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlVerificarPedido);
            pstmt.setInt(1, idPedido);
            rs = pstmt.executeQuery();
            if (!rs.next() || rs.getInt("ID_Usuario") != idUsuario) {
                throw new SQLException("El pedido no pertenece al usuario.");
            }

            // Actualizar el metodo de pago del pedido
            String sqlActualizarMetodoPago = "UPDATE pedido SET Tipo_Pago = ? WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlActualizarMetodoPago);
            pstmt.setString(1, tipoMetodoPago);
            pstmt.setInt(2, idPedido);
            pstmt.executeUpdate();

            conn.commit(); // Confirmar transacción
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Revertir transacción en caso de error
            }
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }

}
