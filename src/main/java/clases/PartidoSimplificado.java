package clases;

public class PartidoSimplificado { // clase que se devuelve cuando el usuario pide los partidos

    private int idPartido;
    private String tituloPartido;
    private String ubicacion;
    private String ciudad;
    private double precio;
    private int dia;
    private int mes; //fecha asi porque json me daba problemas al transformar fecha LocalDateTime a json
    private int anio;
    private int hora;
    private int minutos;
    private String estado;
    private boolean soloVerificados;
    private String nombreUsuario;
    private int idUsuario;
    private String fotoUsuario;

    public PartidoSimplificado(int idPartido, String tituloPartido, String ubicacion, double precio, int dia, int mes, int anio, int hora, int minutos, String estado, boolean soloVerificados, String nombreUsuario, int idUsuario, String fotoUsuario, String ciudad) {
        this.idPartido = idPartido;
        this.tituloPartido = tituloPartido;
        this.ubicacion = ubicacion;
        this.ciudad = ciudad;
        this.precio = precio;
        this.dia = dia;
        this.mes = mes;
        this.anio = anio;
        this.hora = hora;
        this.estado = estado;
        this.soloVerificados = soloVerificados;
        this.nombreUsuario = nombreUsuario;
        this.idUsuario = idUsuario;
        this.fotoUsuario = fotoUsuario;
    }

    public String getTituloPartido() {
        return tituloPartido;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public double getPrecio() {
        return precio;
    }



    public String getEstado() {
        return estado;
    }

    public boolean isSoloVerificados() {
        return soloVerificados;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getFotoUsuario() {
        return fotoUsuario;
    }

    public int getIdPartido() {
        return idPartido;
    }

    public int getDia() {
        return dia;
    }

    public int getMes() {
        return mes;
    }

    public int getAnio() {
        return anio;
    }

    public int getHora() {
        return hora;
    }

    public int getMinutos() {
        return minutos;
    }

    public String getCiudad() {
        return ciudad;
    }

    

}
