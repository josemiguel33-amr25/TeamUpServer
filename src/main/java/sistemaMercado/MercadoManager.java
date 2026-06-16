package sistemaMercado;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.Query;

import ClasesServer.BaseDatosManager;
import clases.AyudanteConteston;
import clases.MercadoSimplificado;
import claseshibernate.Cosmetico;
import claseshibernate.Mercado;
import claseshibernate.Usuario;

public class MercadoManager {
    private static MercadoManager mercadoManager;

    private MercadoManager() {
        System.out.println("TeamUp|MensajeIntereno|Modulos mercado creado");
    }

    public static MercadoManager getMercadoManager() {
        if (mercadoManager == null) 
            mercadoManager = new MercadoManager();

        return mercadoManager;
    }


    public String obtenerElementosMercado(BaseDatosManager bdm) { // Sistema cosmetico
        String respuesta = AyudanteConteston.contestarError("eNHAM", "No hay articulos en el mercado", "obtenerElementosMercado");
        List<MercadoSimplificado> objetosSimplificados = new ArrayList<>();
        Map<String,Object> datos = new HashMap<>();

        
        try (Session session = bdm.getSessionFactory().openSession()) {
            Query<Mercado> q = session.createQuery("FROM Mercado ORDER BY fechaPublicacion DESC",Mercado.class);

            List<Mercado> elementosMercado = q.list();

            if (!elementosMercado.isEmpty()) {
                for (Mercado m : elementosMercado) 
                    objetosSimplificados.add(new MercadoSimplificado(m.getCosmetico().getNombre(), m.getVendedor().getNombre(), m.getVendedor().getId(), m.getPrecio(), m.getId(), m.getCosmetico().getTipo()));
                
                datos.put("elementos", objetosSimplificados);
                respuesta = AyudanteConteston.contestarTodoBien("sHDEM", "Devuelto elementos dentro de mercado", "obtenerElementosMercado", datos);
            }


        }

        return respuesta;
    }

    public String obtenerElementosUsuarioMercado(int idUsuario, BaseDatosManager bdm) { // Sistema cosmetico
        String respuesta = AyudanteConteston.contestarError("eNHAM", "No tienes articulos en el mercado","obtenerElementosUsuarioMercado");
        List<MercadoSimplificado> objetosSimplificados = new ArrayList<>();
        Map<String,Object> datos = new HashMap<>();

        
        try (Session session = bdm.getSessionFactory().openSession()) {

            Query<Mercado> q = session.createQuery("FROM Mercado " +"WHERE vendedor.id = :idUsuario " +"ORDER BY fechaPublicacion DESC",Mercado.class);
            q.setParameter("idUsuario", idUsuario);
            List<Mercado> elementosMercado = q.list();

            if (!elementosMercado.isEmpty()) {
                for (Mercado m : elementosMercado) 
                    objetosSimplificados.add(new MercadoSimplificado(m.getCosmetico().getNombre(), m.getVendedor().getNombre(), m.getVendedor().getId(), m.getPrecio(), m.getId(), m.getCosmetico().getTipo()));
                
                datos.put("elementos", objetosSimplificados);
                respuesta = AyudanteConteston.contestarTodoBien("sHDEM", "Devuelto elementos dentro de mercado del usuario", "obtenerElementosUsuarioMercado", datos);
            }


        }

        return respuesta;
    }

    public String quitarArticuloMercado(int idUsuario, int idElementoMercado, BaseDatosManager bdm) {  // sistema cosmetico
        String respuesta = AyudanteConteston.contestarError("nSHPQEADM", "No se ha podido quitar el articulo del mercado", "quitarArticuloMercado");
        Mercado elemento = bdm.obtenerElementoMercado(idElementoMercado);
        
    
        if (elemento != null) {
            bdm.darCosmetico(idUsuario, elemento.getCosmetico(), 1);
            respuesta = AyudanteConteston.contestarTodoBien("sHQEADM", "Se ha quitado el articulo del mercado", "quitarArticuloMercado", null);
            bdm.eliminarArticuloMercado(elemento);
        }
        
        

        return respuesta;

    
    }


    public String comprarArticulo(int idUsuario, int idArticuloMercado, BaseDatosManager bdm) { // sistema cosmetico
        String respuesta = AyudanteConteston.contestarError("nSHPCEA", "No se ha podido comprar el articulo del mercado, porque eres el dueño o no tienes monedas", "comprarArticulo");
        Mercado elemento = bdm.obtenerElementoMercado(idArticuloMercado);
        Usuario u = bdm.obtenerUsuarioPorId(idUsuario);
        Map<String,String> datos = new HashMap<>();
        if (elemento.getVendedor().getId() != idUsuario) {
            if (u.getMonedas() >= elemento.getPrecio()) {
                int monedas = u.getMonedas() - elemento.getPrecio();
                datos.put("nuevasMonedas", String.valueOf(monedas));
                bdm.darCosmetico(idUsuario, elemento.getCosmetico(), 1);
                bdm.quitarMonedas(idUsuario, elemento.getPrecio());
                bdm.darMonedas(elemento.getVendedor().getId(), elemento.getPrecio());
                bdm.eliminarArticuloMercado(elemento);
                respuesta = AyudanteConteston.contestarTodoBien("sHCEA", "Se ha comprado el articulo", "comprarArticulo", null);
            }
        } 
        return respuesta;
    }

    public String ponerArticuloVenta(int idUsuario, int idCosmetico, int precio, BaseDatosManager bdm) { 
        String respuesta = AyudanteConteston.contestarError("nSHPPEV", "No se ha podido poner en venta el articulo porque este articulo no se puede vender", "ponerArticuloVenta");
        Cosmetico c = bdm.obtenerCosmetico(idCosmetico);

        if (c != null && c.isVendible()) {
            Mercado m = new Mercado(bdm.obtenerUsuarioPorId(idUsuario), c, precio);
            bdm.persistirObjeto(m);
            bdm.quitarCosmetico(idUsuario, idCosmetico);
            respuesta = AyudanteConteston.contestarTodoBien("aPEV", "Articulo puesto en venta en el mercado", "ponerArticuloVenta", null);
        }

        return respuesta;

    }

}
