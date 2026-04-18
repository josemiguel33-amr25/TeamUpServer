

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

    public void buzon(String mensaje) { // le llamo buzon porque se encarga de recibir mensajes y enviar a su punto Formatos disponibles en la documentacion
        String [] mensajePartido = mensaje.split(";");
        String [] datosDivididos = mensajePartido[2].split("\\|");
        String datos = datosDivididos[1];
        String opcion = datosDivididos[0];
        switch (opcion) { 
            case "registro":
                    // en el indice 3 estan los datos del usuario, solo tenemos que extrearlos y ir haciendo metodos lo escribo por si no me da tiempo hacerlo hoy para que te acuerdes
                    registrarUsuario(datos);
            case "iniciarSesion":

        }
    }

    public void registrarUsuario(String datos) { //datos formato es ---> |registroºvalor:contraseniaºvalor:
        Map<String, String> mapaDatos = new HashMap<>();
        String [] divisionDatos = datos.split(":");
        for (String dato : divisionDatos) {
            String [] datoParticionado = dato.split("º");
            mapaDatos.put(datoParticionado[0], datoParticionado[1]);
        }

        //Por hacer aqui Creacion del usuario y añadirlo a base de datos, si marca la opcion recuerdame que genere el token a la base de datos y comprobar que funcione
        // tambien tienes que pensar el sistema de verificacion 
        // hacer lo de validar el correo
        // y la estadisticas teniendo en cuenta las dos posiciones que haya elegido

    }

    public void iniciarSesion() {

    }

}


