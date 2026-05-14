import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AyudanteConteston {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String contestarTodoBien(String codigo, String mensaje, Map<String, Object> datos) {
        return crearMensajeJson("Perfecto", codigo, mensaje, datos);
    }

    public static String contestarError(String codigo, String mensaje) {
        return crearMensajeJson("error", codigo, mensaje, null);
    }

    private static String crearMensajeJson(String status, String codigo, String mensaje, Map<String, Object> datos) {
        try {
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("status", status);
            respuesta.put("codigo", codigo);

            if (mensaje != null) 
                respuesta.put("mensaje", mensaje);
            if (datos != null) 
                respuesta.put("datos", datos);

            return mapper.writeValueAsString(respuesta);

        } catch (Exception e) {
            System.out.println("TeamUp|MensajeInterno| Error en crear mensaje json por: " + e.getMessage());
            return "{\"status\":\"error\",\"codigo\":\"EM6\"}";
        }
    }
}