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


    // 3) TRIGGER garantizar que existe el usuario cuando vas a añadir metodo pago
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

    // 4) TRIGGER garantizar si existe el metodo de pago cuando vayas a hacer un pago
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

    //  5) TRIGGER para verificar la cantidad de producto antes de insertar o actualizar en la tabla 'tiene' Gabriel
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

    //7)  Trigger para verificar que el ID_Usuario asociado con un pedido existe en la tabla usuario
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

