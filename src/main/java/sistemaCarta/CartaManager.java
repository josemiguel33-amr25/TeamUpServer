package sistemaCarta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ClasesServer.BaseDatosManager;
import claseshibernate.Carta;
import claseshibernate.Usuario;

public class CartaManager {
    private static CartaManager cartaManager;

    private final Integer BASE_PORTER = 70;
    private final String[] ESTADISTICAS_CAMPO = {"ritmo", "tiro","pase","regate","defensa","fisico"};

    private CartaManager() {
        System.out.println("TeamUp|MensajeIntereno|Modulos de cartas creado");
    }

    public static CartaManager getCartaManager() {
        if (cartaManager == null) 
            cartaManager = new CartaManager();

        return cartaManager;
    }

    public String obtenerEstadisticaAleatoriaPortero() { // sistema carta
        List<String> estadisticasPortero = new ArrayList<>();
        Random generador = new Random();
        estadisticasPortero.add("posicionamiento");
        estadisticasPortero.add("reflejos");
        estadisticasPortero.add("velocidad");
        estadisticasPortero.add("saque");
        estadisticasPortero.add("manejo");
        estadisticasPortero.add("estirada");
        return estadisticasPortero.get(generador.nextInt(6));
    }

    public List<String> obtenerBonus(List<String> posicionesRecibidas) { //primera y tercera mejorar, segunda y cuarta empeorar
        List<String> estadistica = new ArrayList<>();


        for (String posicion : posicionesRecibidas) {
            List<String> temporal = obtenerBonus(posicion);
            estadistica.add(temporal.get(0));
            estadistica.add(temporal.get(1));
        }


        return estadistica;
    }

    public List<String> obtenerBonus(String posicion) { //devolvemos dos estadisticas la primera el bonus y la segunda la que empeora es 
        List<String> estadistica = new ArrayList<>();
        Random generador = new Random();
        int caraCruz = generador.nextInt(2);
        System.out.println("TeamUp|MensajeInterno|Entramos en obtener bonus");

        switch (posicion) {
            case "dc" :
                if (caraCruz == 0) {
                    estadistica.add("tiro");
                    estadistica.add("defensa");
                } else {
                    estadistica.add("regate");
                    estadistica.add("pase");
                }
                break;
            case "ei":
                if (caraCruz == 0) {
                    estadistica.add("ritmo");
                    estadistica.add("fisico");
                } else {
                    estadistica.add("regate");
                    estadistica.add("defensa");
                }
                break;
            case "ed":
                if (caraCruz == 0) {
                    estadistica.add("regate");
                    estadistica.add("fisico");
                } else {
                    estadistica.add("ritmo");
                    estadistica.add("defensa");
                }
                break;
            case "mc":
                if (caraCruz == 0) {
                    estadistica.add("regate");
                    estadistica.add("tiro");
                } else {
                    estadistica.add("pase");
                    estadistica.add("ritmo");
                }
                break;
            case "mcd":
                if (caraCruz == 0) {
                    estadistica.add("defensa");
                    estadistica.add("tiro");
                } else {
                    estadistica.add("pase");
                    estadistica.add("ritmo");
                }
                break;
            case "mco":
                if (caraCruz == 0) {
                    estadistica.add("tiro");
                    estadistica.add("defensa");
                } else {
                    estadistica.add("pase");
                    estadistica.add("fisico");
                }
                break;
            case "dfc":
                if (caraCruz == 0) {
                    estadistica.add("defensa");
                    estadistica.add("regate");
                } else {
                    estadistica.add("fisico");
                    estadistica.add("tiro");
                }
                break;
            case "li":
                if (caraCruz == 0) {
                    estadistica.add("ritmo");
                    estadistica.add("fisico");
                } else {
                    estadistica.add("defensa");
                    estadistica.add("tiro");
                }
                break;
            case "ld":
                if (caraCruz == 0) {
                    estadistica.add("ritmo");
                    estadistica.add("fisico");
                } else {
                    estadistica.add("defensa");
                    estadistica.add("tiro");
                }
                break;
            default:
                throw new AssertionError();
        }


        return estadistica;
    }

    public String obtenerEstadisticaAleatoria() { // Sistema carta
        List<String> estadisticasJugador = new ArrayList<>();
        Random generador = new Random();
        estadisticasJugador.add("tiro");
        estadisticasJugador.add("regate");
        estadisticasJugador.add("ritmo");
        estadisticasJugador.add("defensa");
        estadisticasJugador.add("pase");
        estadisticasJugador.add("fisico");
        return estadisticasJugador.get(generador.nextInt(6));
    }

