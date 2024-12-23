package practica;  //TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

public class Main {
    public static void main(String[] args) {
        Connection.inicializarFrameRegistro();
        Connection.inicializarFramePrincipal();
        Connection.conectarBD();

        // Iniciar sesión o registrarse
        int idUsuario = Diseño.pantalla_registro(Connection.frame_registro);

        if (idUsuario != -1) {
            Connection.id_user = idUsuario;
            Connection.frame_registro.dispose(); // Cerrar frame de registro
            Diseño.pantalla_inicio(Connection.frame); // Mostrar frame principal
        }
    }
}