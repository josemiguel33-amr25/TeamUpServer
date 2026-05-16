

//Este es la clase protocolo, le pongo sistema de juego por guiño a las alineaciones en el futbol, muchas veces se usa el termino Sistema de Juego

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import clases.Rango;
import clases.VotacionJugador;


public class SistemaDeJuego {
    
    private Servidor sv;
    private List<Rango> listaRangos = new ArrayList<>();
    private Set<JugadorSistema> jugadores = Collections.synchronizedSet(new HashSet<>()); // clientes actualmente conectados



    public SistemaDeJuego(Servidor sv) {
        this.sv = sv;
        prepararRangos();
    }

    public void prepararRangos() {
        listaRangos.add(new Rango(0, 150, "sobreBronce", "Bronce"));
        listaRangos.add(new Rango(150, 300, "sobrePlata", "Plata"));
        listaRangos.add(new Rango(300, 450, "sobreOro", "Oro"));
        listaRangos.add(new Rango(450, 600, "sobreElite", "Elite"));
    }

    public String salirAplicacion(JugadorSistema j) {
        jugadores.remove(j);
        System.out.println("TeamUp|MensajeInterno|Jugador con id " + j.getIdUsuario() + " sale de la aplicacion");
        j.cerrarConexion();
        return AyudanteConteston.contestarTodoBien("jSdA", "Jugador ha salido de la aplicacion correctamente", null);
    }

