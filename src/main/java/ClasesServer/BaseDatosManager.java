package ClasesServer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import clases.SobreSimplificado;
import claseshibernate.Carta;
import claseshibernate.Cosmetico;
import claseshibernate.Inventario;
import claseshibernate.InventarioCosmetico;
import claseshibernate.InventarioSobre;
import claseshibernate.Mercado;
import claseshibernate.Participacion;
import claseshibernate.Partido;
import claseshibernate.RememberToken;
import claseshibernate.Sobre;
import claseshibernate.Usuario;
import claseshibernate.Votacion;

public class BaseDatosManager {
    private SessionFactory sessionFactory;
    private Map<Integer, Object> mapaConcurrencia = new ConcurrentHashMap<>();

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


    public boolean comprobarUsuarioExiste(String nombre) {
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
    
    public boolean comprobarCorreoAsociado(String correo) {
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

    public void verificadorCuentas() { // sistema de cuentas
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

    public void limpiadorVotos(int idPartido) { //funcion que se encarga de limpiar todos los votos de la base de datos
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            Query q = session.createQuery("DELETE FROM Votacion WHERE partido.id = :idPartido" );
            q.setParameter("idPartido", idPartido);

            int votosEliminados = q.executeUpdate();

            try {
                transaction.commit();

                System.out.println("TeamUp|MensajeInterno|Votos eliminados: "   + votosEliminados );

            } catch (IllegalStateException em) {
                transaction.rollback();
                System.out.println("TeamUp|Error|EM2|");
            }
        }

    }

    public void verificadorExpiracionToken() { // sistema autenticacion
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


    public void quitarSobre(int idUsuario, String nombreSobre) {
        Sobre sobre = obtenerSobre(nombreSobre);

        InventarioSobre invSo = comprobarUsuarioTieneSobre(idUsuario, sobre);

        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            if (invSo != null) {

                invSo.setCantidad(invSo.getCantidad() - 1);

                if (invSo.getCantidad() <= 0) {
                        session.remove(session.merge(invSo)); // evitar el error detached entity de hibernate (por eso hago el merge, porque viene de otra funcion)
                    try {
                        transaction.commit();
                        System.out.println("TeamUp|MensajeInterno|Eliminado sobre del inventario");
                    } catch (IllegalStateException em) {
                        transaction.rollback();
                        System.out.println("TeamUp|Error|EM2|");
                    }
                } else {
                    actualizarObjeto(invSo);
                }
            }
        }

    }



    public void eliminarArticuloMercado(Mercado articulo) { // sistema cosmetico
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.remove(session.merge(articulo)); // Uso merge explicado en quitarSobre
            try {
                    transaction.commit();
                    System.out.println("TeamUp|MensajeInterno|Eliminado articulo del mercado");
            } catch (IllegalStateException em) {
                    transaction.rollback();
                    System.out.println("TeamUp|Error|EM2|");
            }
        }
    }


    public void quitarCosmetico(int idUsuario, int idCosmetico) { 
        Cosmetico cosmetico = obtenerCosmetico(idCosmetico);

        InventarioCosmetico invCo = comprobarUsuarioTieneCosmetico(idUsuario, cosmetico);

        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            if (invCo != null) {

                invCo.setCantidad(invCo.getCantidad() - 1);

                if (invCo.getCantidad() <= 0) {
                    session.remove(session.merge(invCo)); // Uso merge explicado en quitarSobre
                    try {
                        transaction.commit();
                        System.out.println("TeamUp|MensajeInterno|Eliminado cosmetico del inventario");
                    } catch (IllegalStateException em) {
                        transaction.rollback();
                        System.out.println("TeamUp|Error|EM2|");
                    }
                } else {
                    actualizarObjeto(invCo);
                }
            }
        }

    }



