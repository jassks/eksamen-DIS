package utils;

import controllers.UserController;

public final class Encryption {

  public static String encryptDecryptXOR(String rawString) {

    // If encryption is enabled in Config.
    if (Config.getEncryption()) {

      // The key is predefined and hidden in code
      // TODO: Create a more complex code and store it somewhere better (FIX)

      char[] key = Config.getKEY().toCharArray();

      // Stringbuilder enables you to play around with strings and make useful stuff
      StringBuilder thisIsEncrypted = new StringBuilder();

      // TODO: This is where the magic of XOR is happening. Are you able to explain what is going on? (FIX)
      /**
       * For loopet har en variable(int) "i" som stiger med 1 hvergang loopet kører.
       * Loopet fortsætter med at kører indtil den har været igennem alle karakter i rawString (rawString er den String som vi ønsker at kryptere)
       * Når loopet kører for første gang med i=0, tager den de binære værdier af:
       *   (1) Den char som har placering 0 i rawString  -->  rawString.charAt(i)  -->  rawString.charAt(0)
       *   (2) Den første char som ligger i arraylisten "key"  -->  key[i % key.length]  --> key[0 % 9]  -->  key[0]
       * Disse to binære værdier ligges sammen ved brug af XOR, og vi får en ny binære værdi som konverteres til en char .
       * Denne nye char ligges til slutningen af StringBuilder thisIsEncrypted ved brug af append metoden.
       * Dette gentages indtil den har været igennem alle karakter i rawString.
       * Hvis key er mindre end rawString starter den med at bruge key forfra. Dette gøres ved brug af %
       *
       * Eksempel med XOR:
       *     D (fra rawString) = 0100 0100
       *     K (fra key)       = 0100 1011
       *    -------------------------------------
       *     Krypteret værdi --> 0000 1111  = 15
       *
       * Når den krypteret værdi ligges sammen med key, kan man få den originale String tilbage (rawString).
       * Det er derfor meget vigtigt at gemme key et sted hvor andre ikke har adgang til den.
       *
       *     15 (krypteret)    = 0000 1111
       *     K (fra key)       = 0100 1011
       *    -----------------------------------
       *     Orginal         --> 0100 0100 = D
       */

      for (int i = 0; i < rawString.length(); i++) {
        thisIsEncrypted.append((char) (rawString.charAt(i) ^ key[i % key.length]));
      }

      // We return the encrypted string
      return thisIsEncrypted.toString();

    } else {
      // We return without having done anything
      return rawString;
    }
  }
}
