package jarrunner;

import java.net.MalformedURLException;
import java.security.AccessControlException;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;

public class JarRunnerA extends JarRunner {
    
    final Subject subject;
    
    static private final String CLASS_NAME = JarRunner.class.getName();
    static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    
    public JarRunnerA( final Subject subject, String location, String[] args) throws MalformedURLException {
        super(location, args);
        this.subject = subject;
    }
    
    @Override
    public Object run() {
        try {
            return Subject.doAsPrivileged(subject, ( PrivilegedAction<Object> ) () -> {
                return super.run();
            }, null);
        } catch ( final AccessControlException ex ) {
            LOGGER.log( Level.WARNING, "sujeto sin permisos", ex );
            System.out.println( "Error: " + ex.getMessage() );
            return null;
        }
    }

}
