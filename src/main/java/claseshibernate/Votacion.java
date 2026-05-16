package claseshibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "votacion",
    uniqueConstraints = {
        @UniqueConstraint( // evitamos votar a la misma persona dos veces, aunqeu tambien lo compruebo en codigo
            columnNames = {
                "partido_id",
                "votante_id",
                "votado_id"
            }
        )
    }
)
public class Votacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "partido_id")
    private Partido partido;

    @ManyToOne
    @JoinColumn(name = "votante_id")
    private Usuario votante;

    @ManyToOne
    @JoinColumn(name = "votado_id")
    private Usuario votado;

    @Column(name = "puntuacion")
    private int puntuacion;

    @Column(name = "goles")
    private int goles = 0;

    @Column(name = "asistencias")
    private int asistencias = 0;

    @Column(name = "mvp")
    private boolean mvp = false;

    public Votacion() {

    }

    public Votacion(Partido partido,Usuario votante,Usuario votado,int puntuacion,int goles,int asistencias,boolean mvp) {
        this.partido = partido;
        this.votante = votante;
        this.votado = votado;
        this.puntuacion = puntuacion;
        this.goles = goles;
        this.asistencias = asistencias;
        this.mvp = mvp;
    }

    public int getId() {
        return id;
    }

    public Partido getPartido() {
        return partido;
    }

    public void setPartido(Partido partido) {
        this.partido = partido;
    }

    public Usuario getVotante() {
        return votante;
    }

    public void setVotante(Usuario votante) {
        this.votante = votante;
    }

    public Usuario getVotado() {
        return votado;
    }

    public void setVotado(Usuario votado) {
        this.votado = votado;
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