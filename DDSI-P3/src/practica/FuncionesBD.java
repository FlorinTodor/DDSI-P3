package practica;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class FuncionesBD {

    public static void mostrarTablas() {
        if (Connection.connection == null) {
            JOptionPane.showMessageDialog(Connection.frame, "No hay conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (PreparedStatement ps = Connection.connection.prepareStatement("SELECT table_name FROM user_tables");
             ResultSet rs = ps.executeQuery()) {

            JDialog dialog = new JDialog(Connection.frame, "Tablas en la BD", true);
            dialog.setLayout(new BorderLayout());

            JTabbedPane tabbedPane = new JTabbedPane();

            boolean hayTablas = false;
            while (rs.next()) {
                hayTablas = true;
                String tableName = rs.getString("table_name");

                // Consulta para obtener datos de la tabla ordenados de forma ascendente por la primera columna
                String sql = "SELECT * FROM \"" + tableName + "\" ORDER BY 1 ASC";
                try (Statement st = Connection.connection.createStatement();
                     ResultSet rsTable = st.executeQuery(sql)) {

                    ResultSetMetaData metaData = rsTable.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    // Columnas
                    String[] columnNames = new String[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        columnNames[i - 1] = metaData.getColumnName(i);
                    }

                    // Filas
                    ArrayList<Object[]> rows = new ArrayList<>();
                    while (rsTable.next()) {
                        Object[] rowData = new Object[columnCount];
                        for (int i = 1; i <= columnCount; i++) {
                            rowData[i - 1] = rsTable.getObject(i);
                        }
                        rows.add(rowData);
                    }

                    Object[][] data = rows.toArray(new Object[0][]);

                    JTable table = new JTable(data, columnNames);
                    JScrollPane scrollPane = new JScrollPane(table);

                    JPanel panelTabla = new JPanel(new BorderLayout());
                    panelTabla.add(scrollPane, BorderLayout.CENTER);

                    tabbedPane.addTab(tableName, panelTabla);
                } catch (SQLException ex) {
                    JPanel panelError = new JPanel(new BorderLayout());
                    panelError.add(new JLabel("Error cargando datos de la tabla " + tableName + ": " + ex.getMessage(), JLabel.CENTER), BorderLayout.CENTER);
                    tabbedPane.addTab(tableName, panelError);
                }
            }

            if (!hayTablas) {
                JPanel panelSinTablas = new JPanel(new BorderLayout());
                panelSinTablas.add(new JLabel("No se encontraron tablas.", JLabel.CENTER), BorderLayout.CENTER);
                tabbedPane.addTab("Sin tablas", panelSinTablas);
            }

            dialog.add(tabbedPane, BorderLayout.CENTER);

            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(Connection.frame);
            dialog.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(Connection.frame, "Error al obtener las tablas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void eliminarDatos_tabla() {
        try (Statement stmt = Connection.connection.createStatement()) {
            // Eliminar datos en el orden correcto respetando las dependencias
            stmt.executeUpdate("DELETE FROM Gestion_Reseña");
            stmt.executeUpdate("DELETE FROM Realiza");
            stmt.executeUpdate("DELETE FROM GestionPago");
            stmt.executeUpdate("DELETE FROM GestionCarrito");
            stmt.executeUpdate("DELETE FROM tiene");
            stmt.executeUpdate("DELETE FROM GestionPedido");
            stmt.executeUpdate("DELETE FROM modificaProducto");

            // Eliminar datos de las tablas principales
            stmt.executeUpdate("DELETE FROM reseña");
            stmt.executeUpdate("DELETE FROM pedido");
            stmt.executeUpdate("DELETE FROM producto");
            stmt.executeUpdate("DELETE FROM pago");
            stmt.executeUpdate("DELETE FROM carrito");
            stmt.executeUpdate("DELETE FROM usuario");

            Connection.connection.commit();
            JOptionPane.showMessageDialog(Connection.frame, "Datos eliminados correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(Connection.frame, "Error al eliminar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void crearSecuenciaPedido() {
        String borrarSecuenciaSQL = "BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_id_pedido'; EXCEPTION WHEN OTHERS THEN NULL; END;";
        String crearSecuenciaSQL = "CREATE SEQUENCE seq_id_pedido START WITH 1 INCREMENT BY 1";
        try (Statement stmt = Connection.connection.createStatement()) {
            // Borrar la secuencia si existe
            stmt.executeUpdate(borrarSecuenciaSQL);
            // Crear la nueva secuencia
            stmt.executeUpdate(crearSecuenciaSQL);
            Connection.connection.commit();
            JOptionPane.showMessageDialog(Connection.frame, "Secuencia creada correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(Connection.frame, "Error al crear la secuencia: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void crearSecuenciaCarrito() {
        String borrarSecuenciaSQL = "BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_id_carrito'; EXCEPTION WHEN OTHERS THEN NULL; END;";

        String crearSecuenciaSQL = "CREATE SEQUENCE seq_id_carrito START WITH 1 INCREMENT BY 1";
        try (Statement stmt = Connection.connection.createStatement()) {
            // Borrar la secuencia si existe
            stmt.executeUpdate(borrarSecuenciaSQL);
            // Crear la nueva secuencia
            stmt.executeUpdate(crearSecuenciaSQL);
            Connection.connection.commit();
            JOptionPane.showMessageDialog(Connection.frame, "Secuencia creada correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(Connection.frame, "Error al crear la secuencia: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void insertarDatosPrueba_tabla() {
        try (Statement stmt = Connection.connection.createStatement()) {
            // Insertar Usuarios
            stmt.executeUpdate("INSERT INTO usuario (ID_Usuario, Correo, Nombre, Estado, Contraseña, Direccion) " +
                    "VALUES (2, 'user1@example.com', 'Juan', 'A', 'contraseña', 'Calle Falsa 123')");
            stmt.executeUpdate("INSERT INTO usuario (ID_Usuario, Correo, Nombre, Estado, Contraseña, Direccion) " +
                    "VALUES (3, 'user2@example.com', 'Maria', 'A', 'contraseña', 'Avenida Principal 456')");

            // Insertar Productos
            stmt.executeUpdate("INSERT INTO producto (ID_Producto, NombreProducto, Cantidad, Precio) " +
                    "VALUES (10, 'Camisa', 50, 19.99)");
            stmt.executeUpdate("INSERT INTO producto (ID_Producto, NombreProducto, Cantidad, Precio) " +
                    "VALUES (11, 'Pantalón', 20, 29.99)");
            stmt.executeUpdate("INSERT INTO modificaProducto (ID_Usuario, ID_Producto) " +
                    "VALUES (1, 11)");
            stmt.executeUpdate("INSERT INTO modificaProducto (ID_Usuario, ID_Producto) " +
                    "VALUES (1, 10)");

            // Insertar Pagos
            stmt.executeUpdate("INSERT INTO pago (ID_metodoPago, ID_Usuario, Tipo_MetodoPago, Numero_Tarjeta, Fecha_Expiracion, Codigo_CVV, Nombre_Titular, Correo_Paypal, Fecha) VALUES (1,2,'tarjeta de crédito','2145658732146589', '10/26', 'Juan', 'user1@paypal.com', SYSDATE)");
            stmt.executeUpdate("INSERT INTO pago (ID_metodoPago, ID_Usuario, Tipo_MetodoPago, Numero_Tarjeta, Fecha_Expiracion, Codigo_CVV, Nombre_Titular, Correo_Paypal, Fecha) VALUES (2,3,'tarjeta de crédito','2145658732146588', '11/26', 'María', 'user2@paypal.com', SYSDATE)");

            // Insertar Pedidos
            stmt.executeUpdate("INSERT INTO pedido (ID_Pedido, Direccion, Estado_Pedido, Tipo_Pago, Metodo_Envio, ID_Usuario) " +
                    "VALUES (100, 'Calle Falsa 123', 'Entregado', 1, 'Correo', 1)");
            stmt.executeUpdate("INSERT INTO pedido (ID_Pedido, Direccion, Estado_Pedido, Tipo_Pago, Metodo_Envio, ID_Usuario) " +
                    "VALUES (101, 'Avenida Principal 456', 'Entregado', 2, 'Mensajeria', 2)");

            // Insertar Reseñas
            stmt.executeUpdate("INSERT INTO reseña (ID_Resena, Comentario, Valoracion) " +
                    "VALUES (1, 'Muy buen producto', 5)");
            stmt.executeUpdate("INSERT INTO reseña (ID_Resena, Comentario, Valoracion) " +
                    "VALUES (2, 'Rápida entrega, producto aceptable', 4)");

            // Asociar Reseñas con Pedidos
            stmt.executeUpdate("INSERT INTO Gestion_Reseña (ID_Resena, ID_Pedido) VALUES (1, 100)");
            stmt.executeUpdate("INSERT INTO Gestion_Reseña (ID_Resena, ID_Pedido) VALUES (2, 101)");

            // Asociar Pagos
            stmt.executeUpdate("INSERT INTO GestionPago (ID_Usuario, ID_metodoPago) VALUES (1, 1)");
            stmt.executeUpdate("INSERT INTO GestionPago (ID_Usuario, ID_metodoPago) VALUES (2, 2)");
            stmt.executeUpdate("INSERT INTO Realiza (ID_metodoPago, ID_Pedido, Metodo_Pago) VALUES (1, 100, 'Tarjeta')");
            stmt.executeUpdate("INSERT INTO Realiza (ID_metodoPago, ID_Pedido, Metodo_Pago) VALUES (2, 101, 'PayPal')");

            Connection.connection.commit();
            JOptionPane.showMessageDialog(Connection.frame, "Datos de prueba insertados correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(Connection.frame, "Error al insertar datos de prueba: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void eliminarSiExiste(String tipo, String nombre, Statement stmt) throws SQLException {
        try {
            String sql = "DROP " + tipo + " " + nombre + " PURGE";
            stmt.executeUpdate(sql);
            System.out.println(tipo + " " + nombre + " eliminado.");
        } catch (SQLException e) {
            if (!e.getMessage().contains("ORA-00942")) { // Ignorar si el objeto no existe
                throw e; // Re-lanzar si es otro tipo de error
            }
        }
    }

    private static void TruncarSiExiste(String nombre, Statement stmt) throws SQLException {
        try {
            String sql = "TRUNCATE TABLE " + nombre;
            stmt.executeUpdate(sql);
            System.out.println(nombre + " TRUNCATED.");
        } catch (SQLException e) {
            if (!e.getMessage().contains("ORA-00942")) { // Ignorar si el objeto no existe
                throw e; // Re-lanzar si es otro tipo de error
            }
        }
    }

    public static void borraryCrearTablas() {
        try (Statement stmt = Connection.connection.createStatement()) {
            // Truncar tablas dependientes primero
            TruncarSiExiste("Gestion_Reseña", stmt);
            TruncarSiExiste("Realiza", stmt);
            TruncarSiExiste("GestionPago", stmt);

            // Truncar tablas principales después
            TruncarSiExiste("reseña", stmt);
            TruncarSiExiste("pedido", stmt);
            TruncarSiExiste("producto", stmt);
            TruncarSiExiste("pago", stmt);
            TruncarSiExiste("carrito", stmt);


            // Eliminar tablas relacionadas primero
            eliminarSiExiste("TABLE", "Gestion_Reseña", stmt);
            eliminarSiExiste("TABLE", "Realiza", stmt);
            eliminarSiExiste("TABLE", "GestionPago", stmt);
            eliminarSiExiste("TABLE", "GestionCarrito", stmt);
            eliminarSiExiste("TABLE", "GestionPedido", stmt);
            eliminarSiExiste("TABLE", "modificaProducto", stmt);
            eliminarSiExiste("TABLE", "tiene", stmt);

            // Eliminar tablas base después
            eliminarSiExiste("TABLE", "reseña", stmt);
            eliminarSiExiste("TABLE", "pedido", stmt);
            eliminarSiExiste("TABLE", "producto", stmt);
            eliminarSiExiste("TABLE", "pago", stmt);
            eliminarSiExiste("TABLE", "carrito", stmt);
            eliminarSiExiste("TABLE", "usuario", stmt);

            // Eliminar secuencia pago_seq
            stmt.executeUpdate("DROP SEQUENCE PAGO_SEQ");
            System.out.println("SEQUENCE PAGO_SEQ eliminado.");


            // Crear tablas base
            crearTablas();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(Connection.frame, "Error al crear las tablas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Crear solamente tablas
    public static void crearTablas() throws SQLException {
        try (Statement stmt = Connection.connection.createStatement()) {
            // Crear tablas base
            stmt.executeUpdate("CREATE TABLE usuario (\n" +
                    "    ID_Usuario INTEGER NOT NULL,\n" +
                    "    Correo VARCHAR(30),\n" +
                    "    Nombre VARCHAR(30),\n" +
                    "    Estado CHAR(1) CHECK (Estado IN ('A', 'I')),\n" +
                    "    Direccion VARCHAR(50),\n" +
                    "    Contraseña VARCHAR(50),\n" +
                    "    Fecha_Registro TIMESTAMP DEFAULT SYSTIMESTAMP,\n" +
                    "    Fecha_Desactivacion TIMESTAMP DEFAULT NULL,\n" +
                    "    PRIMARY KEY(ID_Usuario),\n" +
                    "    UNIQUE(Correo)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE producto (\n" +
                    "    ID_Producto integer,\n" +
                    "    NombreProducto varchar(30),\n" +
                    "    Cantidad integer NOT NULL CHECK ( cantidad >= 0 ),\n" +
                    "    Precio float NOT NULL CHECK (precio >= 0),\n" +
                    "    PRIMARY KEY(ID_Producto)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE carrito (\n" +
                    "    ID_Carrito integer NOT NULL,\n" +
                    "    PRIMARY KEY(ID_Carrito)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE pedido (\n" +
                    "    ID_Pedido integer NOT NULL,\n" +
                    "    Direccion varchar(30),\n" +
                    "    Estado_Pedido varchar(10),\n" +
                    "    Tipo_Pago varchar(30),\n" +
                    "    Metodo_Envio varchar(10),\n" +
                    "    ID_Usuario integer REFERENCES usuario(ID_Usuario),\n" +
                    "    PRIMARY KEY(ID_Pedido)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE reseña (\n" +
                    "    ID_Resena INT PRIMARY KEY,\n" +
                    "    Comentario VARCHAR2(500),\n" +
                    "    Valoracion INT\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE pago (\n" +
                    "    ID_metodoPago INT PRIMARY KEY,\n" +
                    "    ID_Usuario INT,\n" +
                    "    Tipo_MetodoPago VARCHAR(50) NOT NULL,\n" +
                    "    Numero_Tarjeta VARCHAR(16),\n" +
                    "    Fecha_Expiracion VARCHAR(5),\n" +
                    "    Codigo_CVV VARCHAR(4),\n" +
                    "    Nombre_Titular VARCHAR(100),\n" +
                    "    Correo_PayPal VARCHAR(100),\n" +
                    "    Fecha DATE,\n" +
                    "    FOREIGN KEY(ID_Usuario) REFERENCES usuario(ID_Usuario)\n" +
                    ")");

// Crear secuencia para ID_metodoPago en la tabla pago
            stmt.executeUpdate("CREATE SEQUENCE pago_seq START WITH 1 INCREMENT BY 1 NOCACHE");
// Crear tablas relacionadas
            stmt.executeUpdate("CREATE TABLE GestionPago (\n" +
                    "    ID_Usuario INT,\n" +
                    "    ID_metodoPago INT,\n" +
                    "    PRIMARY KEY (ID_Usuario),\n" +
                    "    UNIQUE (ID_metodoPago),\n" +
                    "    FOREIGN KEY (ID_Usuario) REFERENCES usuario(ID_Usuario),\n" +
                    "    FOREIGN KEY (ID_metodoPago) REFERENCES pago(ID_metodoPago)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE Realiza (\n" +
                    "    ID_metodoPago INT,\n" +
                    "    ID_Pedido INT,\n" +
                    "    Metodo_Pago VARCHAR(30),\n" +
                    "    PRIMARY KEY (ID_Pedido),\n" +
                    "    UNIQUE (ID_metodoPago),\n" +
                    "    FOREIGN KEY (ID_Pedido) REFERENCES pedido(ID_Pedido),\n" +
                    "    FOREIGN KEY (ID_metodoPago) REFERENCES pago(ID_metodoPago)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE Gestion_Reseña (\n" +
                    "    ID_Resena INT,\n" +
                    "    ID_Pedido INT,\n" +
                    "    PRIMARY KEY (ID_Resena),\n" +
                    "    FOREIGN KEY (ID_Resena) REFERENCES reseña(ID_Resena),\n" +
                    "    FOREIGN KEY (ID_Pedido) REFERENCES pedido(ID_Pedido)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE modificaProducto (\n" +
                    "    ID_Usuario integer REFERENCES usuario(ID_Usuario),\n" +
                    "    ID_Producto integer REFERENCES producto(ID_Producto),\n" +
                    "    PRIMARY KEY(ID_Producto)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE tiene (\n" +
                    "    ID_Carrito integer REFERENCES carrito(ID_Carrito),\n" +
                    "    ID_Producto integer REFERENCES producto(ID_Producto),\n" +
                    "    Cantidad integer CHECK ( Cantidad > 0 ),\n" +
                    "    PRIMARY KEY(ID_Carrito, ID_Producto)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE GestionCarrito (\n" +
                    "    ID_Carrito integer REFERENCES carrito(ID_Carrito),\n" +
                    "    ID_Pedido integer REFERENCES pedido(ID_Pedido),\n" +
                    "    PRIMARY KEY(ID_Carrito, ID_Pedido)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE GestionPedido (\n" +
                    "    ID_Usuario integer REFERENCES usuario(ID_Usuario),\n" +
                    "    ID_Pedido integer REFERENCES pedido(ID_Pedido),\n" +
                    "    PRIMARY KEY(ID_Usuario, ID_Pedido),\n" +
                    "    UNIQUE(ID_Pedido)\n" +
                    ")");

            Connection.connection.commit();
            JOptionPane.showMessageDialog(Connection.frame, "Tablas creadas correctamente.");
            JOptionPane.showMessageDialog(Connection.frame, "Secuencia creada correctamente.");
        }
    }


}


