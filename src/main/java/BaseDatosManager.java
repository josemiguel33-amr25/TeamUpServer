
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;

import clases.Participante;
import clases.PartidoSimplificado;
import clases.UsuarioSimplificado;
import clases.VotacionJugador;
import claseshibernate.Carta;
import claseshibernate.Cosmetico;
import claseshibernate.Inventario;
import claseshibernate.InventarioCosmetico;
import claseshibernate.Participacion;
import claseshibernate.Partido;
import claseshibernate.RememberToken;
import claseshibernate.Usuario;
import claseshibernate.Votacion;

public class BaseDatosManager {
    private SessionFactory sessionFactory;
    private Map<Integer, Object> mapaConcurrencia = new ConcurrentHashMap<>();

    private final Integer BASE_PORTER = 70;
    private final String[] ESTADISTICAS_CAMPO = {"ritmo", "tiro","pase","regate","defensa","fisico"};




    public BaseDatosManager() {
        //ERROR tonto que pasaba aqui, en el session factory creaba un objeto en vez de usar el atributo, madre mia
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.SEVERE);
        sessionFactory = HibernateUtil.getSessionFactory();

    }


    public boolean comprobarConexion() {
        boolean conexionBandera;
        Session session = sessionFactory.openSession();
        
        try {
            session.createQuery("select 1").uniqueResult();
            conexionBandera = true;
        } catch (Exception em) {
            System.out.println("TeamUp|Error|EM2");
            conexionBandera = false;
        }

        return conexionBandera;
    }


    private boolean comprobarUsuarioExiste(String nombre) {
        boolean usuarioExiste = false;
        Session session = sessionFactory.openSession();	
        Query<Usuario> q = session.createQuery("from Usuario where nombre = :nombre", Usuario.class);

        q.setParameter("nombre", nombre);
        List<Usuario> lista = q.list();
        if (!lista.isEmpty()) {
            usuarioExiste  = true;
        }

        return usuarioExiste;
    }

    

    private boolean comprobarCorreoAsociado(String correo) {
        boolean correoAsociado = false;
        
        try (Session session = sessionFactory.openSession()) {
            Query<Usuario> q = session.createQuery("from Usuario where correo = :correo", Usuario.class);

            q.setParameter("correo", correo);
            List<Usuario> lista = q.list();
            if (!lista.isEmpty()) {
                correoAsociado  = true;
            }
        }


        return correoAsociado;
    }

    public void verificadorCuentas() {
        int contadorUsuariosVerificados = 0;
        try (Session session = sessionFactory.openSession()) {
            List<Usuario> usuarios = session.createQuery("FROM Usuario u WHERE u.verificado = false", Usuario.class).list();
            for (Usuario uSel: usuarios) {
                if (uSel.getFechaCreacion().plusDays(14).isBefore(LocalDateTime.now())) {
                    uSel.setVerificado(true);
                    contadorUsuariosVerificados = contadorUsuariosVerificados + 1;
                    actualizarObjeto(uSel);
                }
            }

        }
        System.out.println("TeamUp|MensajeInterno| Usuarios verificados ---> " + contadorUsuariosVerificados);
    }

    public int obtenerId(String nombre) {
        return obtenerUsuario(nombre).getId();
    }

    public void verificadorExpiracionToken() {
        int contadorTokenExpirados = 0;
        try (Session session = sessionFactory.openSession()){
            List<RememberToken> tokens = session.createQuery("FROM RememberToken", RememberToken.class).list();
            for (RememberToken t : tokens) {
                if (!LocalDateTime.now().isBefore(t.getFechaExpiracion())) {
                    contadorTokenExpirados++;

                    Transaction transaction = session.beginTransaction();
                    session.remove(t);

                    try {
                        transaction.commit();
                        System.out.println("TeamUp|MensajeInterno|Token Eliminado.");
                    } catch (IllegalStateException em) {
                        transaction.rollback();
                        System.out.println("TeamUp|Error|EM2|.");
                    }
                }
            }
        }
        System.out.println("TeamUp|MensajeInterno|Tokens eliminados por caducados -----> " + contadorTokenExpirados);
    }

    public String obtenerListaJugadoresRango(String rango, String mayorMenor) {
        String respuesta = "";

        System.out.println("TeamUp|MensajeInterno| Entro en obtener lista de jugadores por rango con: " +rango + " y el usuario quiere filtrar por " + mayorMenor);
        try (Session session = sessionFactory.openSession()){  
            Query<Usuario> q;
            if (mayorMenor.equals("mayor")) {
                q = session.createQuery("FROM Usuario WHERE rango = :rango ORDER BY puntos DESC",Usuario.class);
                q.setParameter("rango", rango);
            } else {
                q = session.createQuery("FROM Usuario WHERE rango = :rango ORDER BY puntos ASC",Usuario.class);
                q.setParameter("rango", rango);
            }

            List<Usuario> jugadores = q.list();
            System.out.println("TeamUp|MensajeInterno|Lista de jugadores con: " + jugadores.size() + " jugadores y el primer jugador es:  " + jugadores.get(0).getNombre());
            List<UsuarioSimplificado> listaUsuariosSimplificada = new ArrayList<>();

            for (Usuario u : jugadores) {
                listaUsuariosSimplificada.add(new UsuarioSimplificado(u.getNombre(), rango, u.getPuntos(), u.getReputacion(), u.getGoles(), u.getAsistencias(), u.getMvps(), u.isVerificado()));
            }

            Map<String, Object> datos = new HashMap<>();
            datos.put("jugadores", listaUsuariosSimplificada);
            respuesta = AyudanteConteston.contestarTodoBien("rCc", "Ranking creado correctamente", datos);

        }


        return respuesta;
    }

    public String registarPartido(Map<String,String> datos, JugadorSistema j) {
        String respuesta = "";
        
        try (Session session = sessionFactory.openSession()){
            if (datos.get("titulo").length() <= Servidor.caracteresMaximoTitulo) {
                boolean soloVerificados = true;
                Usuario creador = obtenerUsuarioPorId(j.getIdUsuario());
                if (datos.get("verificados").equals("no"))
                    soloVerificados = false;
                int anio = Integer.parseInt(datos.get("anio"));
                int mes = Integer.parseInt(datos.get("mes"));
                int dia = Integer.parseInt(datos.get("dia"));
                int hora = Integer.parseInt(datos.get("hora"));
                int minutos = Integer.parseInt(datos.get("minutos"));

                LocalDateTime fecha = LocalDateTime.of(anio, mes, dia, hora, minutos);
                Partido p = new Partido(datos.get("titulo"), datos.get("ubicacion"), Integer.parseInt(datos.get("precio")), datos.get("ciudad"), creador, soloVerificados, fecha);
                persistirObjeto(p);
                mapaConcurrencia.put(p.getId(), p);
                Participacion participacion = new Participacion(obtenerUsuarioPorId(j.getIdUsuario()), p, "equipo1"); // hay que dar de alta al mismo creador del partido el usuario creador siempre va a estar en el equipo1 porque realmente es indiferente en que equipo este el creador
                persistirObjeto(participacion);
                respuesta = AyudanteConteston.contestarTodoBien("pCC", "El partido se ha creado correctamente", null);
            } else
                respuesta = AyudanteConteston.contestarError("erTlnv", "Titulo tiene mas de 100 caracteres");
        
        }


        return respuesta;
    }

    public String votarJugadores(int idUsuario, int idPartido, List<VotacionJugador> votaciones) {
        String respuesta = AyudanteConteston.contestarError("nSHPVC", "No se ha podido votar correctamente");
        
        synchronized (mapaConcurrencia.get(idPartido)) { // Aqui empieza la mgia de la concurrencia
        
            for (VotacionJugador vJ : votaciones) {
                Votacion v = new Votacion(obtenerPartidoPorId(idPartido), obtenerUsuarioPorId(idUsuario), obtenerUsuarioPorId(vJ.getIdUsuario()), vJ.getPuntuacion(), vJ.getGoles(), vJ.getAsistencias(), vJ.isMvp()); 
                persistirObjeto(v);
            }

            respuesta = AyudanteConteston.contestarTodoBien("sHPVC", "Se ha podido votar correctamente", null);

            if (obtenerVotacionesPartido(idPartido).size() == 182) { // 182 porque en total x partido se esperan 182 votos
                Partido p = obtenerPartidoPorId(idPartido);
                p.setEstado("completado");
                actualizarObjeto(p);
                mapaConcurrencia.remove(idPartido);
            }


        }

        return respuesta;
    }

    private List<Votacion> obtenerVotacionesPartido(int idPartido) {
        List<Votacion> votaciones = new ArrayList<>();

        try (Session session = sessionFactory.openSession()) {

            Query<Votacion> q = session.createQuery("FROM Votacion WHERE partido.id = :idPartido",Votacion.class);

            q.setParameter("idPartido", idPartido);

            votaciones = q.list();
        }

        return votaciones;
    }

    public String verMasInfoPartido(int idPartido) {
        String respuesta = "";

        try (Session session = sessionFactory.openSession()){

            Query<Participacion> q = session.createQuery("FROM Participacion WHERE partido.id = :idPartido",Participacion.class);
            Partido partido = obtenerPartidoPorId(idPartido);
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


    public void rellenadorMapaConcurrencia() {
        List<Partido> partidos = obtenerPartidos();
        //List<Cosmetico> cosmeticosMercado = obtenerCosmeticosMercado hay que hacerlo
        for (Partido partido : partidos) 
            mapaConcurrencia.put(partido.getId(), partido);

        System.out.println("TeamUp|MensajeInterno| Numero de partidos en el mapa de concurrencia " + partidos.size());


    }


    private List<Partido> obtenerPartidos() {
        List<Partido> partidos = null;

        try (Session session = sessionFactory.openSession()) {

            Query<Partido> q = session.createQuery("FROM Partido WHERE estado = :abierto OR estado = :terminado", Partido.class );

            q.setParameter("abierto", "abierto");
            q.setParameter("terminado", "terminado");

            partidos = q.list();
        }

        return partidos;
    }
    

    public String partidoFinalizado(int idUsuario, int idPartido) {
        String respuesta = AyudanteConteston.contestarError("nSHPTP", "No se ha podido terminar el partido porque no ha empezado o no eres el creador");
        Partido partido = obtenerPartidoPorId(idPartido);
        if (LocalDateTime.now().isAfter(partido.getFecha())) { // cambiado solo con el ! para
            partido.setEstado("terminado");
            actualizarObjeto(partido);
            respuesta = AyudanteConteston.contestarTodoBien("pTC", "Partido terminado correctamente", null);
        }
        return respuesta;
    }


    private List<Participacion> obtenerParticipacionesUsuario(int idUsuario) {

        List<Participacion> participaciones = null;

        try (Session session = sessionFactory.openSession()) {

            Query<Participacion> q = session.createQuery("FROM Participacion WHERE usuario.id = :idUsuario",Participacion.class);
            q.setParameter("idUsuario", idUsuario);

            participaciones = q.list();
        }

        return participaciones;
    }

    public String obtenerPartidosUsuario(int idUsuario,  String estado) {
        String respuesta = AyudanteConteston.contestarError("eDPDU", "El usuario no participa en ningun partido con ese estado");
        List<Participacion> partidosUsuario = obtenerParticipacionesUsuario(idUsuario);
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


    public String obtenerPartidos(String ciudad, String soloVerificados) {
        String respuesta = "";
        try (Session session = sessionFactory.openSession()){
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

    private Participacion obtenerParticipacionId(int idUsuario, int idPartido) {
        Participacion p = null;

        try (Session session = sessionFactory.openSession()){

            Query<Participacion> q = session.createQuery("FROM Participacion " +"WHERE usuario.id = :idUsuario " +"AND partido.id = :idPartido",Participacion.class);

            q.setParameter("idUsuario", idUsuario);
            q.setParameter("idPartido", idPartido);

            List<Participacion> lista = q.list();

            if (!lista.isEmpty()) {
                p = lista.get(0);
            }
            
        }


        return p;
    }

    public String abandonarPartido(int idUsuario, int idPartido) {
        String respuesta = AyudanteConteston.contestarError("nSHPAP", "No se ha podido abanondar el partido porque quedan menos de 24 horas para que empiece");

        synchronized (mapaConcurrencia.get(idPartido)) {
            if (comprobarAbandonarPartido(idUsuario, idPartido)) {
                try (Session session = sessionFactory.openSession()){
                    Transaction transaction = session.beginTransaction();
                    Partido partido  = obtenerPartidoPorId(idPartido);
                    Participacion participacion = obtenerParticipacionId(idUsuario, idPartido);
                    session.remove(participacion);
                    
                    if (partido.getEstado().equals("lleno")) {
                        partido.setEstado("abierto");
                        mapaConcurrencia.put(idPartido, partido);
                        actualizarObjeto(partido);
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

    private boolean comprobarAbandonarPartido(int idUsuario, int idPartido) {
        boolean sePuedeAbandonar = true;
        Partido partido = obtenerPartidoPorId(idPartido);
        List<Participacion> p = obtenerParticipantes(idPartido);

        if (partido.getFecha().isBefore(LocalDateTime.now().plusHours(24))) 
            sePuedeAbandonar =  false;
        
        if (partido.getEstado().equals("terminado"))
            sePuedeAbandonar = false;

        if (!comprobarUsuarioParticipaEnPartido(idUsuario, p))
            sePuedeAbandonar = false;

        if (partido.getCreador().getId() == idUsuario)
            sePuedeAbandonar =  false; // el creador no puede abandonar un partido que ha creado el mismo

        return sePuedeAbandonar;
    }


    public String unirsePartido(int idUsuario, int idPartido, String equipo) {
        String respuesta = AyudanteConteston.contestarError("nPUP", "No has podido unirte al partido porque ya estas dentro del partido o esta completo");


        synchronized (mapaConcurrencia.get(idPartido)) {
            if (comprobarUnirsePartido(idUsuario, idPartido)) {
                try (Session session = sessionFactory.openSession()){
                    Participacion p = new Participacion(obtenerUsuarioPorId(idUsuario), obtenerPartidoPorId(idPartido), equipo);
                    persistirObjeto(p);
                    respuesta = AyudanteConteston.contestarTodoBien("jSHUC", "Jugador se ha unido correctamente", null);
                    if (obtenerParticipantes(idPartido).size() == Servidor.JUGADORES_MAXIMO) {
                        System.out.println("TeamUp|MensajeInterno| El partido ha llegado a 14 jugadores por lo tanto pasa a estado lleno :)");
                        Partido partido = obtenerPartidoPorId(idPartido);
                        partido.setEstado("lleno");
                        actualizarObjeto(partido);
                    }

                }
            }
        }
        
        return respuesta;

    }


    private void actualizarObjeto(Object objeto) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
                session.merge(objeto);
                try {
                    transaction.commit();
                    System.out.println("TeamUp|MensajeInterno|Objeto actualizado perfectamente.");
                } catch (IllegalStateException em) {
                    transaction.rollback();
                    System.out.println("TeamUp|Error|EM2|");
                }
        }
    }


    private List<Participacion> obtenerParticipantes(int idPartido) {
        List<Participacion> participaciones = null;
        try (Session session = sessionFactory.openSession()){
            Query<Participacion> q = session.createQuery("FROM Participacion WHERE partido.id = :idPartido",Participacion.class);

            q.setParameter("idPartido", idPartido);

            participaciones = q.list();
        
        }

        return participaciones;
    }

    private Partido obtenerPartidoPorId(int idPartido) {
        Partido p = null;

        try (Session session = sessionFactory.openSession()){  

            Query<Partido> q = session.createQuery(
                "FROM Partido WHERE id = :idPartido",
                Partido.class
            );

            q.setParameter("idPartido", idPartido);

            List<Partido> lista = q.list();

            if (!lista.isEmpty()) {
                p = lista.get(0);
            }
        }
        return p;
    }

    private boolean comprobarUsuarioParticipaEnPartido(int idUsuario, List<Participacion> p) {
        boolean participa = false;

        for (Participacion participante : p) {
            if (participante.getUsuario().getId() == idUsuario) {
                participa = true;
                break;
            }
        }

        return participa;
    }

    private boolean comprobarUnirsePartido(int idUsuario, int idPartido) {
        boolean sePuedeUnir = true;
        Partido partido = obtenerPartidoPorId(idPartido);
        Usuario u = obtenerUsuarioPorId(idUsuario);
        
        List<Participacion> p = obtenerParticipantes(idPartido);

        if (comprobarUsuarioParticipaEnPartido(idUsuario, p))
            sePuedeUnir = false;

        if (partido.isSoloVerificados()) 
            if (!u.isVerificado())
                sePuedeUnir = false;

        if (!partido.getEstado().equals("abierto")) 
            sePuedeUnir = false;


        return sePuedeUnir;
    }

    
    private Usuario obtenerUsuarioPorId(int idUsuario) {
        Usuario u = null;
        try (Session session = sessionFactory.openSession()){

            Query<Usuario> q = session.createQuery("from Usuario where id = :id", Usuario.class);

            q.setParameter("id", idUsuario);
            List<Usuario> lUsuario = q.list();
            u = lUsuario.get(0);

        }

        return u;
    }

    public String verPerfilJugador(int idUsuario) {
        Map<String, Object> datos = construirUsuario(obtenerUsuarioPorId(idUsuario));
        return AyudanteConteston.contestarTodoBien("iNSUC", "Informacion del usuario conseguida correctamente", datos);
    }


    public String obtenerInventarioUsuario(int idUsuario) {
        String respuesta = "";

        try (Session session = sessionFactory.openSession()){
            Query<InventarioCosmetico> q = session.createQuery( "FROM InventarioCosmetico WHERE inventario.usuario.id = :idUsuario", InventarioCosmetico.class);

            q.setParameter("idUsuario", idUsuario);

            List<InventarioCosmetico> listaProcesar = q.list();
            List<Cosmetico> cosmeticos = new ArrayList<>();

            for (InventarioCosmetico elemento : listaProcesar) {
                cosmeticos.add(elemento.getCosmetico());
            }
            Map<String, Object> datos = new HashMap<>();

            datos.put("cosmeticos", cosmeticos);
            respuesta = AyudanteConteston.contestarTodoBien("iNe", "Inventario enviado", datos);
        }

        return respuesta;
    }
    


    
    //devuelve string pero pongo void para que no salga en rojo
    public String iniciarSesionContrasenia(String correo, String contrasenia, JugadorSistema j) { // pasarle los datos en esta funcion comprobar antes si tiene token, comprobar token y darle directamente paso o comprobar contrasenia y nombre
        // si no tiene remember token usamos la funcion que tenemos para comprobar contraseña, si tiene remember token creo que tenemos funcion para comprobar rmemeber token
        String respuesta = "";
        boolean correoExiste = comprobarCorreoAsociado(correo);
        if (!correoExiste) {
            respuesta = AyudanteConteston.contestarError("erIncnoe", "El correo introducido no existe");
        } else if (!comprobarUsuario(correo, contrasenia))
            respuesta = AyudanteConteston.contestarError("erIncin", "La contraseña introducida es incorrecta");
        else {
            Usuario u = obtenerUsuarioPorCorreo(correo);
            Map<String,Object> datos = construirUsuario(u);
            respuesta = AyudanteConteston.contestarTodoBien("iC", "Inicio de sesion correcto", datos);
            j.setIdUsuario(u.getId());
        }
        
        
        return respuesta;
    }

    public String iniciarSesionToken(String selector, String token, JugadorSistema j) { 
        String respuesta = "";
        boolean rememberTokenExiste = comprobarTokenExiste(selector);

        if (!rememberTokenExiste) {
            respuesta = AyudanteConteston.contestarError("ertkNe", "El token no existe");
        } else if (comprobarRememberToken(selector, token)) {
            Usuario u = obtenerRememberToken(selector).getUsuario();
            Map<String,Object> datos = construirUsuario(u);
            respuesta = AyudanteConteston.contestarTodoBien("iCcTkC", "Inicio de sesión correcto", datos);
            j.setIdUsuario(u.getId());
        }

        return respuesta;
    }

    private Carta obtenerCartaUsuario(int idUsuario) { //cosas importantes de este tipo de funciones, en muchas no hay comprobaciones basicamente porque no hay posibilidad real de que por ejemplo el usuario no tenga carta, ya que cuando un usuario se crea siempre se crea con la carta
        Carta carta = null;

        try (Session session = sessionFactory.openSession()){
            Query<Carta> q = session.createQuery("FROM Carta WHERE usuario.id = :idUsuario",Carta.class);
            q.setParameter("idUsuario", idUsuario);

            List<Carta> lista = q.list();
            carta = lista.get(0);
        
        }

        return carta;
    }

    public Map<String,Object> construirUsuario(Usuario u) {
        Carta carta = obtenerCartaUsuario(u.getId());
        Map<String, Object> datos  = new HashMap<>();
        //Usuario general
        datos.put("nombre", u.getNombre());
        datos.put("idUsuario", u.getId()); 
        datos.put("monedas", u.getMonedas()); // la moneda de la app
        datos.put("puntosRango", u.getPuntos());  // son los puntos del rango por ejemplo estas en el rango 1 con 100 puntos y al rango 2 se llega cuando tienes 300 puntos
        datos.put("posicion1", u.getPosicion1());
        datos.put("posicion2", u.getPosicion2());
        datos.put("correo", u.getCorreo());
        datos.put("titulo", u.getTitulo().getNombre());
        datos.put("tarjetaVisita", u.getTarjetaVisita().getNombre());
        datos.put("goles", u.getGoles());
        datos.put("asistencias", u.getAsistencias());
        datos.put("mvp", u.getMvps());
        datos.put("partidosJugados", u.getPartidosJugados());
        datos.put("reputacion", u.getReputacion()); 
        datos.put("rango", u.getRango());
        datos.put("verificado", u.isVerificado());
        datos.put("fotoperfil", u.getFotoPerfil());

        //Carta
        datos.put("cartaCosmetico", carta.getCosmetico().getNombre());
        datos.put("ritmo", carta.getRitmo());
        datos.put("tiro", carta.getTiro());
        datos.put("defensa", carta.getDefensa());
        datos.put("fisico", carta.getFisico());
        datos.put("regate", carta.getRegate());
        datos.put("pase",carta.getPase());

        //carta stats portero
        datos.put("manejo", carta.getManejo());
        datos.put("estirada", carta.getEstirada());
        datos.put("saque", carta.getSaque());
        datos.put("reflejos", carta.getReflejos());
        datos.put("velocidad", carta.getVelocidad());
        datos.put("posicionamiento", carta.getPosicionamiento());


        //media de las cartas
        datos.put("mediaJugador", carta.getMediaJugador());
        datos.put("mediaPortero", carta.getMediaPortero());
        

        
        return datos;
    }


    private boolean comprobarTokenExiste(String selector) {
        boolean tokenExiste = false;
        Session session = sessionFactory.openSession();	
        Query<RememberToken> q = session.createQuery("from RememberToken where selector = :selector", RememberToken.class);

        q.setParameter("selector", selector);
        List<RememberToken> lista = q.list();
        if (!lista.isEmpty()) {
            tokenExiste  = true;
        }

        return tokenExiste;
    }

    private boolean comprobarRememberToken(String selector, String token) {
        boolean rememberValido = false;
        String hash = hashearToken(token);
        RememberToken rm = obtenerRememberToken(selector);

        if (rm.getTokenHash().equals(hash)) {
            rememberValido = true;
        }

        return rememberValido;
    }

    private RememberToken obtenerRememberToken(String selector) {
        RememberToken rm = null;


        try (Session session = sessionFactory.openSession()){
            Query<RememberToken> q = session.createQuery("from RememberToken where selector = :selector", RememberToken.class);

            q.setParameter("selector", selector);
            List<RememberToken> rememberTokens = q.list();
            rm = rememberTokens.get(0);
        }

        return rm;


    }

    
    public void registrarCarta(Carta c) {
        persistirObjeto(c);
    }

    private void crearInventario(Usuario u) {
        System.out.println("TeamUp|MensajeInterno| Hemos entrado para crear inventario"); 
        Inventario inv = new Inventario(u);
        persistirObjeto(inv);

        // todo esto son cosmeticos default que todo usuario consigue automaticamente al crear su perfil
        Cosmetico c1 = obtenerCosmetico(1); // carta comun 
        Cosmetico c2 = obtenerCosmetico(2); // tarjeta visita
        Cosmetico c3 = obtenerCosmetico(3); // titulo rookie
        System.out.println("TeamUp|MensajeInterno| cosmetico 1 con nombre:" + c1.getNombre());
        System.out.println("TeamUp|MensajeInterno| cosmetico 2 con nombre:" + c2.getNombre());
        System.out.println("TeamUp|MensajeInterno| cosmetico 3 con nombre:" + c3.getNombre());


        InventarioCosmetico ic1 = new InventarioCosmetico(1, inv, c1);
        InventarioCosmetico ic2 = new InventarioCosmetico(1, inv, c2);
        InventarioCosmetico ic3 = new InventarioCosmetico(1, inv, c3);

        persistirObjeto(ic1);
        persistirObjeto(ic2);
        persistirObjeto(ic3);
                  
        
        
    }

    private void persistirObjeto(Object objeto) { // la verdad se me ocurro probarlo y me sorprendio que funcionará, si no esta usado mucho es que lo pense mucho mucho mas tarde para los cosmeticos por eso la mayoria de cosas no usan esta funcion
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(objeto);

            
            try {
                transaction.commit();
                System.out.println("TeamUp|MensajeInterno|Objetos añadidos al inventario");
            } catch (IllegalStateException em) {
                transaction.rollback();
                System.out.println("TeamUp|Error|EM2|");
            }  

        }
    }   

    

    public String registrarUsuario(String nombre, String contrasenia, String correo, String posicion1, String posicion2, String recordarme, JugadorSistema j) {
        String resultado = "";
        System.out.println("TeamUp|MensajeInterno|" + nombre + " con correo: " + correo + " con posicion1" + posicion1 + " y con recordarme " + recordarme);
        boolean usuarioExiste = comprobarUsuarioExiste(nombre);
        boolean correoExiste = comprobarCorreoAsociado(correo);


        try (Session session = sessionFactory.openSession()) {
        // Esto lo podriamos llevar a una funcion que deolviera un objeto que estuviera fomrado por UN boolean false / true y el mensaje, la verdad seria bueno y ayudaria al codigo pero mas adelante
            if (usuarioExiste) {
                resultado = AyudanteConteston.contestarError("errU", "Usuario ya existe");
            } else if (correoExiste){
                resultado = AyudanteConteston.contestarError("errC", "Correo ya existe");
            } else if (!validarNombreUsuario(nombre)) {
                resultado = AyudanteConteston.contestarError("errUu", "Nombre del usuario no cumple los requisitos");
            } else if (!validarCorreo(correo)) {
                resultado = AyudanteConteston.contestarError("errCc", "El correo no cumple el formato de correo");
            } else if (!validarContrasenia(contrasenia)) {
                resultado = AyudanteConteston.contestarError("errCo", "La contraseña no cumple los minimos de seguridad");
            } else {
                String contraseniaEncriptada  = encriptarContrasenia(contrasenia);
                Usuario u = new Usuario(nombre, correo, contraseniaEncriptada, posicion1, posicion2, obtenerCosmetico(2), obtenerCosmetico(3));
                persistirObjeto(u);
                j.setIdUsuario(u.getId());
                crearInventario(u);
                generadorCarta(posicion1, posicion2, nombre);

                if (recordarme.equals("1")) {
                    Map<String,Object> datos = construirUsuario(u);
                    List<String> lista = generarRememberToken(u, j);
                    datos.put("selector", lista.get(0));
                    datos.put("token", lista.get(1));
                    resultado = AyudanteConteston.contestarTodoBien("rC", "Registro completo", datos);
                } else {
                    Map<String,Object> datos = construirUsuario(u);
                    resultado = AyudanteConteston.contestarTodoBien("rC", "Registro Completo", datos); 
                }
            }
        }


        return resultado;
    }


        private void generadorCarta(String posicion1, String posicion2, String nombre) {
        Random generador = new Random();
        System.out.println("TeamUp|MensajeInterno|Estoy dedntro de generador de carta, buenas con usuario " + nombre);
        Usuario usu = obtenerUsuario(nombre);
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
            Carta c  = new Carta(estadisticasCampo.get("ritmo"), estadisticasCampo.get("tiro"), estadisticasCampo.get("pase"), estadisticasCampo.get("regate"), estadisticasCampo.get("defensa"), estadisticasCampo.get("fisico"), usu, obtenerCosmetico(1));
            
            c.setPosicionamiento(BASE_PORTER + generador.nextInt(8));
            c.setReflejos(BASE_PORTER + generador.nextInt(8));
            c.setManejo(BASE_PORTER + generador.nextInt(8));
            c.setVelocidad(BASE_PORTER + generador.nextInt(8));
            c.setEstirada(BASE_PORTER + generador.nextInt(8));
            c.setSaque(BASE_PORTER + generador.nextInt(8));
            c.setMediaPortero(c.calcularMedia(c.getEstirada(), c.getManejo(), c.getReflejos(), c.getVelocidad(), c.getSaque(), c.getPosicionamiento()));
            System.out.println("TeamUp|MensajeInterno|Carta con estadisticas " + c.getRegate() + " regate");
            registrarCarta(c);

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
            Carta c = new Carta(estadisticasCampo.get("ritmo"), estadisticasCampo.get("tiro"), estadisticasCampo.get("pase"), estadisticasCampo.get("regate"), estadisticasCampo.get("defensa"),estadisticasCampo.get("fisico"), usu, obtenerCosmetico(1));            

            registrarCarta(c);
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



    public Cosmetico obtenerCosmetico(int id) {
        Cosmetico c = null;

        try (Session session = sessionFactory.openSession()){
            Query<Cosmetico> q = session.createQuery("from Cosmetico where id = :id", Cosmetico.class);

            q.setParameter("id", id);
            List<Cosmetico> cosmeticos = q.list();
            c = cosmeticos.get(0);
        }


        return c;
    }

    private String cadenaAleatoria() {
        String cancion = "";
        Random generador = new Random();
        int numeroAleatorio = generador.nextInt(15);

        switch (numeroAleatorio) {
            case 0 :
                cancion = "graxxqjsm";
                break;
            case 1:
                cancion = "joe";
                break;
            case 2:
                cancion = "vidarockstar";
                break;
            case 3:
                cancion = "corazon";
                break;
            case 4:
                cancion = "pasiempre";
                break;
            case 5:
                cancion = "cuandomevaya";
                break;
            case 6:
                cancion = "medialuna";
                break;
            case 7:
                cancion = "lologre";
                break;
            case 8:
                cancion = "tokyo";
                break;
            case 9:
                cancion = "flakito";
                break;
            case 10:
                cancion = "comoantes";
                break;
            case 11:
                cancion = "maniac";
                break;
            case 12:
                cancion = "esacruz";
                break;
            case 13:
                cancion = "mojabighost";
                break;
            case 14:
                cancion = "fantasmaavc";
                break;
        }

        return cancion;
    }

    private List<String> generarRememberToken(Usuario u, JugadorSistema j) { //esto devuelve una lista con selector + el token sin hashear en documenta el proceso que he segudio
        List<String> listaDatos = new ArrayList<>();
        System.out.println("TeamUp|MensajeInterno|Usario con id" + u.getId() + " con nombre " + u.getNombre());
        String selector = u.getId() + "" + u.getNombre().substring(0,3) + "14" + u.getFechaCreacion().getMinute() + u.getFechaCreacion().getDayOfWeek();
        String cadenaExtra = cadenaAleatoria();
        String token =  u.getFechaCreacion().getHour() + "" + u.getNombre().substring(0,3) + "14" + u.getFechaCreacion().getYear() + u.getFechaCreacion().getMinute() + u.getPosicion1() + cadenaExtra;
        listaDatos.add(selector);
        listaDatos.add(token);
        
        InetAddress datosCliente = j.getZocalo().getInetAddress();
        String ip = datosCliente.getHostAddress();
        String dispositivo = datosCliente.getHostName();
        System.out.println("TeamUp|MensajeInterno|Remember token con selector:  " + selector + " y para el usuario con nombre: " + u.getNombre());
        RememberToken rm = new RememberToken(u, selector,hashearToken(token),dispositivo, ip);
        persistirObjeto(rm);

        //remember token, slelector, token, fecha expi, dispositivo, ip
        return listaDatos;

    }



    private String hashearToken(String token) {
        String em = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            System.out.println("TeamUp|Error|EM4|");
        }
        return  em;
    }

    private boolean validarCorreo(String correo) {
        // en documentacion especificados estos dominios
        String regex = "^[A-Za-z0-9+_.-]+@(gmail\\.com|outlook\\.com|hotmail\\.com|yahoo\\.com|icloud\\.com)$";
        return Pattern.matches(regex, correo);
    }

    private boolean validarContrasenia(String contrasenia) {
        // comprobacion Longitud
        boolean contraseniaValida = true;
        if (contrasenia.length() < 8 || contrasenia.length() > 33) 
            contraseniaValida = false;

        // Al menos 2 números
        int contadorNumeros = 0;
        for (char c : contrasenia.toCharArray()) {
            if (Character.isDigit(c)) {
                contadorNumeros++;
            }
        }

        if (contadorNumeros < 2) 
            contraseniaValida = false;

        // Al menos 1 carácter especial
        Pattern especial = Pattern.compile("[^a-zA-Z0-9]");
        if (!especial.matcher(contrasenia).find()) 
            contraseniaValida = false;

        return contraseniaValida;
    }

    private boolean validarNombreUsuario(String nombre) {
        return nombre.length() <= 20 && nombre.length() >= 3 && !nombre.trim().isEmpty();
    }



    private String encriptarContrasenia(String contrasenia) {

        //Esto es como en el biginteger is probable prime, cuanta mas certeza tenia la comprobacion, pero en la fuerza de la encriptacion
        int vueltasEncriptacion = 12;
        // Esto es el "salt",
        String sal = BCrypt.gensalt(vueltasEncriptacion);

        return BCrypt.hashpw(contrasenia,sal);

    }

    public Usuario obtenerUsuario(String nombreUsuario) {
        Usuario u = null;
        try (Session session = sessionFactory.openSession()){
            Query<Usuario> q = session.createQuery("from Usuario where nombre = :nombre", Usuario.class);

            q.setParameter("nombre", nombreUsuario);
            List<Usuario> lUsuario = q.list();
            u = lUsuario.get(0);
        }

        return u;
    }

    public Usuario obtenerUsuarioPorCorreo(String correo) {
        Usuario u = null;
        try (Session session = sessionFactory.openSession()){
            Query<Usuario> q = session.createQuery("from Usuario where correo = :correo", Usuario.class);

            q.setParameter("correo", correo);
            List<Usuario> lUsuario = q.list();
            u = lUsuario.get(0);
        }

        return u;
    }

    private boolean comprobarUsuario(String correo, String contrasenia) {
        boolean entrar = false;
        Usuario u = obtenerUsuarioPorCorreo(correo);
        if (BCrypt.checkpw(contrasenia, u.getContrasena())) 
            entrar = true;

        return entrar;
    }
    

}