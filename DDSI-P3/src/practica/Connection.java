package practica;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Connection {
    /* ATRIBUTOS */
    private static final String URL = "jdbc:oracle:thin:@oracle0.ugr.es:1521/practbd";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    public static java.sql.Connection connection;
    public static JFrame frame;
    public static JFrame frame_registro;
    public static int id_user = -1;

    /* MÉTODOS */

    // Inicializador del frame principal
    public static JFrame inicializarFramePrincipal() {
        frame = new JFrame("Gestión de Base de Datos Oracle - Principal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        // Asegurar cierre de conexión al cerrar la aplicación
        Runtime.getRuntime().addShutdownHook(new Thread(() -> cerrarConexion(false)));

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarConexion(true);
                System.exit(0);
            }
        });
        return frame;
    }

    // Inicializador del frame de registro
    public static JFrame inicializarFrameRegistro() {
        frame_registro = new JFrame("Registro / Inicio de Sesión");
        frame_registro.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo este frame
        frame_registro.setSize(400, 300);
        frame_registro.setLayout(new BorderLayout());
        // Asegurar cierre de conexión al cerrar la aplicación
        Runtime.getRuntime().addShutdownHook(new Thread(() -> cerrarConexion(false)));

        frame_registro.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarConexion(true);
                System.exit(0);
            }
        });
        return frame_registro;
    }

    // Método para conectar a la base de datos
    public static void conectarBD() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(false); // Desactivamos los commit automáticos
            JOptionPane.showMessageDialog(frame, "Conexión establecida con éxito.");
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error al conectar a la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para cerrar la conexión de manera segura
    public static void cerrarConexion(boolean mostrarMensaje) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                if (mostrarMensaje) {
                    JOptionPane.showMessageDialog(frame, "Conexión cerrada correctamente.");
                }
            }
        } catch (SQLException e) {
            if (mostrarMensaje) {
                JOptionPane.showMessageDialog(frame, "Error al cerrar la conexión: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    /**
     * Método auxiliar para verificar si un usuario existe en la base de datos.
     */
    public static boolean doesUserExist(int idUsuario) throws Exception {
        if (Connection.connection == null) {
            throw new Exception("No hay conexión a la base de datos.");
        }

        java.sql.Connection conn = Connection.connection;

        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE ID_Usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
