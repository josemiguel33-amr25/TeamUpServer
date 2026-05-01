

//Este es la clase protocolo, le pongo sistema de juego por guiño a las alineaciones en el futbol, muchas veces se usa el termino Sistema de Juego

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import claseshibernate.Carta;
import claseshibernate.Usuario;

public class SistemaDeJuego {
    private final Integer BASE_PORTER = 70;
    private final String[] ESTADISTICAS_CAMPO = {"ritmo", "tiro","pase","regate","defensa","fisico"};
    //creo que no lo necesitamosprivate final String[] ESTADISTICAS_PORTERO = {"estirada","manejo","saque","reflejos","velocidad","posicionamiento"};
    
    private Servidor sv;
    private Set<Jugador> jugadores = Collections.synchronizedSet(new HashSet<>()); // clientes actualmente conectados



    public SistemaDeJuego(Servidor sv) {
        this.sv = sv;
    }

    public String buzon(String mensaje, Jugador j) { // le llamo buzon porque se encarga de recibir mensajes y enviar a su punto Formatos disponibles en la documentacion
        String respuesta = "TeamUp|Directriz|"; 
        String [] mensajePartido = mensaje.split(";");  // TeamUpCliente;Respuesta|registro|nombreºvalor:contraseniaºvalor:
        String [] datosDivididos = mensajePartido[1].split("\\|");
        String datos = datosDivididos[2];
        String opcion = datosDivididos[1];
        switch (opcion) { 
            case "registro":
                    respuesta = registrarUsuario(datos, j); // Comprobar en la interfaz si el usuario introduce dato o no porque aqui pensamos que llega todo "bien" bien no se pero al menos informacion llega
                    break;
            case "iniciarSesion": // iniciar sesion, el usuario le da el cliente comprueba si tenemos un token, si tenemos un token al darle al boton entraremos directamente a la aplicacion, sino pues a poner los datos
                    respuesta = iniciarSesion(datos, j); // TeamUpCliente;Respuesta|iniciarSesion|correo:valorºcontraseniaºvalor:token:siºselector:valorºhash:valor
                    break; 
            }

        return respuesta;
    }

    public String iniciarSesion(String datos, Jugador j) { //TeamUpCliente;Respuesta|iniciarSesion|correo:valorºcontraseniaºvalor:remember:siºselector:valorºtoken:valor
            String respuesta = "";
            
            Map<String, String> mapaDatos = new HashMap<>();
            String [] divisionDatos = datos.split(":"); 
            for (String dato : divisionDatos) {
                String [] datoParticionado = dato.split("º");
                mapaDatos.put(datoParticionado[0], datoParticionado[1]);
            }
            if (mapaDatos.get("remember").equals("si")) {
                respuesta = sv.getBaseDatosManager().iniciarSesionToken(mapaDatos.get("selector"), mapaDatos.get("token"));
            } else if (mapaDatos.get("remember").equals("no")) {
                respuesta = sv.getBaseDatosManager().iniciarSesionContrasenia(mapaDatos.get("correo"), mapaDatos.get("contrasenia"));
            }

            String [] comprobacionRespuesta = respuesta.split("\\|");

            if (comprobacionRespuesta[2].equals("rC")) {
                j.setIdUsuario(sv.getBaseDatosManager().obtenerId(mapaDatos.get("nombre")));
                generadorCarta(mapaDatos.get("posicion1"), mapaDatos.get("posicion2"), mapaDatos.get("nombre"));
                jugadores.add(j);
            } else 
                j.setIdUsuario(-33);


            return respuesta;
    }

