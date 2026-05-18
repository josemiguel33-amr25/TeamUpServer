package claseshibernate;

import jakarta.persistence.Column;
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


    @Column(name="vendible")
    private boolean vendible; // nueva caracteristica para el mercado, si se puede vender o no un cosmetico por ejemplo los cosmeticos de rangos, y los que vienen default no se pueden vender

    public Cosmetico() {
        
    }

    public Cosmetico(String nombre, String tipo, String rareza, boolean vendible) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.rareza = rareza;
        this.vendible = vendible;
    }

    public String getNombre() {
        return nombre;
    }

    public int getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getRareza() {
        return rareza;
    }

    public boolean isVendible() {
        return vendible;
    }
}