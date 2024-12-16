package practica;

import java.sql.*;
import java.util.ArrayList;

public class Usuario {

    /**
     * RF2.1: Registrar Usuario
     */
    public int registerUser(String correo, String nombre, String telefono, String estado, String direccion, String contraseña) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;
        int generatedId = -1;

        // Validar que el correo no esté registrado
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE CORREO = ?")) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new Exception("El correo ya está registrado.");
                }
            }
        }

        // Insertar nuevo usuario sin ID (autogenerado)
        String sql = "INSERT INTO USUARIO (CORREO, NOMBRE, TELEFONO, ESTADO, DIRECCION, CONTRASEÑA) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, correo);
            ps.setString(2, nombre);
            ps.setString(3, telefono);
            ps.setString(4, estado);
            ps.setString(5, direccion);
            ps.setString(6, contraseña);
            ps.executeUpdate();

            // Recuperar el ID generado
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                }
            }
        }

        conn.commit(); // Confirmar cambios
        return generatedId; // Devolver el ID generado
    }


    /**
     * RF2.2: Dar de Baja Usuario
     */
    public void deleteUser(int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;

        // Verificar si el usuario existe
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE ID_USUARIO = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new Exception("El usuario no existe.");
                }
            }
        }

        // Cambiar estado a 'Eliminado'
        try (PreparedStatement ps = conn.prepareStatement("UPDATE USUARIO SET ESTADO = 'Eliminado' WHERE ID_USUARIO = ?")) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }

        conn.commit(); // Confirmar cambios
    }

    /**
     * RF2.3: Modificar Datos de Usuario
     */
    public void updateUser(int idUsuario, String nuevoCorreo, String nuevoNombre, String nuevoTelefono, String nuevaDireccion) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;

        // Comprobar si el usuario existe
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE ID_USUARIO = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new Exception("El usuario no existe.");
                }
            }
        }

        // Actualizar datos del usuario
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE USUARIO SET CORREO = ?, NOMBRE = ?, TELEFONO = ?, DIRECCION = ? WHERE ID_USUARIO = ?")) {
            ps.setString(1, nuevoCorreo);
            ps.setString(2, nuevoNombre);
            ps.setString(3, nuevoTelefono);
            ps.setString(4, nuevaDireccion);
            ps.setInt(5, idUsuario);
            ps.executeUpdate();
        }

        conn.commit(); // Confirmar cambios
    }

    /**
     * RF2.4: Recuperar Contraseña
     */
    public String recoverPassword(String correo) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;
        String contraseña = null;

        // Recuperar contraseña
        try (PreparedStatement ps = conn.prepareStatement("SELECT CONTRASEÑA FROM USUARIO WHERE CORREO = ?")) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    contraseña = rs.getString("CONTRASEÑA");
                } else {
                    throw new Exception("El correo no está registrado.");
                }
            }
        }

        return contraseña;
    }

    /**
     * RF2.5: Iniciar Sesión
     */
    public boolean loginUser(String correo, String contraseña) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;
        boolean isAuthenticated = false;

        // Verificar credenciales
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE CORREO = ? AND CONTRASEÑA = ?")) {
            ps.setString(1, correo);
            ps.setString(2, contraseña);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    isAuthenticated = true;
                }
            }
        }

        return isAuthenticated;
    }
}
