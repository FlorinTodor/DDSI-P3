package practica;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Connection {
    /* ATRIBUTOS */
    private static final String URL = "jdbc:oracle:thin:@oracle0.ugr.es:1521/practbd";
    private static final String USER = "x4049463";
    private static final String PASSWORD = "x4049463";
    public static java.sql.Connection connection;
    public static JFrame frame;

    /* MÉTODOS */

    // Inicializador de la interfaz gráfica y configuración de cierre
    public static JFrame inicializarFrame() {
        frame = new JFrame("Gestión de Base de Datos Oracle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
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








}
