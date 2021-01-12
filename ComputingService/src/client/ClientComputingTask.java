package client;
/**
 * ClientComputingTask: esta clase hace de cliente del servicio de computación.
 *  - Transfiere el fichero jar
 *  - Transfiere el fichero con los argumentos
 *  - En la versión protegida, debe ademas enviar el sujeto cliente autenticado
 *    al servicio de computación; dado que la clase Subject cumple con la interfaz
 *    Serializable, se puede utilizar un ObjectOutputStream para transferir el sujeto
 *    autenticado.
 * 
 * @author MAZ
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
//
import filetransfer.FileTransfer;
import java.io.ObjectOutputStream;
import javax.security.auth.Subject;
//
final class ClientComputingTask {
  
    private static final String CLASS_NAME = ClientComputingTask.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);  
  
    private final Subject client;
    private final String jarFileName;
    private final String argsFileName;
    private final String resultsFileName;
  
    ClientComputingTask (final Subject client,
                       final String jarFileName,
                       final String argsFileName,
                       final String resultsFileName) {
        this.client = client;
        this.jarFileName = jarFileName;
        this.argsFileName = argsFileName;
        this.resultsFileName = resultsFileName;
    }
    
    void compute () {
    
        final String pathFile = System.getProperty("user.dir") + File.separator +
                                                    "data" + File.separator +
                                                  "client" + File.separator;
        
        final File resultsFile = new File(pathFile + resultsFileName);
        final File jarFile = new File(pathFile + jarFileName);
        final File argsFile = new File(pathFile + argsFileName);
    
        try ( final Socket socket = new Socket("127.0.0.1", 2050) ) {
            
            try ( final InputStream  is = socket.getInputStream();
                  final OutputStream os = socket.getOutputStream() ) {
                
                // Serializacion y transmision del sujeto
                final ObjectOutputStream oos = new ObjectOutputStream( os );
                oos.writeObject(client);
                
                // Se transfiere fichero jar a ejecutar
                final FileTransfer ftout0 = new FileTransfer(jarFile, os);
                ftout0.transfer();
                
                // Se transfiere fichero con argumentos
                final FileTransfer ftout1 = new FileTransfer(argsFile, os);
                ftout1.transfer();

                // Se escribe el resultado en el fichero de resultados
                try ( final FileOutputStream fos = new FileOutputStream( resultsFile ) ) {
                    final byte[] buffer = new byte[1024];
                    for (int len = is.read(buffer); len > 0; len = is.read(buffer)) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        } catch (final IOException ex) {
            LOGGER.info("problema en transferencia de ficheros");
            LOGGER.log(Level.SEVERE, "", ex.getCause());
        }
    }
    
}