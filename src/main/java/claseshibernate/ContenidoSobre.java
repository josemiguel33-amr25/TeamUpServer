package claseshibernate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="contenido_sobre")
public class ContenidoSobre {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private double probabilidad;

    @ManyToOne
    @JoinColumn(name="sobre_id")
    private Sobre sobre;

    @ManyToOne
    @JoinColumn(name="cosmetico_id")
    private Cosmetico cosmetico;

    public ContenidoSobre() {
        
    }

    public double getProbabilidad() {
        return probabilidad;
    }

    public Sobre getSobre() {
        return sobre;
    }

    public Cosmetico getCosmetico() {
        return cosmetico;
    }
}