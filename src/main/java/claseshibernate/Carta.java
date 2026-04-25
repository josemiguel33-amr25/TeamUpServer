package claseshibernate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="carta")
public class Carta {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private int ritmo;
    private int tiro;
    private int pase;
    private int regate;
    private int defensa;
    private int fisico;

    @ManyToOne
    @JoinColumn(name="usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name="cosmetico_id")
    private Cosmetico cosmetico;

    public Carta() {
        
    }
}