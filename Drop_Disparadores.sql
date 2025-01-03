------------------------------------------------------------------
-- SCRIPT: Eliminar (DROP) disparadores creados previamente
------------------------------------------------------------------

BEGIN
EXECUTE IMMEDIATE 'DROP TRIGGER TRIG_VerificaPedidoYUsuario';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -4080 THEN  -- ORA-04080: trigger does not exist
            RAISE;
END IF;
END;
/

BEGIN
EXECUTE IMMEDIATE 'DROP TRIGGER validar_usuario_en_modificaProducto';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -4080 THEN
            RAISE;
END IF;
END;
/

BEGIN
EXECUTE IMMEDIATE 'DROP TRIGGER TRIG_VERIFICAR_USUARIO_EXISTE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -4080 THEN
            RAISE;
END IF;
END;
/

BEGIN
EXECUTE IMMEDIATE 'DROP TRIGGER TRIG_VERIFICAR_METODO_PAGO_EXISTE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -4080 THEN
            RAISE;
END IF;
END;
/

BEGIN
EXECUTE IMMEDIATE 'DROP TRIGGER verificar_cantidad_producto';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -4080 THEN
            RAISE;
END IF;
END;
/

BEGIN
EXECUTE IMMEDIATE 'DROP TRIGGER evitar_carrito_duplicado';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -4080 THEN
            RAISE;
END IF;
END;
/

BEGIN
EXECUTE IMMEDIATE 'DROP TRIGGER verificar_usuario_en_pedido';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -4080 THEN
            RAISE;
END IF;
END;
/

COMMIT;
/
