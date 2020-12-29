package service;
/**
 * ComputingService:
 * - Implementa el bucle de escucha de peticiones de clientes.
 * - Incluye mecanismos que esperan a que todas las transacciones en curso
 *   de clientes hayan terminado antes de proceder a detener el servicio.
 * - En la versión protegida, debe ejecutarse con los permisos asignados
 *   al administrado de la aplicación
 *
 * @author MAZ
 *
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
//
public final class ComputingService implements Runnable {

    static private final String CLASS_NAME = ComputingService.class.getName();
    static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // Interrupción para preguntar si han ordenado detener la aplicación.
    static private final int INTERRUPTION_TIME1 =  500; // Milliseconds
  
    // Interrupción para preguntar si han terminado las tareas pendientes.
    static private final int INTERRUPTION_TIME2 = 1000; // Miliseconds

    // Servicio ejecutor para la propia infraestructura (método run())
    static private final ExecutorService CSE = Executors.newSingleThreadExecutor();
  
    // Singleton: sólo va a existir una instancia de esta clase.
    static private ComputingService SINGLETON;

    // Sujeto autenticado (administrador)
    private final Subject admin;
  
    // Servicio ejecutor con hebras de ejecución para atender tareas entrantes.
    private final ExecutorService executorForTasks;
    
    // Puerto de escucha de solicitudes de clientes
    private final int port;

    // Constructor privado
    private ComputingService (final Subject admin, final int port, final int numThreads) {
        
        if (numThreads <= 0)
            throw new IllegalArgumentException("");
        
        this.admin = admin;
        this.port = port;
        
        // numThreads limita el número máximo de clientes atendidos simultáneamente.
        this.executorForTasks = Executors.newFixedThreadPool( numThreads );
  
        }

    // Arranque del servicio; permitido solo al administrador
    static public void start ( final Subject admin, final int port, final int numThreads ) {
        
        AccessController.doPrivileged( new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                try {
                    if ( SINGLETON == null ) {
                        SINGLETON = new ComputingService( admin, port, numThreads );
                        System.out.println("debuggation");
                        CSE.submit( SINGLETON );
                    }
                } catch (Exception e) {
                    System.out.println(e.getCause());
                }
                
                return null;
            }
        });
                
        //if ( SINGLETON == null ) {
        //    SINGLETON = new ComputingService( admin, port, numThreads );
        //    CSE.submit( SINGLETON );
        //}
    }

     // Parada del servicio: permitido solo al administrador
    static public void stop (final Subject admin) {
        
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                try {
                    SINGLETON._stop(admin);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                return null;
            }
        });
        
        //SINGLETON._stop(admin)
    }

    @Override
    public void run () {
        
        try ( final ServerSocket serverSocket = new ServerSocket( port ) ) {
        
            System.out.println("ComputingService en operación");
            LOGGER.info("ComputingService en operación");
            
            System.out.println("ServerSocket: " + serverSocket.toString() );
            
            // Servicio ejecutor para implementar timeout en transacciones entrantes.
            final ExecutorService auxiliarExecutor = Executors.newSingleThreadExecutor();
            
            do { // Bucle de escucha

                Socket socket = null;
                
                // Se reutiliza la tarea hasta que se complete
                Future<Socket> future = auxiliarExecutor.submit( serverSocket::accept );
                
                do {
                    try {
                        // La espera se interrumpe cada cierta cantidad de milisegundos
                        // para conocer si el admnistrador ha detenido el servicio.
                        socket = future.get(INTERRUPTION_TIME1, MILLISECONDS);
                    } catch (final ExecutionException ex) {
                        future = auxiliarExecutor.submit(serverSocket::accept);
                    } catch (final InterruptedException | TimeoutException ex) {
                    }
                
                } while ((!future.isDone()) && (!executorForTasks.isShutdown()));
                
                if ( future.isDone() ) {
                    //LOGGER.info("Tarea entrante");
                    System.out.println("Tarea entrante");
                    final ComputingTask task = new ComputingTask(admin, socket);
                    executorForTasks.submit(task);
                }
            } while ( !executorForTasks.isShutdown());
            
            // Se detiene el ejecutor empleado en el socket de escucha.
            auxiliarExecutor.shutdown();

        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Server socket opening error {0}", ex);
            LOGGER.log(Level.SEVERE, "", ex.getCause());
            System.out.println("Server socket openning error");
            ex.printStackTrace();
        }
    }

    private void _stop (final Subject subject) {
        
        try {
            Subject.doAsPrivileged(subject, (PrivilegedAction<Void>) () -> {
                executorForTasks.shutdown();
                return null;
            }, null);
        } catch ( final AccessControlException ex ) {
            LOGGER.log( Level.WARNING, "sujeto sin permisos", ex );
            System.out.println( "Error: " + ex.getMessage() );
            return;
        }
      
        // Se detiene el bucle de escucha; no se admiten nuevas tareas.
        //executorForTasks.shutdown();

        // Bucle que espera a terminar todas las tareas en curso.
        do {
            try {
                // Consulta cada dos segundos si todavia quedan tareas de clientes activas.
                final boolean x = executorForTasks.awaitTermination(INTERRUPTION_TIME2, MILLISECONDS);
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "{0}", ex);
            }
        } while (!executorForTasks.isTerminated());
        
        try {
            Subject.doAsPrivileged(subject, new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    CSE.shutdown();
                    return null;
                }
            }, null);
        } catch ( final AccessControlException ex ) {
            LOGGER.log( Level.WARNING, "sujeto sin permisos", ex );
            System.out.println( "Error: " + ex.getMessage() );
            return;
        }
        
        // Se detiene el ejecutor para el método run().
        //CSE.shutdown();
        
        LOGGER.info("ComputingService detenido");
    }

}