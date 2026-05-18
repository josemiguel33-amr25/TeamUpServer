package claseshibernate;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "mercado")
public class Mercado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    @ManyToOne
    @JoinColumn(name = "cosmetico_id")
    private Cosmetico cosmetico;

    @Column(name = "precio")
    private int precio;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    public Mercado() {

    }

    public Mercado(Usuario vendedor,Cosmetico cosmetico,int precio) {
        this.vendedor = vendedor;
        this.cosmetico = cosmetico;
        this.precio = precio;
        this.fechaPublicacion = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public void setVendedor(Usuario vendedor) {
        this.vendedor = vendedor;
    }

    public Cosmetico getCosmetico() {
        return cosmetico;
    }

    public void setCosmetico(Cosmetico cosmetico) {
        this.cosmetico = cosmetico;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }
}