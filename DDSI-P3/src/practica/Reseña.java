package practica;

import java.sql.*;
import java.util.ArrayList;

public class Reseña {

    /**
     * RF5.1: Añadir reseña sobre un pedido
     */
    public void addReview(int idReseña, int idPedido, int idUsuario, int valoracion, String comentario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn =  Connection.connection;

        // Verificar si el usuario existe
        if (!Connection.doesUserExist(idUsuario)) {
            throw new Exception("El usuario no existe.");
        }


        // Comprobar que exista el pedido, que pertenece al usuario y está en estado 'Entregado'
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT ID_USUARIO, ESTADO_PEDIDO FROM PEDIDO WHERE ID_PEDIDO = ?")) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("El pedido no existe.");
                }
                int userIdFromPedido = rs.getInt("ID_USUARIO");
                String estadoPedido = rs.getString("ESTADO_PEDIDO");

                if (userIdFromPedido != idUsuario) {
                    throw new Exception("El pedido no pertenece al usuario dado.");
                }

                if (!"Entregado".equalsIgnoreCase(estadoPedido)) {
                    throw new Exception("El pedido no está en estado 'Entregado'.");
                }
            }
        }

        // Insertar la reseña
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO RESEÑA (ID_RESENA, COMENTARIO, VALORACION) VALUES (?, ?, ?)")) {
            ps.setInt(1, idReseña);
            ps.setString(2, comentario);
            ps.setInt(3, valoracion);
            ps.executeUpdate();
        }

        // Asociar la reseña con el pedido
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO GESTION_RESEÑA (ID_RESENA, ID_PEDIDO) VALUES (?, ?)")) {
            ps.setInt(1, idReseña);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        }

        conn.commit(); // Confirmar cambios
    }

    /**
     * RF5.2: Editar reseña
     */
    public void editReview(int idReseña, int idUsuario, int nuevaValoracion, String nuevoComentario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        // Verificar si el usuario existe
        if (!Connection.doesUserExist(idUsuario)) {
            throw new Exception("El usuario no existe.");
        }

        java.sql.Connection conn = Connection.connection;

        // Comprobar existencia de la reseña y detalles
        int userFromPedido = -1;
        Integer valoracionActual = null;

        String verificarReseñaQuery =
                "SELECT r.VALORACION, p.ID_USUARIO " +
                        "FROM RESEÑA r " +
                        "JOIN GESTION_RESEÑA gr ON r.ID_RESENA = gr.ID_RESENA " +
                        "JOIN PEDIDO p ON gr.ID_PEDIDO = p.ID_PEDIDO " +
                        "WHERE r.ID_RESENA = ?";
        try (PreparedStatement ps = conn.prepareStatement(verificarReseñaQuery)) {
            ps.setInt(1, idReseña);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("La reseña no existe.");
                }
                valoracionActual = rs.getInt("VALORACION");
                userFromPedido = rs.getInt("ID_USUARIO");
            }
        }

        if (userFromPedido != idUsuario) {
            throw new Exception("La reseña no pertenece al usuario.");
        }

        if (valoracionActual == null) {
            throw new Exception("No se puede editar una reseña que ya está eliminada (valoración null).");
        }

        // Actualizar la reseña
        String actualizarReseñaQuery = "UPDATE RESEÑA SET VALORACION = ?, COMENTARIO = ? WHERE ID_RESENA = ?";
        try (PreparedStatement ps = conn.prepareStatement(actualizarReseñaQuery)) {
            ps.setInt(1, nuevaValoracion);
            ps.setString(2, nuevoComentario);
            ps.setInt(3, idReseña);
            ps.executeUpdate();
        }

        conn.commit(); // Confirmar cambios
    }

    /**
     * RF5.3: Eliminar reseña
     */
    public void deleteReview(int idReseña, int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;

        // Verificar si el usuario existe
        if (!Connection.doesUserExist(idUsuario)) {
            throw new Exception("El usuario no existe.");
        }

        // Comprobar existencia y detalles
        int userFromPedido = -1;
        Integer valoracionActual = null;

        String verificarReseñaQuery =
                "SELECT r.VALORACION, p.ID_USUARIO " +
                        "FROM RESEÑA r " +
                        "JOIN GESTION_RESEÑA gr ON r.ID_RESENA = gr.ID_RESENA " +
                        "JOIN PEDIDO p ON gr.ID_PEDIDO = p.ID_PEDIDO " +
                        "WHERE r.ID_RESENA = ?";
        try (PreparedStatement ps = conn.prepareStatement(verificarReseñaQuery)) {
            ps.setInt(1, idReseña);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("La reseña no existe.");
                }
                valoracionActual = rs.getInt("VALORACION");
                userFromPedido = rs.getInt("ID_USUARIO");
            }
        }

        if (userFromPedido != idUsuario) {
            throw new Exception("La reseña no pertenece al usuario.");
        }

        if (valoracionActual == null) {
            throw new Exception("La reseña ya está eliminada o no se puede eliminar.");
        }

        // Eliminar reseña (poner valores en null)
        String eliminarReseñaQuery = "UPDATE RESEÑA SET VALORACION = NULL, COMENTARIO = NULL WHERE ID_RESENA = ?";
        try (PreparedStatement ps = conn.prepareStatement(eliminarReseñaQuery)) {
            ps.setInt(1, idReseña);
            ps.executeUpdate();
        }

        conn.commit(); // Confirmar cambios
    }

    /**
     * RF5.4: Obtener reseñas de un pedido
     * @param idPedido ID del pedido
     * @return Lista de reseñas (formato "ID_Reseña, Valoración, Comentario")
     * @throws Exception si el pedido no existe
     */
    public ArrayList<String> getReviewsByOrder(int idPedido) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        ArrayList<String> reviews = new ArrayList<>();
        java.sql.Connection conn = Connection.connection;

        // Comprobar que el pedido existe
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM PEDIDO WHERE ID_PEDIDO = ?")) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new Exception("El pedido no existe.");
                }
            }
        }

        // Obtener las reseñas asociadas al pedido
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT r.ID_RESENA, r.VALORACION, r.COMENTARIO " +
                        "FROM RESEÑA r " +
                        "JOIN GESTION_RESEÑA gr ON r.ID_RESENA = gr.ID_RESENA " +
                        "WHERE gr.ID_PEDIDO = ?")) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idReseña = rs.getInt("ID_RESENA");
                    Integer valoracion = rs.getObject("VALORACION") != null ? rs.getInt("VALORACION") : null;
                    String comentario = rs.getString("COMENTARIO");
                    reviews.add("ID_Reseña: " + idReseña + ", Valoración: " + valoracion + ", Comentario: " + comentario);
                }
            }
        }

        return reviews;
    }

    /**
     * RF5.5: Obtener reseñas de un usuario
     * @param idUsuario ID del usuario
     * @return Lista de reseñas del usuario (formato "ID_Reseña, Valoración, Comentario")
     * @throws Exception si el usuario no existe
     */
    public ArrayList<String> getReviewsByUser(int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        ArrayList<String> reviews = new ArrayList<>();
        java.sql.Connection conn = Connection.connection;

        // Verificar si el usuario existe
        if (!Connection.doesUserExist(idUsuario)) {
            throw new Exception("El usuario no existe.");
        }


        // Obtener las reseñas asociadas al usuario
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT r.ID_RESENA, r.VALORACION, r.COMENTARIO " +
                        "FROM RESEÑA r " +
                        "JOIN GESTION_RESEÑA gr ON r.ID_RESENA = gr.ID_RESENA " +
                        "JOIN PEDIDO p ON gr.ID_PEDIDO = p.ID_PEDIDO " +
                        "WHERE p.ID_USUARIO = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idReseña = rs.getInt("ID_RESENA");
                    Integer valoracion = rs.getObject("VALORACION") != null ? rs.getInt("VALORACION") : null;
                    String comentario = rs.getString("COMENTARIO");
                    reviews.add("ID_Reseña: " + idReseña + ", Valoración: " + valoracion + ", Comentario: " + comentario);
                }
            }
        }

        return reviews;
    }






}