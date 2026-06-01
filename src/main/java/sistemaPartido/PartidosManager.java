package sistemaPartido;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import ClasesServer.BaseDatosManager;
import ClasesServer.JugadorSistema;
import clases.AyudanteConteston;
import clases.Participante;
import clases.PartidoSimplificado;
import clases.VotacionJugador;
import claseshibernate.Carta;
import claseshibernate.Participacion;
import claseshibernate.Partido;
import claseshibernate.Usuario;
import claseshibernate.Votacion;
import sistemaCarta.CartaManager;


public class PartidosManager {
    public static final int CARACTERES_MAXIMO_TITULO = 100;
    public static final int JUGADORES_MAXIMOS = 14;
    private static PartidosManager partidosManager;

    private PartidosManager() {
        System.out.println("TeamUp|MensajeIntereno|Modulos partidos creado");
    }

    public static PartidosManager getPartidosManager() {
        if (partidosManager == null) 
            partidosManager = new PartidosManager();

        return partidosManager;
    }

    public String registarPartido(Map<String,String> datos, JugadorSistema j, BaseDatosManager bdm) { // Sistema Partido
        String respuesta = "";
        
        if (datos.get("titulo").length() <= CARACTERES_MAXIMO_TITULO) {
            boolean soloVerificados = true;
            Usuario creador = bdm.obtenerUsuarioPorId(j.getIdUsuario());
            if (datos.get("verificados").equals("no"))
                soloVerificados = false;
            int anio = Integer.parseInt(datos.get("anio"));
            int mes = Integer.parseInt(datos.get("mes"));
            int dia = Integer.parseInt(datos.get("dia"));
            int hora = Integer.parseInt(datos.get("hora"));
            int minutos = Integer.parseInt(datos.get("minutos"));

            LocalDateTime fecha = LocalDateTime.of(anio, mes, dia, hora, minutos);
            Partido p = new Partido(datos.get("titulo"), datos.get("ubicacion"), Integer.parseInt(datos.get("precio")), datos.get("ciudad"), creador, soloVerificados, fecha);
            bdm.persistirObjeto(p);
            bdm.getMapaConcurrencia().put(p.getId(), p);
            Participacion participacion = new Participacion(bdm.obtenerUsuarioPorId(j.getIdUsuario()), p, "equipo1"); // hay que dar de alta al mismo creador del partido el usuario creador siempre va a estar en el equipo1 porque realmente es indiferente en que equipo este el creador
            bdm.persistirObjeto(participacion);
            respuesta = AyudanteConteston.contestarTodoBien("pCC", "El partido se ha creado correctamente", null);
        } else
            respuesta = AyudanteConteston.contestarError("erTlnv", "Titulo tiene mas de 100 caracteres");
            
        return respuesta;        
        }

        public String obtenerPartidos(String ciudad, String soloVerificados, BaseDatosManager bdm) {
            String respuesta = "";
            try (Session session = bdm.getSessionFactory().openSession()){
                System.out.println("TeamUp|MensajeInterno|Has llegado a obtener partido");
                String consulta = "FROM Partido WHERE estado = :estado";

                if (soloVerificados.equals("si")) {
                    consulta = consulta + " AND soloVerificados = true";
                }

                if (!ciudad.equals("todas")) {
                    consulta = consulta + " AND ciudad = :ciudad";
                }


                Query<Partido> q = session.createQuery(consulta, Partido.class);
                q.setParameter("estado", "abierto");
                if (!ciudad.equals("todas")) {
                    q.setParameter("ciudad", ciudad);
                }
                List<Partido> partidos = q.list();
                List<PartidoSimplificado> listaPartidos = new ArrayList<>();
                for (Partido p : partidos) {     // idPartido, tituloPartido, ubicacion, precio, fecha, estado, soloVerificados, nombreUsuario, idUsuario, fotoUsuario
                    System.out.println("TeamUp|MensajeInterno| Estas en el bucle simplificador y estas simplificando el partido con titulo: " + p.getTitulo());
                    int dia = p.getFecha().getDayOfMonth();
                    int mes = p.getFecha().getMonthValue();
                    int anio = p.getFecha().getYear();
                    int hora = p.getFecha().getHour();
                    int minutos = p.getFecha().getMinute();
                    PartidoSimplificado pS = new PartidoSimplificado(p.getId(), p.getTitulo(), p.getUbicacion(), p.getPrecio(), dia, mes, anio, hora, minutos, p.getEstado(), p.isSoloVerificados(), p.getCreador().getNombre(), p.getCreador().getId(), p.getCreador().getFotoPerfil(), p.getCiudad());
                    listaPartidos.add(pS);
                }

                Map<String, Object> datos = new HashMap<>();
                datos.put("partidos", listaPartidos);
                respuesta = AyudanteConteston.contestarTodoBien("pDc", "Partidos devueltos correctamente", datos);


        }
        return respuesta;
    }

