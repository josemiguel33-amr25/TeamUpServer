package utilidades;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

import ClasesServer.BaseDatosManager;
import claseshibernate.RememberToken;
import claseshibernate.Usuario;

public class UtilidadesCuentas {
    private static UtilidadesCuentas utilidades;

    private UtilidadesCuentas() {
        System.out.println("TeamUp|MensajeIntereno|Modulos utilidades cuentas creado");
    }

    public static UtilidadesCuentas getUtilidades() {
        if (utilidades == null) 
            utilidades = new UtilidadesCuentas();

        return utilidades;
    }    

    public boolean validarCorreo(String correo) { 
        // en documentacion especificados estos dominios
        String regex = "^[A-Za-z0-9+_.-]+@(gmail\\.com|outlook\\.com|hotmail\\.com|yahoo\\.com|icloud\\.com)$";
        return Pattern.matches(regex, correo);
    }

    public boolean validarContrasenia(String contrasenia) { 
        // comprobacion Longitud
        boolean contraseniaValida = true;
        if (contrasenia.length() < 8 || contrasenia.length() > 33) 
            contraseniaValida = false;

        // Al menos 2 números
        int contadorNumeros = 0;
        for (char c : contrasenia.toCharArray()) {
            if (Character.isDigit(c)) {
                contadorNumeros++;
            }
        }

        if (contadorNumeros < 2) 
            contraseniaValida = false;

        // Al menos 1 carácter especial
        Pattern especial = Pattern.compile("[^a-zA-Z0-9]");
        if (!especial.matcher(contrasenia).find()) 
            contraseniaValida = false;

        return contraseniaValida;
    }

    public boolean validarNombreUsuario(String nombre) { // sistema autenticaci0n
        return nombre.length() <= 20 && nombre.length() >= 3 && !nombre.trim().isEmpty();
    }



    public String encriptarContrasenia(String contrasenia) { // sistema autenticacion

        //Esto es como en el biginteger is probable prime, cuanta mas certeza tenia la comprobacion, pero en la fuerza de la encriptacion
        int vueltasEncriptacion = 12;
        // Esto es el "salt",
        String sal = BCrypt.gensalt(vueltasEncriptacion);

        return BCrypt.hashpw(contrasenia,sal);

    }

    public String hashearToken(String token) { // sistema autenticacion
        String em = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            System.out.println("TeamUp|Error|EM4|");
        }
        return  em;
    }

    public String cadenaAleatoria() { 
        String cancion = "";
        Random generador = new Random();
        int numeroAleatorio = generador.nextInt(15);

        switch (numeroAleatorio) {
            case 0 :
                cancion = "graxxqjsm";
                break;
            case 1:
                cancion = "joe";
                break;
            case 2:
                cancion = "vidarockstar";
                break;
            case 3:
                cancion = "corazon";
                break;
            case 4:
                cancion = "pasiempre";
                break;
            case 5:
                cancion = "cuandomevaya";
                break;
            case 6:
                cancion = "medialuna";
                break;
            case 7:
                cancion = "lologre";
                break;
            case 8:
                cancion = "tokyo";
                break;
            case 9:
                cancion = "flakito";
                break;
            case 10:
                cancion = "comoantes";
                break;
            case 11:
                cancion = "maniac";
                break;
            case 12:
                cancion = "esacruz";
                break;
            case 13:
                cancion = "mojabighost";
                break;
            case 14:
                cancion = "fantasmaavc";
                break;
        }

        return cancion;
    }

    public String generarNombreFotoPerfil(String nombreUsuario) {
        return nombreUsuario.trim().toLowerCase().replace(" ", "") + ".png";
    }

    public boolean comprobarUsuario(String correo, String contrasenia, BaseDatosManager bdm) { // sistema autenticacion
        boolean entrar = false;
        Usuario u = bdm.obtenerUsuarioPorCorreo(correo);
        if (BCrypt.checkpw(contrasenia, u.getContrasena())) 
            entrar = true;

        return entrar; // cambiar que ahora da igual la contraseña que pongas, cambiar a entrar dejar true para probar
    }

    public boolean comprobarRememberToken(String selector, String token, BaseDatosManager bdm) {
        boolean rememberValido = false;
        String hash = hashearToken(token);
        RememberToken rm = bdm.obtenerRememberToken(selector);

        if (rm.getTokenHash().equals(hash)) {
            rememberValido = true;
        }

        return rememberValido;
    }
}
