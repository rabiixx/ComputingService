package service;
/**
 *
 * CathegoryQuery: consulta a la BBDD de la categoría del principal.
 * Hay que proteger el código para que sea ejecutado con los permisos asignados
 * al adminstrador de la aplicación.
 * 
 * @author MAZ
 */
import java.io.File;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.kerberos.KerberosPrincipal;
//
final class CathegoryQuery {

    static private final String CLASS_NAME = CathegoryQuery.class.getName();
    static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
  
    private final byte[] idCode;
  
    CathegoryQuery (final KerberosPrincipal principal) throws NoSuchAlgorithmException {
        try {
            final String principalName = principal.getName();
            final MessageDigest md = MessageDigest.getInstance("MD5");
            this.idCode = md.digest(principalName.getBytes());
        } catch (final NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, "MD5 digest algorithm not provided", ex.getCause());
            throw ex;
        }
    }

    String query (final String dbUser, final String passwd)
            throws SQLTimeoutException, SQLException {
    
        final String dbPath = System.getProperty("user.dir") + File.separator +
                                                  "data" + File.separator + 
                                               "service" + File.separator +
                                              "database" + File.separator +
                                                  "CSDB";
        
        final String DB_URL = "jdbc:h2:file:" + dbPath;    
          
        try (final Connection connection =
            DriverManager.getConnection(DB_URL, dbUser, passwd)) {
    
            final String selectStatement
                    = "SELECT Cathegory FROM engineers WHERE IdCode = ?";       

            try (final PreparedStatement statement =
                    connection.prepareStatement(selectStatement)) {
                
                statement.setBytes(1, idCode);
                
                final ResultSet rs = statement.executeQuery();
                String result = null;
                while (rs.next()) {
                    result = rs.getString("Cathegory").trim();
                    break;
                }
                
                if (result != null)
                    return result;
                else
                    throw new IllegalArgumentException("principal not found");
            
            } catch (final SQLException ex) {
                LOGGER.info("error al realizar consulta");
                LOGGER.severe(ex.getMessage());
                throw ex;
            }
            
        } catch (final SQLTimeoutException ex) {
            LOGGER.info("timeout al establecer la conexion");
            LOGGER.severe(ex.getMessage());
            throw ex;
        } catch (final SQLException ex) {
            LOGGER.info("error al establecer la conexion");
            LOGGER.severe(ex.getMessage());
            throw ex;
        }
    }
}