package clases;

public class UsuarioSimplificado { // clase para simplificar el usuario de hibernate para cuando lo tengo que pasar al cliente
    private String nombre; //no uso esta clase para todo
    private String rango;
    private int puntos;
    private int reputacion;
    private int goles;
    private int asistencias;
    private int mvps;
    private boolean verificado;

    public UsuarioSimplificado(String nombre, String rango, int puntos, int reputacion, int goles, int asistencias, int mvps, boolean verificado) {
        this.nombre = nombre;
        this.rango = rango;
        this.puntos = puntos;
        this.reputacion = reputacion;
        this.goles = goles;
        this.asistencias  = asistencias;
        this.mvps = mvps;
        this.verificado = verificado;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRango() {
        return rango;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getReputacion() {
        return reputacion;
    }

    public int getGoles() {
        return goles;
    }

    public int getAsistencias() {
        return asistencias;
    }

    public int getMvps() {
        return mvps;
    }
}
