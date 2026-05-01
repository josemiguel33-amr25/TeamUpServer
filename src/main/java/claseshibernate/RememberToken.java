package claseshibernate;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
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

    @Column(name="token_hash")
    private String tokenHash;

    @Column(name="fecha_expiracion")
    private LocalDateTime fechaExpiracion;
    
    @Column(name="fecha_creacion")
    private LocalDateTime fechaCreacion;

    private String dispositivo;
    private String ip;

    @ManyToOne
    @JoinColumn(name="usuario_id")
    private Usuario usuario;

    public RememberToken() {
        
    }

    public RememberToken(String selector, String tokenHash, String dispositivo, String ip) {
        this.selector = selector;
        this.tokenHash = tokenHash;
        this.dispositivo = dispositivo;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaExpiracion = fechaCreacion.plusMonths(1);
        this.ip = ip;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public String getSelector() {
        return selector;
    }

    public String getTokenHash() {
        return tokenHash;
    }
}