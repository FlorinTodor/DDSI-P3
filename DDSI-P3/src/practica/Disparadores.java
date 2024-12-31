package practica;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Disparadores {

    // 1) TRIGGER TRIG_VerificaPedidoYUsuario FLorin
    private static final String TRIG_VERIFICA_PEDIDO_USUARIO =
            "CREATE OR REPLACE TRIGGER TRIG_VerificaPedidoYUsuario "
                    + "BEFORE INSERT OR UPDATE ON Gestion_Reseña "
                    + "FOR EACH ROW "
                    + "DECLARE "
                    + "   v_contador NUMBER; "
                    + "BEGIN "
                    + "   SELECT COUNT(*) INTO v_contador "
                    + "     FROM pedido "
                    + "    WHERE ID_Pedido = :NEW.ID_Pedido; "
                    + "   IF v_contador = 0 THEN "
                    + "      RAISE_APPLICATION_ERROR(-20000, "
                    + "         'Error RS5.1.1: El ID_Pedido ' || :NEW.ID_Pedido || ' no existe en la tabla Pedido.' "
                    + "      ); "
                    + "   END IF; "
                    + "   SELECT COUNT(*) INTO v_contador "
                    + "     FROM pedido p "
                    + "          JOIN usuario u ON p.ID_Usuario = u.ID_Usuario "
                    + "    WHERE p.ID_Pedido = :NEW.ID_Pedido; "
                    + "   IF v_contador = 0 THEN "
                    + "      RAISE_APPLICATION_ERROR(-20001, "
                    + "         'Error RS5.1.1: El Pedido ' || :NEW.ID_Pedido || ' no tiene un Usuario válido asociado.' "
                    + "      ); "
                    + "   END IF; "
                    + "END;";

    // 2) TRIGGER validar_usuario_en_modificaProducto
    private static final String TRIG_VALIDAR_USUARIO_MODIFICAPRODUCTO =
            "CREATE OR REPLACE TRIGGER validar_usuario_en_modificaProducto "
                    + "AFTER INSERT OR UPDATE ON modificaProducto "
                    + "FOR EACH ROW "
                    + "DECLARE "
                    + "   usuario_existe INTEGER; "
                    + "BEGIN "
                    + "   SELECT COUNT(*) INTO usuario_existe "
                    + "     FROM usuario "
                    + "    WHERE ID_Usuario = :NEW.ID_Usuario; "
                    + "   IF usuario_existe = 0 THEN "
                    + "   RAISE_APPLICATION_ERROR(-20002, " //este error hace un rollback automático
                    + "       'El usuario asociado al producto no existe.' "
                    + "   ); "
                    + "   END IF; "
                    + "END;";

    // 3) TRIGGER validar_producto_en_modificaProducto
    /*private static final String TRIG_VALIDAR_PRODUCTO_MODIFICAPRODUCTO =
            "CREATE OR REPLACE TRIGGER validar_producto_en_modificaProducto "
                    + "AFTER INSERT OR UPDATE ON modificaProducto "
                    + "FOR EACH ROW "
                    + "DECLARE "
                    + "   producto_existe INTEGER; "
                    + "BEGIN "
                    + "   -- Verificar si el producto existe en la tabla producto "
                    + "   SELECT COUNT(*) INTO producto_existe "
                    + "     FROM producto "
                    + "    WHERE ID_Producto = :NEW.ID_Producto; "
                    + "   IF producto_existe = 0 THEN "
                    + "   -- Opcional: Lanzar un error para notificar el problema "
                    + "   RAISE_APPLICATION_ERROR(-20003, "
                    + "       'El producto asociado no existe s.' "
                    + "   ); "
                    + "   END IF; "
                    + "END;";
*/
    // 4) TRIGGER validar_relacion_producto_modificaProducto
    /*private static final String TRIG_VALIDAR_RELACION_PRODUCTO_MODIFICAPRODUCTO =
            "CREATE OR REPLACE TRIGGER validar_relacion_producto_modificaProducto "
                    + "AFTER INSERT OR UPDATE ON producto "
                    + "FOR EACH ROW "
                    + "DECLARE "
                    + "   relacion_valida INTEGER; "
                    + "BEGIN "
                    + "   -- Verificar si el producto tiene una relación válida en modificaProducto "
                    + "   SELECT COUNT(*) INTO relacion_valida "
                    + "     FROM modificaProducto "
                    + "    WHERE ID_Producto = :NEW.ID_Producto; "
                    + "   -- Opcional: Lanzar un error para informar sobre la eliminación "
                    + "   RAISE_APPLICATION_ERROR(-20004, "
                    + "       'El producto no tiene una relación válida en modificaProducto. Producto eliminado.' "
                    + "   ); "
                    + "END;";
*/
    // 5) TRIGGER garantizar que existe el usuario cuando vas a añadir metodo pago
    private static final String TRIG_VERIFICAR_USUARIO_EXISTE =
            "CREATE OR REPLACE TRIGGER TRIG_VERIFICAR_USUARIO_EXISTE " +
                    "BEFORE INSERT ON pago " +
                    "FOR EACH ROW " +
                    "DECLARE " +
                    "    usuario_existe NUMBER; " +
                    "BEGIN " +
                    "    SELECT COUNT(*) INTO usuario_existe " +
                    "    FROM usuario " +
                    "    WHERE ID_Usuario = :NEW.ID_Usuario; " +
                    "    IF usuario_existe = 0 THEN " +
                    "        RAISE_APPLICATION_ERROR(-20002, 'Error: El usuario asociado al método de pago no existe.'); " +
                    "    END IF; " +
                    "END;";

    // 6) TRIGGER garantizar si existe el metodo de pago cuando vayas a hacer un pago
    private static final String TRIG_VERIFICAR_METODO_PAGO_EXISTE =
            "CREATE OR REPLACE TRIGGER TRIG_VERIFICAR_METODO_PAGO_EXISTE " +
                    "BEFORE INSERT ON Realiza " +
                    "FOR EACH ROW " +
                    "DECLARE " +
                    "    metodo_pago_existe NUMBER; " +
                    "BEGIN " +
                    "    SELECT COUNT(*) INTO metodo_pago_existe " +
                    "    FROM pago " +
                    "    WHERE ID_metodoPago = :NEW.ID_metodoPago; " +
                    "    IF metodo_pago_existe = 0 THEN " +
                    "        RAISE_APPLICATION_ERROR(-20003, 'Error: El método de pago especificado no existe.'); " +
                    "    END IF; " +
                    "END;";

    // TRIGGER para verificar la cantidad de producto antes de insertar o actualizar en la tabla 'tiene' Gabriel
    private static final String TRIG_VERIFICAR_CANTIDAD_PRODUCTO =
            "CREATE OR REPLACE TRIGGER verificar_cantidad_producto " +
                    "BEFORE INSERT OR UPDATE ON tiene " +
                    "FOR EACH ROW " +
                    "DECLARE " +
                    "    cantidad_producto INTEGER; " +
                    "    producto_existe INTEGER; " +
                    "BEGIN " +
                    "    SELECT COUNT(*) " +
                    "    INTO producto_existe " +
                    "    FROM producto " +
                    "    WHERE ID_Producto = :NEW.ID_Producto; " +
                    "    IF producto_existe = 0 THEN " +
                    "        RAISE_APPLICATION_ERROR( " +
                    "                -20006, " +
                    "                'Error: El producto no existe en la tabla Producto.' " +
                    "        ); " +
                    "    END IF; " +
                    "    SELECT Cantidad " +
                    "    INTO cantidad_producto " +
                    "    FROM producto " +
                    "    WHERE ID_Producto = :NEW.ID_Producto; " +
                    "    IF :NEW.Cantidad > cantidad_producto THEN " +
                    "        RAISE_APPLICATION_ERROR( " +
                    "                -20005, " +
                    "                'Error: La cantidad a insertar es mayor que la cantidad disponible del producto.' " +
                    "        ); " +
                    "    END IF; " +
                    "END;";

    // TRIGGER que evita que se inserte una relación duplicada entre un carrito y un pedido en GESTIONCARRITO. Gabriel
    private static final String TRIG_EVITAR_CARRITO_DUPLICADO =
            "CREATE OR REPLACE TRIGGER evitar_carrito_duplicado " +
                    "BEFORE INSERT ON gestioncarrito " +
                    "FOR EACH ROW " +
                    "DECLARE " +
                    "    carrito_duplicado INTEGER; " +
                    "BEGIN " +
                    "    SELECT COUNT(*) INTO carrito_duplicado " +
                    "    FROM gestioncarrito " +
                    "    WHERE ID_Carrito = :NEW.ID_Carrito AND ID_Pedido = :NEW.ID_Pedido; " +
                    "    IF carrito_duplicado > 0 THEN " +
                    "        RAISE_APPLICATION_ERROR(-20007, 'Error: Ya existe una relación entre este carrito y pedido.'); " +
                    "    END IF; " +
                    "END;";

    // Trigger para verificar que el ID_Usuario asociado con un pedido existe en la tabla usuario
    private static final String TRIG_VERIFICAR_USUARIO_EN_PEDIDO =
            "CREATE OR REPLACE TRIGGER verificar_usuario_en_pedido "
                    + "BEFORE INSERT OR UPDATE ON pedido "
                    + "FOR EACH ROW "
                    + "DECLARE "
                    + "   usuario_existe INTEGER; "
                    + "BEGIN "
                    + "   SELECT COUNT(*) INTO usuario_existe "
                    + "     FROM usuario "
                    + "    WHERE ID_Usuario = :NEW.ID_Usuario; "
                    + "   IF usuario_existe = 0 THEN "
                    + "      RAISE_APPLICATION_ERROR(-20007, "
                    + "         'Error: El usuario asociado al pedido no existe.' "
                    + "      ); "
                    + "   END IF; "
                    + "END;";

    /**
     * Método que crea (o reemplaza) todos los disparadores en la BD.
     * Recibe una conexión abierta y ejecuta cada sentencia CREATE TRIGGER.
     */
    public static void crearDisparadores(Connection conn) throws SQLException {
        // Usa try-with-resources para asegurar que se cierra el Statement
        try (Statement st = conn.createStatement()) {
            // Ejecutar cada disparador
            st.execute(TRIG_VERIFICA_PEDIDO_USUARIO);
            st.execute(TRIG_VALIDAR_USUARIO_MODIFICAPRODUCTO);
            st.execute(TRIG_VERIFICAR_USUARIO_EXISTE);
            st.execute(TRIG_VERIFICAR_METODO_PAGO_EXISTE);
            st.execute(TRIG_VERIFICAR_CANTIDAD_PRODUCTO);
            st.execute(TRIG_VERIFICAR_USUARIO_EN_PEDIDO );
            st.execute(TRIG_EVITAR_CARRITO_DUPLICADO);
            //st.execute(TRIG_VALIDAR_RELACION_PRODUCTO_MODIFICAPRODUCTO);
            System.out.println("Disparadores creados/reemplazados con éxito.");
        }
    }

    public static void eliminarDisparadores() throws SQLException {
        java.sql.Connection con = practica.Connection.connection;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.createStatement();

            // Obtener todos los nombres de los triggers
            String obtenerTriggersSQL = "SELECT TRIGGER_NAME FROM USER_TRIGGERS";
            rs = stmt.executeQuery(obtenerTriggersSQL);

            // Eliminar cada trigger encontrado
            while (rs.next()) {
                String triggerName = rs.getString("TRIGGER_NAME");

                // Usar un nuevo Statement para ejecutar el DROP TRIGGER
                try (Statement dropStmt = con.createStatement()) {
                    String dropTriggerSQL = "DROP TRIGGER " + triggerName;
                    System.out.println("Eliminando trigger: " + triggerName);
                    dropStmt.executeUpdate(dropTriggerSQL);
                } catch (SQLException e) {
                    System.err.println("Error al eliminar trigger " + triggerName + ": " + e.getMessage());
                }
            }
            JOptionPane.showMessageDialog(practica.Connection.frame, "Triggers eliminados correctamente.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            // Cerrar recursos
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


}

