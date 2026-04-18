import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import EjemplosServer.Juego;
import EjemplosServer.Jugador;
import EjemplosServer.Producto;

public class Servidor {

    private static final String rutaProperties = "";

    private ExecutorService ejecutador;
    private Properties propiedades;
    private ServerSocket zocaloServidor;


    public Servidor() {
        propiedades = new Properties();
        cargarPropiedades();
        ejecutador = Executors.newFixedThreadPool(Integer.parseInt(propiedades.getProperty("maxConnections")));
        try {
            zocaloServidor = new ServerSocket(Integer.parseInt(propiedades.getProperty("port")));
        } catch (UnknownHostException em) {
            System.out.println("ERROR FATIDICO NO CONOZCO AL HOST :( |13| |14| |33|");
        } catch (IOException me) {
            System.out.println("ERROR FATIDICO CREANDO SERVER |23| |13| |14| ");
        }   
    }

    public void cargarPropiedades() {
        try {
            InputStream inEm = Servidor.class.getResourceAsStream("configuracion.properties");
            propiedades.load(inEm);
        } catch (Exception em) {
            System.out.println("ERROR FATIDICO |14| AL CARGAR PROPIEDADES");
        }
    }

    public void iniciarServidor() {
        while (true) {
            System.out.println("SERVIDOR INICIADO");
            System.out.println("DEPURACION|PARTIDA NUMERO" + contadorPartidas);
            while (jugadores.size() < cantidadJugadores) {
                try {
                    Socket cliente  = zocaloServidor.accept();
                    Jugador j = new Jugador(cliente, contador, juego);
                    jugadores.add(j);
                    ejecutador.execute(j);
                    contador++;
                } catch (IOException em) {
                    System.out.println("ERROR FATIDICO |14|");
                }
            }
            contadorPartidas++;
            if (contadorPartidas == numeroPartidas) {
                break;
            }
        }
    }


    public static void main(String[] args) {
        System.out.println(Servidor.class.getClassLoader().getResource("configuracion.properties"));
        Servidor sv = new Servidor();
    }
}