    public Mercado obtenerElementoMercado(int idElementoMercado) {
        Mercado m = null;

        try (Session session = sessionFactory.openSession()) {

            Query<Mercado> q = session.createQuery("FROM Mercado WHERE id = :idElemento",Mercado.class);

            q.setParameter("idElemento",idElementoMercado);

            List<Mercado> lista = q.list();

            if (!lista.isEmpty()) {
                m = lista.get(0);
            }
        }


        return m;
    }

    public List<SobreSimplificado> obtenerSobresUsuario (int idUsuario) {
        List<InventarioSobre> listaSobresUsuario = new ArrayList<>();
        List<SobreSimplificado> sobresSimplicados = new ArrayList<>();

        try (Session session = sessionFactory.openSession()) {

            Query<InventarioSobre> q = session.createQuery("FROM InventarioSobre " +"WHERE inventario.usuario.id = :idUsuario",InventarioSobre.class);

            q.setParameter("idUsuario", idUsuario);

            listaSobresUsuario = q.list();

            for (InventarioSobre invS : listaSobresUsuario) {
                SobreSimplificado sS = new SobreSimplificado(invS.getSobre().getNombre(), 0, invS.getSobre().getId());
                sS.setCantidad(invS.getCantidad());
                sobresSimplicados.add(sS);
            }
    }

        

        return sobresSimplicados;
    }

    public List<SobreSimplificado> obtenerSobresVenta() {
        List<Sobre> listaSobreVenta = new ArrayList<>();
        List<SobreSimplificado> sobresSimplicados = new ArrayList<>();

        try (Session session = sessionFactory.openSession()) {

            Query<Sobre> q = session.createQuery("FROM Sobre WHERE precio > 0",Sobre.class);
            listaSobreVenta = q.list();

            for (Sobre s : listaSobreVenta) 
                sobresSimplicados.add(new SobreSimplificado(s.getNombre(), s.getPrecio(), s.getId()));
        
        }

        return sobresSimplicados;

    }

    public void quitarMonedas (int idUsuario, int cantidad) {
        Usuario u = obtenerUsuarioPorId(idUsuario);
        u.setMonedas(u.getMonedas() - cantidad);
        actualizarObjeto(u);
    }    

    public void darMonedas(int idUsuario, int cantidadMonedas) {
        Usuario u = obtenerUsuarioPorId(idUsuario);
        u.setMonedas(u.getMonedas() + cantidadMonedas);
        actualizarObjeto(u);
    }

    public void darPuntosRango(int idUsuario, int cantidadPuntos) {
        Usuario u = obtenerUsuarioPorId(idUsuario);
        u.setPuntos(u.getPuntos() + cantidadPuntos);
        actualizarObjeto(u);
        comprobacionSubidaRango(idUsuario);
    }

    public void darReputacion(int idUsuario, int cantidadReputacion) {
        Usuario u = obtenerUsuarioPorId(idUsuario);
        u.setReputacion(u.getReputacion() + cantidadReputacion);
        actualizarObjeto(u);
        comprobacionSubidaReputacion(idUsuario);
    }

    public void comprobacionSubidaReputacion(int idUsuario) { // esta funcion es para dar recompensas en base a la reputacion del jugador (lo que creo que es obvio por el nombre pero bueno)
        //sistema ranking
        Usuario u = obtenerUsuarioPorId(idUsuario);

        if (u.getReputacion() >= 1000 && u.getReputacion() < 2000 && u.getNivelReputacion() != 2) {
            u.setNivelReputacion(2);
            darMonedas(idUsuario, 1000);
            darSobre(idUsuario, obtenerSobre("Sobre Reputacion"), 1);
            darCosmetico(idUsuario, obtenerCosmetico(10), 1);
        } else if (u.getReputacion() >= 2000 && u.getReputacion() < 3000 && u.getNivelReputacion() != 3) {
            u.setNivelReputacion(3);
            darMonedas(idUsuario, 1500);
            darSobre(idUsuario, obtenerSobre("Sobre Reputacion"), 2);
            darCosmetico(idUsuario, obtenerCosmetico(11), 1);

        } else if (u.getReputacion() >= 3000 && u.getReputacion() < 4000 && u.getNivelReputacion() != 4) {
            u.setNivelReputacion(4);
            darMonedas(idUsuario, 2000);
            darSobre(idUsuario, obtenerSobre("Sobre Reputacion"), 3);
            darCosmetico(idUsuario, obtenerCosmetico(12), 1);


        } else if (u.getReputacion() >= 4000 && u.getReputacion() < 6000 && u.getNivelReputacion() != 5) {
            u.setNivelReputacion(5);
            darMonedas(idUsuario, 3000);
            darSobre(idUsuario, obtenerSobre("Sobre Reputacion"), 5);
            darCosmetico(idUsuario, obtenerCosmetico(13), 1);
        }
        
    }

