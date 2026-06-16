package clases;

public class CosmeticoSimplificado {
    private String tituloCosmetico;
    private String tipo;
    private String rareza;
    private int cantidad;
    private boolean vendible;
    private int idCosmetico;

    public CosmeticoSimplificado() {

    }

    public CosmeticoSimplificado(String tituloCosmetico, String tipo, String rareza, int cantidad, boolean vendible, int idCosmetico) {
        this.tituloCosmetico = tituloCosmetico;
        this.tipo = tipo;
        this.rareza = rareza;
        this.cantidad = cantidad;
        this.vendible = vendible;
        this.idCosmetico = idCosmetico;
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

    public int getIdCosmetico() {
        return idCosmetico;
    }



    
}
