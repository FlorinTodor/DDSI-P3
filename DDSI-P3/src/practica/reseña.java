package practica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class reseña {

    // Este método es un ejemplo de cómo obtener la conexión.
    // Deberás implementar la lógica de conexión según tu entorno:
    // (DriverManager.getConnection(...), DataSource, etc.)
    private Connection getConnection() throws SQLException {
        // Implementar la obtención de la conexión
        return null;
    }

    /**
     * RF5.1: Añadir reseña sobre un pedido
     * @param idReseña ID de la reseña a insertar
     * @param idPedido ID del pedido asociado
     * @param idUsuario ID del usuario (dueño del pedido)
     * @param valoracion Valoración (1-5)
     * @param comentario Comentario hasta 500 chars
     * @throws Exception si las condiciones no se cumplen (usuario no existe, pedido no existe, etc.)
     */
    public void addReview(int idReseña, int idPedido, int idUsuario, int valoracion, String comentario) throws Exception {
        try (Connection conn = getConnection()) {

            // 1. Comprobar que exista el usuario
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Usuario WHERE ID_Usuario = ?")) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt(1) == 0) {
                            throw new Exception("El usuario no existe");
                        }
                    }
                }
            }

            // 2. Comprobar que exista el pedido, que el pedido pertenece al usuario y el estado del pedido (recepcion del pedido)
            try (PreparedStatement ps = conn.prepareStatement("SELECT ID_Usuario, Estado_Pedido FROM Pedido WHERE ID_Pedido = ?")) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception("El pedido no existe");
                    }
                    int userIdFromPedido = rs.getInt("ID_Usuario");
                    String estadoPedido = rs.getString("Estado_Pedido");
                    // Comprobar que el pedido pertenece al usuario
                    if (userIdFromPedido != idUsuario) {
                        throw new Exception("El pedido no pertenece al usuario dado");
                    }
                    // Comprobar el estado (ejemplo: debe ser "Entregado")
                    if (!"Entregado".equalsIgnoreCase(estadoPedido)) {
                        throw new Exception("El pedido no está en estado para reseñar (debe estar entregado)");
                    }
                }
            }

            // Insertar la reseña
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO Reseña (ID_Reseña, Comentario, Valoracion) VALUES (?, ?, ?)")) {
                ps.setInt(1, idReseña);
                ps.setString(2, comentario);
                ps.setInt(3, valoracion);
                ps.executeUpdate();
            }

            // Asociar la reseña con el pedido
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO Gestion_Reseña (ID_Reseña, ID_Pedido) VALUES (?, ?)")) {
                ps.setInt(1, idReseña);
                ps.setInt(2, idPedido);
                ps.executeUpdate();
            }

            // Confirmar inserción (si llega aquí es correcto)
        }
    }


    /**
     * RF5.2: Editar reseña
     * @param idReseña ID de la reseña a editar
     * @param idUsuario ID del usuario que solicita la edición
     * @param nuevaValoracion Nueva valoración
     * @param nuevoComentario Nuevo comentario
     * @throws Exception si no se cumple alguna condición
     */
    public void editReview(int idReseña, int idUsuario, int nuevaValoracion, String nuevoComentario) throws Exception {
        try (Connection conn = getConnection()) {

            // Comprobar existencia de la reseña, pertenencia y que valoracion no sea null
            int userFromPedido = -1;
            Integer valoracionActual = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT r.Valoracion, p.ID_Usuario " +
                            "FROM Reseña r " +
                            "JOIN Gestion_Reseña gr ON r.ID_Reseña = gr.ID_Reseña " +
                            "JOIN Pedido p ON gr.ID_Pedido = p.ID_Pedido " +
                            "WHERE r.ID_Reseña = ?")) {
                ps.setInt(1, idReseña);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception("La reseña no existe");
                    }
                    valoracionActual = rs.getInt("Valoracion");
                    userFromPedido = rs.getInt("ID_Usuario");
                }
            }

            if (userFromPedido != idUsuario) {
                throw new Exception("La reseña no pertenece al usuario");
            }

            if (valoracionActual == null) {
                throw new Exception("No se puede editar una reseña que ya está eliminada (valoración null)");
            }

            // Actualizar la reseña
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Reseña SET Valoracion = ?, Comentario = ? WHERE ID_Reseña = ?")) {
                ps.setInt(1, nuevaValoracion);
                ps.setString(2, nuevoComentario);
                ps.setInt(3, idReseña);
                ps.executeUpdate();
            }
        }
    }


    /**
     * RF5.3: Eliminar reseña (colocar valoracion y comentario a null)
     * @param idReseña ID de la reseña a eliminar
     * @param idUsuario ID del usuario que solicita la eliminación
     * @throws Exception si no se cumple alguna condición
     */
    public void deleteReview(int idReseña, int idUsuario) throws Exception {
        try (Connection conn = getConnection()) {

            // Comprobar existencia y pertenencia
            int userFromPedido = -1;
            Integer valoracionActual = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT r.Valoracion, p.ID_Usuario " +
                            "FROM Reseña r " +
                            "JOIN Gestion_Reseña gr ON r.ID_Reseña = gr.ID_Reseña " +
                            "JOIN Pedido p ON gr.ID_Pedido = p.ID_Pedido " +
                            "WHERE r.ID_Reseña = ?")) {
                ps.setInt(1, idReseña);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception("La reseña no existe");
                    }
                    valoracionActual = rs.getInt("Valoracion");
                    userFromPedido = rs.getInt("ID_Usuario");
                }
            }

            if (userFromPedido != idUsuario) {
                throw new Exception("La reseña no pertenece al usuario");
            }

            if (valoracionActual == null) {
                throw new Exception("La reseña ya está eliminada o no se puede eliminar");
            }

            // Eliminar (poner valoracion y comentario a null)
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Reseña SET Valoracion = NULL, Comentario = NULL WHERE ID_Reseña = ?")) {
                ps.setInt(1, idReseña);
                ps.executeUpdate();
            }
        }
    }


    /**
     * RF5.4: Ver reseñas de un pedido
     * @param idPedido ID del pedido
     * @return Lista de reseñas (ID_Reseña, Valoracion, Comentario)
     * @throws Exception si el pedido no existe
     */
    public ArrayList<String> getReviewsByOrder(int idPedido) throws Exception {
        ArrayList<String> reviews = new ArrayList<>();
        try (Connection conn = getConnection()) {

            // Comprobar que el pedido existe
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Pedido WHERE ID_Pedido = ?")) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt(1) == 0) {
                            throw new Exception("El pedido no existe");
                        }
                    }
                }
            }

            // Obtener reseñas
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT r.ID_Reseña, r.Valoracion, r.Comentario " +
                            "FROM Reseña r " +
                            "JOIN Gestion_Reseña gr ON r.ID_Reseña = gr.ID_Reseña " +
                            "WHERE gr.ID_Pedido = ?")) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idRese = rs.getInt("ID_Reseña");
                        Integer val = rs.getObject("Valoracion") != null ? rs.getInt("Valoracion") : null;
                        String com = rs.getString("Comentario");
                        reviews.add("ID_Reseña: " + idRese + ", Valoración: " + val + ", Comentario: " + com);
                    }
                }
            }

        }
        return reviews;
    }

    /**
     * RF5.5: Ver reseñas de un usuario
     * @param idUsuario ID del usuario
     * @return Lista de reseñas del usuario
     * @throws Exception si no existe el usuario
     */
    public ArrayList<String> getReviewsByUser(int idUsuario) throws Exception {
        ArrayList<String> reviews = new ArrayList<>();
        try (Connection conn = getConnection()) {

            // Comprobar que exista el usuario
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Usuario WHERE ID_Usuario = ?")) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt(1) == 0) {
                            throw new Exception("El usuario no existe");
                        }
                    }
                }
            }

            // Obtener reseñas del usuario
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT r.ID_Reseña, r.Valoracion, r.Comentario " +
                            "FROM Reseña r " +
                            "JOIN Gestion_Reseña gr ON r.ID_Reseña = gr.ID_Reseña " +
                            "JOIN Pedido p ON gr.ID_Pedido = p.ID_Pedido " +
                            "WHERE p.ID_Usuario = ?")) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idRese = rs.getInt("ID_Reseña");
                        Integer val = rs.getObject("Valoracion") != null ? rs.getInt("Valoracion") : null;
                        String com = rs.getString("Comentario");
                        reviews.add("ID_Reseña: " + idRese + ", Valoración: " + val + ", Comentario: " + com);
                    }
                }
            }

        }
        return reviews;
    }
}