    public void darCosmetico(int idusuario, Cosmetico cosmetico, int cantidad ) {
        System.out.println("TeamUp|MensajeInterno| Entro en dar cosmetico al usuario " + idusuario);
        InventarioCosmetico invCo = comprobarUsuarioTieneCosmetico(idusuario, cosmetico);
        if (invCo != null) {
            invCo.setCantidad(invCo.getCantidad() + cantidad);
            actualizarObjeto(invCo);
        } else {
            InventarioCosmetico invC = new InventarioCosmetico(cantidad, obtenerInventarioPorId(idusuario), cosmetico);
            persistirObjeto(invC);
        }
    }

    public InventarioCosmetico comprobarUsuarioTieneCosmetico(int idUsuario, Cosmetico cosmetico) { // calco de la funcion de comprobarSielusuario tiene sobre
        InventarioCosmetico invCo = null;

        try (Session session = sessionFactory.openSession()) {


            Query<InventarioCosmetico> q = session.createQuery("FROM InventarioCosmetico " +"WHERE inventario.usuario.id = :idUsuario " +"AND cosmetico.id = :idCosmetico",InventarioCosmetico.class);

            q.setParameter("idUsuario", idUsuario);
            q.setParameter("idCosmetico", cosmetico.getId());

            List<InventarioCosmetico> lista = q.list();

            if (!lista.isEmpty()) {
                invCo = lista.get(0);
            }
        }


        return invCo;
    }

    public void comprobacionSubidaRango(int idUsuario) {
        Usuario u = obtenerUsuarioPorId(idUsuario);

        if (u.getPuntos() >= 150 && u.getPuntos() < 300 && u.getRango() != 2) {
            u.setRango(2);
            darSobre(idUsuario, obtenerSobre("Sobre Plata"), 1);
            darMonedas(idUsuario, 300);

        } else if (u.getPuntos() >= 300 && u.getPuntos() < 450 && u.getRango() != 3) {
            u.setRango(3);
            darSobre(idUsuario, obtenerSobre("Sobre Oro"), 1);
            darMonedas(idUsuario, 600);

        } else if (u.getPuntos() >= 450 && u.getPuntos() < 600 && u.getRango() != 4) {
            u.setRango(4);
            darSobre(idUsuario, obtenerSobre("Sobre Elite"), 1);
            darMonedas(idUsuario, 1200);
        }
    }

    public Sobre obtenerSobre (String nombreSobre) { //tonteria haberlo hecho con nombreSObre creo yo que hubiera sido mejor hacerlo por id pero es que lo hecho pensando en una funcion de moderacion que puedadar sobre por nombre y claro el moderador no va a saber el id del sobre
        Sobre sobre = null;

        try (Session session = sessionFactory.openSession()) {

            Query<Sobre> q = session.createQuery("FROM Sobre WHERE nombre = :nombre",Sobre.class);

            q.setParameter("nombre", nombreSobre);

            List<Sobre> lista = q.list();

            sobre = lista.get(0);

            sobre.getContenidos().size(); // esto para que no de el error lazy

        }

        return sobre;
    }