    public String abandonarPartido(int idUsuario, int idPartido, BaseDatosManager bdm) {
        String respuesta = AyudanteConteston.contestarError("nSHPAP", "No se ha podido abanondar el partido porque quedan menos de 24 horas para que empiece");

        synchronized (bdm.getMapaConcurrencia().get(idPartido)) {
            if (comprobarAbandonarPartido(idUsuario, idPartido, bdm)) {
                try (Session session = bdm.getSessionFactory().openSession()){
                    Transaction transaction = session.beginTransaction();
                    Partido partido  = bdm.obtenerPartidoPorId(idPartido);
                    Participacion participacion = bdm.obtenerParticipacionId(idUsuario, idPartido);
                    session.remove(participacion);
                    
                    if (partido.getEstado().equals("lleno")) {
                        partido.setEstado("abierto");
                        bdm.getMapaConcurrencia().put(idPartido, partido);
                        bdm.actualizarObjeto(partido);
                    }

                    try {
                        transaction.commit();
                        System.out.println("TeamUp|MensajeInterno|El usuario ya no forma parte de ese partido.");
                        respuesta = AyudanteConteston.contestarTodoBien("uHAEPC", "El usuario ha abandonado el partido correctamente", null);
                    } catch (IllegalStateException em) {
                        transaction.rollback();
                        System.out.println("TeamUp|Error|EM2|.");
                    }

                }
            }
            }

        return respuesta;
    }

    public String unirsePartido(int idUsuario, int idPartido, String equipo, BaseDatosManager bdm) {
        String respuesta = AyudanteConteston.contestarError("nPUP", "No has podido unirte al partido porque ya estas dentro del partido o esta completo");
        synchronized (bdm.getMapaConcurrencia().get(idPartido)) {
            if (comprobarUnirsePartido(idUsuario, idPartido, bdm)) {
                try (Session session = bdm.getSessionFactory().openSession()){
                    Participacion p = new Participacion(bdm.obtenerUsuarioPorId(idUsuario), bdm.obtenerPartidoPorId(idPartido), equipo);
                    bdm.persistirObjeto(p);
                    respuesta = AyudanteConteston.contestarTodoBien("jSHUC", "Jugador se ha unido correctamente", null);
                    if (bdm.obtenerParticipantes(idPartido).size() == JUGADORES_MAXIMOS) {
                        System.out.println("TeamUp|MensajeInterno| El partido ha llegado a 14 jugadores por lo tanto pasa a estado lleno :)");
                        Partido partido = bdm.obtenerPartidoPorId(idPartido);
                        partido.setEstado("lleno");
                        bdm.actualizarObjeto(partido);
                    }

                }
            }
        }
        
        return respuesta;

    }

    public boolean comprobarAbandonarPartido(int idUsuario, int idPartido, BaseDatosManager bdm) {
        boolean sePuedeAbandonar = true;
        Partido partido = bdm.obtenerPartidoPorId(idPartido);
        List<Participacion> p = bdm.obtenerParticipantes(idPartido);

        if (partido.getFecha().isBefore(LocalDateTime.now().plusHours(24))) 
            sePuedeAbandonar =  false;
        
        if (partido.getEstado().equals("terminado"))
            sePuedeAbandonar = false;

        if (!bdm.comprobarUsuarioParticipaEnPartido(idUsuario, p))
            sePuedeAbandonar = false;

        if (partido.getCreador().getId() == idUsuario)
            sePuedeAbandonar =  false; // el creador no puede abandonar un partido que ha creado el mismo

        return sePuedeAbandonar;
    }

