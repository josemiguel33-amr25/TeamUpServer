
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class BaseDatosManager {
    private SessionFactory sessionFactory;


    public BaseDatosManager() {
        //ERROR tonto que pasaba aqui, en el session factory creaba un objeto en vez de usar el atributo, madre mia
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.SEVERE);
        sessionFactory = HibernateUtil.getSessionFactory();

    }


    public boolean comprobarConexion() {
        boolean conexionBandera;
        Session session = sessionFactory.openSession();
        
        try {
            session.createQuery("select 1").uniqueResult();
            conexionBandera = true;
        } catch (Exception em) {
            System.out.println("TeamUp|Error|EM2");
            conexionBandera = false;
        }

        return conexionBandera;
    }

}

/*
Session session = sessionFactory.openSession(); CREO UNA sesion cada consulta y cierro 
Transaction tx = session.beginTransaction();

try {
    // operaciones
    tx.commit();
} catch (Exception e) {
    tx.rollback();
} finally {
    session.close();
}

// CONSULTA PARAMETRIZADA que no la usste en el examen, que menos ahora en el proyecto ;:()
Query<Hospital> q = session.createQuery(
    "from Hospital where nombre = :nombre", Hospital.class);

q.setParameter("nombre", nombreHospital);
 */


