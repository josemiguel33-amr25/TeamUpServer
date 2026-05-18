package clases;

public class Rango {
    private int puntosMin;
    private int puntosMax;
    private String nombreSobre; // recompensa final de temporada, he puesto el string del nombre porque solo queremos mostrar la imagen asi que mucho mejor tener esto que la id
    private String nombreRango;

    public Rango(int puntosMin, int puntosMax, String nombreSobre, String nombreRango) {
        this.puntosMin = puntosMin;
        this.puntosMax = puntosMax;
        this.nombreSobre = nombreSobre;
        this.nombreRango = nombreRango;
    }

    public int getPuntosMin() {
        return puntosMin;
    }

    public int getPuntosMax() {
        return puntosMax;
    }

    public String getNombreSobre() {
        return nombreSobre;
    }

    public String getNombreRango() {
        return nombreRango;
    }

    

}
