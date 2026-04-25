package claseshibernate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="inventario_cosmetico")
public class InventarioCosmetico {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private int cantidad;

    @ManyToOne
    @JoinColumn(name="inventario_id")
    private Inventario inventario;

    @ManyToOne
    @JoinColumn(name="cosmetico_id")
    private Cosmetico cosmetico;

    public InventarioCosmetico() {
        
    }
}