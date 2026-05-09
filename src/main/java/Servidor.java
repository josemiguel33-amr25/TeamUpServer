import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class Servidor {
    public static final int caracteresMaximoTitulo = 100;

    private ExecutorService ejecutador;
    private Properties propiedades;
    private ServerSocket zocaloServidor;
    private BaseDatosManager baseDatosManager;
    private SistemaDeJuego sdj;


    public Servidor() {
        propiedades = new Properties();
        baseDatosManager = new BaseDatosManager();
        sdj = new SistemaDeJuego(this);
        cargarPropiedades();
        ejecutador = Executors.newFixedThreadPool(Integer.parseInt(propiedades.getProperty("maxConnections")));

        try {
            zocaloServidor = new ServerSocket(Integer.parseInt(propiedades.getProperty("port")));
        }  catch (IOException em) {
            System.out.println("TeamUp|Error|EM1|");
        }   
    }

    public void cargarPropiedades() {
        try {
            InputStream inEm = Servidor.class.getResourceAsStream("configuracion.properties");
            propiedades.load(inEm);
        } catch (Exception em) {
            System.out.println("TeamUp|Error|EM0|");
        }
    }

    public void iniciarServidor() {
        if (baseDatosManager.comprobarConexion()) {
            while (true) {
                System.out.println("TeamUp|MensajeInterno| Servidor Iniciado");
                System.out.println("TeamUp|MensajeInterno| Iniciando Conexión Con Base De Datos");
                
                System.out.println("TeamUp|MensajeInterno| Iniciando comprobacion de verificacion de cuentas");
                baseDatosManager.verificadorCuentas();

                System.out.println("TeamUp|MensajeInterno| Iniciando comprobacion de token expirados");
                baseDatosManager.verificadorExpiracionToken();

                
                System.out.println("\n\n\nTeamUp|MensajeInterno| Comprobaciones terminadas \n SERVIDOR INICIADO");

                while (true) {
                    try {
                        Socket cliente  = zocaloServidor.accept();
                        JugadorSistema j = new JugadorSistema(cliente, sdj);
                        Thread hilo = new Thread(j); //usar executor service que para algo esta
                        hilo.start();
                    } catch (IOException em) {
                        System.out.println("TeamUp|Error|EM1|");
                    }
                }
            
            }
        } else
            System.out.println("TeamUp|Error|EM2|");
    }


    public static void main(String[] args) {
        System.out.println(Servidor.class.getClassLoader().getResource("configuracion.properties"));
        Servidor sv = new Servidor();
        sv.iniciarServidor();
    }

    public BaseDatosManager getBaseDatosManager() {
        return baseDatosManager;
    }
}
