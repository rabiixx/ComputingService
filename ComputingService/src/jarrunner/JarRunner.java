package jarrunner;
/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the className of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * JarRunner: runs a jar application from an url.
 * 
 * Modified by MAZ
 *
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Runs a jar application from an url.
 */
public class JarRunner {
  
  static private final String CLASS_NAME = JarRunner.class.getName();
  static private final Logger LOGGER = Logger.getLogger(CLASS_NAME);
  
  static private final String NO_MAIN_CLASS =
          "specified jar file does not contain a 'Main-Class' manifest attribute";
  static private final String IO_ERROR = "I/O error while loading JAR file";
  static private final String CLASS_NOT_FOUND = "Class not found";
  static private final String NO_MAIN_METHOD = "Class does not define a method main()";
  static private final String CAN_NOT_BE_LOADED = "Class cannot be loaded";
  static private final String EXCEPTION_THROWN = "method exception thrown";
  
  private URL url = null; // url of the jar file
  private final String[] args; // arguments to be passed to the application's main method.
  
  public JarRunner (final String location,
                    final String[] args) throws MalformedURLException {
      
        final String path = System.getProperty("user.dir") + File.separator +
                                                  "data" + File.separator +
                                               "client" + File.separator +
                                             "traza.txt" + File.separator;
        try {
            FileWriter myWriter = new FileWriter(path);
            myWriter.write("Files in Java might be tricky, but it is fun enough!");
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    try {
      url = new URL(location);
    } catch (final MalformedURLException ex) {
      LOGGER.log(Level.WARNING, "JarRunner: invalid URL: {0}", location);
      throw ex;
    }
    
    // Get arguments for the application
    this.args = new String[args.length];
    System.arraycopy(args, 0, this.args, 0, args.length);

  }

  public void run () {

    // Create the class classLoader for the application jar file
    final JarClassLoader classLoader = new JarClassLoader(url);
    
    // Get the application's main class className
    final String className; 
    try {
      className = classLoader.getMainClassName();
      if (className == null) {
        LOGGER.info(NO_MAIN_CLASS);
        LOGGER.log(Level.WARNING, "JarRunner (fatal error): {0}", NO_MAIN_CLASS);
        System.err.println(NO_MAIN_CLASS);
        return;
      }      
    } catch (final IOException ex) {
      LOGGER.info(IO_ERROR);
      LOGGER.log(Level.WARNING, "JarRunner: {0}", IO_ERROR);
      System.err.println(IO_ERROR);
      return;
    }

    try {

      // Invoke application's main class
      classLoader.invokeClass(className, args);
      
    } catch (final ClassNotFoundException ex) {
      LOGGER.info(CLASS_NOT_FOUND);
      LOGGER.log(Level.WARNING, "JarRunner: class {0} not found", className);
      System.err.println(CLASS_NOT_FOUND);
    } catch (final NoSuchMethodException ex) {
      LOGGER.info(NO_MAIN_METHOD);
      LOGGER.log(Level.WARNING, "JarRunner: class {0} does not define a method main()", className);
      System.err.println(NO_MAIN_METHOD);
    } catch (final IllegalAccessException ex) {
      LOGGER.info(CAN_NOT_BE_LOADED);
      LOGGER.log(Level.WARNING, "JarRunner: class {0} cannot be loaded", className);
      System.err.println(CAN_NOT_BE_LOADED);
    } catch (final InvocationTargetException ex) {
      LOGGER.info(EXCEPTION_THROWN);
      LOGGER.log(Level.WARNING, "JarRunner: method exception thrown", ex.getTargetException());
      System.err.println(EXCEPTION_THROWN);
    }

  }

}