    public String buzon(String mensaje, JugadorSistema j) { // le llamo buzon porque se encarga de recibir mensajes y enviar a su punto Formatos disponibles en la documentacion
        String respuesta = ""; 
        System.out.println("TeamUp|MensajeInterno|Ha llegado hasta aqui con " + mensaje);
        // idea podemos hacer algo para evitar que dos cuentas esten conectadas a la misma vez para evitar desincronizaciones, esto para nuestro sistema es facil porque el objeto JugadorSistema tiene la id del usuario
        //entonces solo tenemos que crear un metodo que el usuario al salir de la aplicacion se elimine del set sincronizado y al entrar le ponemos la id pero antes de meterle en el set de jugadores comprobamos si contains j
        // para esot hacemos  el equals en jugadorSistema por la id de usuario
        try {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> mensajeMapita = mapper.readValue(mensaje, Map.class);

            String opcion = (String) mensajeMapita.get("tipo");

            Map<String, String> datos = mapper.convertValue(mensajeMapita.get("data"), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});

            switch (opcion) {  // podemos añadir un case que sea ver perfil de jugador y se llamara cada vez que un jugador pinche en un usuario y al final podriamos reciclar lo de ver nuestro perfil
                case "registro":
                        respuesta = registrarUsuario(datos, j); // Comprobar en la interfaz si el usuario introduce dato o no porque aqui pensamos que llega todo "bien" bien no se pero al menos informacion llega
                        break;
                case "iniciarSesion": // iniciar sesion, el usuario le da el cliente comprueba si tenemos un token, si tenemos un token al darle al boton entraremos directamente a la aplicacion, sino pues a poner los datos
                        respuesta = iniciarSesion(datos, j); 
                        break;
                case "ranking":
                        System.out.println("TeamUp|MensajeInterno|Entro en ranking"); // desde el  cliente se seleccionara dos filtros mayor o menor y el rango 
                        respuesta = obtenerRanking(datos.get("rango"), datos.get("mayorMenor") );
                        break;
                case "rangos": // al entrar a ver los rangos
                        System.out.println("TeamUp|MensajeInterno|Entro en rangos");
                        respuesta = obtenerRangos();
                        break;
                case "partidos": // el usuario entra en mis partidos, entra aqui en este case dentro de este case habra otro switch porque la primera vez mostramos todos los partidos del usuario pero el usuario puede pinchar en el partido y ver todos los participantes y quien gano el mvp y esas cosas
                        // aqui es cuando el usuario quiere ver el historial de partidos que ha jugado, pasamos los partidos que ha jugado facil, con toda la informacion de los partidos
                        String opcionPartidos = datos.get("tipoPartido");
                        System.out.println("TeamUp|MensajeInterno|Has llegado a partidos y la opcion partidos es: " + opcionPartidos);
                        switch (opcionPartidos) {
                            case "verPartidos" : // opcion cambiada antes se llamaba primera carga, aqui directamente le paso tambien los filtros y si se recarga se vuelve aqui
                                respuesta = partidosPrimeraCarga(datos.get("ciudad"), datos.get("soloverificados"));
                                break;
                            case "crearPartido": // el usuario pincha crear partido y en la interfaz se le abre una ventana para crear el partid
                                respuesta = crearPartido(datos, j);
                                break;
                            case "unirsePartido":
                                respuesta = unirsePartido(j.getIdUsuario(),Integer.parseInt(datos.get("idPartido")), datos.get("equipo")); // equipo solo puede ser equipo1 o equipo2
                                break;
                            case "abandonarPartido": // solo se podrá abandonar el partido si quedan mas de 24 horas para el partido
                                respuesta = abandonarPartido(j.getIdUsuario(), Integer.parseInt(datos.get("idPartido")));
                                break;
                            case "masInfoPartido":
                                respuesta = verMasInfoPartido(Integer.parseInt(datos.get("idPartido")));
                                break;
                            case "verMisPartidos":
                                respuesta = verMisPartidos(j.getIdUsuario(), datos.get("estado")); // para que el usuario pueda filtrar por estado (abierto o terminado)
                                break;
                            case "pasarPartidoFinalizado":
                                respuesta = partidoFinalizado(j.getIdUsuario(), Integer.parseInt(datos.get("idPartido")));
                                break;
                            case "votarJugadores": // el usuario vota punto a cada jugador, el creador envia goles y asistencias de cada uno y al mvp 
                                List<VotacionJugador> votaciones = mapper.convertValue(((Map<String,Object>) mensajeMapita.get("data")).get("votaciones"),new TypeReference<List<VotacionJugador>>() {});
                                respuesta = votarJugadores(j.getIdUsuario(), Integer.parseInt(datos.get("idPartido")), votaciones);
                                break; // el codigo de respuesta si ha votado correctamente inmediatmente se deshabilitara el boton de votar en ese partido
                        }
                        break;
                case "salirAplicacion":
                        salirAplicacion(j);
                        break;
                case "inventarioJugador":
                        respuesta = obtenerInventarioJugador(j.getIdUsuario()); //puedo aprovechar que el JugadorSistema tiene la id del usuario del que esta registrado y que aqui solo se puede llegar si esstas registrado
                        break;
                case "verPerfilJugador":
                        //esto es una funcion para cuando le demos click a cualquier foto de jugador pues en la interfaz veremos el perfil y esta es la funcion que se encarga
                        respuesta = verMasInfoUsuario(Integer.parseInt(datos.get("idJugador")));
                        break;
                case "cosmeticos": // esto va a ser la funcion como partidos pero para todo lo relacionado con cosmeticos abrir sobres, vender cosas en el mercado, ver mercado etc
                        String opcionCosmeticos = datos.get("tipoCosmeticos");
                        System.out.println("TeamUp|MensajeInterno|Has llegado a partidos y la opcion partidos es: " + opcionCosmeticos);
                        switch (opcionCosmeticos) {
                            case "abrirSobre" : // opcion cambiada antes se llamaba primera carga, aqui directamente le paso tambien los filtros y si se recarga se vuelve aqui
                                // recibimos id del sobre que quiere el usuario abrir, hacemos la simulacion de lo que toca devolvemos lo que ha tocado simple, se comprueba si se puede comprar
                                break; // en esta funcion va implicito comprar si le damos a abrir se recibe tambien el precio del sobre
                            case "verSobres":
                                //devolvemos todos los sobres disponibles con su precio id y las cosas que lo formen como foto etc nombre esta funcion se ejecuta siempre que el usuario entra en tienda
                                break;
                            case "cosmeticosConseguidos": // usuario entra en esta funcion en cuanto le da a guardar en o recoger todo no se como le llamare a esta funcion, pero el usuario entra cuando ha abierto el sobre, esto es para recoger todo 
                                break;
                            case "mercado": // merrcado funcionamiento >> Usuario pone a la venta algo eso pasa a estar en la tabla mercado y "desaparece del inventario del usuario" el usuario en la pestaña mercado podrá ver mis articulos y cada articulo irá con la id del usuario por lo tanto si alguien compra algo, el usuario recibe las monedas automaticamente, en mis articulos el usuario podrá quitar el articulo de la venta, se paga con monedas 
                                break; // aqui imitaremos lo que hicimos en partido y pondremoss filtro  de calidad
                            case "comprarArticulo": // se recibe la id del articulo en mercado, y la id del usuario directamente aqui comprobamos si el usuario tiene monedas suficinetes y si tiene lo compra, desaparece del mercado y se pasa al inventario del usuario
                                break;
                            case "venderArticulo": // se recibe id del usuario y id del aarticulo del inventario y el precio, se comprueba si se puede vender el articulo y si se puede lo pone en venta (se añade al mercado y se quita del inventario del usuario)
                                break;
                            case "quitarArticulo": // pasamos idArticulo y idUsuario supongo y lo quitariamos y lo devolveriamos al inventario del jugador
                        }   
                    break;
            }
        } catch (Exception em) {
            System.out.println("TeamUp|Error|EM5" + em.getMessage());
        }

