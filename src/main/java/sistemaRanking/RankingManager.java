package sistemaRanking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import ClasesServer.BaseDatosManager;
import clases.AyudanteConteston;
import clases.UsuarioSimplificado;
import claseshibernate.Usuario;

public class RankingManager {
    private static RankingManager rankingManager;

    private RankingManager() {
        System.out.println("TeamUp|MensajeIntereno|Modulos ranking creado");
    }

    public static RankingManager getRankingManager() {
        if (rankingManager == null) 
            rankingManager = new RankingManager();

        return rankingManager;
    }

    public String obtenerListaJugadoresRango(String rango, String mayorMenor, BaseDatosManager bdm) { // sistema ranking
        String respuesta = "";

        System.out.println("TeamUp|MensajeInterno| Entro en obtener lista de jugadores por rango con: " +rango + " y el usuario quiere filtrar por " + mayorMenor);
        try (Session session = bdm.getSessionFactory().openSession()){  
            org.hibernate.query.Query<Usuario> q; // no se porque aqui se pone la url entera del paquete y en otros no
            if (mayorMenor.equals("mayor")) {
                q = session.createQuery("FROM Usuario WHERE rango = :rango ORDER BY puntos DESC",Usuario.class);
                q.setParameter("rango", rango);
            } else {
                q = session.createQuery("FROM Usuario WHERE rango = :rango ORDER BY puntos ASC",Usuario.class);
                q.setParameter("rango", rango);
            }

            List<Usuario> jugadores = q.list();
            if (!jugadores.isEmpty()) {
                System.out.println("TeamUp|MensajeInterno|Lista de jugadores con: " + jugadores.size() + " jugadores y el primer jugador es:  " + jugadores.get(0).getNombre());
                List<UsuarioSimplificado> listaUsuariosSimplificada = new ArrayList<>();

                for (Usuario u : jugadores) {
                    listaUsuariosSimplificada.add(new UsuarioSimplificado(u.getNombre(), rango, u.getPuntos(), u.getReputacion(), u.getGoles(), u.getAsistencias(), u.getMvps(), u.isVerificado()));
                }
                Map<String, Object> datos = new HashMap<>();
                datos.put("jugadores", listaUsuariosSimplificada);
                respuesta = AyudanteConteston.contestarTodoBien("rCc", "Ranking creado correctamente", "ranking", datos);
            } else 
                respuesta = AyudanteConteston.contestarError("nHJR", "No hay jugadores en el rango seleccionado", "ranking");



        }


        return respuesta;
    }

    public String obtenerRangoUsuario(int rango) {
        String rangoNombre = "";

        if (rango == 1) { // a lo mejor switch mejor?
            rangoNombre = "Bronce";
        } else if (rango == 2) {
            rangoNombre = "Plata";
        } else if (rango == 3) {
            rangoNombre = "Oro";
        } else if (rango == 4) {
            rangoNombre = "Elite";
        }
        

        return rangoNombre;
    }


}
