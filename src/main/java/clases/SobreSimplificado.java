package clases;

public class SobreSimplificado {
    private String nombre;
    private int precio;
    private int idSobre;
    private int cantidad;

    public SobreSimplificado() {

    }

    public SobreSimplificado(String nombre, int precio, int idSobre) {
        this.nombre = nombre;
        this.precio = precio;
        this.idSobre = idSobre;
        cantidad = 1; // default no hace nada para misSobres en la tienda
    }

    public String getNombre() {
        return nombre;
    }

    public int getPrecio() {
        return precio;
    }

    public int getIdSobre() {
        return idSobre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
