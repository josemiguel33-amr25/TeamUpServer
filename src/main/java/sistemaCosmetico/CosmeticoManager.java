package sistemaCosmetico;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ClasesServer.BaseDatosManager;
import clases.AyudanteConteston;
import clases.SobreSimplificado;
import claseshibernate.Carta;
import claseshibernate.Cosmetico;
import claseshibernate.Sobre;
import claseshibernate.Usuario;

public class CosmeticoManager {
    private static CosmeticoManager cosmeticoManager;

    private CosmeticoManager() {
        System.out.println("TeamUp|MensajeIntereno|Modulos cosmetico creado");
    }

    public static CosmeticoManager getCosmeticoManager() {
        if (cosmeticoManager == null) 
            cosmeticoManager = new CosmeticoManager();

        return cosmeticoManager;
    }


    public String comprarSobre(int idUsuario, String nombreSobre, BaseDatosManager bdm) { // Sistema cosmetico
        String respuesta = AyudanteConteston.contestarError("eNSHCS", "No se ha podido comprar el sobre porque no tienes monedas suficientes", "comprarSobre");
        Sobre sobre = bdm.obtenerSobre(nombreSobre);
        Usuario u = bdm.obtenerUsuarioPorId(idUsuario);
        Map<String, Object> datos = new HashMap<>();
        
        
        if (u.getMonedas() >= sobre.getPrecio()) {
            bdm.darSobre(idUsuario, sobre, 1);
            bdm.quitarMonedas(idUsuario, sobre.getPrecio());
            int monedas = u.getMonedas()-sobre.getPrecio();
            datos.put("nuevasMonedas", String.valueOf(monedas));
            respuesta = AyudanteConteston.contestarTodoBien("sEC", "Sobre comprado correctamente", "comprarSobre", datos);
        }

        return respuesta;

    }

    public String cambiarCosmetico(int idUsuario, int idCosmetico, BaseDatosManager bdm) { // Sistema cosmetico
        Usuario u = bdm.obtenerUsuarioPorId(idUsuario);
        Cosmetico c = bdm.obtenerCosmetico(idCosmetico);
        Carta carta = bdm.obtenerCartaUsuario(idUsuario);
        Map<String, Object> datos = new HashMap<>(); // aqui se va a poner el tipo de cosmetico que se ha cambiado para actualizarlo en el "viewModel de la interfaz para que los cambios funcionen por ejemplo  cambio mi tarjeta de visita pues habrá que actualizar los sitios que se guardan"
        datos.put("cambiado", c.getTipo());
        datos.put("nombreNuevo", c.getNombre()); 

        if (c.getTipo().equals("carta")) {
            carta.setCosmetico(c);// Como lo que tiene el usuario en la interfaz es el nombre del objeto para mandarlo aqui y conseguir la imagen, pues a la interfaz llega cambiado > coge lo que se ha cambiado y lo acutaliza en el objeto que habra en la interfaz (usuario con los datos que le pasamos (como si fuera un viewModel))
            bdm.actualizarObjeto(carta);
        } else if (c.getTipo().equals("tarjetaVisita")) {
            u.setTarjetaVisita(c);
            bdm.actualizarObjeto(u);
        } else if (c.getTipo().equals("titulo")) {
            u.setTitulo(c);
            bdm.actualizarObjeto(u);
        }


        return AyudanteConteston.contestarTodoBien("sHCECC", "Se ha cambiado el cosmetico correctamente", "cambiarCosmetico", datos);
    }

    public String verSobres(int idUsuario, String tipo, BaseDatosManager bdm) { // Sistema cosmetico
        String respuesta = AyudanteConteston.contestarError("uNTS", "El usuario no tiene sobres", "verSobres");
        
        if (tipo.equals("usuario")) { // Sobres que tiene el usuario
            Map<String, Object> datos = new HashMap<>();
            List<SobreSimplificado> sobres = bdm.obtenerSobresUsuario(idUsuario);
            if (!sobres.isEmpty()) {
                datos.put("sobres", sobres);
                respuesta = AyudanteConteston.contestarTodoBien("sHPTLSU", "Todos los sobres del usuario pasados", "verSobres", datos);
            }
        } else { // sobres de la tienda
            Map<String, Object> datos = new HashMap<>();
            List<SobreSimplificado> sobres = bdm.obtenerSobresVenta();
            datos.put("sobres", sobres);
            respuesta = AyudanteConteston.contestarTodoBien("sHPTLST", "Todos los sobres de la tienda pasados", "verSobresTienda", datos);
        }

        return respuesta;
    }

    public String abrirSobre(int idUsuario, String nombreSobre, BaseDatosManager bdm) { // Sistema cosmetico
        String respuesta = AyudanteConteston.contestarError("nSHPAS", "No se ha podido abrir el sobre", "abrirSobre");
        Sobre s = bdm.obtenerSobre(nombreSobre);
        Cosmetico cosmetico = s.obtenerContenidoAleatorio().getCosmetico();
        Map<String, Object> datos = new HashMap<>();

        datos.put("nombreCosmetico", cosmetico.getNombre());
        datos.put("rareza", cosmetico.getRareza());
        
        bdm.darCosmetico(idUsuario, cosmetico, 1);
        bdm.quitarSobre(idUsuario, nombreSobre);
    
        respuesta = AyudanteConteston.contestarTodoBien("sAC", "Sobre abierto correctamente", "abrirSobre", datos);

        return respuesta;
    }
    

}