    public void generadorCarta(String posicion1, String posicion2, String nombre, BaseDatosManager bdm) { // sistema cartas
        Random generador = new Random();
        System.out.println("TeamUp|MensajeInterno|Estoy dedntro de generador de carta, buenas con usuario " + nombre);
        Usuario usu = bdm.obtenerUsuario(nombre);
        System.out.println("TeamUp|MensajeInterno|He obtenido el siguiente usuario: " + usu.getNombre() + " con " + usu.getId());
        if (posicion1.equals("por") || posicion2.equals("por")) {
            String posicionCampo = "";
            if (!posicion1.equals("por")) {
                posicionCampo = posicion1;
            } else {
                posicionCampo = posicion2;
            }
            
            List<String>estadisticasCambiantes = obtenerBonus(posicionCampo);
            Map<String, Integer> estadisticasCampo = new HashMap<>();
            for (String estadistica : ESTADISTICAS_CAMPO) {
                int sumaEstadistica = 70 + generador.nextInt(4)+1;
                if (estadistica.equals(estadisticasCambiantes.get(0))) { 
                    estadisticasCampo.put(estadistica,sumaEstadistica+5 );
                } else if (estadistica.equals(estadisticasCambiantes.get(1))) {
                    estadisticasCampo.put(estadistica,sumaEstadistica-3 );
                } else {
                    estadisticasCampo.put(estadistica, sumaEstadistica);
                }
            }
            Carta c  = new Carta(estadisticasCampo.get("ritmo"), estadisticasCampo.get("tiro"), estadisticasCampo.get("pase"), estadisticasCampo.get("regate"), estadisticasCampo.get("defensa"), estadisticasCampo.get("fisico"), usu, bdm.obtenerCosmetico(1));
            
            c.setPosicionamiento(BASE_PORTER + generador.nextInt(8));
            c.setReflejos(BASE_PORTER + generador.nextInt(8));
            c.setManejo(BASE_PORTER + generador.nextInt(8));
            c.setVelocidad(BASE_PORTER + generador.nextInt(8));
            c.setEstirada(BASE_PORTER + generador.nextInt(8));
            c.setSaque(BASE_PORTER + generador.nextInt(8));
            c.setMediaPortero(c.calcularMedia(c.getEstirada(), c.getManejo(), c.getReflejos(), c.getVelocidad(), c.getSaque(), c.getPosicionamiento()));
            System.out.println("TeamUp|MensajeInterno|Carta con estadisticas " + c.getRegate() + " regate");
            bdm.registrarCarta(c);

        } else {
            System.out.println("TeamUp|MensajeInterno|Entramos en el else donde se crean las cartas para gente con posicion de campo no portero");
            List<String>posiciones = new ArrayList<>();
            posiciones.add(posicion1);
            posiciones.add(posicion2);
            List<String> estadisticasCambiantes = obtenerBonus(posiciones);
            System.out.println("TeamUp|MensajeInterno|Tamanio de estadisticas cambiantes " + estadisticasCambiantes.size());
            Map<String, Integer> estadisticasCampo = new HashMap<>();
            for (String estadistica : ESTADISTICAS_CAMPO) {
                int sumaEstadistica = 70 + generador.nextInt(4)+1;
                if (estadistica.equals(estadisticasCambiantes.get(0)) || estadistica.equals(estadisticasCambiantes.get(2))) {
                    estadisticasCampo.put(estadistica, sumaEstadistica + 5);
                } else if (estadistica.equals(estadisticasCambiantes.get(1)) || estadistica.equals(estadisticasCambiantes.get(3))) {
                    estadisticasCampo.put(estadistica, sumaEstadistica - 3);
                } else {
                    estadisticasCampo.put(estadistica, sumaEstadistica);
                }
            }
            Carta c = new Carta(estadisticasCampo.get("ritmo"), estadisticasCampo.get("tiro"), estadisticasCampo.get("pase"), estadisticasCampo.get("regate"), estadisticasCampo.get("defensa"),estadisticasCampo.get("fisico"), usu, bdm.obtenerCosmetico(1));            

            bdm.registrarCarta(c);
        }
        
    }
}
