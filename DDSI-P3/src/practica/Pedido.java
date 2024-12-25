package practica;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public class Pedido {
    private int idPedido;
    private List<Integer> productos;
    private String estadoPedido;
    private int idUsuario;

    //constructor sin parámetros
    public Pedido() {
    }

    public Pedido(int idPedido, List<Integer> productos, String estadoPedido, int idUsuario) {
        this.idPedido = idPedido;
        this.productos = productos;
        this.estadoPedido = estadoPedido;
        this.idUsuario = idUsuario;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "idPedido='" + idPedido + '\'' +
                ", productos=" + productos +
                ", estadoPedido='" + estadoPedido + '\'' +
                ", idUsuario=" + idUsuario +
                '}';
    }
    /**
     * RF4.1: Añadir reseña sobre un pedido
     */
    public void realizarPedido(String direccion, int idPedido, String estadoPedido, int tipoPago, String metodoEnvio, int idUsuario, List<Integer> carrito) throws SQLException {
        java.sql.Connection  conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // EStados validos para un pedido
        String[] validStates = {"pendiente", "procesando", "enviado", "entregado"};

        // Comprobar si es valido el estado del pedido
        if (!Arrays.asList(validStates).contains(estadoPedido)) {
            throw new IllegalArgumentException("El estado del pedido no es válido.");
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

            // Verificar stock y estado de los productos en el carrito
            for (int idProducto : carrito) {
                String sqlProducto = "SELECT Cantidad FROM producto WHERE ID_Producto = ?";
                pstmt = conn.prepareStatement(sqlProducto);
                pstmt.setInt(1, idProducto);
                rs = pstmt.executeQuery();
                if (!rs.next() || rs.getInt("Cantidad") <= 0) {
                    throw new SQLException("El producto con ID " + idProducto + " no tiene stock o no está habilitado.");
                }
            }

            // Registrar el pedido
            String sqlPedido = "INSERT INTO pedido (ID_Pedido, Direccion, Estado_Pedido, Tipo_Pago, Metodo_Envio, ID_Usuario) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sqlPedido);
            pstmt.setInt(1, idPedido);
            pstmt.setString(2, direccion);
            pstmt.setString(3, estadoPedido);
            pstmt.setInt(4, tipoPago);
            pstmt.setString(5, metodoEnvio);
            pstmt.setInt(6, idUsuario);
            pstmt.executeUpdate();

            // Actualizar el stock de los productos
            for (int idProducto : carrito) {
                String sqlUpdateStock = "UPDATE producto SET Cantidad = Cantidad - 1 WHERE ID_Producto = ?";
                pstmt = conn.prepareStatement(sqlUpdateStock);
                pstmt.setInt(1, idProducto);
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

    /**
     * RF4.2: Ver historial de pedidos
     */
    public List<Pedido> verHistorialPedidos(int idUsuario) throws SQLException {
        java.sql.Connection  conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Pedido> pedidos = new ArrayList<>();

        try {
            conn = Connection.connection;

            // Consultar los pedidos del usuario
            String sqlPedidos = "SELECT ID_Pedido, Estado_Pedido FROM pedido WHERE ID_Usuario = ?";
            pstmt = conn.prepareStatement(sqlPedidos);
            pstmt.setInt(1, idUsuario);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int idPedido = rs.getInt("ID_Pedido");
                String estadoPedido = rs.getString("Estado_Pedido");

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
                Pedido pedido = new Pedido(idPedido, productos, estadoPedido, idUsuario);
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

            // Verificar que el pedido pertenece al usuario
            String sqlVerificarPedido = "SELECT ID_Usuario FROM pedido WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlVerificarPedido);
            pstmt.setInt(1, idPedido);
            rs = pstmt.executeQuery();
            if (!rs.next() || rs.getInt("ID_Usuario") != idUsuario) {
                throw new SQLException("El pedido no pertenece al usuario.");
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
        java.sql.Connection  conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connection.connection;
            conn.setAutoCommit(false); // Iniciar transacción

            // Verificar que el pedido pertenece al usuario
            String sqlVerificarPedido = "SELECT ID_Usuario, Fecha_Entrega FROM pedido WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlVerificarPedido);
            pstmt.setInt(1, idPedido);
            rs = pstmt.executeQuery();
            if (!rs.next() || rs.getInt("ID_Usuario") != idUsuario) {
                throw new SQLException("El pedido no pertenece al usuario.");
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
    public void elegirMetodoPago(int idMetodoPago, String tipoMetodoPago, int idPedido, int idUsuario) throws SQLException {
        java.sql.Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connection.connection;
            conn.setAutoCommit(false); // Iniciar transacción

            // Verificar que el metodo de pago es válido
            String sqlVerificarMetodoPago = "SELECT ID_metodoPago FROM pago WHERE ID_metodoPago = ? AND Tipo_MetodoPago = ?";
            pstmt = conn.prepareStatement(sqlVerificarMetodoPago);
            pstmt.setInt(1, idMetodoPago);
            pstmt.setString(2, tipoMetodoPago);
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("El método de pago no es válido.");
            }

            // Verificar que el pedido pertenece al usuario
            String sqlVerificarPedido = "SELECT ID_Usuario FROM pedido WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlVerificarPedido);
            pstmt.setInt(1, idPedido);
            rs = pstmt.executeQuery();
            if (!rs.next() || rs.getInt("ID_Usuario") != idUsuario) {
                throw new SQLException("El pedido no pertenece al usuario.");
            }

            // Actualizar el metodo de pago del pedido
            String sqlActualizarMetodoPago = "UPDATE pedido SET Tipo_Pago = ?, ID_metodoPago = ? WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlActualizarMetodoPago);
            pstmt.setString(1, tipoMetodoPago);
            pstmt.setInt(2, idMetodoPago);
            pstmt.setInt(3, idPedido);
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
