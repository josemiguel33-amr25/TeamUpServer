package claseshibernate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="participacion")
public class Participacion {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private int puntuacion;
    private boolean mvp;
    private boolean asistio;
    private String equipo;

    @ManyToOne
    @JoinColumn(name="usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name="partido_id")
    private Partido partido;

    public Participacion() {}
}