

//Este es la clase protocolo, le pongo sistema de juego por guiño a las alineaciones en el futbol, muchas veces se usa el termino Sistema de Juego

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import claseshibernate.Carta;
import claseshibernate.Usuario;

public class SistemaDeJuego {
    private final Integer BASE_PORTER = 70;
    private final String[] ESTADISTICAS_CAMPO = {"ritmo", "tiro","pase","regate","defensa","fisico"};
    //creo que no lo necesitamosprivate final String[] ESTADISTICAS_PORTERO = {"estirada","manejo","saque","reflejos","velocidad","posicionamiento"};
    
    private Servidor sv;
    private Set<JugadorSistema> jugadores = Collections.synchronizedSet(new HashSet<>()); // clientes actualmente conectados



    public SistemaDeJuego(Servidor sv) {
        this.sv = sv;
    }

    public String buzon(String mensaje, JugadorSistema j) { // le llamo buzon porque se encarga de recibir mensajes y enviar a su punto Formatos disponibles en la documentacion
        String respuesta = "TeamUp|Directriz|"; 
        System.out.println("TeamUp|MensajeInterno|Ha llegado hasta aqui con " + mensaje);
        
        try {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> mensajeMapita = mapper.readValue(mensaje, Map.class);

            String opcion = (String) mensajeMapita.get("tipo");

            Map<String, String> datos = mapper.convertValue(
                mensajeMapita.get("data"),
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {}
            );

            switch (opcion) { 
                case "registro":
                        respuesta = registrarUsuario(datos, j); // Comprobar en la interfaz si el usuario introduce dato o no porque aqui pensamos que llega todo "bien" bien no se pero al menos informacion llega
                        break;
                case "iniciarSesion": // iniciar sesion, el usuario le da el cliente comprueba si tenemos un token, si tenemos un token al darle al boton entraremos directamente a la aplicacion, sino pues a poner los datos
                        respuesta = iniciarSesion(datos, j); // TeamUpCliente;Respuesta|iniciarSesion|correo:valorºcontraseniaºvalor:token:siºselector:valorºhash:valor
                        break; 
            }
        } catch (Exception em) {
            System.out.println("TeamUp|Error|EM5" + em.getMessage());
        }

        return respuesta;
    }

    public String iniciarSesion(Map<String,String> mapaDatos, JugadorSistema j) { //TeamUpCliente;Respuesta|iniciarSesion|correo:valorºcontraseniaºvalor:remember:siºselector:valorºtoken:valor
            String respuesta = "";
            System.out.println("TeamUp|MensajeInterno|Ha llegado hasta aqui (iniciar sesion) con " + mapaDatos.get("correo"));
            
            if (mapaDatos.get("remember").equals("si")) {
                respuesta = sv.getBaseDatosManager().iniciarSesionToken(mapaDatos.get("selector"), mapaDatos.get("token"), j);
            } else if (mapaDatos.get("remember").equals("no")) {
                respuesta = sv.getBaseDatosManager().iniciarSesionContrasenia(mapaDatos.get("correo"), mapaDatos.get("contrasenia"), j);
            }


            if (j.getIdUsuario() != -33) 
                jugadores.add(j);
            


            return respuesta;
    }

    public String registrarUsuario(Map<String,String> mapaDatos, JugadorSistema j) { //datos formato es ---> |registroºvalor:contraseniaºvalor: el recordarmeºvalor (0 Falso o 1 true) va al final
        String respuesta = "TeamUp|Directriz|errOe";
        System.out.println("TeamUp|MensajeInterno|Ha llegado hasta aqui (registrar) con " + mapaDatos.get("nombre"));



        respuesta = sv.getBaseDatosManager().registrarUsuario(mapaDatos.get("nombre"), mapaDatos.get("contrasenia"), mapaDatos.get("correo"), mapaDatos.get("posicion1"), mapaDatos.get("posicion2"),mapaDatos.get("recordarme"), j);


        if (j.getIdUsuario() != -33) {
            System.out.println("TeamUp|MensajeInterno|Voy a entrar a generador de carta con: " + j.getIdUsuario());
            generadorCarta(mapaDatos.get("posicion1"), mapaDatos.get("posicion2"), mapaDatos.get("nombre"));
            jugadores.add(j);
        }


        return respuesta;

    }

    private void generadorCarta(String posicion1, String posicion2, String nombre) {
        Random generador = new Random();
        System.out.println("TeamUp|MensajeInterno|Estoy dedntro de generador de carta, buenas con usuario " + nombre);
        Usuario usu = sv.getBaseDatosManager().obtenerUsuario(nombre);
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
            Carta c  = new Carta(estadisticasCampo.get("ritmo"), estadisticasCampo.get("tiro"), estadisticasCampo.get("pase"), estadisticasCampo.get("regate"), estadisticasCampo.get("defensa"), estadisticasCampo.get("fisico"), usu, sv.getBaseDatosManager().obtenerCosmetico(1));
            
            c.setPosicionamiento(BASE_PORTER + generador.nextInt(8));
            c.setReflejos(BASE_PORTER + generador.nextInt(8));
            c.setManejo(BASE_PORTER + generador.nextInt(8));
            c.setVelocidad(BASE_PORTER + generador.nextInt(8));
            c.setEstirada(BASE_PORTER + generador.nextInt(8));
            System.out.println("TeamUp|MensajeInterno|Carta con estadisticas " + c.getRegate() + " regate");
            sv.getBaseDatosManager().registrarCarta(c);

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


}


