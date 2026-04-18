package claseshibernate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="inventario")
public class Inventario {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name="usuario_id")
    private Usuario usuario;

    public Inventario() {}
}