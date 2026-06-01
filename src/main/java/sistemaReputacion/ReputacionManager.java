package sistemaReputacion;

public class ReputacionManager {
    private static ReputacionManager reputacionManager;

    private ReputacionManager() {
        System.out.println("TeamUp|MensajeIntereno|Modulos reputacion creado");
    }

    public static ReputacionManager getReputacionManager() {
        if (reputacionManager == null) 
            reputacionManager = new ReputacionManager();

        return reputacionManager;
    }
}