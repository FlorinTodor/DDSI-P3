package practica;  //TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import practica.connection;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        connection.inicializarFrame();
        connection.conectarBD();
        //1. Iniciar sesión o registrarse
        diseño.pantalla_registro(connection.frame);


        diseño.pantalla_inicio(connection.frame);


        }
}
