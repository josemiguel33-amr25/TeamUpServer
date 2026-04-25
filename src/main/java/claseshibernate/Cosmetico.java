package claseshibernate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="cosmetico")
public class Cosmetico {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String nombre;
    private String tipo;
    private String rareza;

    public Cosmetico() {
        
    }
}