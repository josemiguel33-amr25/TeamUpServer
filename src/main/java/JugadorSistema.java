//La clase cliente, paras seguir con los guiños alguien que se conecta a mi "sistema de juego" pasa a ser un jugador que esta dentro de mi sistema

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class JugadorSistema implements  Runnable {
    private PrintWriter impresora;
    private BufferedReader lector;
    private Socket zocalo;
    private SistemaDeJuego sdj;
    private int idUsuario; // referencia directa al id que tiene dentro de la base de datos, para hacer mas eficiente supongo toda la interaccion


    public JugadorSistema(Socket zocalo, SistemaDeJuego sdj) {
        this.zocalo = zocalo;
        this.sdj = sdj;
        abrirFlujos();
    }

    private void abrirFlujos() {
        try {
            impresora = new PrintWriter(new OutputStreamWriter(zocalo.getOutputStream()),true);
            lector = new BufferedReader(new InputStreamReader(zocalo.getInputStream()));
        } catch (IOException em) {
            System.out.println("TeamUp|Error|EM3");
        }
    }

    public void enviarMensajeDirecto(String mensaje) {
        if (impresora != null) {
            impresora.println(mensaje);
        }
    }

    @Override
    public void run() {
       String linea = "";
        try {
            while ((linea = lector.readLine()) != null) {
                String feedback = this.sdj.buzon(linea, this);
                enviarMensajeDirecto(feedback);   
            }
        } catch (IOException em) {
            System.out.println("ERROR FATIDICO |14|");
        } finally {
            System.out.println("TeamUp|MensajeInterno|Usuario Desconectado|UsuarioId:" + this.idUsuario);
            cerrarConexion();
        }
    }

    private void cerrarConexion() {
        try {
            if (zocalo != null) zocalo.close();
            System.out.println("Socket de jugador cerrado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public Socket getZocalo() {
        return zocalo;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
}
