
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;

import claseshibernate.Carta;
import claseshibernate.Cosmetico;
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

    
    //devuelve string pero pongo void para que no salga en rojo
    public String iniciarSesionContrasenia(String correo, String contrasenia) { // pasarle los datos en esta funcion comprobar antes si tiene token, comprobar token y darle directamente paso o comprobar contrasenia y nombre
        // si no tiene remember token usamos la funcion que tenemos para comprobar contraseña, si tiene remember token creo que tenemos funcion para comprobar rmemeber token
        String respuesta = "TeamUp|Directriz|Inicio Sesion Fallido|erIn";
        boolean correoExiste = comprobarCorreoAsociado(correo);
        if (!correoExiste) {
            respuesta = "TeamUp|Directriz|Inicio Sesion Fallido|erIncnoe";
        } else if (!comprobarUsuario(correo, contrasenia))
            respuesta = "TeamUp|Directriz|Inicio Sesion Fallido|erIncin";
        else
            respuesta = "TeamUp|Directriz|Inicio Sesion Correcto|iC";
        
        
        return respuesta;
    }

    public String iniciarSesionToken(String selector, String token) { 
        String respuesta = "TeamUp|Directriz|Inicio Sesion Fallido|ertkNv";
        boolean rememberTokenExiste = comprobarTokenExiste(selector);

        if (!rememberTokenExiste) {
            respuesta = "TeamUp|Directriz|Inicio Sesion Fallido|ertkNe";
        } else if (comprobarRememberToken(selector, token))
            respuesta = "TeamUp|Directriz|Inicio Sesion Correcto|iCcTkC";
        

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

    public String registrarUsuario(String nombre, String contrasenia, String correo, String posicion1, String posicion2, String recordarme, JugadorSistema j) {
        String resultado = "";
        System.out.println("TeamUp|MensajeInterno|" + nombre + " con correo: " + correo + " con posicion1" + posicion1 + " y con recordarme " + recordarme);
        boolean usuarioExiste = comprobarUsuarioExiste(nombre);
        boolean correoExiste = comprobarCorreoAsociado(correo);


        try (Session session = sessionFactory.openSession()) {
        // Esto lo podriamos llevar a una funcion que deolviera un objeto que estuviera fomrado por UN boolean false / true y el mensaje, la verdad seria bueno y ayudaria al codigo pero mas adelante
            if (usuarioExiste) {
                resultado = "TeamUp|Directriz|Registro Fallido|errU";
            } else if (correoExiste){
                resultado = "TeamUp|Directriz|Registro Fallido|errC";
            } else if (!validarNombreUsuario(nombre)) {
                resultado = "TeamUp|Directriz|Registro Fallido|errUu";
            } else if (!validarCorreo(correo)) {
                resultado = "TeamUp|Directriz|Registro Fallido|errCc";
            } else if (!validarContrasenia(contrasenia)) {
                resultado = "TeamUp|Directriz|Registro Fallido|errCo";
            } else {
                String contraseniaEncriptada  = encriptarContrasenia(contrasenia);
                Usuario u = new Usuario(nombre, correo, contraseniaEncriptada, posicion1, posicion2);
                
                Transaction transaction = session.beginTransaction();
                session.persist(u);
                System.out.println("TeamUp|MensajeInterno| id del usuario despues del persisit " + u.getId());

                try {
                    transaction.commit();
                    System.out.println("TeamUp|MensajeInterno|Usuario dado de alta.");
                } catch (IllegalStateException em) {
                    System.out.println("TeamUp|Error|EM2|.");
                }

                if (recordarme.equals("1")) {
                    List<String> lista = generarRememberToken(u, j);
                    resultado = "TeamUp|Directriz|Registro Completo|rCºtoken:" + lista.get(0) + ":" + lista.get(1);
                } else {
                    resultado = "TeamUp|Directriz|Registro Completo|rC";
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

/*
Session session = sessionFactory.openSession(); CREO UNA sesion cada consulta y cierro 
Transaction tx = session.beginTransaction();

try {
    // operaciones
    tx.commit();
} catch (Exception e) {
    tx.rollback();
} finally {
    session.close();
}

// CONSULTA PARAMETRIZADA que no la usste en el examen, que menos ahora en el proyecto ;:()
Query<Hospital> q = session.createQuery(
    "from Hospital where nombre = :nombre", Hospital.class);

q.setParameter("nombre", nombreHospital);
 */


