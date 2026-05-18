package claseshibernate;

import java.util.Random;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="sobre")
public class Sobre {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String nombre; // el nombre tambien funciona como ruta para la imagen del sobre
    private String temporada;


    private int precio = 0;

    @OneToMany(mappedBy="sobre")
    private Set<ContenidoSobre> contenidos;

    public Sobre() {
        
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTemporada() {
        return temporada;
    }

    public int getPrecio() {
        return precio;
    }

    public Set<ContenidoSobre> getContenidos() {
        return contenidos;
    }

    public ContenidoSobre obtenerContenidoAleatorio() {

        Random random = new Random();

        int numero = random.nextInt(100) + 1;

        int acumulado = 0;

        for (ContenidoSobre c : contenidos) {

            acumulado += c.getProbabilidad();

            if (numero <= acumulado) {
                return c;
            }
        }

        return null;
    }
}