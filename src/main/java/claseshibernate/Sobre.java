package claseshibernate;

import java.util.Set;
import jakarta.persistence.*;

@Entity
@Table(name="sobre")
public class Sobre {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String nombre;
    private String temporada;

    @OneToMany(mappedBy="sobre")
    private Set<ContenidoSobre> contenidos;

    public Sobre() {}
}