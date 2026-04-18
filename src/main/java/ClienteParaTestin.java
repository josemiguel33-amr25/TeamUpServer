


// Cliente solo con finalidad de testing del servidor

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteParaTestin {
    public static final String IP = "127.0.0.1";
    public static final int PORT = 3333;
    public static void main(String[] args) {
        try (
            Socket socket = new Socket(IP, PORT);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            Scanner entrada = new Scanner(System.in);
            Thread hiloEscucha = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = lector.readLine()) != null) {
                        if (msg.startsWith("JUEGOTERMINADO")) {
                            System.exit(0);
                            break;
                        } else {
                            System.out.println(msg);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Conexion con el servidor terminada");
                } 
            });
            hiloEscucha.setDaemon(true);
            hiloEscucha.start();

            String lectura;
            System.out.print("Comando: ");
            while ((lectura = entrada.nextLine()) != null) {
                salida.println(lectura);
                if (lectura.equalsIgnoreCase("SALIR")) {
                    break;
                }
            }


        } catch (IOException em) {
            System.out.println(" PARTIDA FINALIZADA|14|");
        } catch(IllegalStateException e) {
        } 

    }
}
