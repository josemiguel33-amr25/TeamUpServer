package sistemaAutenticacion;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.Query;

import ClasesServer.BaseDatosManager;
import ClasesServer.JugadorSistema;
import clases.AyudanteConteston;
import clases.CosmeticoSimplificado;
import claseshibernate.Carta;
import claseshibernate.InventarioCosmetico;
import claseshibernate.RememberToken;
import claseshibernate.Usuario;
import sistemaCarta.CartaManager;
import utilidades.UtilidadesCuentas;

public class UsuarioManager {
    private static UsuarioManager usuarioManager;

    private UsuarioManager() {
        System.out.println("TeamUp|MensajeIntereno|Modulos autenticacion creado");
    }

    public static UsuarioManager getUsuarioManager() {
        if (usuarioManager == null) 
            usuarioManager = new UsuarioManager();

        return usuarioManager;
    }

    public String registrarUsuario(String nombre, String contrasenia, String correo, String posicion1, String posicion2, String recordarme, JugadorSistema j, BaseDatosManager bdm) {
        String resultado = "";
        System.out.println("TeamUp|MensajeInterno|" + nombre + " con correo: " + correo + " con posicion1" + posicion1 + " y con recordarme " + recordarme);
        boolean usuarioExiste = bdm.comprobarUsuarioExiste(nombre);
        boolean correoExiste = bdm.comprobarCorreoAsociado(correo);


        try (Session session = bdm.getSessionFactory().openSession()) {
        // Esto lo podriamos llevar a una funcion que deolviera un objeto que estuviera fomrado por UN boolean false / true y el mensaje, la verdad seria bueno y ayudaria al codigo pero mas adelante
            if (usuarioExiste) {
                resultado = AyudanteConteston.contestarError("errU", "Usuario ya existe");
            } else if (correoExiste){
                resultado = AyudanteConteston.contestarError("errC", "Correo ya existe");
            } else if (!UtilidadesCuentas.getUtilidades().validarNombreUsuario(nombre)) {
                resultado = AyudanteConteston.contestarError("errUu", "Nombre del usuario no cumple los requisitos");
            } else if (!UtilidadesCuentas.getUtilidades().validarCorreo(correo)) {
                resultado = AyudanteConteston.contestarError("errCc", "El correo no cumple el formato de correo");
            } else if (!UtilidadesCuentas.getUtilidades().validarContrasenia(contrasenia)) {
                resultado = AyudanteConteston.contestarError("errCo", "La contraseña no cumple los minimos de seguridad");
            } else {
                String contraseniaEncriptada  = UtilidadesCuentas.getUtilidades().encriptarContrasenia(contrasenia);
                Usuario u = new Usuario(nombre, correo, contraseniaEncriptada, posicion1, posicion2, bdm.obtenerCosmetico(2), bdm.obtenerCosmetico(3));
                bdm.persistirObjeto(u);
                j.setIdUsuario(u.getId());
                bdm.crearInventario(u);
                CartaManager.getCartaManager().generadorCarta(posicion1, posicion2, nombre, bdm);

                if (recordarme.equals("1")) {
                    Map<String,Object> datos = construirUsuario(u, bdm);
                    List<String> lista = generarRememberToken(u, j, bdm);
                    datos.put("selector", lista.get(0));
                    datos.put("token", lista.get(1));
                    resultado = AyudanteConteston.contestarTodoBien("rC", "Registro completo", datos);
                } else {
                    Map<String,Object> datos = construirUsuario(u, bdm);
                    resultado = AyudanteConteston.contestarTodoBien("rC", "Registro Completo", datos); 
                }
            }
        }
        return resultado;
    }