    public void darSobre(int idUsuario, Sobre sobre, int cantidad) {
        System.out.println("TeamUp|MensajeInterno| Entramos a dar sobre al usuario " + idUsuario);
        InventarioSobre invSo = comprobarUsuarioTieneSobre(idUsuario, sobre);
        if (invSo != null) {
            invSo.setCantidad(invSo.getCantidad() + cantidad);
            actualizarObjeto(invSo);
        } else {
            InventarioSobre invS = new InventarioSobre(obtenerInventarioPorId(idUsuario), sobre, cantidad);
            persistirObjeto(invS);
        }

    }   
    
    public InventarioSobre comprobarUsuarioTieneSobre(int idUsuario, Sobre sobre) {
        InventarioSobre invSo = null;

        try (Session session = sessionFactory.openSession()) {

            Query<InventarioSobre> q = session.createQuery("FROM InventarioSobre " +"WHERE inventario.usuario.id = :idUsuario " +"AND sobre.id = :idSobre",InventarioSobre.class);

            q.setParameter("idUsuario", idUsuario);
            q.setParameter("idSobre", sobre.getId());

            List<InventarioSobre> lista = q.list();

            if (!lista.isEmpty()) {
                invSo = lista.get(0);
            }
        }


        return invSo;
    }

    public Inventario obtenerInventarioPorId(int idUsuario) {
        Inventario inv = null;

        try (Session session = sessionFactory.openSession()) {

            Query<Inventario> q = session.createQuery("FROM Inventario WHERE usuario.id = :idUsuario",Inventario.class);

            q.setParameter("idUsuario", idUsuario);

            List<Inventario> lista = q.list();

            inv = lista.get(0);

        }


        return inv;
    }




    public void actualizarMediaEquipos(int idPartido) {
        Partido p = obtenerPartidoPorId(idPartido);
        int mediaFinalEquipo1 = 0;
        int mediaFinalEquipo2 = 0;
        List<Participacion> participantes = obtenerParticipantes(idPartido);

        for (Participacion participante : participantes) { 
            int mediaUsuario = 0;
            Usuario u = participante.getUsuario();
            for (Votacion v : obtenerVotaciones(idPartido, u.getId())) {
                mediaUsuario = mediaUsuario + v.getPuntuacion();
            }
            mediaUsuario = mediaUsuario / 13; // 13 son los votos que recibe cada jugador
            participante.setPuntuacion(mediaUsuario);
            actualizarObjeto(participante);

            if (participante.getEquipo().equals("equipo1")) {
                mediaFinalEquipo1 = mediaFinalEquipo1 + u.getPuntos();
            } else 
                mediaFinalEquipo2 = mediaFinalEquipo2 + u.getPuntos();

        }

        p.setMediaEquipo1(mediaFinalEquipo1 / 7);

        p.setMediaEquipo2(mediaFinalEquipo2 / 7);

        actualizarObjeto(p);

    }

    public List<Votacion> obtenerVotaciones(int idPartido, int idUsuario ) {
        List<Votacion> votaciones = null;
        
        try (Session session = sessionFactory.openSession()) {
            Query<Votacion> q = session.createQuery("FROM Votacion " +"WHERE partido.id = :idPartido " +"AND votado.id = :idUsuario",Votacion.class);

            q.setParameter("idPartido", idPartido);
            q.setParameter("idUsuario", idUsuario);

            votaciones = q.list();
        }

        return votaciones;
    }

    public List<Votacion> obtenerVotacionesPartido(int idPartido) {
        List<Votacion> votaciones = new ArrayList<>();

        try (Session session = sessionFactory.openSession()) {

            Query<Votacion> q = session.createQuery("FROM Votacion WHERE partido.id = :idPartido",Votacion.class);

            q.setParameter("idPartido", idPartido);

            votaciones = q.list();
        }

        return votaciones;
    }

