package claseshibernate;

import jakarta.persistence.*;

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

    public ContenidoSobre() {}
}