        return respuesta;
    }

    public String votarJugadores(int idUsuario, int idPartido, List<VotacionJugador> votaciones) {
        return sv.getBaseDatosManager().votarJugadores(idUsuario, idPartido, votaciones);
    }

    public String partidoFinalizado(int idUsuario, int idPartido ) {
        return sv.getBaseDatosManager().partidoFinalizado(idUsuario,  idPartido);
    }

    public String verMisPartidos(int idUsuario,  String estado) {
        System.out.println("TeamUp|MensajeInterno| Has entrado en partidos del usuario");
        return sv.getBaseDatosManager().obtenerPartidosUsuario(idUsuario,  estado);
    }

    public String verMasInfoUsuario(int idUsuario) {
        return sv.getBaseDatosManager().verPerfilJugador(idUsuario);
    }

    public String verMasInfoPartido(int idPartido) {
        return sv.getBaseDatosManager().verMasInfoPartido(idPartido);
    }

    public String unirsePartido(int idUsuario, int idPartido, String equipo) {
        return sv.getBaseDatosManager().unirsePartido(idUsuario, idPartido, equipo);
    }

    public String abandonarPartido(int idUsuario, int idPartido) {
        return sv.getBaseDatosManager().abandonarPartido(idUsuario, idPartido);
    }

    public String partidosPrimeraCarga(String ciudad, String soloverificados) {
        System.out.println("TeamUp|MensajeInterno| Has entrado en partidos primera carga");
        return sv.getBaseDatosManager().obtenerPartidos(ciudad, soloverificados);
    }

    public String crearPartido(Map<String,String> mapaDatos, JugadorSistema j) {
        return sv.getBaseDatosManager().registarPartido(mapaDatos, j);
    }

    public String obtenerRangos() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("rangos", listaRangos);
        String respuesta = AyudanteConteston.contestarTodoBien("Er", "Rangos enviados", datos);


        return respuesta;
    }

    public String obtenerRanking(String rangoFiltro, String mayorMenor) { // rango filtro el numero del rango 1-4 y si es mayor o menor (mayor/menor)
        return sv.getBaseDatosManager().obtenerListaJugadoresRango(rangoFiltro, mayorMenor);
    }

    public String obtenerInventarioJugador(int idUsuario) {
        return sv.getBaseDatosManager().obtenerInventarioUsuario(idUsuario);
    }

    public String iniciarSesion(Map<String,String> mapaDatos, JugadorSistema j) { 
            String respuesta = "";
            System.out.println("TeamUp|MensajeInterno|Ha llegado hasta aqui (iniciar sesion) con " + mapaDatos.get("correo"));
            
            if (mapaDatos.get("remember").equals("si")) {
                respuesta = sv.getBaseDatosManager().iniciarSesionToken(mapaDatos.get("selector"), mapaDatos.get("token"), j);
            } else if (mapaDatos.get("remember").equals("no")) {
                respuesta = sv.getBaseDatosManager().iniciarSesionContrasenia(mapaDatos.get("correo"), mapaDatos.get("contrasenia"), j);
            }


            if (j.getIdUsuario() != -33 && !jugadores.contains(j) ) { 
                jugadores.add(j);
            } else {
                j.setIdUsuario(-33);
                respuesta = AyudanteConteston.contestarError("eJyC", "El usuario esta conectado desde otro dispositivo");
            }
            


            return respuesta;
    }

    public String registrarUsuario(Map<String,String> mapaDatos, JugadorSistema j) { 
        String respuesta = "TeamUp|Directriz|errOe";
        System.out.println("TeamUp|MensajeInterno|Ha llegado hasta aqui (registrar) con " + mapaDatos.get("nombre"));



        respuesta = sv.getBaseDatosManager().registrarUsuario(mapaDatos.get("nombre"), mapaDatos.get("contrasenia"), mapaDatos.get("correo"), mapaDatos.get("posicion1"), mapaDatos.get("posicion2"),mapaDatos.get("recordarme"), j);


        if (j.getIdUsuario() != -33) 
            jugadores.add(j);
        


        return respuesta;

    }
}


