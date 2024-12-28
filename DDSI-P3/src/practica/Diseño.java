package practica;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;
import static practica.Connection.id_user;

public class Diseño {

    private static JTextArea textArea;
    private Reseña reviewService = new Reseña(); // Instancia de la clase reseña con los métodos JDBC


    public static int pantalla_registro(JFrame frame) {
        AtomicInteger id_usuario = new AtomicInteger();

        JTabbedPane tabbedPane = new JTabbedPane();

        // Panel de Registro
        JPanel panelRegistro = new JPanel(new GridLayout(5, 2, 10, 10));
        JTextField txtCorreo = new JTextField();
        JTextField txtNombre = new JTextField();
        JTextField txtDireccion = new JTextField();
        JTextField txtContraseña = new JTextField();

        panelRegistro.add(new JLabel("Correo:"));
        panelRegistro.add(txtCorreo);
        panelRegistro.add(new JLabel("Nombre:"));
        panelRegistro.add(txtNombre);
        panelRegistro.add(new JLabel("Dirección:"));
        panelRegistro.add(txtDireccion);
        panelRegistro.add(new JLabel("Contraseña:"));
        panelRegistro.add(txtContraseña);

        JButton btnRegistrar = new JButton("Registrar");
        panelRegistro.add(new JLabel("")); // Espacio vacío
        panelRegistro.add(btnRegistrar);

        btnRegistrar.addActionListener(e -> {
            try {
                String correo = txtCorreo.getText().trim();
                String nombre = txtNombre.getText().trim();
                String direccion = txtDireccion.getText().trim();
                String contraseña = txtContraseña.getText().trim();

                // Validaciones de entrada
                if (correo.isEmpty() || nombre.isEmpty() || direccion.isEmpty() || contraseña.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Todos los campos son obligatorios.", "Error de entrada", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    JOptionPane.showMessageDialog(frame, "El correo no tiene un formato válido.", "Error de entrada", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Usuario userService = new Usuario();
                int idGenerado = userService.registerUser(correo, nombre, direccion, contraseña);
                id_usuario.set(idGenerado);
                JOptionPane.showMessageDialog(frame, "Usuario registrado con éxito. ID: " + idGenerado);
                frame.dispose(); // Cerrar la ventana después del registro
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error al registrar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        tabbedPane.addTab("Registrar", panelRegistro);

        // Panel de Inicio de Sesión
        JPanel panelLogin = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField txtCorreoLogin = new JTextField();
        JTextField txtContraseñaLogin = new JTextField();

        panelLogin.add(new JLabel("Correo:"));
        panelLogin.add(txtCorreoLogin);
        panelLogin.add(new JLabel("Contraseña:"));
        panelLogin.add(txtContraseñaLogin);

        JButton btnLogin = new JButton("Iniciar Sesión");
        panelLogin.add(new JLabel("")); // Espacio vacío
        panelLogin.add(btnLogin);

        btnLogin.addActionListener(e -> {
            try {
                String correo = txtCorreoLogin.getText().trim();
                String contraseña = txtContraseñaLogin.getText().trim();

                // Validaciones de entrada
                if (correo.isEmpty() || contraseña.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Correo y contraseña son obligatorios.", "Error de entrada", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    JOptionPane.showMessageDialog(frame, "El correo no tiene un formato válido.", "Error de entrada", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Usuario userService = new Usuario();
                int id = userService.loginUser(correo, contraseña);

                if (id > 0) {
                    id_usuario.set(id);
                    JOptionPane.showMessageDialog(frame, "Inicio de sesión exitoso.");
                    frame.dispose(); // Cerrar la ventana después del inicio de sesión
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error al iniciar sesión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        tabbedPane.addTab("Iniciar Sesión", panelLogin);

        frame.getContentPane().removeAll();
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);

        while (id_usuario.get() == 0) {
            try {
                sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        return id_usuario.get();
    }

    public static void pantalla_inicio(JFrame frame) {


        // Barra de Menú
        JMenuBar menuBar = new JMenuBar();

        // Menú Archivo con la opción Salir
        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem salirItem = new JMenuItem("Salir");
        salirItem.addActionListener(e -> {
            Connection.cerrarConexion(true);
            System.exit(0);
        });
        menuArchivo.add(salirItem);

        // Menú Gestión de Tablas
        JMenu menuGestion = new JMenu("Gestión de Tablas");

        // Opción 1: Borrar y crear tablas
        JMenuItem crearBorrarTablas = new JMenuItem("Borrar y Crear Tablas");
        crearBorrarTablas.addActionListener(e -> {
            FuncionesBD.eliminarDatos_tabla();
            FuncionesBD.borraryCrearTablas();
            FuncionesBD.crearSecuenciaPedido();
            FuncionesBD.crearSecuenciaCarrito();
        });
        menuGestion.add(crearBorrarTablas);

        // Opción 2: Eliminar datos de las tablas
        JMenuItem eliminarDatosTablas = new JMenuItem("Eliminar Datos de las Tablas");
        eliminarDatosTablas.addActionListener(e -> {
            FuncionesBD.eliminarDatos_tabla();
        });
        menuGestion.add(eliminarDatosTablas);

        // Opción 3: Insertar datos de prueba
        JMenuItem insertarDatosPrueba = new JMenuItem("Insertar Datos de Prueba");
        insertarDatosPrueba.addActionListener(e -> {
            FuncionesBD.insertarDatosPrueba_tabla();
        });
        menuGestion.add(insertarDatosPrueba);

        // Opción 4: Insertar disparadores
        JMenuItem insertarDisparadores = new JMenuItem("Insertar disparadores");
        insertarDisparadores.addActionListener(e -> {
            try {
                Disparadores.crearDisparadores(Connection.connection);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        menuGestion.add(insertarDisparadores);




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
        mostrarTablasButton.addActionListener(e -> FuncionesBD.mostrarTablas());
        panelMostrarTablas.add(mostrarTablasButton, BorderLayout.NORTH);
        panelMostrarTablas.add(scrollPane, BorderLayout.CENTER);

        // Crear pestañas específicas
        JTabbedPane reseñasTabbedPane = crearPestañasReseñas();
        JTabbedPane productosTabbedPane = crearPestañasProductos();
        JTabbedPane CarritoTabbedPane = crearPestañasCarrito();
        JTabbedPane usuariosTabbedPane = crearPestañasUsuarios();
        JTabbedPane pedidosTabbedPane = crearPestañasPedidos();
        JTabbedPane pagosTabbedPane = crearPestañasPago();



        // -----------------------------------------------------------
        // Pestañas principales
        // -----------------------------------------------------------
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Mostrar Tablas", panelMostrarTablas);
        tabbedPane.addTab("Reseñas", reseñasTabbedPane);
        tabbedPane.addTab("Productos", productosTabbedPane);
        tabbedPane.addTab("Carrito", CarritoTabbedPane);
        tabbedPane.addTab("Usuarios", usuariosTabbedPane);
        tabbedPane.addTab("Pedidos", pedidosTabbedPane);
        tabbedPane.addTab("Pagos", pagosTabbedPane);



        frame.add(tabbedPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }


// -------------------------------------------------------------------------------------------Ç
    // PESTAÑAS DE CADA SUBSISTEMA

// -----------------------------------------------------------------------------------------------

    private static JTabbedPane crearPestañasCarrito() {

    JTabbedPane carritoTabbedPane = new JTabbedPane();

    // -----------------------------------------------------------
    // RF3.1: Añadir producto al carrito
    // -----------------------------------------------------------
    JPanel panelAddToCart = new JPanel(new BorderLayout(5, 5));
    JPanel inputPanelAdd = new JPanel(new GridLayout(3, 2, 5, 5));
    JTextField txtIdUsuarioAdd = new JTextField();
    JTextField txtIdProductoAdd = new JTextField();
    JTextField txtCantidadAdd = new JTextField();

    inputPanelAdd.add(new JLabel("ID Usuario: eres el usuario " + id_user));
    inputPanelAdd.add(new JLabel(""));
    inputPanelAdd.add(new JLabel("ID Producto:"));
    inputPanelAdd.add(txtIdProductoAdd);
    inputPanelAdd.add(new JLabel("Cantidad:"));
    inputPanelAdd.add(txtCantidadAdd);

    JButton btnAddToCart = new JButton("Añadir al Carrito");
    JPanel buttonPanelAdd = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanelAdd.add(btnAddToCart);

    panelAddToCart.add(inputPanelAdd, BorderLayout.CENTER);
    panelAddToCart.add(buttonPanelAdd, BorderLayout.SOUTH);

    btnAddToCart.addActionListener(e -> {
        try {
            int idProducto = Integer.parseInt(txtIdProductoAdd.getText().trim());
            int cantidad = Integer.parseInt(txtCantidadAdd.getText().trim());

            Carrito carritoService = new Carrito();
            carritoService.addProductToCart(id_user, idProducto, cantidad);
            JOptionPane.showMessageDialog(panelAddToCart, "Producto añadido con éxito al carrito.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panelAddToCart, "Error al añadir producto: " + ex.getMessage());
        }
    });

    carritoTabbedPane.addTab("Añadir Producto al Carrito", panelAddToCart);

    // -----------------------------------------------------------
    // RF3.2: Ver carrito de compras
    // -----------------------------------------------------------

    JPanel panelViewCart = new JPanel(new BorderLayout(10, 10));
    JPanel inputPanelView = new JPanel(new GridLayout(1, 2, 5, 5));
    JTextField txtIdUsuarioView = new JTextField();

    inputPanelView.add(new JLabel("ID Usuario: eres el usuario " + id_user));

    JButton btnViewCart = new JButton("Ver Carrito");
    JPanel buttonPanelView = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanelView.add(btnViewCart);

    JTextArea textAreaViewCart = new JTextArea(10, 40);
    textAreaViewCart.setEditable(false);
    textAreaViewCart.setAlignmentX(JTextArea.CENTER_ALIGNMENT);

    panelViewCart.add(inputPanelView, BorderLayout.NORTH);
    panelViewCart.add(new JScrollPane(textAreaViewCart), BorderLayout.CENTER);
    panelViewCart.add(buttonPanelView, BorderLayout.SOUTH);

    btnViewCart.addActionListener(e -> {
        try {
            textAreaViewCart.setText("");

            Carrito carritoService = new Carrito();
            ArrayList<String> productos = carritoService.viewCart(id_user);

            for (String linea : productos) {
                textAreaViewCart.append(linea + "\n");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panelViewCart, "Error al ver el carrito: " + ex.getMessage());
        }
    });

    carritoTabbedPane.addTab("Ver Carrito", panelViewCart);

        // -----------------------------------------------------------
    // RF3.3: Modificar cantidad de un producto en el carrito
    // -----------------------------------------------------------
    JPanel panelModifyQuantity = new JPanel(new BorderLayout(5, 5));
    JPanel inputPanelModify = new JPanel(new GridLayout(3, 2, 5, 5));
    JTextField txtIdUsuarioModify = new JTextField();
    JTextField txtIdProductoModify = new JTextField();
    JTextField txtNuevaCantidad = new JTextField();

    inputPanelModify.add(new JLabel("ID Usuario: eres el usuario " + id_user));
    inputPanelModify.add(new JLabel(""));
    inputPanelModify.add(new JLabel("ID Producto:"));
    inputPanelModify.add(txtIdProductoModify);
    inputPanelModify.add(new JLabel("Nueva Cantidad:"));
    inputPanelModify.add(txtNuevaCantidad);

    JButton btnModifyQuantity = new JButton("Modificar Cantidad");
    JPanel buttonPanelModify = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanelModify.add(btnModifyQuantity);

    panelModifyQuantity.add(inputPanelModify, BorderLayout.CENTER);
    panelModifyQuantity.add(buttonPanelModify, BorderLayout.SOUTH);

    btnModifyQuantity.addActionListener(e -> {
        try {
            int idProducto = Integer.parseInt(txtIdProductoModify.getText().trim());
            int cantidad = Integer.parseInt(txtNuevaCantidad.getText().trim());

            Carrito carritoService = new Carrito();
            carritoService.modifyCartQuantity(id_user, idProducto, cantidad);
            JOptionPane.showMessageDialog(panelModifyQuantity, "Cantidad modificada con éxito.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panelModifyQuantity, "Error al modificar cantidad: " + ex.getMessage());
        }
    });

    carritoTabbedPane.addTab("Modificar Cantidad", panelModifyQuantity);

    // -----------------------------------------------------------
    // RF3.4: Eliminar producto del carrito
    // -----------------------------------------------------------
    JPanel panelRemoveProduct = new JPanel(new BorderLayout(5, 5));
    JPanel inputPanelRemove = new JPanel(new GridLayout(2, 2, 5, 5));
    JTextField txtIdUsuarioRemove = new JTextField();
    JTextField txtIdProductoRemove = new JTextField();

    inputPanelRemove.add(new JLabel("ID Usuario: eres el usuario " + id_user));
    inputPanelRemove.add(new JLabel(""));
    inputPanelRemove.add(new JLabel("ID Producto:"));
    inputPanelRemove.add(txtIdProductoRemove);

    JButton btnRemoveProduct = new JButton("Eliminar Producto del Carrito");
    JPanel buttonPanelRemove = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanelRemove.add(btnRemoveProduct);

    panelRemoveProduct.add(inputPanelRemove, BorderLayout.CENTER);
    panelRemoveProduct.add(buttonPanelRemove, BorderLayout.SOUTH);

    btnRemoveProduct.addActionListener(e -> {
        try {
            int idProducto = Integer.parseInt(txtIdProductoRemove.getText().trim());

            Carrito carritoService = new Carrito();
            carritoService.removeProductFromCart(id_user, idProducto);
            JOptionPane.showMessageDialog(panelRemoveProduct, "Producto eliminado del carrito con éxito.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panelRemoveProduct, "Error al eliminar producto: " + ex.getMessage());
        }
    });

    carritoTabbedPane.addTab("Eliminar Producto del Carrito", panelRemoveProduct);

    // -----------------------------------------------------------
    // RF3.5: Vaciar carrito de compras
    // -----------------------------------------------------------
    JPanel panelEmptyCart = new JPanel(new BorderLayout(5, 5));
    JPanel inputPanelEmpty = new JPanel(new GridLayout(1, 2, 5, 5));
    JTextField txtIdUsuarioEmpty = new JTextField();

    inputPanelEmpty.add(new JLabel("ID Usuario: eres el usuario " + id_user));

    JTextArea textAreaEmptyCart = new JTextArea(10, 40);
    textAreaEmptyCart.setEditable(false);
    textAreaEmptyCart.setAlignmentX(JTextArea.CENTER_ALIGNMENT);

    JButton btnEmptyCart = new JButton("Vaciar Carrito");
    JPanel buttonPanelEmpty = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanelEmpty.add(btnEmptyCart);

    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
    centerPanel.add(Box.createVerticalGlue());
    centerPanel.add(textAreaEmptyCart);
    centerPanel.add(Box.createVerticalStrut(10));
    centerPanel.add(buttonPanelEmpty);
    centerPanel.add(Box.createVerticalGlue());

    panelEmptyCart.add(inputPanelEmpty, BorderLayout.NORTH);
    panelEmptyCart.add(centerPanel, BorderLayout.CENTER);

    btnEmptyCart.addActionListener(e -> {
        try {
            Carrito carritoService = new Carrito();
            carritoService.emptyCart(id_user);
            textAreaEmptyCart.setText("Carrito vaciado con éxito.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panelEmptyCart, "Error al vaciar el carrito: " + ex.getMessage());
        }
    });

    carritoTabbedPane.addTab("Vaciar Carrito", panelEmptyCart);

    return carritoTabbedPane;
}


    private static JTabbedPane crearPestañasProductos() {

        // -----------------------------------------------------------
        // PESTAÑA PARA PRODUCTOS
        // -----------------------------------------------------------

        JTabbedPane productosTabbedPane = new JTabbedPane();

        // Panel para Añadir Producto
        JPanel panelAddProduct = new JPanel(new GridLayout(6, 2, 5, 5));
        JTextField txtIdProducto = new JTextField();
        JTextField txtNombreProducto = new JTextField();
        JTextField txtPrecioProducto = new JTextField();
        JTextField txtCantidadProducto = new JTextField();
        JTextField txtIdUsuarioProducto = new JTextField();

        panelAddProduct.add(new JLabel("ID Producto:"));
        panelAddProduct.add(txtIdProducto);
        panelAddProduct.add(new JLabel("Nombre:"));
        panelAddProduct.add(txtNombreProducto);
        panelAddProduct.add(new JLabel("Precio:"));
        panelAddProduct.add(txtPrecioProducto);
        panelAddProduct.add(new JLabel("Cantidad:"));
        panelAddProduct.add(txtCantidadProducto);
        panelAddProduct.add(new JLabel("ID Usuario: eres el usuario " + id_user));

        JButton btnAddProduct = new JButton("Añadir Producto");
        panelAddProduct.add(btnAddProduct);

        btnAddProduct.addActionListener(e -> {
            try {
                int idProducto = Integer.parseInt(txtIdProducto.getText().trim());
                String nombre = txtNombreProducto.getText().trim();
                double precio = Double.parseDouble(txtPrecioProducto.getText().trim());
                int cantidad = Integer.parseInt(txtCantidadProducto.getText().trim());
                int idUsuario = id_user;

                // Llamada al método addProduct
                Producto productoService = new Producto();
                productoService.addProduct(idProducto, nombre, precio, cantidad, idUsuario);
                JOptionPane.showMessageDialog(panelAddProduct, "Producto añadido con éxito.");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelAddProduct, "Error al añadir producto: " + ex.getMessage());
            }
        });

        productosTabbedPane.addTab("Añadir Producto", panelAddProduct);

        // Panel para Editar Producto
        JPanel panelEditProduct = new JPanel(new GridLayout(6, 2, 5, 5));
        JTextField txtEditIdProducto = new JTextField();
        JTextField txtEditNombreProducto = new JTextField();
        JTextField txtEditPrecioProducto = new JTextField();
        JTextField txtEditCantidadProducto = new JTextField();
        JTextField txtEditIdUsuarioProducto = new JTextField();

        panelEditProduct.add(new JLabel("ID Producto:"));
        panelEditProduct.add(txtEditIdProducto);
        panelEditProduct.add(new JLabel("Nuevo Nombre:"));
        panelEditProduct.add(txtEditNombreProducto);
        panelEditProduct.add(new JLabel("Nuevo Precio:"));
        panelEditProduct.add(txtEditPrecioProducto);
        panelEditProduct.add(new JLabel("Nueva Cantidad:"));
        panelEditProduct.add(txtEditCantidadProducto);
        panelEditProduct.add(new JLabel("ID Usuario: " + id_user));

        JButton btnEditProduct = new JButton("Editar Producto");
        panelEditProduct.add(btnEditProduct);

        btnEditProduct.addActionListener(e -> {
            try {
                int idProducto = Integer.parseInt(txtEditIdProducto.getText().trim());
                String nombre = txtEditNombreProducto.getText().trim();
                double precio = Double.parseDouble(txtEditPrecioProducto.getText().trim());
                int cantidad = Integer.parseInt(txtEditCantidadProducto.getText().trim());
                int idUsuario = id_user;

                Producto productoService = new Producto();
                productoService.editProduct(idProducto, nombre, cantidad, precio, idUsuario);
                JOptionPane.showMessageDialog(panelEditProduct, "Producto editado con éxito.");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelEditProduct, "Error al editar producto: " + ex.getMessage());
            }
        });

        productosTabbedPane.addTab("Editar Producto", panelEditProduct);

        // Panel para Eliminar Producto
        JPanel panelDeleteProduct = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField txtDeleteIdProducto = new JTextField();

        panelDeleteProduct.add(new JLabel("ID Producto:"));
        panelDeleteProduct.add(txtDeleteIdProducto);
        panelDeleteProduct.add(new JLabel("ID Usuario:" + id_user));

        JButton btnDeleteProduct = new JButton("Eliminar Producto");
        panelDeleteProduct.add(btnDeleteProduct);

        btnDeleteProduct.addActionListener(e -> {
            try {
                int idProducto = Integer.parseInt(txtDeleteIdProducto.getText().trim());
                int idUsuario = id_user;

                Producto productoService = new Producto();
                productoService.deleteProduct(idProducto, idUsuario);
                JOptionPane.showMessageDialog(panelDeleteProduct, "Producto eliminado con éxito.");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelDeleteProduct, "Error al eliminar producto: " + ex.getMessage());
            }
        });

        productosTabbedPane.addTab("Eliminar Producto", panelDeleteProduct);

        // Panel para Filtrar Productos por Precio
        JPanel panelFilterByPrice = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanelPrice = new JPanel(new GridLayout(1, 2, 5, 5));

        // Campo de texto para introducir el precio
        JTextField txtPriceFilter = new JTextField();
        inputPanelPrice.add(new JLabel("Precio:"));
        inputPanelPrice.add(txtPriceFilter);

        // Botón para filtrar por precio
        JButton btnFilterByPrice = new JButton("Filtrar por Precio");
        JPanel topPanelPrice = new JPanel(new FlowLayout());
        topPanelPrice.add(inputPanelPrice);
        topPanelPrice.add(btnFilterByPrice);

        // Área de texto para mostrar los productos filtrados
        JTextArea textAreaPrice = new JTextArea(10, 40);
        textAreaPrice.setEditable(false);

        // Añade los componentes al panel principal
        panelFilterByPrice.add(topPanelPrice, BorderLayout.NORTH);
        panelFilterByPrice.add(new JScrollPane(textAreaPrice), BorderLayout.CENTER);

        // Acción del botón para filtrar productos por precio
        btnFilterByPrice.addActionListener(e -> {
            try {
                // Limpia el área de texto antes de mostrar resultados
                textAreaPrice.setText("");

                // Obtiene y valida el precio introducido por el usuario
                String precioStr = txtPriceFilter.getText().trim();

                if (precioStr.isEmpty()) {
                    JOptionPane.showMessageDialog(panelFilterByPrice, "El campo de precio no puede estar vacío.", "Error de entrada", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double precio = Double.parseDouble(precioStr);

                // Obtiene los productos que coinciden con el precio introducido
                Producto productoService = new Producto();
                ArrayList<String> productos = productoService.getProductsByPrice(precio);

                // Muestra los productos en el área de texto
                if (productos.isEmpty()) {
                    textAreaPrice.setText("No se encontraron productos con el precio especificado.");
                } else {
                    for (String producto : productos) {
                        textAreaPrice.append(producto + "\n");
                    }
                }
            } catch (NumberFormatException ex) {
                // Maneja errores en caso de formato inválido
                JOptionPane.showMessageDialog(panelFilterByPrice, "Por favor, introduce un precio válido.", "Error de formato", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                // Maneja errores generales
                JOptionPane.showMessageDialog(panelFilterByPrice, "Error al filtrar productos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        productosTabbedPane.addTab("Filtrar por Precio", panelFilterByPrice);

        // Panel para Filtrar Productos por Usuario
        JPanel panelFilterByUser = new JPanel(new BorderLayout(10, 10));
        panelFilterByUser.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Margen

        // Panel superior para la entrada del ID y el botón
        JPanel inputPanelUser2 = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espaciado interno
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta "ID Usuario"
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanelUser2.add(new JLabel("Ingrese el ID del Usuario:"), gbc);

        // Campo de texto para el ID Usuario
        JTextField txtUserFilter = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        inputPanelUser2.add(txtUserFilter, gbc);

        // Botón "Filtrar por Usuario"
        JButton btnFilterByUser = new JButton("Filtrar");
        gbc.gridx = 2;
        gbc.gridy = 0;
        inputPanelUser2.add(btnFilterByUser, gbc);

        // Panel para mostrar resultados
        JTextArea textAreaUser2 = new JTextArea(10, 40);
        textAreaUser2.setEditable(false);
        textAreaUser2.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Borde ligero
        JScrollPane scrollPane2 = new JScrollPane(textAreaUser2);

        // Agregar los componentes al panel principal
        panelFilterByUser.add(inputPanelUser2, BorderLayout.NORTH);
        panelFilterByUser.add(scrollPane2, BorderLayout.CENTER);

        // Acción del botón
        btnFilterByUser.addActionListener(e -> {
            try {
                textAreaUser2.setText(""); // Usar la variable correcta
                String input = txtUserFilter.getText().trim(); // Obtener y recortar el texto

                // Validar el campo
                if (input.isEmpty()) {
                    JOptionPane.showMessageDialog(panelFilterByUser, "El campo ID Usuario no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int idUsuario;
                try {
                    idUsuario = Integer.parseInt(input); // Validar que sea un número
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(panelFilterByUser, "El ID de usuario debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Obtener productos
                Producto productoService = new Producto();
                ArrayList<String> productos = productoService.getProductsByUser(idUsuario);
                for (String producto : productos) {
                    textAreaUser2.append(producto + "\n");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelFilterByUser, "Error al filtrar productos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Agregar el panel mejorado al tabbedPane
        productosTabbedPane.addTab("Filtrar por Usuario", panelFilterByUser);
        return productosTabbedPane;
    }

    private static JTabbedPane crearPestañasUsuarios() {
        JTabbedPane usuariosTabbedPane = new JTabbedPane();
            /*
            Implementado en pestalla_registro del fichero Diseño.java
             */
        // -----------------------------------------------------------
        // RF1.1: Registrar Usuario (Devuelve ID generado)
        // -----------------------------------------------------------


        // -----------------------------------------------------------
        // RF1.2: Dar de Baja Usuario (Contraseña obligatoria)
        // -----------------------------------------------------------
        JPanel panelDeleteUser = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField txtIdUserDelete = new JTextField();
        JTextField txtPasswordDelete = new JTextField();

        panelDeleteUser.add(new JLabel("ID Usuario:"));
        panelDeleteUser.add(txtIdUserDelete);
        panelDeleteUser.add(new JLabel("Contraseña:"));
        panelDeleteUser.add(txtPasswordDelete);

        JButton btnDeleteUser = new JButton("Dar de Baja");
        panelDeleteUser.add(btnDeleteUser);

        btnDeleteUser.addActionListener(e -> {
            try {
                Usuario userService = new Usuario();
                userService.deleteUser(
                        Integer.parseInt(txtIdUserDelete.getText().trim()),
                        txtPasswordDelete.getText().trim()
                );
                JOptionPane.showMessageDialog(panelDeleteUser,
                        "Usuario dado de baja con éxito.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelDeleteUser,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        usuariosTabbedPane.addTab("Dar de Baja Usuario", panelDeleteUser);

        // -----------------------------------------------------------
        // RF1.3: Modificar Datos de Usuario
        // -----------------------------------------------------------
        JPanel panelUpdateUser = new JPanel(new GridLayout(6, 2, 5, 5));
        JTextField txtIdUserUpdate = new JTextField();
        JTextField txtCorreoUpdate = new JTextField();
        JTextField txtNombreUpdate = new JTextField();
        JTextField txtDireccionUpdate = new JTextField();
        JTextField txtPasswordUpdate = new JTextField();

        panelUpdateUser.add(new JLabel("ID Usuario:"));
        panelUpdateUser.add(txtIdUserUpdate);
        panelUpdateUser.add(new JLabel("Nuevo Correo:"));
        panelUpdateUser.add(txtCorreoUpdate);
        panelUpdateUser.add(new JLabel("Nuevo Nombre:"));
        panelUpdateUser.add(txtNombreUpdate);
        panelUpdateUser.add(new JLabel("Nueva Dirección:"));
        panelUpdateUser.add(txtDireccionUpdate);
        panelUpdateUser.add(new JLabel("Nueva Contraseña:"));
        panelUpdateUser.add(txtPasswordUpdate);

        JButton btnUpdateUser = new JButton("Modificar Datos");
        panelUpdateUser.add(btnUpdateUser);

        btnUpdateUser.addActionListener(e -> {
            try {
                Usuario userService = new Usuario();
                userService.updateUser(
                        Integer.parseInt(txtIdUserUpdate.getText().trim()),
                        txtCorreoUpdate.getText().trim(),
                        txtNombreUpdate.getText().trim(),
                        txtDireccionUpdate.getText().trim(),
                        txtPasswordUpdate.getText().trim()
                );
                JOptionPane.showMessageDialog(panelUpdateUser,
                        "Datos actualizados con éxito.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelUpdateUser,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        usuariosTabbedPane.addTab("Modificar Datos de Usuario", panelUpdateUser);

        // -----------------------------------------------------------
        // RF1.4: Recuperar Contraseña (Muestra Token Generado)
        // -----------------------------------------------------------
        JPanel panelRecoverPassword = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField txtCorreoRecover = new JTextField();

        panelRecoverPassword.add(new JLabel("Correo:"));
        panelRecoverPassword.add(txtCorreoRecover);

        JButton btnRecoverPassword = new JButton("Recuperar Contraseña");
        panelRecoverPassword.add(btnRecoverPassword);

        btnRecoverPassword.addActionListener(e -> {
            try {
                Usuario userService = new Usuario();
                String token = userService.recoverPassword(txtCorreoRecover.getText().trim());
                JOptionPane.showMessageDialog(panelRecoverPassword,
                        "Token de recuperación generado: " + token);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelRecoverPassword,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        usuariosTabbedPane.addTab("Recuperar Contraseña", panelRecoverPassword);

        /*
            Implementado en pestalla_registro del fichero Diseño.java
             */
        // -----------------------------------------------------------
        // RF1.5: Iniciar Sesión
        // -----------------------------------------------------------

        return usuariosTabbedPane;
    }





    private static JTabbedPane crearPestañasReseñas() {
        // -----------------------------------------------------------
        // PESTAÑA PARA RESEÑAS
        // -----------------------------------------------------------

        JTabbedPane reseñasTabbedPane = new JTabbedPane();

        // Panel para Añadir Reseña
        JPanel panelAddReview = new JPanel(new GridLayout(6, 2, 5, 5));
        JTextField txtIdReseña = new JTextField();
        JTextField txtIdPedido = new JTextField();
        JLabel txtIdUsuario = new JLabel( "Eres el usuario: " + Integer.toString(id_user));
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
                int idUser = id_user;
                int val = Integer.parseInt(txtValoracion.getText().trim());
                String com = txtComentario.getText().trim();

                // Llamada a método addReview
                Reseña reviewService = new Reseña();
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
        JLabel txtIdUsuario_2 = new JLabel( "Eres el usuario: " + Integer.toString(id_user));
        JTextField txtEditValoracion = new JTextField();
        JTextField txtEditComentario = new JTextField();

        panelEditReview.add(new JLabel("ID Reseña:"));
        panelEditReview.add(txtEditIdReseña);
        panelEditReview.add(new JLabel("ID Usuario:"));
        panelEditReview.add(txtIdUsuario_2);
        panelEditReview.add(new JLabel("Nueva Valoración (1-5):"));
        panelEditReview.add(txtEditValoracion);
        panelEditReview.add(new JLabel("Nuevo Comentario:"));
        panelEditReview.add(txtEditComentario);

        JButton btnEditReview = new JButton("Editar Reseña");
        panelEditReview.add(btnEditReview);

        btnEditReview.addActionListener(e -> {
            try {
                int idRes = Integer.parseInt(txtEditIdReseña.getText().trim());
                int idUser = id_user;
                int val = Integer.parseInt(txtEditValoracion.getText().trim());
                String com = txtEditComentario.getText().trim();

                Reseña reviewService = new Reseña();
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
        JLabel txtIdUsuario_3 = new JLabel( "Eres el usuario: " + Integer.toString(id_user)); // Se elimina las reseñas del propio usuario

        panelDeleteReview.add(new JLabel("ID Reseña:"));
        panelDeleteReview.add(txtDeleteIdReseña);
        panelDeleteReview.add(new JLabel("ID Usuario:"));
        panelDeleteReview.add(txtIdUsuario_3);

        JButton btnDeleteReview = new JButton("Eliminar Reseña");
        panelDeleteReview.add(btnDeleteReview);

        btnDeleteReview.addActionListener(e -> {
            try {
                int idRes = Integer.parseInt(txtDeleteIdReseña.getText().trim());
                int idUser = id_user;

                Reseña reviewService = new Reseña();
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
                Reseña reviewService = new Reseña();
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
                Reseña reviewService = new Reseña();
                ArrayList<String> reviews = reviewService.getReviewsByUser(idUser);
                for (String review : reviews) {
                    textAreaUser.append(review + "\n");
                }
            } catch (Exception ex2) {
                JOptionPane.showMessageDialog(panelViewByUser, "Error al obtener reseñas: " + ex2.getMessage());
            }
        });

        reseñasTabbedPane.addTab("Ver Reseñas por Usuario", panelViewByUser);

        return reseñasTabbedPane;
    }

    private static JTabbedPane crearPestañasPedidos(){
        // -----------------------------------------------------------
        // PESTAÑA PARA PEDIDOS
        // -----------------------------------------------------------

        JTabbedPane pedidosTabbedPane = new JTabbedPane();

        //Panel para añadir pedido

        JPanel panelRealizarPedido = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtDireccion = new JTextField();
        JTextField txtTipoPago = new JTextField();
        JTextField txtMetodoEnvio = new JTextField();
        JLabel labelUsuario = new JLabel( "Eres el usuario: " + Integer.toString(id_user));

        panelRealizarPedido.add(labelUsuario);

        panelRealizarPedido.add(new JLabel("Tipo de Pago:"));
        panelRealizarPedido.add(txtTipoPago);

        panelRealizarPedido.add(new JLabel("Método de Envío:"));
        panelRealizarPedido.add(txtMetodoEnvio);

        panelRealizarPedido.add(new JLabel("Dirección:"));
        panelRealizarPedido.add(txtDireccion);

        JButton btnRealizarPedido = new JButton("Añadir Pedido");
        panelRealizarPedido.add(btnRealizarPedido);

        btnRealizarPedido.addActionListener(e -> {
            try {
                String direcc = txtDireccion.getText().trim();
                int tipPag = Integer.parseInt(txtTipoPago.getText().trim());
                String metEnv= txtMetodoEnvio .getText().trim();
                Carrito carrito = new Carrito();
                // Llamada a metodo realizarPedido
                Pedido orderService = new Pedido();
                orderService.realizarPedido(direcc, carrito, tipPag, metEnv, id_user);
                JOptionPane.showMessageDialog(panelRealizarPedido, "Pedido realizado con exito");

            } catch (Exception ex2) {
                JOptionPane.showMessageDialog(panelRealizarPedido, "Error al añadir pedido: " + ex2.getMessage());
            }
        });

        pedidosTabbedPane.addTab("Añadir Pedido", panelRealizarPedido);

        // Panel para Ver Historial de Pedidos
        JPanel panelVerHistorialPedidos = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanelHistorial = new JPanel(new GridLayout(1, 2, 5, 5));
        JLabel idUsuarioHistorial = new JLabel( "Eres el usuario: " + Integer.toString(id_user));
        inputPanelHistorial.add(new JLabel("ID Usuario: " + Integer.toString(id_user)));
        panelVerHistorialPedidos.add(idUsuarioHistorial);


        JButton btnVerHistorial = new JButton("Ver Historial de Pedidos");
        JPanel topPanelHistorial = new JPanel(new FlowLayout());
        topPanelHistorial.add(inputPanelHistorial);
        topPanelHistorial.add(btnVerHistorial);

        JTextArea textAreaHistorial = new JTextArea(10, 40);
        textAreaHistorial.setEditable(false);

        panelVerHistorialPedidos.add(topPanelHistorial, BorderLayout.NORTH);
        panelVerHistorialPedidos.add(new JScrollPane(textAreaHistorial), BorderLayout.CENTER);

        btnVerHistorial.addActionListener(e -> {
            try {
                textAreaHistorial.setText("");
                Pedido pedidoService = new Pedido();
                List<Pedido> pedidos = pedidoService.verHistorialPedidos(id_user);
                for (Pedido pedido : pedidos) {
                    textAreaHistorial.append(pedido.toString() + "\n");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelVerHistorialPedidos, "Error al obtener el historial de pedidos: " + ex.getMessage());
            }
        });

        pedidosTabbedPane.addTab("Ver Historial de Pedidos", panelVerHistorialPedidos);

        // Panel para Cancelar Pedido
        JPanel panelCancelarPedido = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanelCancelar = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField txtIdPedidoCancelar = new JTextField();


        inputPanelCancelar.add(new JLabel("ID Usuario: " + id_user));
        inputPanelCancelar.add(new JLabel(""));
        inputPanelCancelar.add(new JLabel("ID Pedido:"));
        inputPanelCancelar.add(txtIdPedidoCancelar);

        JButton btnCancelarPedido = new JButton("Cancelar Pedido");
        JPanel topPanelCancelar = new JPanel(new FlowLayout());
        topPanelCancelar.add(inputPanelCancelar);
        topPanelCancelar.add(btnCancelarPedido);

        JTextArea textAreaCancelar = new JTextArea(10, 40);
        textAreaCancelar.setEditable(false);

        panelCancelarPedido.add(topPanelCancelar, BorderLayout.NORTH);
        panelCancelarPedido.add(new JScrollPane(textAreaCancelar), BorderLayout.CENTER);

        btnCancelarPedido.addActionListener(e -> {
            try {
                textAreaCancelar.setText("");
                int idPedido = Integer.parseInt(txtIdPedidoCancelar.getText().trim());

                Pedido pedidoService = new Pedido();
                pedidoService.cancelarPedido(idPedido, id_user);
                textAreaCancelar.append("Pedido cancelado con éxito.\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelCancelarPedido, "Error al cancelar el pedido: " + ex.getMessage());
            }
        });

        pedidosTabbedPane.addTab("Cancelar Pedido", panelCancelarPedido);

        // Panel para cambiar  Método de Envío
        JPanel panelCambiarMetodoEnvio = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanelEnvio = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField txtIdPedidoEnvio = new JTextField();
        JTextField txtnuevoMetodoEnvio = new JTextField();

        inputPanelEnvio.setLayout(new GridLayout(3, 2, 5, 5)); // 3 rows, 2 columns, 5px horizontal and vertical gaps

        inputPanelEnvio.add(new JLabel("ID Usuario: " + id_user));
        inputPanelEnvio.add(new JLabel("")); // Empty label to fill the second column
        inputPanelEnvio.add(new JLabel("ID Pedido:"));
        inputPanelEnvio.add(txtIdPedidoEnvio);
        inputPanelEnvio.add(new JLabel("Nuevo Método de Envío:"));
        inputPanelEnvio.add(txtnuevoMetodoEnvio);


        JButton btnCambiarMetodoEnvio = new JButton("Cambiar Método de Envío");
        JPanel topPanelEnvio = new JPanel(new FlowLayout());
        topPanelEnvio.add(inputPanelEnvio);
        topPanelEnvio.add(btnCambiarMetodoEnvio);

        JTextArea textAreaEnvio = new JTextArea(10, 40);
        textAreaEnvio.setEditable(false);

        panelCambiarMetodoEnvio.add(topPanelEnvio, BorderLayout.NORTH);
        panelCambiarMetodoEnvio.add(new JScrollPane(textAreaEnvio), BorderLayout.CENTER);

        btnCambiarMetodoEnvio.addActionListener(e -> {
            try {
                textAreaEnvio.setText("");
                int idPedido = Integer.parseInt(txtIdPedidoEnvio.getText().trim());
                String metodoEnvio = txtnuevoMetodoEnvio.getText().trim();

                // Validar que el método de envío sea uno de los permitidos
                String[] validStates = {"express", "normal", "frágil"};
                if (!Arrays.asList(validStates).contains(metodoEnvio)) {
                    throw new IllegalArgumentException("El método del envío no es válido.");
                }

                Pedido pedidoService = new Pedido();
                pedidoService.elegirMetodoEnvio(metodoEnvio, id_user, idPedido);
                textAreaEnvio.append("Método de envío elegido con éxito.\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelCambiarMetodoEnvio, "Error al elegir el método de envío: " + ex.getMessage());
            }
        });

        pedidosTabbedPane.addTab("Cambiar Método de Envío", panelCambiarMetodoEnvio);

// Panel para Confirmar Recepción del Pedido
        JPanel panelConfirmarRecepcion = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanelRecepcion = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField txtIdPedidoRecepcion = new JTextField();

        inputPanelRecepcion.add(new JLabel("ID Usuario:" + id_user));
        inputPanelRecepcion.add(new JLabel(""));
        inputPanelRecepcion.add(new JLabel("ID Pedido:"));
        inputPanelRecepcion.add(txtIdPedidoRecepcion);


        JButton btnConfirmarRecepcion = new JButton("Confirmar Recepción");
        JPanel topPanelRecepcion = new JPanel(new FlowLayout());
        topPanelRecepcion.add(inputPanelRecepcion);
        topPanelRecepcion.add(btnConfirmarRecepcion);

        JTextArea textAreaRecepcion = new JTextArea(10, 40);
        textAreaRecepcion.setEditable(false);

        panelConfirmarRecepcion.add(topPanelRecepcion, BorderLayout.NORTH);
        panelConfirmarRecepcion.add(new JScrollPane(textAreaRecepcion), BorderLayout.CENTER);

        btnConfirmarRecepcion.addActionListener(e -> {
            try {
                textAreaRecepcion.setText("");
                int idPedido = Integer.parseInt(txtIdPedidoRecepcion.getText().trim());

                Pedido pedidoService = new Pedido();
                pedidoService.confirmarRecepcionPedido(id_user, idPedido);
                textAreaRecepcion.append("Recepción del pedido confirmada con éxito.\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelConfirmarRecepcion, "Error al confirmar la recepción del pedido: " + ex.getMessage());
            }
        });

        pedidosTabbedPane.addTab("Confirmar Recepción", panelConfirmarRecepcion);

        // Panel para Cambiar el metodo de pago
        JPanel panelCambiarMetodoPago = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanelPago = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField txtIdPedidoPago = new JTextField();
        JTextField txtIdMetodoPago = new JTextField();
        JTextField txtTipoMetodoPago = new JTextField();

        inputPanelPago.add(new JLabel("ID Usuario: " + id_user));
        inputPanelPago.add(new JLabel("")); // Empty label to fill the second column
        inputPanelPago.add(new JLabel("ID Pedido:"));
        inputPanelPago.add(txtIdPedidoPago);
        inputPanelPago.add(new JLabel("ID Método de Pago:"));
        inputPanelPago.add(txtIdMetodoPago);
        inputPanelPago.add(new JLabel("Tipo de Método de Pago:"));
        inputPanelPago.add(txtTipoMetodoPago);

        JButton btnElegirMetodoPago = new JButton("Cambiar Método de Pago");
        JPanel topPanelPago = new JPanel(new FlowLayout());
        topPanelPago.add(inputPanelPago);
        topPanelPago.add(btnElegirMetodoPago);

        JTextArea textAreaPago = new JTextArea(10, 40);
        textAreaPago.setEditable(false);

        panelCambiarMetodoPago.add(topPanelPago, BorderLayout.NORTH);
        panelCambiarMetodoPago.add(new JScrollPane(textAreaPago), BorderLayout.CENTER);

        btnElegirMetodoPago.addActionListener(e -> {
            try {
                textAreaPago.setText("");
                int idPedido = Integer.parseInt(txtIdPedidoPago.getText().trim());
                int idMetodoPago = Integer.parseInt(txtIdMetodoPago.getText().trim());
                String tipoMetodoPago = txtTipoMetodoPago.getText().trim();

                Pedido pedidoService = new Pedido();
                pedidoService.elegirMetodoPago(idMetodoPago, tipoMetodoPago, idPedido, id_user);
                textAreaPago.append("Método de pago elegido con éxito.\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelCambiarMetodoPago, "Error al cambiar el método de pago: " + ex.getMessage());
            }
        });

        pedidosTabbedPane.addTab("Cambiar Método de Pago", panelCambiarMetodoPago);
        return pedidosTabbedPane;
    }

    private static JTabbedPane crearPestañasPago(){
        JTabbedPane pagosTabbedPane = new JTabbedPane();
        // -----------------------------------------------------------
        // RF6.1 Agregar Método de Pago
        // -----------------------------------------------------------
        JPanel panelAgregarMetodo = new JPanel(new GridLayout(7, 2, 5, 5));
        JTextField txtTipoPago = new JTextField();
        JTextField txtNumeroTarjeta = new JTextField();
        JTextField txtFechaExpiracion = new JTextField();
        JTextField txtCodigoCVV = new JTextField();
        JTextField txtNombreTitular = new JTextField();
        JTextField txtCorreoPayPal = new JTextField();

        panelAgregarMetodo.add(new JLabel("Tipo de Método de Pago:"));
        panelAgregarMetodo.add(txtTipoPago);
        panelAgregarMetodo.add(new JLabel("Número de Tarjeta:"));
        panelAgregarMetodo.add(txtNumeroTarjeta);
        panelAgregarMetodo.add(new JLabel("Fecha de Expiración (MM/YY):"));
        panelAgregarMetodo.add(txtFechaExpiracion);
        panelAgregarMetodo.add(new JLabel("Código CVV:"));
        panelAgregarMetodo.add(txtCodigoCVV);
        panelAgregarMetodo.add(new JLabel("Nombre del Titular:"));
        panelAgregarMetodo.add(txtNombreTitular);
        panelAgregarMetodo.add(new JLabel("Correo PayPal:"));
        panelAgregarMetodo.add(txtCorreoPayPal);

        JButton btnAgregarMetodo = new JButton("Agregar Método de Pago");
        panelAgregarMetodo.add(btnAgregarMetodo);

        btnAgregarMetodo.addActionListener(e -> {
            try {
                String tipoPago = txtTipoPago.getText().trim();
                String numeroTarjeta = txtNumeroTarjeta.getText().trim();
                String fechaExpiracion = txtFechaExpiracion.getText().trim();
                String codigoCVV = txtCodigoCVV.getText().trim();
                String nombreTitular = txtNombreTitular.getText().trim();
                String correoPayPal = txtCorreoPayPal.getText().trim();

                Pago pagoService = new Pago();
                pagoService.agregarMetodoPago(id_user, tipoPago, numeroTarjeta, fechaExpiracion, codigoCVV, nombreTitular, correoPayPal);
                JOptionPane.showMessageDialog(panelAgregarMetodo, "Método de pago agregado con éxito. ID: \n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelAgregarMetodo, "Error al agregar método de pago: " + ex.getMessage());
            }
        });

        pagosTabbedPane.addTab("Agregar Método de Pago", panelAgregarMetodo);

        // -----------------------------------------------------------
        // RF6.2 Eliminar Método de Pago
        // -----------------------------------------------------------
        JPanel panelEliminarMetodo = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField txtIdMetodoEliminar = new JTextField();

        panelEliminarMetodo.add(new JLabel("ID Método de Pago:"));
        panelEliminarMetodo.add(txtIdMetodoEliminar);

        JButton btnEliminarMetodo = new JButton("Eliminar Método de Pago");
        panelEliminarMetodo.add(btnEliminarMetodo);

        btnEliminarMetodo.addActionListener(e -> {
            try {
                int idMetodo = Integer.parseInt(txtIdMetodoEliminar.getText().trim());

                Pago pagoService = new Pago();
                pagoService.eliminarMetodoPago(id_user, idMetodo);
                JOptionPane.showMessageDialog(panelEliminarMetodo, "Método de pago eliminado con éxito\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelEliminarMetodo, "Error al eliminar método de pago: " + ex.getMessage());
            }
        });

        pagosTabbedPane.addTab("Eliminar Método de Pago", panelEliminarMetodo);

        // -----------------------------------------------------------
        // RF6.3 Ver Métodos de Pago
        // -----------------------------------------------------------
        JPanel panelVerMetodos = new JPanel(new BorderLayout(5, 5));
        JTextArea textAreaMetodos = new JTextArea(10, 40);
        textAreaMetodos.setEditable(false);
        JButton btnVerMetodos = new JButton("Ver Métodos de Pago");

        panelVerMetodos.add(new JScrollPane(textAreaMetodos), BorderLayout.CENTER);
        panelVerMetodos.add(btnVerMetodos, BorderLayout.SOUTH);

        btnVerMetodos.addActionListener(e -> {
            try {
                Pago pagoService = new Pago();
                List<Pago.MetodoPago> metodos = pagoService.verMetodosPago(id_user);

                textAreaMetodos.setText("");
                for (Pago.MetodoPago metodo : metodos) {
                    textAreaMetodos.append(metodo.toString() + "\n");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelVerMetodos, "Error al ver métodos de pago: " + ex.getMessage());
            }
        });

        pagosTabbedPane.addTab("Ver Métodos de Pago", panelVerMetodos);

        // -----------------------------------------------------------
        // RF6.4 Realizar Pago
        // -----------------------------------------------------------
        JPanel panelRealizarPago = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField txtIdPedidoPago = new JTextField();
        JTextField txtIdMetodoPago = new JTextField();
        JTextField txtCantidadPago = new JTextField();

        panelRealizarPago.add(new JLabel("ID Pedido:"));
        panelRealizarPago.add(txtIdPedidoPago);
        panelRealizarPago.add(new JLabel("ID Método de Pago:"));
        panelRealizarPago.add(txtIdMetodoPago);
        panelRealizarPago.add(new JLabel("Cantidad a Pagar:"));
        panelRealizarPago.add(txtCantidadPago);

        JButton btnRealizarPago = new JButton("Realizar Pago");
        panelRealizarPago.add(btnRealizarPago);

        btnRealizarPago.addActionListener(e -> {
            try {
                int idPedido = Integer.parseInt(txtIdPedidoPago.getText().trim());
                int idMetodo = Integer.parseInt(txtIdMetodoPago.getText().trim());
                double cantidad = Double.parseDouble(txtCantidadPago.getText().trim());

                Pago pagoService = new Pago();
                pagoService.realizarPago(idPedido, idMetodo, cantidad, id_user);
                JOptionPane.showMessageDialog(panelRealizarPago, "Pago realizado con éxito \n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelRealizarPago, "Error al realizar el pago: " + ex.getMessage());
            }
        });

        pagosTabbedPane.addTab("Realizar Pago", panelRealizarPago);

        // -----------------------------------------------------------
        // RF6.5 Ver Historial de Transacciones
        // -----------------------------------------------------------
        JPanel panelHistorialTransacciones = new JPanel(new BorderLayout(5, 5));
        JTextArea textAreaHistorial = new JTextArea(10, 40);
        textAreaHistorial.setEditable(false);
        JButton btnVerHistorial = new JButton("Ver Historial de Transacciones");

        panelHistorialTransacciones.add(new JScrollPane(textAreaHistorial), BorderLayout.CENTER);
        panelHistorialTransacciones.add(btnVerHistorial, BorderLayout.SOUTH);

        btnVerHistorial.addActionListener(e -> {
            try {
                Pago pagoService = new Pago();
                List<Pago.Transaccion> transacciones = pagoService.verHistorialTransacciones(id_user);

                textAreaHistorial.setText("");
                for (Pago.Transaccion transaccion : transacciones) {
                    textAreaHistorial.append(transaccion.toString() + "\n");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelHistorialTransacciones, "Error al ver historial: " + ex.getMessage());
            }
        });

        pagosTabbedPane.addTab("Ver Historial de Transacciones", panelHistorialTransacciones);

        return pagosTabbedPane;
    }







}