package practica;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Stack;

public class diseño {

    private static JTextArea textArea;
    private reseña reviewService = new reseña(); // Instancia de la clase reseña con los métodos JDBC


    public static void pantalla_registro(JFrame frame) {

    }

    public static void pantalla_inicio(JFrame frame) {


        // Barra de Menú
        JMenuBar menuBar = new JMenuBar();

        // Menú Archivo con la opción Salir
        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem salirItem = new JMenuItem("Salir");
        salirItem.addActionListener(e -> {
            connection.cerrarConexion(true);
            System.exit(0);
        });
        menuArchivo.add(salirItem);

        // Menú Gestión de Tablas
        JMenu menuGestion = new JMenu("Gestión de Tablas");

        // Opción 1: Borrar y crear tablas
        JMenuItem crearBorrarTablas = new JMenuItem("Borrar y Crear Tablas");
        crearBorrarTablas.addActionListener(e -> {
            eliminarDatos_tabla();
            borraryCrearTablas();
        });
        menuGestion.add(crearBorrarTablas);

        // Opción 2: Eliminar datos de las tablas
        JMenuItem eliminarDatosTablas = new JMenuItem("Eliminar Datos de las Tablas");
        eliminarDatosTablas.addActionListener(e -> {
            eliminarDatos_tabla();
        });
        menuGestion.add(eliminarDatosTablas);

        // Opción 3: Insertar datos de prueba
        JMenuItem insertarDatosPrueba = new JMenuItem("Insertar Datos de Prueba");
        insertarDatosPrueba.addActionListener(e -> {
            insertarDatosPrueba_tabla();
        });
        menuGestion.add(insertarDatosPrueba);


        // Añadir menús a la barra de menú
        menuBar.add(menuArchivo);
        menuBar.add(menuGestion);
        frame.setJMenuBar(menuBar);

        // Panel para "Mostrar Tablas"
        JPanel panelMostrarTablas = new JPanel(new BorderLayout());
        textArea = new JTextArea(10, 40);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JButton mostrarTablasButton = new JButton("Mostrar Tablas");
        // Podríamos implementar una funcionalidad para mostrar tablas si se requiere
        mostrarTablasButton.addActionListener(e -> mostrarTablas());
        panelMostrarTablas.add(mostrarTablasButton, BorderLayout.NORTH);
        panelMostrarTablas.add(scrollPane, BorderLayout.CENTER);

        // Panel "Dar de alta nuevo pedido" (ejemplo)
        JPanel panelAltaPedido = new JPanel();
        panelAltaPedido.add(new JLabel("Aquí iría el formulario para dar de alta un nuevo pedido"));

        // -----------------------------------------------------------
        // PESTAÑA PARA RESEÑAS
        // -----------------------------------------------------------

        JTabbedPane reseñasTabbedPane = new JTabbedPane();

        // Panel para Añadir Reseña
        JPanel panelAddReview = new JPanel(new GridLayout(6, 2, 5, 5));
        JTextField txtIdReseña = new JTextField();
        JTextField txtIdPedido = new JTextField();
        JTextField txtIdUsuario = new JTextField();
        JTextField txtValoracion = new JTextField();
        JTextField txtComentario = new JTextField();

        panelAddReview.add(new JLabel("ID Reseña:"));
        panelAddReview.add(txtIdReseña);
        panelAddReview.add(new JLabel("ID Pedido:"));
        panelAddReview.add(txtIdPedido);
        panelAddReview.add(new JLabel("ID Usuario:"));
        panelAddReview.add(txtIdUsuario);
        panelAddReview.add(new JLabel("Valoración (1-5):"));
        panelAddReview.add(txtValoracion);
        panelAddReview.add(new JLabel("Comentario (max 500 chars):"));
        panelAddReview.add(txtComentario);

        JButton btnAddReview = new JButton("Añadir Reseña");
        panelAddReview.add(btnAddReview);

        btnAddReview.addActionListener(e -> {
            try {
                int idRes = Integer.parseInt(txtIdReseña.getText().trim());
                int idPed = Integer.parseInt(txtIdPedido.getText().trim());
                int idUser = Integer.parseInt(txtIdUsuario.getText().trim());
                int val = Integer.parseInt(txtValoracion.getText().trim());
                String com = txtComentario.getText().trim();

                // Llamada a método addReview
                reseña reviewService = new reseña();
                reviewService.addReview(idRes, idPed, idUser, val, com);
                JOptionPane.showMessageDialog(panelAddReview, "Reseña añadida con éxito.");

            } catch (Exception ex2) {
                JOptionPane.showMessageDialog(panelAddReview, "Error al añadir reseña: " + ex2.getMessage());
            }
        });

        reseñasTabbedPane.addTab("Añadir Reseña", panelAddReview);

        // Panel para Editar Reseña
        JPanel panelEditReview = new JPanel(new GridLayout(5, 2, 5, 5));
        JTextField txtEditIdReseña = new JTextField();
        JTextField txtEditIdUsuario = new JTextField();
        JTextField txtEditValoracion = new JTextField();
        JTextField txtEditComentario = new JTextField();

        panelEditReview.add(new JLabel("ID Reseña:"));
        panelEditReview.add(txtEditIdReseña);
        panelEditReview.add(new JLabel("ID Usuario:"));
        panelEditReview.add(txtEditIdUsuario);
        panelEditReview.add(new JLabel("Nueva Valoración (1-5):"));
        panelEditReview.add(txtEditValoracion);
        panelEditReview.add(new JLabel("Nuevo Comentario:"));
        panelEditReview.add(txtEditComentario);

        JButton btnEditReview = new JButton("Editar Reseña");
        panelEditReview.add(btnEditReview);

        btnEditReview.addActionListener(e -> {
            try {
                int idRes = Integer.parseInt(txtEditIdReseña.getText().trim());
                int idUser = Integer.parseInt(txtEditIdUsuario.getText().trim());
                int val = Integer.parseInt(txtEditValoracion.getText().trim());
                String com = txtEditComentario.getText().trim();

                reseña reviewService = new reseña();
                reviewService.editReview(idRes, idUser, val, com);
                JOptionPane.showMessageDialog(panelEditReview, "Reseña editada con éxito.");

            } catch (Exception ex2) {
                JOptionPane.showMessageDialog(panelEditReview, "Error al editar reseña: " + ex2.getMessage());
            }
        });

        reseñasTabbedPane.addTab("Editar Reseña", panelEditReview);

        // Panel para Eliminar Reseña
        JPanel panelDeleteReview = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField txtDeleteIdReseña = new JTextField();
        JTextField txtDeleteIdUsuario = new JTextField();

        panelDeleteReview.add(new JLabel("ID Reseña:"));
        panelDeleteReview.add(txtDeleteIdReseña);
        panelDeleteReview.add(new JLabel("ID Usuario:"));
        panelDeleteReview.add(txtDeleteIdUsuario);

        JButton btnDeleteReview = new JButton("Eliminar Reseña");
        panelDeleteReview.add(btnDeleteReview);

        btnDeleteReview.addActionListener(e -> {
            try {
                int idRes = Integer.parseInt(txtDeleteIdReseña.getText().trim());
                int idUser = Integer.parseInt(txtDeleteIdUsuario.getText().trim());

                reseña reviewService = new reseña();
                reviewService.deleteReview(idRes, idUser);
                JOptionPane.showMessageDialog(panelDeleteReview, "Reseña eliminada con éxito.");

            } catch (Exception ex2) {
                JOptionPane.showMessageDialog(panelDeleteReview, "Error al eliminar reseña: " + ex2.getMessage());
            }
        });

        reseñasTabbedPane.addTab("Eliminar Reseña", panelDeleteReview);

        // Panel para Ver Reseñas por Pedido
        JPanel panelViewByOrder = new JPanel(new BorderLayout(5,5));
        JPanel inputPanelOrder = new JPanel(new GridLayout(1,2,5,5));
        JTextField txtViewByOrder = new JTextField();
        inputPanelOrder.add(new JLabel("ID Pedido:"));
        inputPanelOrder.add(txtViewByOrder);

        JButton btnViewByOrder = new JButton("Ver Reseñas del Pedido");
        JPanel topPanelOrder = new JPanel(new FlowLayout());
        topPanelOrder.add(inputPanelOrder);
        topPanelOrder.add(btnViewByOrder);

        JTextArea textAreaOrder = new JTextArea(10,40);
        textAreaOrder.setEditable(false);

        panelViewByOrder.add(topPanelOrder, BorderLayout.NORTH);
        panelViewByOrder.add(new JScrollPane(textAreaOrder), BorderLayout.CENTER);

        btnViewByOrder.addActionListener(e -> {
            try {
                textAreaOrder.setText("");
                int idPed = Integer.parseInt(txtViewByOrder.getText().trim());
                reseña reviewService = new reseña();
                ArrayList<String> reviews = reviewService.getReviewsByOrder(idPed);
                for (String review : reviews) {
                    textAreaOrder.append(review + "\n");
                }
            } catch (Exception ex2) {
                JOptionPane.showMessageDialog(panelViewByOrder, "Error al obtener reseñas: " + ex2.getMessage());
            }
        });

        reseñasTabbedPane.addTab("Ver Reseñas por Pedido", panelViewByOrder);

        // Panel para Ver Reseñas por Usuario
        JPanel panelViewByUser = new JPanel(new BorderLayout(5,5));
        JPanel inputPanelUser = new JPanel(new GridLayout(1,2,5,5));
        JTextField txtViewByUser = new JTextField();
        inputPanelUser.add(new JLabel("ID Usuario:"));
        inputPanelUser.add(txtViewByUser);

        JButton btnViewByUser = new JButton("Ver Reseñas del Usuario");
        JPanel topPanelUser = new JPanel(new FlowLayout());
        topPanelUser.add(inputPanelUser);
        topPanelUser.add(btnViewByUser);

        JTextArea textAreaUser = new JTextArea(10,40);
        textAreaUser.setEditable(false);

        panelViewByUser.add(topPanelUser, BorderLayout.NORTH);
        panelViewByUser.add(new JScrollPane(textAreaUser), BorderLayout.CENTER);

        btnViewByUser.addActionListener(e -> {
            try {
                textAreaUser.setText("");
                int idUser = Integer.parseInt(txtViewByUser.getText().trim());
                reseña reviewService = new reseña();
                ArrayList<String> reviews = reviewService.getReviewsByUser(idUser);
                for (String review : reviews) {
                    textAreaUser.append(review + "\n");
                }
            } catch (Exception ex2) {
                JOptionPane.showMessageDialog(panelViewByUser, "Error al obtener reseñas: " + ex2.getMessage());
            }
        });

        reseñasTabbedPane.addTab("Ver Reseñas por Usuario", panelViewByUser);

        // -----------------------------------------------------------
        // Pestañas principales
        // -----------------------------------------------------------
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Mostrar Tablas", panelMostrarTablas);
        tabbedPane.addTab("Dar de alta nuevo pedido", panelAltaPedido);
        tabbedPane.addTab("Reseñas", reseñasTabbedPane);

        frame.add(tabbedPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }




    public static void mostrarTablas() {
        if (connection.connection == null) {
            JOptionPane.showMessageDialog(connection.frame, "No hay conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (PreparedStatement ps = connection.connection.prepareStatement("SELECT table_name FROM user_tables");
             ResultSet rs = ps.executeQuery()) {

            JDialog dialog = new JDialog(connection.frame, "Tablas en la BD", true);
            dialog.setLayout(new BorderLayout());

            JTabbedPane tabbedPane = new JTabbedPane();

            boolean hayTablas = false;
            while (rs.next()) {
                hayTablas = true;
                String tableName = rs.getString("table_name");

                // Consulta para obtener datos de la tabla ordenados de forma ascendente por la primera columna
                String sql = "SELECT * FROM \"" + tableName + "\" ORDER BY 1 ASC";
                try (Statement st = connection.connection.createStatement();
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
            dialog.setLocationRelativeTo(connection.frame);
            dialog.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(connection.frame, "Error al obtener las tablas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    private static void eliminarDatos_tabla() {
        try (Statement stmt = connection.connection.createStatement()) {
            // Truncar tablas dependientes primero
            stmt.executeUpdate("TRUNCATE TABLE Gestion_Reseña");
            stmt.executeUpdate("TRUNCATE TABLE Realiza");
            stmt.executeUpdate("TRUNCATE TABLE GestionPago");

            // Truncar tablas principales después
            stmt.executeUpdate("TRUNCATE TABLE reseña");
            stmt.executeUpdate("TRUNCATE TABLE pedido");
            stmt.executeUpdate("TRUNCATE TABLE producto");
            stmt.executeUpdate("TRUNCATE TABLE pago");
            stmt.executeUpdate("TRUNCATE TABLE carrito");
            stmt.executeUpdate("TRUNCATE TABLE usuario");

            connection.connection.commit();
            JOptionPane.showMessageDialog(connection.frame, "Datos eliminados correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(connection.frame, "Error al eliminar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }



    private static void insertarDatosPrueba_tabla(){
        try (Statement stmt = connection.connection.createStatement()) {
            // Insertar Usuarios
            stmt.executeUpdate("INSERT INTO usuario (ID_Usuario, Correo, Nombre, Estado, Direccion) VALUES (1, 'user1@example.com', 'Juan', 'A', 'Calle Falsa 123')");
            stmt.executeUpdate("INSERT INTO usuario (ID_Usuario, Correo, Nombre, Estado, Direccion) VALUES (2, 'user2@example.com', 'Maria', 'A', 'Avenida Principal 456')");

            // Insertar Productos
            stmt.executeUpdate("INSERT INTO producto (ID_Producto, NombreProducto, Cantidad, Precio) VALUES (10, 'Camisa', 50, 19.99)");
            stmt.executeUpdate("INSERT INTO producto (ID_Producto, NombreProducto, Cantidad, Precio) VALUES (11, 'Pantalón', 20, 29.99)");

            // Insertar Pedidos
            stmt.executeUpdate("INSERT INTO pedido (ID_Pedido, Direccion, Estado_Pedido, Tipo_Pago, Metodo_Envio, ID_Usuario) VALUES (100, 'Calle Falsa 123', 'Entregado', 1, 'Correo', 1)");
            stmt.executeUpdate("INSERT INTO pedido (ID_Pedido, Direccion, Estado_Pedido, Tipo_Pago, Metodo_Envio, ID_Usuario) VALUES (101, 'Avenida Principal 456', 'Entregado', 2, 'Mensajeria', 2)");

            // Insertar Reseñas
            stmt.executeUpdate("INSERT INTO reseña (ID_Resena, Comentario, Valoracion) VALUES (1, 'Muy buen producto', 5)");
            stmt.executeUpdate("INSERT INTO reseña (ID_Resena, Comentario, Valoracion) VALUES (2, 'Rápida entrega, producto aceptable', 4)");

            // Asociar Reseñas con Pedidos
            stmt.executeUpdate("INSERT INTO Gestion_Reseña (ID_Resena, ID_Pedido) VALUES (1, 100)");
            stmt.executeUpdate("INSERT INTO Gestion_Reseña (ID_Resena, ID_Pedido) VALUES (2, 101)");

            // Insertar Pagos
            stmt.executeUpdate("INSERT INTO pago (ID_metodoPago, Fecha) VALUES (1, SYSDATE)");
            stmt.executeUpdate("INSERT INTO pago (ID_metodoPago, Fecha) VALUES (2, SYSDATE)");

            // Asociar Pagos
            stmt.executeUpdate("INSERT INTO GestionPago (ID_Usuario, ID_metodoPago) VALUES (1, 1)");
            stmt.executeUpdate("INSERT INTO GestionPago (ID_Usuario, ID_metodoPago) VALUES (2, 2)");
            stmt.executeUpdate("INSERT INTO Realiza (ID_metodoPago, ID_Pedido, Metodo_Pago) VALUES (1, 100, 'Tarjeta')");
            stmt.executeUpdate("INSERT INTO Realiza (ID_metodoPago, ID_Pedido, Metodo_Pago) VALUES (2, 101, 'PayPal')");

            connection.connection.commit();
            JOptionPane.showMessageDialog(connection.frame, "Datos de prueba insertados correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(connection.frame, "Error al insertar datos de prueba: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private static void borraryCrearTablas() {
        try (Statement stmt = connection.connection.createStatement()) {
            // Borrar tablas que dependen de otras
            stmt.executeUpdate("DROP TABLE Gestion_Reseña PURGE");
            stmt.executeUpdate("DROP TABLE Realiza PURGE");
            stmt.executeUpdate("DROP TABLE GestionPago PURGE");
            stmt.executeUpdate("DROP TABLE GestionCarrito PURGE");
            stmt.executeUpdate("DROP TABLE GestionPedido PURGE");
            stmt.executeUpdate("DROP TABLE modificaProducto PURGE");
            stmt.executeUpdate("DROP TABLE tiene PURGE");

            // Borrar tablas base
            stmt.executeUpdate("DROP TABLE reseña PURGE");
            stmt.executeUpdate("DROP TABLE pedido PURGE");
            stmt.executeUpdate("DROP TABLE producto PURGE");
            stmt.executeUpdate("DROP TABLE pago PURGE");
            stmt.executeUpdate("DROP TABLE carrito PURGE");
            stmt.executeUpdate("DROP TABLE usuario PURGE");

            // Crear tablas base
            stmt.executeUpdate("CREATE TABLE usuario (\n" +
                    "    ID_Usuario integer NOT NULL,\n" +
                    "    Correo varchar(30),\n" +
                    "    Nombre varchar(30),\n" +
                    "    Estado CHAR(1) CHECK (Estado IN ('A', 'I')),\n" +
                    "    Direccion varchar(50),\n" +
                    "    PRIMARY KEY(ID_Usuario)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE producto (\n" +
                    "    ID_Producto integer NOT NULL,\n" +
                    "    NombreProducto varchar(30),\n" +
                    "    Cantidad integer,\n" +
                    "    Precio float,\n" +
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
                    "    Tipo_Pago integer,\n" +
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
                    "    Fecha DATE\n" +
                    ")");

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
                    "    PRIMARY KEY(ID_Usuario)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE tiene (\n" +
                    "    ID_Carrito integer REFERENCES carrito(ID_Carrito),\n" +
                    "    ID_Producto integer REFERENCES producto(ID_Producto),\n" +
                    "    Cantidad integer,\n" +
                    "    PRIMARY KEY(ID_Carrito, ID_Producto)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE GestionCarrito (\n" +
                    "    ID_Carrito integer REFERENCES carrito(ID_Carrito),\n" +
                    "    ID_Pedido integer REFERENCES pedido(ID_Pedido),\n" +
                    "    PRIMARY KEY(ID_Carrito)\n" +
                    ")");

            stmt.executeUpdate("CREATE TABLE GestionPedido (\n" +
                    "    ID_Usuario integer REFERENCES usuario(ID_Usuario),\n" +
                    "    ID_Pedido integer REFERENCES pedido(ID_Pedido),\n" +
                    "    PRIMARY KEY(ID_Pedido),\n" +
                    "    UNIQUE(ID_Usuario)\n" +
                    ")");

            connection.connection.commit();
            JOptionPane.showMessageDialog(connection.frame, "Tablas creadas correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(connection.frame, "Error al crear las tablas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }





}