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
 *   - Neither the name of Oracle or the names of its
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
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.JarURLConnection;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.jar.Attributes;
/**
 * A class loader for loading jar local files.
 */
final class JarClassLoader extends URLClassLoader {

  private final URL url;

  /**
   * Creates a new JarClassLoader for the specified url.
   *
   * @param url the url of the jar file
   */
  JarClassLoader (final URL url) {
    super(new URL[] { url });
    this.url = url;
  }

  /**
   * Returns the name of the jar file main class, or null if no "Main-Class"
   * manifest attributes was defined.
   */
  String getMainClassName () throws IOException {
    final URL _url = new URL("jar", "", url + "!/");
    final JarURLConnection uc = (JarURLConnection) _url.openConnection();
    final Attributes attr = uc.getMainAttributes();
    return (attr != null) ? attr.getValue(Attributes.Name.MAIN_CLASS) : null;
  }

  /**
   * Invokes the application in this jar file given the name of the main class
   * and an array of arguments. The class must define a static method "main"
   * which takes an array of String arguments and is of return type "void".
   *
   * @param name the name of the main class
   * @param args the arguments for the application
   * @exception ClassNotFoundException if the specified class could not be found
   * @exception NoSuchMethodException if the specified class does not contain a
   * "main" method
   * @exception IllegalAccessException if the invoked class method is not public
   * or its definition is not found
   * @exception InvocationTargetException if the application raised an exception
   */
  void invokeClass (final String name, final Object[] args) throws
          IllegalAccessException,
          ClassNotFoundException,
          NoSuchMethodException,
          InvocationTargetException {
    
    final Class c = loadClass(name);
    final Method mainMethod = c.getMethod("main", new Class[] { args.getClass() });
    final int mods = mainMethod.getModifiers();
    if ((mainMethod.getReturnType() != void.class)
            || !Modifier.isStatic(mods)
            || !Modifier.isPublic(mods)) {
      throw new NoSuchMethodException("main");
    }
    mainMethod.invoke(null, new Object[] { args });
   
  }

}