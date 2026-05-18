package claseshibernate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="partido")
public class Partido {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String titulo;

    private LocalDateTime fecha;

    @Column(name="solo_verificados")
    private boolean soloVerificados;

    private String ubicacion; // campo por ejemplo en san fernando liceo
    private double precio;

    private String ciudad; // ciudad / o por ejemplo cadiz

    @ManyToOne
    @JoinColumn(name="creador_id")
    private Usuario creador;

    @Column(name = "media_equipo1")
    private int mediaEquipo1 = 0;

    @Column(name = "media_equipo2")
    private int mediaEquipo2 = 0;

    @ManyToOne
    @JoinColumn(name = "mvp_id")
    private Usuario mvp;

    @Column(name = "equipo_ganador")
    private String equipoGanador = "empate"; // default empate porque es lo mas seguro arquitectonicamente hablando


    private String estado; //estados (cancelado, abierto, terminado, lleno, completado) 
 
    @OneToMany(mappedBy="partido")
    private Set<Participacion> participaciones = new HashSet<>(); 

    public Partido() {
        
    }

    public Partido(String titulo, String ubicacion, double precio, String ciudad, Usuario creador, boolean soloVerificados, LocalDateTime fecha) {
        this.titulo = titulo;
        this.ubicacion =ubicacion;
        this.precio = precio;
        this.ciudad = ciudad;
        this.creador = creador;
        this.soloVerificados = soloVerificados;
        this.fecha = fecha;
        estado = "abierto";
    }

    public void aniadirJugador(Participacion participacion) {
        participaciones.add(participacion);
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public boolean isSoloVerificados() {
        return soloVerificados;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public double getPrecio() {
        return precio;
    }

    public String getCiudad() {
        return ciudad;
    }

    public Usuario getCreador() {
        return creador;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getMediaEquipo1() {
        return mediaEquipo1;
    }

    public int getMediaEquipo2() {
        return mediaEquipo2;
    }

    public void setMediaEquipo1(int mediaEquipo1) {
        this.mediaEquipo1 = mediaEquipo1;
    }

    public void setMediaEquipo2(int mediaEquipo2) {
        this.mediaEquipo2 = mediaEquipo2;
    }

    public Usuario getMvp() {
        return mvp;
    }

    public void setMvp(Usuario mvp) {
        this.mvp = mvp;
    }

    public void setEquipoGanador(String equipoGanador) {
        this.equipoGanador = equipoGanador;
    }

    public String getEquipoGanador() {
        return equipoGanador;
    }
}