    public String verMasInfoPartido(int idPartido, BaseDatosManager bdm) {
        String respuesta = "";

        try (Session session = bdm.getSessionFactory().openSession()){

            Query<Participacion> q = session.createQuery("FROM Participacion WHERE partido.id = :idPartido",Participacion.class);
            Partido partido = bdm.obtenerPartidoPorId(idPartido);
            q.setParameter("idPartido", idPartido);

            List<Participacion> participantes = q.list();
            List<Participante> listaSimplificada = new ArrayList<>();
            Map<String,Object> datos = new HashMap<>();


            for (Participacion p : participantes) { // nombre, idUsuario, foto, equipo, goles, asistencia, mvp
                listaSimplificada.add(new Participante(p.getUsuario().getNombre(), p.getUsuario().getId(), p.getUsuario().getFotoPerfil(), p.getEquipo(), p.getGoles(), p.getAsistencias(), p.isMvp()));
            }

            datos.put("participantes", listaSimplificada);
            datos.put("estadoPartido", partido.getEstado());
            datos.put("idPartido", partido.getId());
            datos.put("creadorPartido", partido.getCreador().getId());
            respuesta = AyudanteConteston.contestarTodoBien("dDPD", "Se han devuelto correctamente los datos del partido", datos);
        }

        return respuesta;
    }

    public String obtenerPartidosUsuario(int idUsuario,  String estado, BaseDatosManager bdm) {
        String respuesta = AyudanteConteston.contestarError("eDPDU", "El usuario no participa en ningun partido con ese estado");
        List<Participacion> partidosUsuario = bdm.obtenerParticipacionesUsuario(idUsuario);
        List<PartidoSimplificado> listaPartidos = new ArrayList<>();
        if (!partidosUsuario.isEmpty()) {
            for (Participacion parti : partidosUsuario) {     // idPartido, tituloPartido, ubicacion, precio, fecha, estado, soloVerificados, nombreUsuario, idUsuario, fotoUsuario
                Partido p = parti.getPartido();
                if (p.getEstado().equals(estado)) {    
                    int dia = p.getFecha().getDayOfMonth();
                    int mes = p.getFecha().getMonthValue();
                    int anio = p.getFecha().getYear();
                    int hora = p.getFecha().getHour();
                    int minutos = p.getFecha().getMinute();
                    PartidoSimplificado pS = new PartidoSimplificado(p.getId(), p.getTitulo(), p.getUbicacion(), p.getPrecio(), dia, mes, anio, hora, minutos, p.getEstado(), p.isSoloVerificados(), p.getCreador().getNombre(), p.getCreador().getId(), p.getCreador().getFotoPerfil(), p.getCiudad());
                    listaPartidos.add(pS);
                }
            }

            if (!listaPartidos.isEmpty()) {
                Map<String, Object> datos = new HashMap<>();
                datos.put("partidos", listaPartidos);
                respuesta = AyudanteConteston.contestarTodoBien("pDUC", "Partidos devueltos correctamente", datos);
            }
        }
        return respuesta;
    }

    public String partidoFinalizado(int idUsuario, int idPartido, BaseDatosManager bdm) {
        String respuesta = AyudanteConteston.contestarError("nSHPTP", "No se ha podido terminar el partido porque no ha empezado o no eres el creador");
        Partido partido = bdm.obtenerPartidoPorId(idPartido);
        if (LocalDateTime.now().isAfter(partido.getFecha()) && partido.getCreador().getId() == idUsuario) { // cambiado solo con el ! para comprobar, quita EL !
            partido.setEstado("terminado");
            bdm.actualizarObjeto(partido);
            respuesta = AyudanteConteston.contestarTodoBien("pTC", "Partido terminado correctamente", null);
        }
        return respuesta;
    }


