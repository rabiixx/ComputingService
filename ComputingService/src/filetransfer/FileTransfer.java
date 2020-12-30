package filetransfer;
/**
 *
 * FileTransfer: utilidad para trasnferencia de ficheros; no debe modificarse.
 * 
 * @author MAZ
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterOutputStream;
//
public final class FileTransfer {
    
    static private final String CLASS_NAME = FileTransfer.class.getName();
    static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    
    static private final int BLOCK_SIZE = 16 * 1024;
    
    private final DataInputStream  is;
    private final DataOutputStream os;
    private final File file;
    private final boolean outputIsFile;

    // From stream to file
    public FileTransfer (final InputStream is, final File file) throws FileNotFoundException {
        this.is = new DataInputStream(is);
        this.os = null;
        this.file = file;
        this.outputIsFile = true;
    }
    
    // From file to stream
    public FileTransfer (final File file, final OutputStream os) throws FileNotFoundException {
        this.is = null;
        this.os = new DataOutputStream(os);
        this.file = file;
        this.outputIsFile = false;
    }
    
    public void transfer () throws IOException, SecurityException {
        System.out.println("transfer()");
        try {
            if (outputIsFile)
                transferToFile();
            else
                transferFromFile();
        } catch (SecurityException ex) {
            System.out.println(ex.getCause());
        }
        
    }
    
    private void transferFromFile () throws IOException {
        try (   final FileInputStream fis = new FileInputStream(file);
                final DeflaterInputStream dis = new DeflaterInputStream(fis);
                final DataInputStream _dis = new DataInputStream(dis) ) {
            final byte[] buffer = new byte[BLOCK_SIZE];
            for (int len = _dis.read(buffer); len > 0; len = _dis.read(buffer)) {
                os.writeInt(len);
                os.write(buffer, 0, len);
                os.flush();
            }
            os.writeInt(0);
        } catch (final IOException ex) {
            LOGGER.info(CLASS_NAME);
            LOGGER.log(Level.SEVERE, "", ex.getCause());
            throw ex;
        }
    }
    
    private void transferToFile () throws IOException, SecurityException {
        System.out.println("1. transferToFile()");
        try (   final FileOutputStream fos = new FileOutputStream(file);
                final InflaterOutputStream ios = new InflaterOutputStream(fos) ) {
            System.out.println("2. " + this.getClass());
            final byte[] buffer = new byte[BLOCK_SIZE];
            for (int len = is.readInt(); len > 0; len = is.readInt()) {
                is.read(buffer, 0, len);
                ios.write(buffer, 0, len);
                ios.flush();
            }
        } catch (final IOException ex) {
            System.out.println("IOException");
            LOGGER.info(CLASS_NAME);
            LOGGER.log(Level.SEVERE, "", ex.getCause());
            throw ex;
        } catch ( final SecurityException ex ) {
            System.out.println("SecurityException");
            LOGGER.info(CLASS_NAME);
            LOGGER.log(Level.SEVERE, "", ex.getCause());
            System.out.println("Hola: " + ex.getMessage());
            throw ex;
        }
    }
  
}