    public void rellenadorMapaConcurrencia() { 
        List<Partido> partidos = obtenerPartidos();
        //List<Cosmetico> cosmeticosMercado = obtenerCosmeticosMercado hay que hacerlo
        for (Partido partido : partidos) 
            mapaConcurrencia.put(partido.getId(), partido);

        System.out.println("TeamUp|MensajeInterno| Numero de partidos en el mapa de concurrencia " + partidos.size());


    }


    public List<Partido> obtenerPartidos() {
        List<Partido> partidos = null;

        try (Session session = sessionFactory.openSession()) {

            Query<Partido> q = session.createQuery("FROM Partido WHERE estado = :abierto OR estado = :terminado", Partido.class );

            q.setParameter("abierto", "abierto");
            q.setParameter("terminado", "terminado");

            partidos = q.list();
        }

        return partidos;
    }
    
    public List<Participacion> obtenerParticipacionesUsuario(int idUsuario) {

        List<Participacion> participaciones = null;

        try (Session session = sessionFactory.openSession()) {

            Query<Participacion> q = session.createQuery("FROM Participacion WHERE usuario.id = :idUsuario",Participacion.class);
            q.setParameter("idUsuario", idUsuario);

            participaciones = q.list();
        }

        return participaciones;
    }

    public Participacion obtenerParticipacionId(int idUsuario, int idPartido) {
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

    public void actualizarObjeto(Object objeto) {
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


    public List<Participacion> obtenerParticipantes(int idPartido) {
        List<Participacion> participaciones = null;
        try (Session session = sessionFactory.openSession()){
            Query<Participacion> q = session.createQuery("FROM Participacion WHERE partido.id = :idPartido",Participacion.class);

            q.setParameter("idPartido", idPartido);

            participaciones = q.list();
        
        }

        return participaciones;
    }

    public Partido obtenerPartidoPorId(int idPartido) {
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

    public boolean comprobarUsuarioParticipaEnPartido(int idUsuario, List<Participacion> p) {
        boolean participa = false;

        for (Participacion participante : p) {
            if (participante.getUsuario().getId() == idUsuario) {
                participa = true;
                break;
            }
        }

        return participa;
    }

    
    public Usuario obtenerUsuarioPorId(int idUsuario) {
        Usuario u = null;
        try (Session session = sessionFactory.openSession()){

            Query<Usuario> q = session.createQuery("from Usuario where id = :id", Usuario.class);

            q.setParameter("id", idUsuario);
            List<Usuario> lUsuario = q.list();
            u = lUsuario.get(0);

        }

        return u;
    }

    

    public Carta obtenerCartaUsuario(int idUsuario) { //cosas importantes de este tipo de funciones, en muchas no hay comprobaciones basicamente porque no hay posibilidad real de que por ejemplo el usuario no tenga carta, ya que cuando un usuario se crea siempre se crea con la carta
        Carta carta = null;

        try (Session session = sessionFactory.openSession()){
            Query<Carta> q = session.createQuery("FROM Carta WHERE usuario.id = :idUsuario",Carta.class);
            q.setParameter("idUsuario", idUsuario);

            List<Carta> lista = q.list();
            carta = lista.get(0);
        
        }

        return carta;
    }



    public boolean comprobarTokenExiste(String selector) {
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



    public RememberToken obtenerRememberToken(String selector) {
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

    public void crearInventario(Usuario u) {
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

    public void persistirObjeto(Object objeto) { // la verdad se me ocurro probarlo y me sorprendio que funcionará, si no esta usado mucho es que lo pense mucho mucho mas tarde para los cosmeticos por eso la mayoria de cosas no usan esta funcion
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

    
    // sistema cuentas



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


    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Map<Integer, Object> getMapaConcurrencia() {
        return mapaConcurrencia;
    }
    

}