    public String votarJugadores(int idUsuario, int idPartido, List<VotacionJugador> votaciones, String equipoGanador, BaseDatosManager bdm) {
        String respuesta = AyudanteConteston.contestarError("nSHPVC", "No se ha podido votar correctamente");
        Partido partidoObjeto = bdm.obtenerPartidoPorId(idPartido);
        synchronized (bdm.getMapaConcurrencia().get(idPartido)) { // Aqui empieza la mgia de la concurrencia
            for (VotacionJugador vJ : votaciones) {
                Participacion participacion = bdm.obtenerParticipacionId(vJ.getIdUsuario(), idPartido);
                Votacion v = new Votacion(bdm.obtenerPartidoPorId(idPartido), bdm.obtenerUsuarioPorId(idUsuario), bdm.obtenerUsuarioPorId(vJ.getIdUsuario()), vJ.getPuntuacion(), vJ.getGoles(), vJ.getAsistencias(), vJ.isMvp()); 
                bdm.persistirObjeto(v);
                if (partidoObjeto.getCreador().getId() == idUsuario) { // aqui solo entra una vez porque el creador solo puede votar a un mvp
                    if (v.isMvp()) {
                        partidoObjeto.setMvp(v.getVotado());
                        bdm.actualizarObjeto(partidoObjeto);
                        Usuario u = bdm.obtenerUsuarioPorId(vJ.getIdUsuario());
                        u.setMvps(u.getMvps()  + 1);
                        participacion.setMvp(true);
                        bdm.actualizarObjeto(u);
                    }
                    participacion.setGoles(v.getGoles());
                    participacion.setAsistencias(v.getAsistencias());
                    Usuario u = bdm.obtenerUsuarioPorId(vJ.getIdUsuario());
                    u.setGoles(u.getGoles() + v.getGoles());
                    u.setAsistencias(u.getAsistencias() + v.getAsistencias());
                    bdm.actualizarObjeto(u);
                    bdm.actualizarObjeto(participacion);
                    
                }
            }
            
            if (!equipoGanador.equals("-33")) {
                partidoObjeto.setEquipoGanador(equipoGanador);
                bdm.actualizarObjeto(partidoObjeto);
            }

            respuesta = AyudanteConteston.contestarTodoBien("sHPVC", "Se ha podido votar correctamente", null);

            if (bdm.obtenerVotacionesPartido(idPartido).size() == 183) { // 183 porque en total x partido se esperan 183 votos, el creador vota a si mismo porque tendria que poner sus goles y asistencias o si es el mvp, pero no se podra votar su puntuacion
                Partido p = bdm.obtenerPartidoPorId(idPartido); // no uso constante en este dato porque no puede ser otro numero que no sea este ya que la aplicacion gira en torno a partidos de futbol 7 por lo tanto si cambiaramos futbol 7 tendriamos que cambiar toda la aplicacion por lo tanto no tendria sentido
                bdm.actualizarMediaEquipos(idPartido);
                p.setEstado("completado");
                bdm.actualizarObjeto(p);
                bdm.getMapaConcurrencia().remove(idPartido);
                bdm.limpiadorVotos(idPartido);
            }
        }

        return respuesta;
    }

    public boolean comprobarUnirsePartido(int idUsuario, int idPartido, BaseDatosManager bdm) {
        boolean sePuedeUnir = true;
        Partido partido = bdm.obtenerPartidoPorId(idPartido);
        Usuario u = bdm.obtenerUsuarioPorId(idUsuario);
        
        List<Participacion> p = bdm.obtenerParticipantes(idPartido);

        if (bdm.comprobarUsuarioParticipaEnPartido(idUsuario, p))
            sePuedeUnir = false;

        if (partido.isSoloVerificados()) 
            if (!u.isVerificado())
                sePuedeUnir = false;

        if (!partido.getEstado().equals("abierto")) 
            sePuedeUnir = false;


        return sePuedeUnir;
    }

