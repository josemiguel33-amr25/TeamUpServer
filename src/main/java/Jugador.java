//La clase cliente, paras seguir con los guiños alguien que se conecta a mi "sistema de juego" pasa a ser un jugador que esta dentro de mi sistema

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Jugador implements  Runnable {
    private PrintWriter impresora;
    private BufferedReader lector;
    private Socket zocalo;
    private SistemaDeJuego sdj;
    private int idUsuario; // referencia directa al id que tiene dentro de la base de datos, para hacer mas eficiente supongo toda la interaccion


    public Jugador(Socket zocalo, SistemaDeJuego sdj) {
        this.zocalo = zocalo;
        this.sdj = sdj;
    }

    private void abrirFlujos() {
        try {
            impresora = new PrintWriter(new OutputStreamWriter(zocalo.getOutputStream()),true);
            lector = new BufferedReader(new InputStreamReader(zocalo.getInputStream()));
        } catch (IOException em) {
            System.out.println("TeamUp|Error|EM3");
        }
    }

    @Override
    public void run() {
        
    }
}
