package practica;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Pago {
    class MetodoPago {
        private int idMetodoPago;
        private String tipoMetodoPago;
        private String terminacionTarjeta;
        private Timestamp fechaRegistro;

        public MetodoPago(int idMetodoPago, String tipoMetodoPago, String terminacionTarjeta, Timestamp fechaRegistro) {
            this.idMetodoPago = idMetodoPago;
            this.tipoMetodoPago = tipoMetodoPago;
            this.terminacionTarjeta = terminacionTarjeta;
            this.fechaRegistro = fechaRegistro;
        }

        @Override
        public String toString() {
            return "MetodoPago{" +
                    "idMetodoPago=" + idMetodoPago +
                    ", tipoMetodoPago='" + tipoMetodoPago + '\'' +
                    ", terminacionTarjeta='" + terminacionTarjeta + '\'' +
                    ", fechaRegistro=" + fechaRegistro +
                    '}';
        }
    }

    class Transaccion {
        private int idTransaccion;
        private int idPedido;
        private double cantidadPagada;
        private Timestamp fechaHora;
        private String estadoTransaccion;
        private String metodoPagoUtilizado;

        public Transaccion(int idTransaccion, int idPedido, double cantidadPagada, Timestamp fechaHora, String estadoTransaccion, String metodoPagoUtilizado) {
            this.idTransaccion = idTransaccion;
            this.idPedido = idPedido;
            this.cantidadPagada = cantidadPagada;
            this.fechaHora = fechaHora;
            this.estadoTransaccion = estadoTransaccion;
            this.metodoPagoUtilizado = metodoPagoUtilizado;
        }

        @Override
        public String toString() {
            return "Transaccion{" +
                    "idTransaccion=" + idTransaccion +
                    ", idPedido=" + idPedido +
                    ", cantidadPagada=" + cantidadPagada +
                    ", fechaHora=" + fechaHora +
                    ", estadoTransaccion='" + estadoTransaccion + '\'' +
                    ", metodoPagoUtilizado='" + metodoPagoUtilizado + '\'' +
                    '}';
        }
    }
    public int agregarMetodoPago(int idUsuario, String tipoMetodoPago, String numeroTarjeta, String fechaExpiracion, String codigoCVV, String nombreTitular, String correoPayPal) throws SQLException {
        java.sql.Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int idMetodoPago = -1;

        try {
            conn = Connection.connection;
            conn.setAutoCommit(false); // Iniciar transacción

            // Validar los detalles del metodo de pago
            if (tipoMetodoPago.equals("Tarjeta de crédito")) {
                if (numeroTarjeta.length() != 16 || !numeroTarjeta.matches("\\d{16}") ||
                        !fechaExpiracion.matches("\\d{2}/\\d{2}") ||
                        (codigoCVV.length() != 3 && codigoCVV.length() != 4) || !codigoCVV.matches("\\d{3,4}")) {
                    throw new SQLException("Detalles de la tarjeta de crédito no válidos.");
                }
            } else if (tipoMetodoPago.equals("PayPal")) {
                if (correoPayPal.length() > 100 || !correoPayPal.contains("@")) {
                    throw new SQLException("Correo electrónico de PayPal no válido.");
                }
            } else {
                throw new SQLException("Tipo de método de pago no válido.");
            }

            // Insertar los detalles del metodo de pago en la base de datos
            String sqlInsertarMetodoPago = "INSERT INTO pago (ID_metodoPago, Tipo_MetodoPago, Numero_Tarjeta, Fecha_Expiracion, Codigo_CVV, Nombre_Titular, Correo_PayPal, Fecha_Registro, ID_Usuario) VALUES (pago_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sqlInsertarMetodoPago, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, tipoMetodoPago);
            pstmt.setString(2, numeroTarjeta);
            pstmt.setString(3, fechaExpiracion);
            pstmt.setString(4, codigoCVV);
            pstmt.setString(5, nombreTitular);
            pstmt.setString(6, correoPayPal);
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(8, idUsuario);
            pstmt.executeUpdate();

            // Obtener el ID del metodo de pago generado
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                idMetodoPago = rs.getInt(1);
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

        return idMetodoPago;
    }

    public String eliminarMetodoPago(int idUsuario, int idMetodoPago) throws SQLException {
        java.sql.Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connection.connection;
            conn.setAutoCommit(false); // Iniciar transacción

            // Verificar que el metodo de pago pertenece al usuario
            String sqlVerificarMetodoPago = "SELECT ID_Usuario FROM pago WHERE ID_metodoPago = ?";
            pstmt = conn.prepareStatement(sqlVerificarMetodoPago);
            pstmt.setInt(1, idMetodoPago);
            rs = pstmt.executeQuery();
            if (!rs.next() || rs.getInt("ID_Usuario") != idUsuario) {
                throw new SQLException("El método de pago no pertenece al usuario.");
            }

            // Comprobar si el metodo de pago ha sido usado
            String sqlComprobarUso = "SELECT COUNT(*) AS uso FROM Realiza WHERE ID_metodoPago = ?";
            pstmt = conn.prepareStatement(sqlComprobarUso);
            pstmt.setInt(1, idMetodoPago);
            rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt("uso") > 0) {
                return "El método de pago no se ha podido eliminar porque ya ha sido usado.";
            }

            // Eliminar el metodo de pago
            String sqlEliminarMetodoPago = "DELETE FROM pago WHERE ID_metodoPago = ?";
            pstmt = conn.prepareStatement(sqlEliminarMetodoPago);
            pstmt.setInt(1, idMetodoPago);
            pstmt.executeUpdate();

            conn.commit(); // Confirmar transacción
            return "El método de pago ha sido eliminado exitosamente.";
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

    public List<MetodoPago> verMetodosPago(int idUsuario) throws SQLException {
        java.sql.Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<MetodoPago> metodosPago = new ArrayList<>();

        try {
            conn = Connection.connection;

            // Consultar los métodos de pago del usuario autenticado
            String sqlMetodosPago = "SELECT ID_metodoPago, Tipo_MetodoPago, Numero_Tarjeta, Fecha_Registro FROM pago WHERE ID_Usuario = ?";
            pstmt = conn.prepareStatement(sqlMetodosPago);
            pstmt.setInt(1, idUsuario);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int idMetodoPago = rs.getInt("ID_metodoPago");
                String tipoMetodoPago = rs.getString("Tipo_MetodoPago");
                String numeroTarjeta = rs.getString("Numero_Tarjeta");
                String terminacionTarjeta = numeroTarjeta != null ? numeroTarjeta.substring(numeroTarjeta.length() - 4) : null;
                Timestamp fechaRegistro = rs.getTimestamp("Fecha_Registro");

                MetodoPago metodoPago = new MetodoPago(idMetodoPago, tipoMetodoPago, terminacionTarjeta, fechaRegistro);
                metodosPago.add(metodoPago);
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }

        return metodosPago;
    }

    public String realizarPago(int idPedido, int idMetodoPago, double cantidadAPagar, int idUsuario) throws SQLException {
        java.sql.Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connection.connection;
            conn.setAutoCommit(false); // Iniciar transacción

            // Verificar que el pedido está pendiente de pago y que la cantidad a pagar coincide con la cantidad total del pedido
            String sqlVerificarPedido = "SELECT Estado_Pedido, Cantidad_Total FROM pedido WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlVerificarPedido);
            pstmt.setInt(1, idPedido);
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("El pedido no existe.");
            }
            String estadoPedido = rs.getString("Estado_Pedido");
            double cantidadTotal = rs.getDouble("Cantidad_Total");
            if (!"pendiente".equals(estadoPedido)) {
                return "El pedido no está pendiente de pago.";
            }
            if (cantidadAPagar != cantidadTotal) {
                return "La cantidad a pagar no coincide con la cantidad total del pedido.";
            }

            // Verificar que el metodo de pago pertenece al usuario autenticado
            String sqlVerificarMetodoPago = "SELECT ID_Usuario FROM pago WHERE ID_metodoPago = ?";
            pstmt = conn.prepareStatement(sqlVerificarMetodoPago);
            pstmt.setInt(1, idMetodoPago);
            rs = pstmt.executeQuery();
            if (!rs.next() || rs.getInt("ID_Usuario") != idUsuario) {
                return "El método de pago no pertenece al usuario.";
            }

            // Registrar la transacción en la base de datos
            String sqlRegistrarTransaccion = "INSERT INTO transaccion (ID_Transaccion, ID_Pedido, ID_Usuario, ID_metodoPago, Cantidad, Fecha_Hora) VALUES (transaccion_seq.NEXTVAL, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sqlRegistrarTransaccion, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, idPedido);
            pstmt.setInt(2, idUsuario);
            pstmt.setInt(3, idMetodoPago);
            pstmt.setDouble(4, cantidadAPagar);
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();

            // Obtener el ID de la transacción generada
            rs = pstmt.getGeneratedKeys();
            int idTransaccion = -1;
            if (rs.next()) {
                idTransaccion = rs.getInt(1);
            }

            // Actualizar el estado del pedido a "pagado"
            String sqlActualizarEstadoPedido = "UPDATE pedido SET Estado_Pedido = 'pagado' WHERE ID_Pedido = ?";
            pstmt = conn.prepareStatement(sqlActualizarEstadoPedido);
            pstmt.setInt(1, idPedido);
            pstmt.executeUpdate();

            conn.commit(); // Confirmar transacción

            return "Pago realizado exitosamente. ID de la transacción: " + idTransaccion + ", Cantidad pagada: " + cantidadAPagar + ", Fecha y hora: " + LocalDateTime.now();
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

    public List<Transaccion> verHistorialTransacciones(int idUsuario) throws SQLException {
        java.sql.Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Transaccion> transacciones = new ArrayList<>();

        try {
            conn = Connection.connection;

            // Consultar las transacciones del usuario autenticado
            String sqlTransacciones = "SELECT t.ID_Transaccion, t.ID_Pedido, t.Cantidad, t.Fecha_Hora, t.Estado_Transaccion, p.Tipo_MetodoPago " +
                    "FROM transaccion t " +
                    "JOIN pago p ON t.ID_metodoPago = p.ID_metodoPago " +
                    "WHERE t.ID_Usuario = ?";
            pstmt = conn.prepareStatement(sqlTransacciones);
            pstmt.setInt(1, idUsuario);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int idTransaccion = rs.getInt("ID_Transaccion");
                int idPedido = rs.getInt("ID_Pedido");
                double cantidadPagada = rs.getDouble("Cantidad");
                Timestamp fechaHora = rs.getTimestamp("Fecha_Hora");
                String estadoTransaccion = rs.getString("Estado_Transaccion");
                String metodoPagoUtilizado = rs.getString("Tipo_MetodoPago");

                Transaccion transaccion = new Transaccion(idTransaccion, idPedido, cantidadPagada, fechaHora, estadoTransaccion, metodoPagoUtilizado);
                transacciones.add(transaccion);
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }

        return transacciones;
    }

}
