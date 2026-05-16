package claseshibernate;

import jakarta.persistence.Column;
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


    // la media de los jugadores muy importante
    @Column(name="media_jugador")
    private int mediaJugador;

    @Column(name="media_portero")
    private int mediaPortero;

    @ManyToOne
    @JoinColumn(name="usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name="cosmetico_id")
    private Cosmetico cosmetico;

    public Carta() {
        
    }

    public Carta(int ritmo, int tiro, int pase, int regate, int defensa, int fisico, Usuario usuario, Cosmetico cosmetico) {
        this.ritmo = ritmo;
        this.tiro = tiro;
        this.pase = pase;
        this.regate = regate;
        this.defensa = defensa;
        this.fisico = fisico;
        mediaJugador = calcularMedia(ritmo, tiro, pase, regate, defensa, fisico);
        mediaPortero = calcularMedia(estirada, manejo, saque, reflejos, velocidad, posicionamiento);
        this.usuario = usuario;
        this.cosmetico = cosmetico;
        estirada = 50;
        manejo = 50;
        saque = 50;
        reflejos = 50;
        velocidad = 50;
        posicionamiento = 50;
        
    }

    public int calcularMedia(int stat1, int stat2, int stat3, int stat4, int stat5, int stat6) {
        return  (stat1 + stat2 + stat3 + stat4 + stat5 + stat6) / 6;        
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

    public int getId() {
        return id;
    }

    public int getRitmo() {
        return ritmo;
    }

    public int getTiro() {
        return tiro;
    }

    public int getPase() {
        return pase;
    }

    public int getRegate() {
        return regate;
    }

    public int getDefensa() {
        return defensa;
    }

    public int getFisico() {
        return fisico;
    }

    public int getEstirada() {
        return estirada;
    }

    public int getManejo() {
        return manejo;
    }

    public int getSaque() {
        return saque;
    }

    public int getReflejos() {
        return reflejos;
    }

    public int getVelocidad() {
        return velocidad;
    }

    public int getPosicionamiento() {
        return posicionamiento;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Cosmetico getCosmetico() {
        return cosmetico;
    }

    public int getMediaJugador() {
        return mediaJugador;
    }

    public int getMediaPortero() {
        return mediaPortero;
    }

    public void setMediaJugador(int mediaJugador) {
        this.mediaJugador = mediaJugador;
    }

    public void setMediaPortero(int mediaPortero) {
        this.mediaPortero = mediaPortero;
    }

    
}