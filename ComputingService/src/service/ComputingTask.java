package service;
/**
 * ComputingTask: encapsula la recepción, preparación y ejecución del fichero jar.
 * En la versión protegida:
 *  - Debe recibir el sujeto cliente autenticado, extraer el principal Kerberos
 *    del mismo y emplear ese principal para consultar la categoría del cliente
 *    en la base de datos CSDB.
 *  - Debe instanciar un objeto jarrunner que corresponda a la categoría del cliente. 
 * 
 * @author MAZ
 *
 */
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
//
import filetransfer.FileTransfer;
import jarrunner.JarRunnerA;
import jarrunner.JarRunnerB;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Set;
import javax.security.auth.kerberos.KerberosPrincipal;
//
final class ComputingTask implements Runnable {

    static private final String DB_USER = "ComputingService";
    static private final String DB_PASSWORD = "PS2021";  

    static private final String CLASS_NAME = ComputingTask.class.getName();
    static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private final Socket socket;
    private String cathegory;
    private final Subject admin;

    ComputingTask ( final Subject admin, final Socket socket ) throws IOException {
        this.socket = socket;
        this.admin = admin;
    }

    @Override
    public void run () {
        
        try ( final InputStream  is = socket.getInputStream();
              final OutputStream os = socket.getOutputStream() ) {
            
            final String path = System.getProperty("user.dir") + File.separator +
                                                  "data" + File.separator +
                                               "service" + File.separator +
                                             "workspace" + File.separator;
                  
            // Recibimos el sujeto y lo deserializamos
            final ObjectInputStream ois = new ObjectInputStream( is );
            Subject client = (Subject) ois.readObject();
        
            Set<KerberosPrincipal> principals = client.getPrincipals(KerberosPrincipal.class);
        
            if ( principals.isEmpty() ) {
                System.out.println("[+] Service: An error has ocurred when getting Kerberos Principal");
            } else {
                KerberosPrincipal principal = principals.iterator().next();
                final String principalName = principal.getName();
                System.out.println("[+] Principal " + principalName + " recibido.");
                
                CathegoryQuery cathegoryQuery = new CathegoryQuery(principal);
                cathegory = cathegoryQuery.query(DB_USER, DB_PASSWORD);
            }
            
            // Se recibe y deposita el fichero jar
            final RandomStringGenerator rsg = new RandomStringGenerator();
            final String jarFileName = rsg.getString(12);
            final File jarFile = new File(path + jarFileName);
            final FileTransfer ft0 = new FileTransfer(is, jarFile);
            ft0.transfer();
            
            // Se recibe y deposita el fichero con argumentos
            final String argsFileName = rsg.getString(12);
            final File argsFile = new File(path + argsFileName);
            final FileTransfer ft1 = new FileTransfer(is, argsFile);
            ft1.transfer();

            // Se cargan los argumentos
            final String[] args = getArguments(argsFile);

            System.out.println("[+] Preparando ejecutor");
            
            // Se prepara el ejecutor
            final String url = "file://localhost/" + path + jarFileName;
            
            if ( cathegory.equals("A") ) {
                
                JarRunnerA jarRunner = new JarRunnerA( client, url, args );
                
                final PrintStream out = System.out;
                final PrintStream err = System.err;

                final PrintStream ps = new PrintStream(os);
                
                try {
                    System.setOut(ps);
                    System.setErr(ps);
                } catch (final SecurityException ex) {
                    System.out.println("Security exception {0}" + ex.getMessage());
                }
                
                jarRunner.run();
                
                System.setOut(out);
                System.setErr(err);
                
                try {
                    argsFile.delete();
                    jarFile.delete();    
                } catch (final SecurityException ex) {
                    System.out.println(ex.getMessage());
                }
                System.out.println("Task Completed Successfully");
            } else {
                
                JarRunnerB jarRunner = new JarRunnerB( client, url, args );
                
                final PrintStream out = System.out;
                final PrintStream err = System.err;

                final PrintStream ps = new PrintStream(os);
                System.setOut(ps);
                System.setErr(ps);
                
                jarRunner.run();
                
                System.setOut(out);
                System.setErr(err);
                
                try {
                    argsFile.delete();
                    jarFile.delete();    
                } catch (final SecurityException ex) {
                    System.out.println(ex.getMessage());
                }
                System.out.println("Task Completed Successfully");
            }
            

        } catch (final IOException ex) {
            LOGGER.log(Level.WARNING, "", ex.getCause());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ComputingTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ComputingTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ComputingTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Obtiene los argumentos contenidos en el fichero recibido.
    private String[] getArguments (final File argsFile) throws FileNotFoundException {
        
        try (final Scanner scanner = new Scanner(argsFile)) {
            
            final ArrayList<String> args = new ArrayList<>();
            while (scanner.hasNextLine()) {
                args.add(scanner.nextLine());
            }
            
            final String[] _args = new String[args.size()];
            int j = 0;
            
            for (final String arg: args) {
                _args[j] = arg;
                ++j;
            }
            
            return _args;
    
        } catch (final FileNotFoundException ex) {
            LOGGER.info("file with arguments not found");
            LOGGER.log(Level.WARNING, "file with arguments not found", ex.getCause());
            throw ex;
        } catch ( SecurityException ex ) {
            System.out.println("sec ex");
            System.out.println(ex.getMessage());
            System.out.println(ex.getCause());
            throw ex;
        }
    }

}