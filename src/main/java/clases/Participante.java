package clases;

public class Participante { // Clase simplificada para los participantes de un partido
    private String nombreUsuario;
    private int idUsuario;
    private String fotoUsuario;
    private String equipo;
    private int goles;
    private int asistencias;
    private boolean mvp;

    public Participante(String nombreUsuario, int idUsuario, String fotoUsuario, String equipo, int goles, int asistencias, boolean mvp) {
        this.nombreUsuario = nombreUsuario;
        this.idUsuario = idUsuario;
        this.fotoUsuario = fotoUsuario;
        this.equipo = equipo;
        this.goles = goles;
        this.asistencias = asistencias;
        this.mvp = mvp;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getFotoUsuario() {
        return fotoUsuario;
    }

    public String getEquipo() {
        return equipo;
    }

    public int getGoles() {
        return goles;
    }

    public int getAsistencias() {
        return asistencias;
    }

    public boolean isMvp() {
        return mvp;
    }

    
    
}
