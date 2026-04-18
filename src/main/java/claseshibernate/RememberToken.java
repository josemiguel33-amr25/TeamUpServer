package claseshibernate;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="remember_token")
public class RememberToken {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String selector;
    private String tokenHash;

    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaCreacion;

    private String dispositivo;
    private String ip;

    @ManyToOne
    @JoinColumn(name="usuario_id")
    private Usuario usuario;

    public RememberToken() {}
}