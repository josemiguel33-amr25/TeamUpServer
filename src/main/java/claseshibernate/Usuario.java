package claseshibernate;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    public Usuario() {}
}