package clases;

public class CosmeticoSimplificado {
    private String tituloCosmetico;
    private String tipo;
    private String rareza;
    private int cantidad;
    private boolean vendible;

    public CosmeticoSimplificado() {

    }

    public CosmeticoSimplificado(String tituloCosmetico, String tipo, String rareza, int cantidad, boolean vendible) {
        this.tituloCosmetico = tituloCosmetico;
        this.tipo = tipo;
        this.rareza = rareza;
        this.cantidad = cantidad;
        this.vendible = vendible;
    }

    public String getTituloCosmetico() {
        return tituloCosmetico;
    }

    public String getTipo() {
        return tipo;
    }

    public String getRareza() {
        return rareza;
    }

    public int getCantidad() {
        return cantidad;
    }

    public boolean isVendible() {
        return vendible;
    }

    
}
