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
@Table(name="intercambio")
public class Intercambio {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String estado;

    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name="usuario1_id")
    private Usuario usuario1;

    @ManyToOne
    @JoinColumn(name="usuario2_id")
    private Usuario usuario2;

    @OneToMany(mappedBy="intercambio")
    private Set<IntercambioItem> items;

    public Intercambio() {
        
    }
}