package practica;

import java.sql.*;
import java.util.UUID;

public class Usuario {

    /**
     * RF1.1: Registro de Usuario
     * Devuelve el ID del usuario registrado.
     */
    public int registerUser(String correo, String nombre, String direccion, String contraseña) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;
        int nextId = -1;

        try {
            // 1. Obtener el valor máximo actual de ID_USUARIO
            String maxIdQuery = "SELECT NVL(MAX(ID_USUARIO), 0) + 1 FROM USUARIO";
            try (PreparedStatement ps = conn.prepareStatement(maxIdQuery);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nextId = rs.getInt(1);
                }
            }

            // 2. Validar que el correo no esté registrado
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE CORREO = ?")) {
                ps.setString(1, correo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new Exception("El correo ya está registrado.");
                    }
                }
            }

            // 3. Insertar el nuevo usuario y establecer FECHA_ACTIVACION como fecha actual
            String insertSQL = "INSERT INTO USUARIO (ID_USUARIO, CORREO, NOMBRE, DIRECCION, CONTRASEÑA, ESTADO, FECHA_REGISTRO) " +
                    "VALUES (?, ?, ?, ?, ?, 'A', SYSTIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setInt(1, nextId);
                ps.setString(2, correo);
                ps.setString(3, nombre);
                ps.setString(4, direccion);
                ps.setString(5, contraseña);

                ps.executeUpdate();
                conn.commit();
            }

            return nextId;

        } catch (SQLException ex) {
            conn.rollback();
            throw new Exception("Error al registrar usuario: " + ex.getMessage(), ex);
        }
    }




    /**
     * RF1.2: Dar de Baja Usuario
     * Deshabilita la cuenta si la contraseña es correcta.
     */
    public void deleteUser(int idUsuario, String contraseña) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        // Verificar si la cuenta existe y está activa
        try (PreparedStatement ps = Connection.connection.prepareStatement(
                "SELECT ESTADO, CONTRASEÑA FROM USUARIO WHERE ID_USUARIO = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String estadoCuenta = rs.getString("ESTADO");
                    String storedPassword = rs.getString("CONTRASEÑA");
                    if (!storedPassword.equals(contraseña)) {
                        throw new Exception("La contraseña proporcionada es incorrecta.");
                    }
                    if (estadoCuenta.equalsIgnoreCase("I")) {
                        throw new Exception("La cuenta ya está deshabilitada.");
                    }
                } else {
                    throw new Exception("El usuario no existe.");
                }
            }
        }

        // Deshabilitar la cuenta y actualizar FECHA_ACTIVACION a NULL
        try (PreparedStatement ps = Connection.connection.prepareStatement(
                "UPDATE USUARIO SET ESTADO = 'I', FECHA_DESACTIVACION = SYSTIMESTAMP WHERE ID_USUARIO = ?")) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
            Connection.connection.commit();
        }
    }



    /**
     * RF1.3: Modificar Datos del Usuario
     */
    public void updateUser(int idUsuario, String nuevoCorreo, String nuevoNombre, String nuevaDireccion, String nuevaContraseña) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        // Validar que la cuenta esté activa
        try (PreparedStatement ps = Connection.connection.prepareStatement(
                "SELECT ESTADO FROM USUARIO WHERE ID_USUARIO = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("ESTADO").equalsIgnoreCase("Inactivo")) {
                    throw new Exception("No se pueden modificar los datos de una cuenta deshabilitada.");
                }
            }
        }

        // Actualizar datos
        try (PreparedStatement ps = Connection.connection.prepareStatement(
                "UPDATE USUARIO SET CORREO = ?, NOMBRE = ?, DIRECCION = ?, CONTRASEÑA = ? WHERE ID_USUARIO = ?")) {
            ps.setString(1, nuevoCorreo);
            ps.setString(2, nuevoNombre);
            ps.setString(3, nuevaDireccion);
            ps.setString(4, nuevaContraseña);
            ps.setInt(5, idUsuario);
            ps.executeUpdate();
        }
    }

    /**
     * RF1.4: Recuperar Contraseña
     * Genera y devuelve un token de recuperación.
     */
    public String recoverPassword(String correo) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        String token = UUID.randomUUID().toString().substring(0, 8);
        String estadoCuenta = null;

        // Verificar si el correo está registrado y activo
        try (PreparedStatement ps = Connection.connection.prepareStatement(
                "SELECT ESTADO FROM USUARIO WHERE CORREO = ?")) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    estadoCuenta = rs.getString("ESTADO");
                    if (estadoCuenta.equalsIgnoreCase("Inactivo")) {
                        throw new Exception("No se puede recuperar contraseña para cuentas deshabilitadas.");
                    }
                } else {
                    throw new Exception("El correo no está registrado.");
                }
            }
        }

        // Guardar el token en la base de datos
        try (PreparedStatement ps = Connection.connection.prepareStatement(
                "UPDATE USUARIO SET ID_TOKEN = ? WHERE CORREO = ?")) {
            ps.setString(1, token);
            ps.setString(2, correo);
            ps.executeUpdate();
        }
        return token; // Devuelve el token generado
    }

    /**
     * RF1.5: Iniciar Sesión
     */
    public int loginUser(String correo, String contraseña) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        int idUsuario = -1;

        try (PreparedStatement ps = Connection.connection.prepareStatement(
                "SELECT ESTADO, FECHA_DESACTIVACION, ID_USUARIO FROM USUARIO WHERE CORREO = ? AND CONTRASEÑA = ?")) {
            ps.setString(1, correo);
            ps.setString(2, contraseña);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String estado = rs.getString("ESTADO");
                    Timestamp fechaDesactivacion = rs.getTimestamp("FECHA_DESACTIVACION");

                    if (!"A".equalsIgnoreCase(estado) || fechaDesactivacion != null) {
                        throw new Exception("La cuenta está deshabilitada o no está activada.");
                    }
                    idUsuario = rs.getInt("ID_USUARIO");
                } else {
                    throw new Exception("Credenciales incorrectas.");
                }
            }
        }

        return idUsuario;
    }


}

