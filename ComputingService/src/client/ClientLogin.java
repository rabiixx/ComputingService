package client;
/**
 *
 * ClientLogin: clase de arranque del cliente.
 * En la versión protegida, debe realizar la autenticación (Kerberos y X500) del cliente.
 * 
 * @author MAZ
 */
import com.sun.security.auth.callback.TextCallbackHandler;
import java.security.AccessControlException;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
//
public final class ClientLogin {
    
    private static final String CLASS_NAME = ClientLogin.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private static final Scanner SCANNER = new Scanner(System.in);
  
    static public void main (final String[] args) {
        
        final LoginContext loginContext;
        
        try {
            loginContext = new LoginContext("CLIENT", new TextCallbackHandler());
        } catch (final LoginException ex) {
            System.out.println("No configuration entry to create specified LoginContext");
            return;
        } catch (final SecurityException ex) {
            System.out.println("No permission to create specified LoginContext (" + ex.getMessage() + ")");
            return;
        }

        try {

            loginContext.login();
            
            final Subject subject = loginContext.getSubject();

            menu( subject );

            try {
                loginContext.logout();
            } catch (final LoginException ex) {
                LOGGER.log(Level.SEVERE, "Fallo al eliminar el contexto de login", ex.getCause());
                System.out.println("Fallo al eliminar el contexto de login");
            }
        
        } catch (final LoginException ex) {
            LOGGER.log(Level.WARNING, "Autenticación fallida en arranque de la aplicación", ex.getCause());
            System.out.println("Autenticación fallida en arranque de la aplicación");
        } catch (final AccessControlException ex) {
            LOGGER.log(Level.WARNING, "Problema de permisos en arranque de aplicación", ex.getCause());
            System.out.println("Problema de permisos en arranque de aplicación"); 
            System.out.println(ex);
        }
        
    }
  
    static void menu (final Subject subject) {

        int opcion;
        do {

            System.out.println("[+] Opciones:");
            System.out.println("\t1 - Computar");
            System.out.println("\t0 - Salir");
            System.out.print("[+] Introduce opcion: ");

            try {
                opcion = SCANNER.nextInt();
                SCANNER.nextLine();

                switch (opcion) {
                    case 1:
                        computar( subject );
                        break;
                    default:
                }

            } catch (final InputMismatchException ex) {
                SCANNER.nextLine();
                opcion = Integer.MAX_VALUE;
            }

        } while (opcion != 0);
    }

    static private void computar ( final Subject client ) {

        System.out.print("Introduzca nombre de fichero jar: ");
        final String jarFileName = SCANNER.next().trim();
        SCANNER.nextLine();
        
        System.out.print("Introduzca nombre de fichero con argumentos: ");
        final String argsFileName = SCANNER.next().trim();
        SCANNER.nextLine();
        
        System.out.print("Introduzca nombre de fichero para depositar resultados: ");
        final String input = SCANNER.next().trim();
        SCANNER.nextLine();
        
        final String resultsFileName = (!input.isEmpty()) ? input : "empty";
        final ClientComputingTask task =
            new ClientComputingTask(client, jarFileName, argsFileName, resultsFileName);
        
        task.compute();
        
    }
    
}