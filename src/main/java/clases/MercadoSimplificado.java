package clases;

public class MercadoSimplificado {
    private String nombreArticulo;
    private String nombreVendedor;
    private int idVendedor;
    private int precio;
    private int idArticulo; // esto es el id de la entrada del mercado NO DEL COSMETICO


    public MercadoSimplificado() {

    } 

    public MercadoSimplificado(String nombreArticulo, String nombreVendedor, int idVendedor, int precio, int idArticulo) {
        this.nombreArticulo = nombreArticulo;
        this.nombreVendedor = nombreVendedor;
        this.idVendedor = idVendedor;
        this.precio = precio;
        this.idArticulo = idArticulo;
    }

    public String getNombreArticulo() {
        return nombreArticulo;
    }

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public int getIdVendedor() {
        return idVendedor;
    }

    public int getPrecio() {
        return precio;
    }

    public int getIdArticulo() {
        return idArticulo;
    }
}
