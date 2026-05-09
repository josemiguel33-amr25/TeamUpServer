
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
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;

import clases.UsuarioSimplificado;
import claseshibernate.Carta;
import claseshibernate.Cosmetico;
import claseshibernate.Inventario;
import claseshibernate.InventarioCosmetico;
import claseshibernate.Partido;
import claseshibernate.RememberToken;
import claseshibernate.Usuario;

public class BaseDatosManager {
    private SessionFactory sessionFactory;


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
                    Transaction transaction = session.beginTransaction();
                    session.persist(uSel);

                    try {
                        transaction.commit();
                        System.out.println("TeamUp|MensajeInterno|Usuario dado de alta");
                    } catch (IllegalStateException em) {
                        System.out.println("TeamUp|Error|EM2|");
                    }
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
                listaUsuariosSimplificada.add(new UsuarioSimplificado(u.getNombre(), rango, u.getPuntos(), u.getReputacion(), u.getGoles(), u.getAsistencias(), u.getMvps()));
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
                Partido p = new Partido(datos.get("titulo"), datos.get("ubicacion"), Integer.parseInt(datos.get("precio")), datos.get("ciudad"), creador, soloVerificados);
                Transaction transaction = session.beginTransaction();
                session.persist(p);

                try {
                    transaction.commit();
                    System.out.println("TeamUp|MensajeInterno|Partido creado.");
                } catch (IllegalStateException em) {
                    System.out.println("TeamUp|Error|EM2|.");
                }
            } else
                respuesta = AyudanteConteston.contestarError("erTlnv", "Titulo tiene mas de 100 caracteres");
        
        }


        return respuesta;
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
            Map<String,Object> datos = new HashMap<>();
            datos.put("nombre", u.getNombre());
            datos.put("posicion1", u.getPosicion1());
            datos.put("posicion2", u.getPosicion2());
            datos.put("correo", u.getCorreo());
            datos.put("titulo", u.getTitulo().getNombre());
            datos.put("tarjetaVisita", u.getTarjetaVisita().getNombre());
            datos.put("goles", u.getGoles());
            datos.put("asistencias", u.getAsistencias());
            datos.put("mvp", u.getMvps());
            datos.put("partidosJugados", u.getPartidosJugados());
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
            Map<String,Object> datos = new HashMap<>();
            datos.put("nombre", u.getNombre());
            datos.put("posicion1", u.getPosicion1());
            datos.put("posicion2", u.getPosicion2());
            datos.put("correo", u.getCorreo());
            datos.put("titulo", u.getTitulo().getNombre());
            datos.put("tarjetaVisita", u.getTarjetaVisita().getNombre());
            datos.put("goles", u.getGoles());
            datos.put("asistencias", u.getAsistencias());
            datos.put("mvp", u.getMvps());
            datos.put("partidosJugados", u.getPartidosJugados());
            respuesta = AyudanteConteston.contestarTodoBien("iCcTkC", "Inicio de sesión correcto", datos);
            j.setIdUsuario(u.getId());
        }

        return respuesta;
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
        try (Session session = sessionFactory.openSession()){
                Transaction transaction = session.beginTransaction();
                session.persist(c);
                try {
                    transaction.commit();
                    System.out.println("TeamUp|MensajeInterno|Carta Creada.");
                } catch (IllegalStateException em) {
                    System.out.println("TeamUp|Error|EM2|.");
                }

        }
    }

    private void crearInventario(Usuario u) {
        System.out.println("TeamUp|MensajeInterno| Hemos entrado para crear inventario"); 
        try (Session session = sessionFactory.openSession()) {
            Inventario inv = new Inventario(u);
            Transaction transaction = session.beginTransaction();
            session.persist(inv);


            try {
                transaction.commit();
                System.out.println("TeamUp|MensajeInterno|Inventario creado");
            } catch (IllegalStateException em) {
                System.out.println("TeamUp|Error|EM2|.");
            }

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
    }

    private void persistirObjeto(Object objeto) { // la verdad se me ocurro probarlo y me sorprendio que funcionará, si no esta usado mucho es que lo pense mucho mucho mas tarde para los cosmeticos por eso la mayoria de cosas no usan esta funcion
        try (Session session = sessionFactory.openSession()) {
            session.persist(objeto);
            Transaction transaction = session.beginTransaction();
            
            try {
                transaction.commit();
                System.out.println("TeamUp|MensajeInterno|Objetos añadidos al inventario");
            } catch (IllegalStateException em) {
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
                
                Transaction transaction = session.beginTransaction();
                session.persist(u);
                System.out.println("TeamUp|MensajeInterno| id del usuario despues del persisit " + u.getId());

                try {
                    transaction.commit();
                    System.out.println("TeamUp|MensajeInterno|Usuario dado de alta.");
                    j.setIdUsuario(u.getId());
                } catch (IllegalStateException em) {
                    System.out.println("TeamUp|Error|EM2|.");
                }

                crearInventario(u);

                if (recordarme.equals("1")) {
                    Map<String,Object> datos = new HashMap<>();
                    List<String> lista = generarRememberToken(u, j);
                    datos.put("selector", lista.get(0));
                    datos.put("token", lista.get(1));
                    datos.put("nombre", u.getNombre());
                    datos.put("posicion1", u.getPosicion1());
                    datos.put("posicion2", u.getPosicion2());
                    datos.put("correo", u.getCorreo());
                    datos.put("titulo", u.getTitulo().getNombre());
                    datos.put("tarjetaVisita", u.getTarjetaVisita().getNombre());
                    datos.put("goles", u.getGoles());
                    datos.put("asistencias", u.getAsistencias());
                    datos.put("mvp", u.getMvps());
                    datos.put("partidosJugados", u.getPartidosJugados());
                    resultado = AyudanteConteston.contestarTodoBien("rC", "Registro completo", datos);
                } else {
                    Map<String,Object> datos = new HashMap<>();
                    datos.put("nombre", u.getNombre());
                    datos.put("posicion1", u.getPosicion1());
                    datos.put("posicion2", u.getPosicion2());
                    datos.put("correo", u.getCorreo());
                    datos.put("titulo", u.getTitulo().getNombre());
                    datos.put("tarjetaVisita", u.getTarjetaVisita().getNombre());
                    datos.put("goles", u.getGoles());
                    datos.put("asistencias", u.getAsistencias());
                    datos.put("mvp", u.getMvps());
                    datos.put("partidosJugados", u.getPartidosJugados());
                    resultado = AyudanteConteston.contestarTodoBien("rC", "Registro Completo", datos); 
                }
            }
        }


        return resultado;
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
        String selector = u.getId() + "" + u.getNombre().substring(0,4) + "14" + u.getFechaCreacion().getMinute() + u.getFechaCreacion().getDayOfWeek();
        String cadenaExtra = cadenaAleatoria();
        String token =  u.getFechaCreacion().getHour() + "" + u.getNombre().substring(0,4) + "14" + u.getFechaCreacion().getYear() + u.getFechaCreacion().getMinute() + u.getPosicion1() + cadenaExtra;
        listaDatos.add(selector);
        listaDatos.add(token);
        
        InetAddress datosCliente = j.getZocalo().getInetAddress();
        String ip = datosCliente.getHostAddress();
        String dispositivo = datosCliente.getHostName();
        System.out.println("TeamUp|MensajeInterno|Remember token con selector:  " + selector + " y para el usuario con nombre: " + u.getNombre());
        RememberToken rm = new RememberToken(u, selector,hashearToken(token),dispositivo, ip);

        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.persist(rm);

            try {
                transaction.commit();
                System.out.println("TeamUp|MensajeInterno|Usuario dado de alta.");
            } catch (IllegalStateException em) {
                System.out.println("TeamUp|Error|EM2|");
            }
        }

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