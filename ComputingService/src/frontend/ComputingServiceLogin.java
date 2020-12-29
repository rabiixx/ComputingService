package frontend;
/*
 *
 * ComputingServiceLogin: clase de arranque del servicio.
 * En la versión protegida, debe incluir la autenticación (X500)
 * del administrador de la aplicación.
 *
 * @author MAZ
 *
 */
import com.sun.security.auth.callback.TextCallbackHandler;
import java.security.AccessControlException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
//
import service.ComputingService;
//
public final class ComputingServiceLogin {
  
    static private final Scanner SCANNER = new Scanner(System.in); 
    static private final String CLASS_NAME = ComputingServiceLogin.class.getName();
    static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public static void main ( final String[] args ) {
    
        final LoginContext loginContext;
        
        try {
            loginContext = new LoginContext("SERVICE", new TextCallbackHandler());
        } catch (final LoginException ex) {
            System.out.println("No configuration entry to create specified LoginContext");
            return;
        } catch (final SecurityException ex) {
            System.out.println("No permission to create specified LoginContext (" + ex.getMessage() + ")");
            return;
        }

        try {

            loginContext.login();

            final Subject admin = loginContext.getSubject();

            ComputingService.start( admin, 2050, 10 );
            
            menu( admin );
            
            ComputingService.stop( admin );
                
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

    static private void menu ( final Subject subject ) {

        int opcion;

        do {
            System.out.println("Opciones:");
            System.out.println("0 - Salir");
            System.out.print("Introduce opcion: ");
            
            try {
                opcion = SCANNER.nextInt();
                SCANNER.nextLine();
            } catch (final InputMismatchException ex) {
                SCANNER.nextLine();
                opcion = Integer.MAX_VALUE;
            }
        } while ( opcion != 0 );
    }

}