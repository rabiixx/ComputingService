package jarrunner;

import java.net.MalformedURLException;
import java.security.AccessControlException;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;

public class JarRunnerA {
    
    final Subject subject;
    
    static private final String CLASS_NAME = JarRunner.class.getName();
    static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    
    public JarRunnerA(Subject subject, String location, String[] args) throws MalformedURLException {
        //super(location, args);
        this.subject = subject;
        System.out.println("JarRunnerA");
    }
    
    public void runA() {
        /*try {
            return Subject.doAsPrivileged(subject, ( PrivilegedAction ) () -> {
                return super.run();
            }, null);
        } catch ( final AccessControlException ex ) {
            LOGGER.log( Level.WARNING, "sujeto sin permisos", ex );
            System.out.println( "Error: " + ex.getMessage() );
            return null;
        }*/
    }

}
