package clases;

public class VotacionJugador { // clase que estara en cliente y server para la correspondencia de votacion y simplificar el paso de datos, para transformar lo que nos llega del cliente directamente a una lista de este objeto

    private int idUsuario;
    private int puntuacion; // siempre va a ser del 1 al 5
    private int goles;
    private int asistencias;
    private boolean mvp;

    public VotacionJugador() {

    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public int getGoles() {
        return goles;
    }

    public void setGoles(int goles) {
        this.goles = goles;
    }

    public int getAsistencias() {
        return asistencias;
    }

    public void setAsistencias(int asistencias) {
        this.asistencias = asistencias;
    }

    public boolean isMvp() {
        return mvp;
    }

    public void setMvp(boolean mvp) {
        this.mvp = mvp;
    }
}
