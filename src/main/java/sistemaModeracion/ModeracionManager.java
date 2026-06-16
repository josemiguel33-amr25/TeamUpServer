package sistemaModeracion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ClasesServer.BaseDatosManager;
import clases.AyudanteConteston;
import clases.CosmeticoSimplificado;

public class ModeracionManager {
    private static ModeracionManager moderacionManager; // esta clase se va a encargar de todo lo relacionado con moderacion y funciones de administrador en general
// funciones como dar sobres, etc 
    public ModeracionManager() { // no usado al final por falta de tiempo para implementar

    }

    public ModeracionManager getModeracionManager() {
        if (moderacionManager == null)
            moderacionManager = new ModeracionManager();

        return moderacionManager;
    }

    public String darSobreUsuario(String nombreUsuario, String nombreSobre, int cantidad, BaseDatosManager bdm) { // lo mismo que cuento abajo no hago comprobaciones porque los sobres que puede dar se obtienen desde el servidor por tanto no necesito comprobar si el sobre existe
        String respuesta = AyudanteConteston.contestarError("eNSHDSU", "Error no se ha podido dar el sobre al usuario porque el usuario no existe", "darSobreUsuario");
        
        if (bdm.comprobarUsuarioExiste(nombreUsuario) ) {
            respuesta = AyudanteConteston.contestarTodoBien("sEAUCPA", "Sobre entregado al usuario correctamente","darSobreUsuario", null);
            bdm.darSobre(bdm.obtenerUsuario(nombreUsuario).getId(), bdm.obtenerSobre(nombreSobre), cantidad);
        } 

        return respuesta;
    }

    public String darCosmeticoUsauario(String nombreUsuario, int idCosmetico, int cantidad, BaseDatosManager bdm) { // no compruebo si el cosmetico existe porque desde el cliente habra un menu desplegable que se creara llamando al servidor y obteniendo todos los cosmeticos desde el servidor  por lo tanto no hará falta comprobar si existe porque si existirá
        String respuesta = AyudanteConteston.contestarError("eNSHPECU", "Error no se ha podido entregar el cosmetico porque el usuario no existe", "darCosmeticoUsuario");
        
        if (bdm.comprobarUsuarioExiste(nombreUsuario) ) {
            respuesta = AyudanteConteston.contestarTodoBien("sEECAU", "Cosmetico entregado correctamente al usuario","darCosmeticoUsuario", null);
            bdm.darCosmetico(bdm.obtenerUsuario(nombreUsuario).getId(), bdm.obtenerCosmetico(cantidad), cantidad);
        } 

        return respuesta;
    }


    public String obtenerCosmeticosServidor(BaseDatosManager bdm) {
        Map<String, Object> datos = new HashMap<>();
        List<CosmeticoSimplificado> cosmeticosServidor = bdm.obtenerCosmeticosServidor();

        datos.put("cosmeticos", cosmeticosServidor);

        String respuesta = AyudanteConteston.contestarTodoBien("cDSEC", "Cosmeticos del servidor entregados correctamente", "obtenerCosmeticos", datos);

        return respuesta;
    }

    public String darMonedasUsuario(String nombreUsuario, int cantidad, BaseDatosManager bdm) { 
        String respuesta = AyudanteConteston.contestarError("eNSHPDM", "Error no se ha podido dar monedas al usuario porque el usuario no existia", "darMonedas");
        
        if (bdm.comprobarUsuarioExiste(nombreUsuario) ) {
            respuesta = AyudanteConteston.contestarTodoBien("sHELMC", "Se le han entregado las monedas correctamente al usuario","darMonedas", null);
            bdm.darMonedas(bdm.obtenerUsuario(nombreUsuario).getId(),  cantidad);
        } 

        return respuesta;
    }

    

    


}
