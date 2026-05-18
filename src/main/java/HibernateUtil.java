import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import claseshibernate.Carta;
import claseshibernate.ContenidoSobre;
import claseshibernate.Cosmetico;
import claseshibernate.Intercambio;
import claseshibernate.IntercambioItem;
import claseshibernate.Inventario;
import claseshibernate.InventarioCosmetico;
import claseshibernate.InventarioSobre;
import claseshibernate.Mercado;
import claseshibernate.Participacion;
import claseshibernate.Partido;
import claseshibernate.RememberToken;
import claseshibernate.Reporte;
import claseshibernate.Sobre;
import claseshibernate.Usuario;
import claseshibernate.Votacion;
public class HibernateUtil {
    private static SessionFactory sessionFactory ;
    static {
       Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Usuario.class);
        configuration.addAnnotatedClass(Votacion.class);
        configuration.addAnnotatedClass(Mercado.class);
        configuration.addAnnotatedClass(InventarioSobre.class);
        configuration.addAnnotatedClass(Carta.class);
        configuration.addAnnotatedClass(Partido.class);
        configuration.addAnnotatedClass(Participacion.class);
        configuration.addAnnotatedClass(Sobre.class);
        configuration.addAnnotatedClass(ContenidoSobre.class);
        configuration.addAnnotatedClass(Cosmetico.class);
        configuration.addAnnotatedClass(Inventario.class);
        configuration.addAnnotatedClass(InventarioCosmetico.class);
        configuration.addAnnotatedClass(Intercambio.class);
        configuration.addAnnotatedClass(IntercambioItem.class);
        configuration.addAnnotatedClass(Reporte.class);
        configuration.addAnnotatedClass(RememberToken.class);
       StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                      .applySettings(configuration.getProperties());
       sessionFactory = configuration.buildSessionFactory(builder.build());
    }
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
} 