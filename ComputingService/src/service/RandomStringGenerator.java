package service;
/**
 *
 * RandomStringGenerator: utilidad para generar nombres para ficheros recibidos
 * (ficheros jar y ficheros con argumentos); no se debe modificar.
 * 
 * @author MAZ
 */
import java.security.SecureRandom;
//
final class RandomStringGenerator {
  
  static private final String NUMBER = "0987654321";
  static private final String UPPER  = "ABCDEFGHIJKLMNOPQRSTUVXYZ";
  static private final String LOWER  = "abcdefghijklmnopqrstuvxyz";
  static private final String CHAR_FOR_RANDOM_STRING = LOWER + NUMBER + UPPER;
  
  private final SecureRandom rg;
  
  RandomStringGenerator () {
    this.rg = new SecureRandom();
  }
  
  String getString (final int n) {
    
    if (n < 1)
      throw new IllegalArgumentException();
    
    final StringBuilder sb = new StringBuilder(n);
    for (int j = 0; j < n; ++j) {
      final int randCharAt = rg.nextInt(CHAR_FOR_RANDOM_STRING.length());
      sb.append(CHAR_FOR_RANDOM_STRING.charAt(randCharAt));
    }
    
    return sb.toString();
            
  }
  
}