import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientePrueba { //CLIENTE SOLO PARA PRUEBAS

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int puerto = 3333; 

        try {
            Socket socket = new Socket(host, puerto);

            BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            PrintWriter salida = new PrintWriter(
                    socket.getOutputStream(), true
            );

            BufferedReader consola = new BufferedReader(
                    new InputStreamReader(System.in)
            );

            System.out.println("Conectado al servidor ");


            Thread lectorServidor = new Thread(() -> {
                try {
                    String linea;
                    while ((linea = entrada.readLine()) != null) {
                        System.out.println("Servidor: " + linea);
                    }
                } catch (Exception e) {
                    System.out.println("Conexión cerrada.");
                }
            });

            lectorServidor.start();


            String mensaje;
            while ((mensaje = consola.readLine()) != null) {
                salida.println(mensaje);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}