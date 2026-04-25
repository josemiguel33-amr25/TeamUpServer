

//Este es la clase protocolo, le pongo sistema de juego por guiño a las alineaciones en el futbol, muchas veces se usa el termino Sistema de Juego

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SistemaDeJuego {
    private Servidor sv;
    private Set<Jugador> jugadores = Collections.synchronizedSet(new HashSet<>()); // clientes actualmente conectados



    public SistemaDeJuego(Servidor sv) {
        this.sv = sv;
    }

    public String buzon(String mensaje, Jugador j) { // le llamo buzon porque se encarga de recibir mensajes y enviar a su punto Formatos disponibles en la documentacion
        String respuesta = "TeamUp|Directriz|"; 
        String [] mensajePartido = mensaje.split(";");  // TeamUpCliente;Respuesta|registro|nombreºvalor:contraseniaºvalor:
        String [] datosDivididos = mensajePartido[1].split("\\|");
        String datos = datosDivididos[2];
        String opcion = datosDivididos[1];
        switch (opcion) { 
            case "registro":
                    // en el indice 3 estan los datos del usuario, solo tenemos que extrearlos y ir haciendo metodos lo escribo por si no me da tiempo hacerlo hoy para que te acuerdes
                    respuesta = registrarUsuario(datos, j); // Comprobar en la interfaz si el usuario introduce dato o no porque aqui pensamos que llega todo "bien" bien no se pero al menos informacion llega
                    jugadores.add(j);
            case "iniciarSesion": // iniciar sesion, el usuario le da el cliente comprueba si tenemos un token, si tenemos un token al darle al boton entraremos directamente a la aplicacion, sino pues a poner los datos
            
        }

        return respuesta;
    }

    public String registrarUsuario(String datos, Jugador j) { //datos formato es ---> |registroºvalor:contraseniaºvalor: el recordarmeºvalor (0 Falso o 1 true) va al final
        String respuesta = "TeamUp|Directriz|Registro de usuario fallido";
        Map<String, String> mapaDatos = new HashMap<>();
        String [] divisionDatos = datos.split(":"); //nombreºvalor:contraseniaºvalor:recordarmeº0/1:
        for (String dato : divisionDatos) {
            String [] datoParticionado = dato.split("º");
            mapaDatos.put(datoParticionado[0], datoParticionado[1]);
        }

        respuesta = sv.getBaseDatosManager().registrarUsuario(mapaDatos.get("nombre"), mapaDatos.get("contrasenia"), mapaDatos.get("correo"), mapaDatos.get("posicion1"), mapaDatos.get("posicion2"),mapaDatos.get("recordarme"), j);

        // tambien tienes que pensar el sistema de verificacion  --->> para simplificar el sistema de verificacio por ahora, vamos a hacer que la cuenta tenga 14 dias de antiguiedad y que haya participado minimo en un partido
        // y la estadisticas teniendo en cuenta las dos posiciones que haya elegido
        // llamar a la funcion de del baseDatosManager generar carta debajo de respuesta

        return respuesta;

    }

    private void generadorCarta(String posicion1, String posicion2, String nombre) {
        
    }

    public void iniciarSesion() { //correoºvalor:contraseniaºvalor:selectorºdato:tokenªdato

    }

}