    public String recogerRecompensas(int idUsuario, int idPartido, BaseDatosManager bdm) { // recoger recompensa solo se desbloquea en la interfaz cuando el estado es completado ( el del partido)
        String respuesta = AyudanteConteston.contestarError("rEC", "Las recompensas ya han sido recogidas");
        Partido p = bdm.obtenerPartidoPorId(idPartido);
        Participacion participacion = bdm.obtenerParticipacionId(idUsuario, idPartido);
        Carta c = bdm.obtenerCartaUsuario(idUsuario);
        String posicionPredilecta = bdm.obtenerUsuarioPorId(idUsuario).obtenerPosicionPredilecta();
        if (!participacion.isRecompensasRecogidas()) {
            //recompensa para el mvp
            if (p.getMvp().getId() == idUsuario) {
                bdm.darSobre(idUsuario, bdm.obtenerSobre("Sobre MVP"), 1);
                bdm.darMonedas(idUsuario, 300);
                bdm.darPuntosRango(idUsuario, 50);
                c.mejorarEstadistica(CartaManager.getCartaManager().obtenerBonus(posicionPredilecta).get(0), 2);
                c.subidaTodasEstadisticas(1);
            }

            //recompensa para todos los usuarios

            Usuario u = bdm.obtenerUsuarioPorId(idUsuario);
            u.setPartidosJugados(u.getPartidosJugados() + 1);
            bdm.actualizarObjeto(u);
            if (!u.esPortero()) { 
                c.mejorarEstadistica(CartaManager.getCartaManager().obtenerBonus(u.getPosicion1()).get(0), 1);
                c.mejorarEstadistica(CartaManager.getCartaManager().obtenerBonus(u.getPosicion2()).get(0), 1);
            } else {
                c.mejorarEstadistica(CartaManager.getCartaManager().obtenerEstadisticaAleatoriaPortero(), 2);
            }

            if (participacion.getAsistencias() > 0) {
                c.subirPorAsistencia();
            } 
            
            if (participacion.getGoles() > 0) {
                c.subirPorGol();
            }


            bdm.darReputacion(idUsuario, 50);
            if (p.getEquipoGanador().equals("empate")) {
                bdm.darPuntosRango(idUsuario, 25);
                if (u.esPortero()) {
                    c.mejorarEstadistica(CartaManager.getCartaManager().obtenerEstadisticaAleatoriaPortero(), 2);
                } else 
                    c.mejorarEstadistica(CartaManager.getCartaManager().obtenerEstadisticaAleatoria(), 2);
            } else if (participacion.getEquipo().equals(p.getEquipoGanador())) {
                bdm.darPuntosRango(idUsuario, 100);
                bdm.darMonedas(idUsuario, 250);
                bdm.darSobre(idUsuario, bdm.obtenerSobre("Sobre Victoria"), 1);
                if (participacion.getEquipo().equals("equipo1")) {
                    if (p.getMediaEquipo1() < p.getMediaEquipo2())
                        bdm.darPuntosRango(idUsuario, 50);
                } else {
                    if (p.getMediaEquipo2() < p.getMediaEquipo1())
                        bdm.darPuntosRango(idUsuario, 50);
                }
                if (u.esPortero()) {
                    c.mejorarEstadistica(CartaManager.getCartaManager().obtenerEstadisticaAleatoriaPortero(), 2);
                    c.mejorarEstadistica(CartaManager.getCartaManager().obtenerEstadisticaAleatoria(), 2);
                } else {
                    c.mejorarEstadistica(CartaManager.getCartaManager().obtenerBonus(u.getPosicion1()).get(0), 1);
                    c.mejorarEstadistica(CartaManager.getCartaManager().obtenerBonus(u.getPosicion2()).get(0), 1);
                    c.mejorarEstadistica(CartaManager.getCartaManager().obtenerEstadisticaAleatoria(), 2);
                    c.mejorarEstadistica(CartaManager.getCartaManager().obtenerEstadisticaAleatoria(), 2);
                }

            }
            participacion.setRecompensasRecogidas(true);
            bdm.actualizarObjeto(c);
            bdm.actualizarObjeto(participacion);
            respuesta = AyudanteConteston.contestarTodoBien("rEC", "Recompensas entregadas correctamentes", null); // alomejor devolver en fomrato json todo lo que ha consegudio el usuario y enseñarlo en una pantalla
        }
        return respuesta;
    }
 

}
