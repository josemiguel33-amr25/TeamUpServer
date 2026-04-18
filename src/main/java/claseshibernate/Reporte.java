package claseshibernate;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="reporte")
public class Reporte {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String motivo;

    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name="reportador_id")
    private Usuario reportador;

    @ManyToOne
    @JoinColumn(name="reportado_id")
    private Usuario reportado;

    @ManyToOne
    @JoinColumn(name="partido_id")
    private Partido partido;

    public Reporte() {}
}