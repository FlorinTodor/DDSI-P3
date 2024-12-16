package practica;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class Diseño {

    private static JTextArea textArea;
    private Reseña reviewService = new Reseña(); // Instancia de la clase reseña con los métodos JDBC


    public static void pantalla_registro(JFrame frame) {
        /**
         * COdigo de registro/Iniciar sesión
         * Cuando se inice sesion o se registre, tenemos que guardar como variable global el id_usuario
         * ESto implica modificar las interfaces para no pedir el id_usuario ya que se debería de recoger de forma interna
          */

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
        mostrarTablasButton.addActionListener(e -> mostrarTablas());
        panelMostrarTablas.add(mostrarTablasButton, BorderLayout.NORTH);
        panelMostrarTablas.add(scrollPane, BorderLayout.CENTER);

        // Crear pestañas específicas
        JTabbedPane reseñasTabbedPane = crearPestañasReseñas();
        JTabbedPane productosTabbedPane = crearPestañasProductos();
        JTabbedPane CarritoTabbedPane = crearPestañasCarrito();
        JTabbedPane usuariosTabbedPane = crearPestañasUsuarios();








        // -----------------------------------------------------------
        // Pestañas principales
        // -----------------------------------------------------------
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Mostrar Tablas", panelMostrarTablas);
        tabbedPane.addTab("Reseñas", reseñasTabbedPane);
        tabbedPane.addTab("Productos", productosTabbedPane);
        tabbedPane.addTab("Carrito", CarritoTabbedPane);
        tabbedPane.addTab("Usuarios", usuariosTabbedPane);


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
                    carritoService.addProductToCart(idUsuario, idProducto, cantidad);
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
        panelAddProduct.add(new JLabel("ID Usuario:"));
        panelAddProduct.add(txtIdUsuarioProducto);

        JButton btnAddProduct = new JButton("Añadir Producto");
        panelAddProduct.add(btnAddProduct);

        btnAddProduct.addActionListener(e -> {
            try {
                int idProducto = Integer.parseInt(txtIdProducto.getText().trim());
                String nombre = txtNombreProducto.getText().trim();
                double precio = Double.parseDouble(txtPrecioProducto.getText().trim());
                int cantidad = Integer.parseInt(txtCantidadProducto.getText().trim());
                int idUsuario = Integer.parseInt(txtIdUsuarioProducto.getText().trim());

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

        //PESTAÑAS PARA USUARIO
        JTabbedPane usuariosTabbedPane = new JTabbedPane();

        // -----------------------------------------------------------
        // RF2.1: Registrar Usuario
        // -----------------------------------------------------------
        JPanel panelRegisterUser = new JPanel(new GridLayout(8, 2, 5, 5));
        JTextField txtIdUser = new JTextField();
        JTextField txtCorreo = new JTextField();
        JTextField txtNombre = new JTextField();
        JTextField txtTelefono = new JTextField();
        JTextField txtDireccion = new JTextField();
        JTextField txtContraseña = new JTextField();

        panelRegisterUser.add(new JLabel("ID Usuario:"));
        panelRegisterUser.add(txtIdUser);
        panelRegisterUser.add(new JLabel("Correo:"));
        panelRegisterUser.add(txtCorreo);
        panelRegisterUser.add(new JLabel("Nombre:"));
        panelRegisterUser.add(txtNombre);
        panelRegisterUser.add(new JLabel("Teléfono:"));
        panelRegisterUser.add(txtTelefono);
        panelRegisterUser.add(new JLabel("Dirección:"));
        panelRegisterUser.add(txtDireccion);
        panelRegisterUser.add(new JLabel("Contraseña:"));
        panelRegisterUser.add(txtContraseña);

        JButton btnRegister = new JButton("Registrar Usuario");
        panelRegisterUser.add(btnRegister);

        btnRegister.addActionListener(e -> {
            try {
                Usuario userService = new Usuario();
                userService.registerUser(
                        Integer.parseInt(txtIdUser.getText()),
                        txtCorreo.getText(),
                        txtNombre.getText(),
                        txtTelefono.getText(),
                        "Activo",
                        txtDireccion.getText(),
                        txtContraseña.getText()
                );
                JOptionPane.showMessageDialog(panelRegisterUser, "Usuario registrado con éxito.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelRegisterUser, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        usuariosTabbedPane.addTab("Registrar Usuario", panelRegisterUser);

        // -----------------------------------------------------------
        // RF2.2: Dar de Baja Usuario
        // -----------------------------------------------------------
        JPanel panelDeleteUser = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField txtIdUserDelete = new JTextField();

        panelDeleteUser.add(new JLabel("ID Usuario:"));
        panelDeleteUser.add(txtIdUserDelete);

        JButton btnDeleteUser = new JButton("Dar de Baja");
        panelDeleteUser.add(btnDeleteUser);

        btnDeleteUser.addActionListener(e -> {
            try {
                Usuario userService = new Usuario();
                userService.deleteUser(Integer.parseInt(txtIdUserDelete.getText()));
                JOptionPane.showMessageDialog(panelDeleteUser, "Usuario dado de baja con éxito.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelDeleteUser, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        usuariosTabbedPane.addTab("Dar de Baja Usuario", panelDeleteUser);

        // -----------------------------------------------------------
        // RF2.3: Modificar Datos de Usuario
        // -----------------------------------------------------------
        JPanel panelUpdateUser = new JPanel(new GridLayout(6, 2, 5, 5));
        JTextField txtIdUserUpdate = new JTextField();
        JTextField txtCorreoUpdate = new JTextField();
        JTextField txtNombreUpdate = new JTextField();
        JTextField txtTelefonoUpdate = new JTextField();
        JTextField txtDireccionUpdate = new JTextField();

        panelUpdateUser.add(new JLabel("ID Usuario:"));
        panelUpdateUser.add(txtIdUserUpdate);
        panelUpdateUser.add(new JLabel("Nuevo Correo:"));
        panelUpdateUser.add(txtCorreoUpdate);
        panelUpdateUser.add(new JLabel("Nuevo Nombre:"));
        panelUpdateUser.add(txtNombreUpdate);
        panelUpdateUser.add(new JLabel("Nuevo Teléfono:"));
        panelUpdateUser.add(txtTelefonoUpdate);
        panelUpdateUser.add(new JLabel("Nueva Dirección:"));
        panelUpdateUser.add(txtDireccionUpdate);

        JButton btnUpdateUser = new JButton("Modificar Datos");
        panelUpdateUser.add(btnUpdateUser);

        btnUpdateUser.addActionListener(e -> {
            try {
                Usuario userService = new Usuario();
                userService.updateUser(
                        Integer.parseInt(txtIdUserUpdate.getText()),
                        txtCorreoUpdate.getText(),
                        txtNombreUpdate.getText(),
                        txtTelefonoUpdate.getText(),
                        txtDireccionUpdate.getText()
                );
                JOptionPane.showMessageDialog(panelUpdateUser, "Datos actualizados con éxito.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panelUpdateUser, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        usuariosTabbedPane.addTab("Modificar Datos de Usuario", panelUpdateUser);

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




    private static void eliminarDatos_tabla() {
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



    private static void insertarDatosPrueba_tabla(){
        try (Statement stmt = Connection.connection.createStatement()) {
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

            Connection.connection.commit();
            JOptionPane.showMessageDialog(Connection.frame, "Datos de prueba insertados correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(Connection.frame, "Error al insertar datos de prueba: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private static void borraryCrearTablas() {
        try (Statement stmt = Connection.connection.createStatement()) {
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

            Connection.connection.commit();
            JOptionPane.showMessageDialog(Connection.frame, "Tablas creadas correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(Connection.frame, "Error al crear las tablas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }





}