    public Map<String,Object> construirUsuario(Usuario u, BaseDatosManager bdm) { // sistema cuentas
        Carta carta = bdm.obtenerCartaUsuario(u.getId());
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

    public List<String> generarRememberToken(Usuario u, JugadorSistema j, BaseDatosManager bdm) { //esto devuelve una lista con selector + el token sin hashear en documenta el proceso que he segudio
        List<String> listaDatos = new ArrayList<>();
        System.out.println("TeamUp|MensajeInterno|Usario con id" + u.getId() + " con nombre " + u.getNombre());
        String selector = u.getId() + "" + u.getNombre().substring(0,3) + "14" + u.getFechaCreacion().getMinute() + u.getFechaCreacion().getDayOfWeek();
        String cadenaExtra = UtilidadesCuentas.getUtilidades().cadenaAleatoria();
        String token =  u.getFechaCreacion().getHour() + "" + u.getNombre().substring(0,3) + "14" + u.getFechaCreacion().getYear() + u.getFechaCreacion().getMinute() + u.getPosicion1() + cadenaExtra;
        listaDatos.add(selector);
        listaDatos.add(token);
        
        InetAddress datosCliente = j.getZocalo().getInetAddress();
        String ip = datosCliente.getHostAddress();
        String dispositivo = datosCliente.getHostName();
        System.out.println("TeamUp|MensajeInterno|Remember token con selector:  " + selector + " y para el usuario con nombre: " + u.getNombre());
        RememberToken rm = new RememberToken(u, selector,UtilidadesCuentas.getUtilidades().hashearToken(token),dispositivo, ip);
        bdm.persistirObjeto(rm);
        //remember token, slelector, token, fecha expi, dispositivo, ip
        return listaDatos;

    }

    public String obtenerInventarioUsuario(int idUsuario, BaseDatosManager bdm) { // sisstema cuentas
        String respuesta = AyudanteConteston.contestarError("nSHPOBI", "Error no se ha podido obtener el inventario");

        try (Session session = bdm.getSessionFactory().openSession()){
            Query<InventarioCosmetico> q = session.createQuery( "FROM InventarioCosmetico WHERE inventario.usuario.id = :idUsuario", InventarioCosmetico.class);

            q.setParameter("idUsuario", idUsuario);

            List<InventarioCosmetico> listaProcesar = q.list();
            List<CosmeticoSimplificado> cosmeticos = new ArrayList<>();

            for (InventarioCosmetico elemento : listaProcesar) {
                cosmeticos.add(new CosmeticoSimplificado(elemento.getCosmetico().getNombre(),  elemento.getCosmetico().getTipo(), elemento.getCosmetico().getRareza(), elemento.getCantidad(), elemento.getCosmetico().isVendible())); //titulo, tipo, rareza, cantidad, vendible
            }
            Map<String, Object> datos = new HashMap<>();

            datos.put("cosmeticos", cosmeticos);
            respuesta = AyudanteConteston.contestarTodoBien("iNe", "Inventario enviado", datos);
        }

        return respuesta;
    }

        
    //devuelve string pero pongo void para que no salga en rojo // SISTEMA cuentas
    
    //devuelve string pero pongo void para que no salga en rojo // SISTEMA cuentas
    public String iniciarSesionContrasenia(String correo, String contrasenia, JugadorSistema j, BaseDatosManager bdm) { // pasarle los datos en esta funcion comprobar antes si tiene token, comprobar token y darle directamente paso o comprobar contrasenia y nombre
        // si no tiene remember token usamos la funcion que tenemos para comprobar contraseña, si tiene remember token creo que tenemos funcion para comprobar rmemeber token
        String respuesta = "";
        boolean correoExiste = bdm.comprobarCorreoAsociado(correo);
        if (!correoExiste) {
            respuesta = AyudanteConteston.contestarError("erIncnoe", "El correo introducido no existe");
        } else if (!UtilidadesCuentas.getUtilidades().comprobarUsuario(correo, contrasenia, bdm))
            respuesta = AyudanteConteston.contestarError("erIncin", "La contraseña introducida es incorrecta");
        else {
            Usuario u = bdm.obtenerUsuarioPorCorreo(correo);
            Map<String,Object> datos = construirUsuario(u, bdm);
            respuesta = AyudanteConteston.contestarTodoBien("iC", "Inicio de sesion correcto", datos);
            j.setIdUsuario(u.getId());
        }
        
        
        return respuesta;
    }


    public String iniciarSesionToken(String selector, String token, JugadorSistema j, BaseDatosManager bdm) {  // sistema cuentas
        String respuesta = "";
        boolean rememberTokenExiste = bdm.comprobarTokenExiste(selector);

        if (!rememberTokenExiste) {
            respuesta = AyudanteConteston.contestarError("ertkNe", "El token no existe");
        } else if (UtilidadesCuentas.getUtilidades().comprobarRememberToken(selector, token, bdm)) {
            Usuario u = bdm.obtenerRememberToken(selector).getUsuario();
            Map<String,Object> datos = construirUsuario(u, bdm);
            respuesta = AyudanteConteston.contestarTodoBien("iCcTkC", "Inicio de sesión correcto", datos);
            j.setIdUsuario(u.getId());
        }

        return respuesta;
    }

    public String verPerfilJugador(int idUsuario, BaseDatosManager bdm) { // sistema cuentas
        Map<String, Object> datos = construirUsuario(bdm.obtenerUsuarioPorId(idUsuario), bdm);
        return AyudanteConteston.contestarTodoBien("iNSUC", "Informacion del usuario conseguida correctamente", datos);
    }




}
