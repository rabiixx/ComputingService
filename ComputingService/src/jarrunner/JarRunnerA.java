package jarrunner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.AccessControlException;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;

public class JarRunnerA extends JarRunner implements PrivilegedExceptionAction<Object>{
    
    final Subject subject;
    
    static private final String CLASS_NAME = JarRunner.class.getName();
    static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    
    public JarRunnerA(Subject subject, String location, String[] args) throws MalformedURLException {
        super(location, args);
        this.subject = subject;
        /*final String path = System.getProperty("user.dir") + File.separator +
                                                  "data" + File.separator +
                                               "client" + File.separator +
                                             "traza.txt" + File.separator;
        try {
            FileWriter myWriter = new FileWriter(path);
            myWriter.write("Files in Java might be tricky, but it is fun enough!");
            myWriter.close();
            System.out.println("JarRunnerA: Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }*/
        System.out.println("JarRunnerA conttructor");
    }
    
    @Override
    public Object run() {
        System.out.println("JarRunnerA.run()");
        try {
            return Subject.doAsPrivileged(subject, ( PrivilegedAction ) () -> {
                return super.run();
            }, null);
        } catch ( final AccessControlException ex ) {
            LOGGER.log( Level.WARNING, "sujeto sin permisos", ex );
            System.out.println( "Error: " + ex.getMessage() );
            return null;
        }
    }

}