    public String registrarUsuario(String datos, Jugador j) { //datos formato es ---> |registroºvalor:contraseniaºvalor: el recordarmeºvalor (0 Falso o 1 true) va al final
        String respuesta = "TeamUp|Directriz|errOe";
        Map<String, String> mapaDatos = new HashMap<>();
        String [] divisionDatos = datos.split(":"); //nombreºvalor:contraseniaºvalor:recordarmeº0/1:
        for (String dato : divisionDatos) {
            String [] datoParticionado = dato.split("º");
            mapaDatos.put(datoParticionado[0], datoParticionado[1]);
        }

        respuesta = sv.getBaseDatosManager().registrarUsuario(mapaDatos.get("nombre"), mapaDatos.get("contrasenia"), mapaDatos.get("correo"), mapaDatos.get("posicion1"), mapaDatos.get("posicion2"),mapaDatos.get("recordarme"), j);
        String [] comprobacionRespuesta = respuesta.split("\\|");

        if (comprobacionRespuesta[2].equals("rC")) {
            j.setIdUsuario(sv.getBaseDatosManager().obtenerId(mapaDatos.get("nombre")));
            generadorCarta(mapaDatos.get("posicion1"), mapaDatos.get("posicion2"), mapaDatos.get("nombre"));
            jugadores.add(j);
        } else 
            j.setIdUsuario(-33);



        return respuesta;

    }

    private void generadorCarta(String posicion1, String posicion2, String nombre) {
        Random generador = new Random();
        Usuario usu = sv.getBaseDatosManager().obtenerUsuario(nombre);
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
            Carta c  = new Carta(estadisticasCampo.get("ritmo"), estadisticasCampo.get("tiro"), estadisticasCampo.get("pase"), estadisticasCampo.get("regate"), estadisticasCampo.get("defensa"), estadisticasCampo.get("fisico"), usu, sv.getBaseDatosManager().obtenerCosmetico(1));
            
            c.setPosicionamiento(BASE_PORTER + generador.nextInt(8));
            c.setReflejos(BASE_PORTER + generador.nextInt(8));
            c.setManejo(BASE_PORTER + generador.nextInt(8));
            c.setVelocidad(BASE_PORTER + generador.nextInt(8));
            c.setEstirada(BASE_PORTER + generador.nextInt(8));
            
            sv.getBaseDatosManager().registrarCarta(c);

        } else {
            List<String>posiciones = new ArrayList<>();
            posiciones.add(posicion1);
            posiciones.add(posicion2);
            List<String> estadisticasCambiantes = obtenerBonus(posiciones);
            Map<String, Integer> estadisticasCampo = new HashMap<>();
            for (String estadistica : estadisticasCambiantes) {
                int sumaEstadistica = 70 + generador.nextInt(4)+1;
                if (estadistica.equals(estadisticasCambiantes.get(0)) || estadistica.equals(estadisticasCambiantes.get(2))) {
                    estadisticasCampo.put(estadistica, sumaEstadistica + 5);
                } else if (estadistica.equals(estadisticasCambiantes.get(1)) || estadistica.equals(estadisticasCambiantes.get(3))) {
                    estadisticasCampo.put(estadistica, sumaEstadistica - 3);
                } else {
                    estadisticasCampo.put(estadistica, sumaEstadistica);
                }
            }
            Carta c = new Carta(estadisticasCampo.get("ritmo"), estadisticasCampo.get("tiro"), estadisticasCampo.get("pase"), estadisticasCampo.get("regate"), estadisticasCampo.get("defensa"),estadisticasCampo.get("fisico"), usu, sv.getBaseDatosManager().obtenerCosmetico(1));            

            sv.getBaseDatosManager().registrarCarta(c);
        }
        
    }

    private List<String> obtenerBonus(List<String> posicionesRecibidas) { //primera y tercera mejorar, segunda y cuarta empeorar
        List<String> estadistica = new ArrayList<>();


        for (String posicion : posicionesRecibidas) {
            List<String> temporal = obtenerBonus(posicion);
            estadistica.add(temporal.get(0));
            estadistica.add(temporal.get(1));
        }


        return estadistica;
    }

    private List<String> obtenerBonus(String posicion) { //devolvemos dos estadisticas la primera el bonus y la segunda la que empeora es 
        List<String> estadistica = new ArrayList<>();
        Random generador = new Random();
        int caraCruz = generador.nextInt(2);


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


}


