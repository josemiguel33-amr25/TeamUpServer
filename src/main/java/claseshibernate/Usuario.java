package claseshibernate;

import java.time.LocalDateTime;
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
@Table(name="usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String nombre;
    private String correo;
    private String contrasena;
    private boolean verificado;
    private int reputacion;
    private String posicion1;
    private String posicion2;
    private int puntos;
    private int rango;

    @Column(name="fecha_creacion")
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy="usuario")
    private Set<Carta> cartas;

    @OneToMany(mappedBy="usuario")
    private Set<Participacion> participaciones;

    @OneToMany(mappedBy="usuario")
    private Set<RememberToken> tokens;

    @Column(name="foto_perfil")
    private String fotoPerfil;


    @ManyToOne
    @JoinColumn(name="titulo_id")
    private Cosmetico titulo;

    @ManyToOne
    @JoinColumn(name="tarjeta_visita_id")
    private Cosmetico tarjetaVisita;


    private int goles;
    private int asistencias;

    @Column(name="partidos_jugados")
    private int partidosJugados;

    private int mvps;

    public Usuario() {

    }

    public Usuario(String nombre, String correo, String contrasenia, String posicion1, String posicion2) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasenia;
        this.posicion1 = posicion1;
        this.posicion2 = posicion2;
        this.fechaCreacion = LocalDateTime.now();
        this.reputacion = 0; // para el momento de programar la funcion de que los usuarios al terminar un partido den puntos a los otros usuarios dependiendo de la reputacion del usuario damos mas puntos como no hay limitacion de reputacion ya que esto es para la linea de progresion, podemos hacer de escala esto lo tendras que mirar cuando veamos cuantos puntos se dan por reputacion
        this.verificado = false; //para simplificar el sistema de verificacio por ahora, vamos a hacer que la cuenta tenga 14 dias de antiguiedad y que haya participado minimo en un partido
        this.puntos = 0;
        this.rango = 1;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public int getId() {
        return id;
    }

    public String getPosicion1() {
        return posicion1;
    }

    public String getPosicion2() {
        return posicion2;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    public String getCorreo() {
        return correo;
    }

    public int getReputacion() {
        return reputacion;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getRango() {
        return rango;
    }

    public Cosmetico getTitulo() {
        return titulo;
    }

    public Cosmetico getTarjetaVisita() {
        return tarjetaVisita;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public int getGoles() {
        return goles;
    }

    public int getAsistencias() {
        return asistencias;
    }

    public int getPartidosJugados() {
        return partidosJugados;
    }

    public int getMvps() {
        return mvps;
    }
}