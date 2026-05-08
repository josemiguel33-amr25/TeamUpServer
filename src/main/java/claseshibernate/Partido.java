package claseshibernate;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="partido")
public class Partido {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String titulo;

    private LocalDateTime fecha;

    private String ubicacion;
    private double precio;

    @ManyToOne
    @JoinColumn(name="creador_id")
    private Usuario creador;

    @OneToMany(mappedBy="partido")
    private Set<Participacion> participaciones;

    public Partido() {
        
    }
}