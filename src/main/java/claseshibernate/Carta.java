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


    //Estadisticas jugador campo
    private int ritmo;
    private int tiro;
    private int pase;
    private int regate;
    private int defensa;
    private int fisico;

    // estadisticas portero solo estaran si el jugador especifica portero
    private int estirada;
    private int manejo;
    private int saque;
    private int reflejos;
    private int velocidad;
    private int posicionamiento;

    @ManyToOne
    @JoinColumn(name="usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name="cosmetico_id")
    private Cosmetico cosmetico;

    public Carta() {
        
    }

    public Carta(int ritmo, int tiro, int pase, int regate, int defensa, int fisico, Usuario u, Cosmetico c) {
        this.ritmo = ritmo;
        this.tiro = tiro;
        this.pase = pase;
        this.regate = regate;
        this.defensa = defensa;
        this.fisico = fisico;
        estirada = 50;
        manejo = 50;
        saque = 50;
        reflejos = 50;
        velocidad = 50;
        posicionamiento = 50;
        cosmetico = c;
        
    }

    public void setRitmo(int ritmo) {
        this.ritmo = ritmo;
    }

    public void setTiro(int tiro) {
        this.tiro = tiro;
    }

    public void setPase(int pase) {
        this.pase = pase;
    }

    public void setRegate(int regate) {
        this.regate = regate;
    }

    public void setDefensa(int defensa) {
        this.defensa = defensa;
    }

    public void setFisico(int fisico) {
        this.fisico = fisico;
    }

    public void setManejo(int manejo) {
        this.manejo = manejo;
    }

    public void setSaque(int saque) {
        this.saque = saque;
    }

    public void setReflejos(int reflejos) {
        this.reflejos = reflejos;
    }

    public void setVelocidad(int velocidad) {
        this.velocidad = velocidad;
    }

    public void setPosicionamiento(int posicionamiento) {
        this.posicionamiento = posicionamiento;
    }

    public void setCosmetico(Cosmetico cosmetico) {
        this.cosmetico = cosmetico;
    }

    public void setEstirada(int estirada) {
        this.estirada = estirada;
    }

    
}