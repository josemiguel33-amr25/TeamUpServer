
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;

import claseshibernate.Usuario;

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


    private boolean comprobarUsuarioExiste(String nombre) {
        boolean usuarioExiste = false;
        Session session = sessionFactory.openSession();	
        Query<Usuario> q = session.createQuery("from Usuario where nombre = :nombre", Usuario.class);

        q.setParameter("nombre", nombre);
        List<Usuario> lista = q.list();
        if (!lista.isEmpty()) {
            usuarioExiste  = true;
        }

        return usuarioExiste;
    }

    private boolean comprobarCorreoAsociado(String correo) {
        boolean correoAsociado = false;
        Session session = sessionFactory.openSession();	
        Query<Usuario> q = session.createQuery("from Usuario where correo = :correo", Usuario.class);

        q.setParameter("correo", correo);
        List<Usuario> lista = q.list();
        if (!lista.isEmpty()) {
            correoAsociado  = true;
        }

        return correoAsociado;
    }
    

    public String registrarUsuario(String nombre, String correo, String contrasenia, String posicion1, String posicion2) {
        String resultado = "";
        Session session = sessionFactory.openSession();
        boolean usuarioExiste = comprobarUsuarioExiste(nombre);
        boolean correoExiste = comprobarCorreoAsociado(correo);
        if (!usuarioExiste && !correoExiste) {
            String contraseniaEncriptada  = encriptarContrasenia(contrasenia);
            Usuario u = new Usuario(nombre, correo, contraseniaEncriptada, posicion1, posicion2);
            
            Transaction transaction = session.beginTransaction();
			session.persist(u);

			try {
				transaction.commit();
				System.out.println("TeamUp|MensajeInterno|Usuario dado de alta.");
			} catch (IllegalStateException em) {
                System.out.println("TeamUp|Error|EM2|.");
			}
        } else if (usuarioExiste)  {
            resultado = "TeamUp|Directriz|ErrorUsuarioExiste";
        } else if (correoExiste) {
            resultado = "TeamUp|Directriz|ErrorCorreoExiste";
        }

        return resultado;
    }

    private String encriptarContrasenia(String contrasenia) {

        //Esto es como en el biginteger is probable prime, cuanta mas certeza tenia la comprobacion, pero en la fuerza de la encriptacion
        int vueltasEncriptacion = 12;
        // Esto es el "salt",
        String sal = BCrypt.gensalt(vueltasEncriptacion);

        return BCrypt.hashpw(contrasenia,sal);

    }

    private boolean comprobarContrasenia(String usuario, String contrasenia) {
        

        

        return BCrypt.checkpw(contrasenia, storedHash);
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


