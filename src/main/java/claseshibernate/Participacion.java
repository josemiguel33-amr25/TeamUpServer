package claseshibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="participacion")
public class Participacion { // todos los getters por si acaso

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private int puntuacion;
    private boolean mvp;
    private boolean asistio;
    private String equipo;

    private int goles;
    private int asistencias;

    @ManyToOne
    @JoinColumn(name="usuario_id")
    private Usuario usuario;

    @Column(name = "recompensas_recogidas") 
    private boolean recompensasRecogidas;

    @ManyToOne
    @JoinColumn(name="partido_id")
    private Partido partido;

    @Column(name = "ha_votado")
    private boolean haVotado = false;

    public Participacion() {
        
    }

    public Participacion(Usuario usuario, Partido partido, String equipo) {
        goles = 0;
        asistencias = 0;
        mvp = false;
        asistio = false;
        puntuacion = 1;
        this.usuario = usuario;
        this.partido = partido;
        this.equipo = equipo;
        recompensasRecogidas = false;

    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public boolean isMvp() {
        return mvp;
    }

    public boolean isHaVotado() {
    return haVotado;
}

    public void setHaVotado(boolean haVotado) {
        this.haVotado = haVotado;
    }

    public boolean isAsistio() {
        return asistio;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public Partido getPartido() {
        return partido;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((usuario == null) ? 0 : usuario.hashCode());
        result = prime * result + ((partido == null) ? 0 : partido.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Participacion other = (Participacion) obj;
        if (usuario == null) {
            if (other.usuario != null)
                return false;
        } else if (!usuario.equals(other.usuario))
            return false;
        if (partido == null) {
            if (other.partido != null)
                return false;
        } else if (!partido.equals(other.partido))
            return false;
        return true;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public void setMvp(boolean mvp) {
        this.mvp = mvp;
    }

    public void setAsistio(boolean asistio) {
        this.asistio = asistio;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public void setGoles(int goles) {
        this.goles = goles;
    }

    public void setAsistencias(int asistencias) {
        this.asistencias = asistencias;
    }

    public boolean isRecompensasRecogidas() {
        return recompensasRecogidas;
    }

    public void setRecompensasRecogidas(boolean recompensasRecogidas) {
        this.recompensasRecogidas = recompensasRecogidas;
    }
}