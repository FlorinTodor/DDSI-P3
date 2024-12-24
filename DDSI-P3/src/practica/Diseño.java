package practica;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
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
      //  tabbedPane.addTab("Pagos", usuariosTabbedPane);



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
        JPanel panelAddToCart = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField txtIdUsuarioAdd = new JTextField();
        JTextField txtIdProductoAdd = new JTextField();
        JTextField txtCantidadAdd = new JTextField();

        panelAddToCart.add(new JLabel("ID Usuario:"));
        panelAddToCart.add(txtIdUsuarioAdd);

        panelAddToCart.add(new JLabel("ID Producto:"));
        panelAddToCart.add(txtIdProductoAdd);

        panelAddToCart.add(new JLabel("Cantidad:"));
        panelAddToCart.add(txtCantidadAdd);

        JButton btnAddToCart = new JButton("Añadir al Carrito");
        panelAddToCart.add(btnAddToCart);

        btnAddToCart.addActionListener(e -> {
            try {
                int idUsuario = Integer.parseInt(txtIdUsuarioAdd.getText().trim());
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

        inputPanelView.add(new JLabel("ID Usuario:"));
        inputPanelView.add(txtIdUsuarioView);

        JButton btnViewCart = new JButton("Ver Carrito");
        JPanel topPanelView = new JPanel(new FlowLayout());
        topPanelView.add(inputPanelView);
        topPanelView.add(btnViewCart);

        JTextArea textAreaViewCart = new JTextArea(10, 40);
        textAreaViewCart.setEditable(false);

        panelViewCart.add(topPanelView, BorderLayout.NORTH);
        panelViewCart.add(new JScrollPane(textAreaViewCart), BorderLayout.CENTER);

        btnViewCart.addActionListener(e -> {
            try {
                textAreaViewCart.setText("");
                int idUsuario = Integer.parseInt(txtIdUsuarioView.getText().trim());

                Carrito carritoService = new Carrito();
                ArrayList<String> productos = carritoService.viewCart(idUsuario);

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
        JPanel panelModifyQuantity = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField txtIdUsuarioModify = new JTextField();
        JTextField txtIdProductoModify = new JTextField();
        JTextField txtNuevaCantidad = new JTextField();

        panelModifyQuantity.add(new JLabel("ID Usuario:"));
        panelModifyQuantity.add(txtIdUsuarioModify);

        panelModifyQuantity.add(new JLabel("ID Producto:"));
        panelModifyQuantity.add(txtIdProductoModify);

        panelModifyQuantity.add(new JLabel("Nueva Cantidad:"));
        panelModifyQuantity.add(txtNuevaCantidad);

        JButton btnModifyQuantity = new JButton("Modificar Cantidad");
        panelModifyQuantity.add(btnModifyQuantity);

        btnModifyQuantity.addActionListener(e -> {
            try {
                int idUsuario = Integer.parseInt(txtIdUsuarioModify.getText().trim());
                int idProducto = Integer.parseInt(txtIdProductoModify.getText().trim());
                int cantidad = Integer.parseInt(txtNuevaCantidad.getText().trim());

                Carrito carritoService = new Carrito();
                carritoService.modifyCartQuantity(idUsuario, idProducto, cantidad);
                JOptionPane.showMessageDialog(panelModifyQuantity, "Cantidad modificada con éxito.");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelModifyQuantity, "Error al modificar cantidad: " + ex.getMessage());
            }
        });

        carritoTabbedPane.addTab("Modificar Cantidad", panelModifyQuantity);

        // -----------------------------------------------------------
        // RF3.4: Eliminar producto del carrito
        // -----------------------------------------------------------
        JPanel panelRemoveProduct = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField txtIdUsuarioRemove = new JTextField();
        JTextField txtIdProductoRemove = new JTextField();

        panelRemoveProduct.add(new JLabel("ID Usuario:"));
        panelRemoveProduct.add(txtIdUsuarioRemove);

        panelRemoveProduct.add(new JLabel("ID Producto:"));
        panelRemoveProduct.add(txtIdProductoRemove);

        JButton btnRemoveProduct = new JButton("Eliminar Producto del Carrito");
        panelRemoveProduct.add(btnRemoveProduct);

        btnRemoveProduct.addActionListener(e -> {
            try {
                int idUsuario = Integer.parseInt(txtIdUsuarioRemove.getText().trim());
                int idProducto = Integer.parseInt(txtIdProductoRemove.getText().trim());

                Carrito carritoService = new Carrito();
                carritoService.removeProductFromCart(idUsuario, idProducto);
                JOptionPane.showMessageDialog(panelRemoveProduct, "Producto eliminado del carrito con éxito.");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelRemoveProduct, "Error al eliminar producto: " + ex.getMessage());
            }
        });

        carritoTabbedPane.addTab("Eliminar Producto del Carrito", panelRemoveProduct);

        // -----------------------------------------------------------
        // RF3.5: Vaciar carrito de compras
        // -----------------------------------------------------------
        JPanel panelEmptyCart = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField txtIdUsuarioEmpty = new JTextField();

        panelEmptyCart.add(new JLabel("ID Usuario:"));
        panelEmptyCart.add(txtIdUsuarioEmpty);

        JButton btnEmptyCart = new JButton("Vaciar Carrito");
        panelEmptyCart.add(btnEmptyCart);

        btnEmptyCart.addActionListener(e -> {
            try {
                int idUsuario = Integer.parseInt(txtIdUsuarioEmpty.getText().trim());

                Carrito carritoService = new Carrito();
                carritoService.emptyCart(idUsuario);
                JOptionPane.showMessageDialog(panelEmptyCart, "Carrito vaciado con éxito.");

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
        panelEditProduct.add(new JLabel("ID Usuario:"));
        panelEditProduct.add(txtEditIdUsuarioProducto);

        JButton btnEditProduct = new JButton("Editar Producto");
        panelEditProduct.add(btnEditProduct);

        btnEditProduct.addActionListener(e -> {
            try {
                int idProducto = Integer.parseInt(txtEditIdProducto.getText().trim());
                String nombre = txtEditNombreProducto.getText().trim();
                double precio = Double.parseDouble(txtEditPrecioProducto.getText().trim());
                int cantidad = Integer.parseInt(txtEditCantidadProducto.getText().trim());
                int idUsuario = Integer.parseInt(txtEditIdUsuarioProducto.getText().trim());

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
        JTextField txtDeleteIdUsuarioProducto = new JTextField();

        panelDeleteProduct.add(new JLabel("ID Producto:"));
        panelDeleteProduct.add(txtDeleteIdProducto);
        panelDeleteProduct.add(new JLabel("ID Usuario:"));
        panelDeleteProduct.add(txtDeleteIdUsuarioProducto);

        JButton btnDeleteProduct = new JButton("Eliminar Producto");
        panelDeleteProduct.add(btnDeleteProduct);

        btnDeleteProduct.addActionListener(e -> {
            try {
                int idProducto = Integer.parseInt(txtDeleteIdProducto.getText().trim());
                int idUsuario = Integer.parseInt(txtDeleteIdUsuarioProducto.getText().trim());

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
        JTextField txtIdPedido = new JTextField();
        JTextField txtEstadoPedido = new JTextField();
        JTextField txtTipoPago = new JTextField();
        JTextField txtMetodoEnvio = new JTextField();
        JTextField txtIdUsuario = new JTextField();
        JTextField txtCarrito = new JTextField();

        panelRealizarPedido.add(new JLabel("Dirección:"));
        panelRealizarPedido.add(txtDireccion);

        panelRealizarPedido.add(new JLabel("ID Pedido:"));
        panelRealizarPedido.add(txtIdPedido);

        panelRealizarPedido.add(new JLabel("Estado Pedido:"));
        panelRealizarPedido.add(txtEstadoPedido);

        panelRealizarPedido.add(new JLabel("Tipo de Pago:"));
        panelRealizarPedido.add(txtTipoPago);

        panelRealizarPedido.add(new JLabel("Método de Envío:"));
        panelRealizarPedido.add(txtMetodoEnvio);

        panelRealizarPedido.add(new JLabel("ID Usuario:"));
        panelRealizarPedido.add(txtIdUsuario);

        panelRealizarPedido.add(new JLabel("Carrito (IDs de productos separados por comas):"));
        panelRealizarPedido.add(txtCarrito);

        JButton btnRealizarPedido = new JButton("Añadir Pedido");
        panelRealizarPedido.add(btnRealizarPedido);

        btnRealizarPedido.addActionListener(e -> {
            try {
                String direcc = txtDireccion.getText().trim();
                int idPed = Integer.parseInt(txtIdPedido.getText().trim());
                String estPed = txtEstadoPedido .getText().trim();
                int tipPag = Integer.parseInt(txtTipoPago.getText().trim());
                String metEnv= txtMetodoEnvio .getText().trim();
                String[] carritoArray = txtCarrito.getText().trim().split(",");
                List<Integer> carrito = new ArrayList<>();
                for (String idProducto : carritoArray) {
                    carrito.add(Integer.parseInt(idProducto.trim()));
                }
                int idUser = Integer.parseInt(txtIdUsuario.getText().trim());

                // Llamada a metodo realizarPedido
                Pedido orderService = new Pedido(idPed,carrito,estPed,idUser);
                orderService.realizarPedido(direcc,idPed,estPed,tipPag,metEnv,idUser,carrito);
                JOptionPane.showMessageDialog(panelRealizarPedido, "Pedido realizado con exito");

            } catch (Exception ex2) {
                JOptionPane.showMessageDialog(panelRealizarPedido, "Error al añadir pedido: " + ex2.getMessage());
            }
        });

        pedidosTabbedPane.addTab("Añadir Pedido", panelRealizarPedido);

        // Panel para Ver Historial de Pedidos
        JPanel panelVerHistorialPedidos = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanelHistorial = new JPanel(new GridLayout(1, 2, 5, 5));
        JTextField txtIdUsuarioHistorial = new JTextField();
        inputPanelHistorial.add(new JLabel("ID Usuario:"));
        inputPanelHistorial.add(txtIdUsuarioHistorial);

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
                int idUsuario = Integer.parseInt(txtIdUsuarioHistorial.getText().trim());
                Pedido pedidoService = new Pedido();
                List<Pedido> pedidos = pedidoService.verHistorialPedidos(idUsuario);
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
        JTextField txtIdUsuarioCancelar = new JTextField();

        inputPanelCancelar.add(new JLabel("ID Pedido:"));
        inputPanelCancelar.add(txtIdPedidoCancelar);
        inputPanelCancelar.add(new JLabel("ID Usuario:"));
        inputPanelCancelar.add(txtIdUsuarioCancelar);

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
                int idUsuario = Integer.parseInt(txtIdUsuarioCancelar.getText().trim());

                Pedido pedidoService = new Pedido();
                pedidoService.cancelarPedido(idPedido, idUsuario);
                textAreaCancelar.append("Pedido cancelado con éxito.\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelCancelarPedido, "Error al cancelar el pedido: " + ex.getMessage());
            }
        });

        pedidosTabbedPane.addTab("Cancelar Pedido", panelCancelarPedido);

        // Panel para Elegir Método de Envío
        JPanel panelElegirMetodoEnvio = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanelEnvio = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField txtIdPedidoEnvio = new JTextField();
        JTextField txtIdUsuarioEnvio = new JTextField();
        JTextField txtnuevoMetodoEnvio = new JTextField();

        inputPanelEnvio.add(new JLabel("ID Pedido:"));
        inputPanelEnvio.add(txtIdPedidoEnvio);
        inputPanelEnvio.add(new JLabel("ID Usuario:"));
        inputPanelEnvio.add(txtIdUsuarioEnvio);
        inputPanelEnvio.add(new JLabel("Método de Envío:"));
        inputPanelEnvio.add(txtnuevoMetodoEnvio);

        JButton btnElegirMetodoEnvio = new JButton("Elegir Método de Envío");
        JPanel topPanelEnvio = new JPanel(new FlowLayout());
        topPanelEnvio.add(inputPanelEnvio);
        topPanelEnvio.add(btnElegirMetodoEnvio);

        JTextArea textAreaEnvio = new JTextArea(10, 40);
        textAreaEnvio.setEditable(false);

        panelElegirMetodoEnvio.add(topPanelEnvio, BorderLayout.NORTH);
        panelElegirMetodoEnvio.add(new JScrollPane(textAreaEnvio), BorderLayout.CENTER);

        btnElegirMetodoEnvio.addActionListener(e -> {
            try {
                textAreaEnvio.setText("");
                int idPedido = Integer.parseInt(txtIdPedidoEnvio.getText().trim());
                int idUsuario = Integer.parseInt(txtIdUsuarioEnvio.getText().trim());
                String metodoEnvio = txtMetodoEnvio.getText().trim();

                Pedido pedidoService = new Pedido();
                pedidoService.elegirMetodoEnvio(metodoEnvio, idUsuario, idPedido);
                textAreaEnvio.append("Método de envío elegido con éxito.\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelElegirMetodoEnvio, "Error al elegir el método de envío: " + ex.getMessage());
            }
        });

        pedidosTabbedPane.addTab("Elegir Método de Envío", panelElegirMetodoEnvio);

// Panel para Confirmar Recepción del Pedido
        JPanel panelConfirmarRecepcion = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanelRecepcion = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField txtIdPedidoRecepcion = new JTextField();
        JTextField txtIdUsuarioRecepcion = new JTextField();

        inputPanelRecepcion.add(new JLabel("ID Pedido:"));
        inputPanelRecepcion.add(txtIdPedidoRecepcion);
        inputPanelRecepcion.add(new JLabel("ID Usuario:"));
        inputPanelRecepcion.add(txtIdUsuarioRecepcion);

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
                int idUsuario = Integer.parseInt(txtIdUsuarioRecepcion.getText().trim());

                Pedido pedidoService = new Pedido();
                pedidoService.confirmarRecepcionPedido(idUsuario, idPedido);
                textAreaRecepcion.append("Recepción del pedido confirmada con éxito.\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelConfirmarRecepcion, "Error al confirmar la recepción del pedido: " + ex.getMessage());
            }
        });

        pedidosTabbedPane.addTab("Confirmar Recepción", panelConfirmarRecepcion);

        // Panel para Elegir Método de Pago
        JPanel panelElegirMetodoPago = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanelPago = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField txtIdPedidoPago = new JTextField();
        JTextField txtIdUsuarioPago = new JTextField();
        JTextField txtIdMetodoPago = new JTextField();
        JTextField txtTipoMetodoPago = new JTextField();

        inputPanelPago.add(new JLabel("ID Pedido:"));
        inputPanelPago.add(txtIdPedidoPago);
        inputPanelPago.add(new JLabel("ID Usuario:"));
        inputPanelPago.add(txtIdUsuarioPago);
        inputPanelPago.add(new JLabel("ID Método de Pago:"));
        inputPanelPago.add(txtIdMetodoPago);
        inputPanelPago.add(new JLabel("Tipo de Método de Pago:"));
        inputPanelPago.add(txtTipoMetodoPago);

        JButton btnElegirMetodoPago = new JButton("Elegir Método de Pago");
        JPanel topPanelPago = new JPanel(new FlowLayout());
        topPanelPago.add(inputPanelPago);
        topPanelPago.add(btnElegirMetodoPago);

        JTextArea textAreaPago = new JTextArea(10, 40);
        textAreaPago.setEditable(false);

        panelElegirMetodoPago.add(topPanelPago, BorderLayout.NORTH);
        panelElegirMetodoPago.add(new JScrollPane(textAreaPago), BorderLayout.CENTER);

        btnElegirMetodoPago.addActionListener(e -> {
            try {
                textAreaPago.setText("");
                int idPedido = Integer.parseInt(txtIdPedidoPago.getText().trim());
                int idUsuario = Integer.parseInt(txtIdUsuarioPago.getText().trim());
                int idMetodoPago = Integer.parseInt(txtIdMetodoPago.getText().trim());
                String tipoMetodoPago = txtTipoMetodoPago.getText().trim();

                Pedido pedidoService = new Pedido();
                pedidoService.elegirMetodoPago(idMetodoPago, tipoMetodoPago, idPedido, idUsuario);
                textAreaPago.append("Método de pago elegido con éxito.\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelElegirMetodoPago, "Error al elegir el método de pago: " + ex.getMessage());
            }
        });

        pedidosTabbedPane.addTab("Elegir Método de Pago", panelElegirMetodoPago);
        return pedidosTabbedPane;
    }








}