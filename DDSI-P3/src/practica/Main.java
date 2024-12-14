package practica;  //TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

public class Main {
    public static void main(String[] args) {

        Connection.inicializarFrame();
        Connection.conectarBD();
        //1. Iniciar sesión o registrarse
        Diseño.pantalla_registro(Connection.frame);

        Diseño.pantalla_inicio(Connection.frame);


        }
}
