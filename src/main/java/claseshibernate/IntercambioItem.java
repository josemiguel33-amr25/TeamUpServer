package claseshibernate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="intercambio_item")
public class IntercambioItem {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name="intercambio_id")
    private Intercambio intercambio;

    @ManyToOne
    @JoinColumn(name="cosmetico_id")
    private Cosmetico cosmetico;

    @ManyToOne
    @JoinColumn(name="usuario_id")
    private Usuario usuario;

    public IntercambioItem() {
        
    }
}