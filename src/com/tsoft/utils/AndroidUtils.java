/**
 * 
 */
package com.tsoft.utils;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.Resources;

/**
 * @author tom
 * 
 */
public final class AndroidUtils
{
    public static String replaceAccents(String str)
    {
        StringBuilder sb = new StringBuilder( str.length() );

        CharacterIterator iter = new StringCharacterIterator( str );

        for ( char c = iter.first(); c != CharacterIterator.DONE; c = iter.next() )
        {
            sb.append( processChar( c ) );
        }

        return sb.toString();
    }
    
    //FIXME optimize
    private static char processChar( char c )
    {
        switch ( c )
        {
            case 233: /* 'é' */
            case 232: /* 'è' */
            case 234: /* 'ê' */
            case 235: /* 'ë': */
                return 'e';
            case 201: /* 'É' */
            case 200: /* 'È' */
            case 202: /* 'Ê' */
            case 203: /* 'Ë' */
                return 'E';
            case 224: /* 'à' */
            case 225: /* a accent grave */
            case 226: /* 'â' */
            case 227: /* a tilde */
            case 228: /* 'ä' */
            case 229: /* a ring */
            case 230: /* ae */
                return 'a';
            case 192: /* 'À' */
            case 193: /* A accent grave */
            case 194: /* 'Â' */
            case 195: /* A tilde */
            case 196: /* 'Ä' */
            case 197: /* A ring */
            case 198: /* AE */
                return 'A';
            case 253: /* y accent grave */
            case 255: /* 'ÿ' */
                return 'y';
            case 236: /* i accent aigu */
            case 237: /* i accent grave */
            case 238: /* 'î' */
            case 239: /* 'ï' */
                return 'i';
            case 204: /* I accent aigu */
            case 205: /* I accent grave */
            case 206: /* 'Î' */
            case 207: /* 'Ï' */
                return 'I';
            case 249: /* 'ù' */
            case 250: /* u accent grave */
            case 251: /* 'û' */
            case 252: /* 'ü' */
                return 'u';
            case 217: /* 'Ù' */
            case 218: /* U accent grave */
            case 219: /* 'Û' */
            case 220: /* 'Ü' */
                return 'U';
            case 242: /* o accent aigu */
            case 243: /* o accent grave */
            case 244: /* 'ô' */
            case 245: /* o tilde */
            case 246: /* 'ö' */
                return 'o';
            case 210: /* O accent aigu */
            case 211: /* O accent grave */
            case 212: /* 'Ô' */
            case 214: /* 'Ö' */
                return 'O';
            case 231: /* 'ç' */
                return 'c';
            case 199: /* 'Ç' */
                return 'C';
            case 241: /* n tilde espagnol */
                return 'n';
            case 209: /* N tilde espagnol */
                return 'N';
            case 178: /* ² */
                return '2';
            case 179: /* ³ */
                return '3';
                // ici a partir de la 4.5
            case 8364: /* € */
                return 'E';
            case 163: /* £ */
                return 'L';
            case 38: /* & */
            case 126: /* ~ */
                // pas d'escape du xml
                //case 34: /* " */
            case 35: /* # */
                // pas d'escape du xml
                //case 39: /* ' */
            case 123: /* { */
            case 40: /* ( */
                // pas d'escape du xml
                //case 91: /* [ */
                // pas d'escape du xml
                //case 124: /* | */
            case 96: /* ` */
            case 180: /* ´ */
                // pas d'escape du xml
                //case 95: /* _ */
                // pas d'escape du xml
                //case 92: /* \ */
            case 94: /* ^ */
                // pas d'escape du xml
                //case 64: /* @ */
                // pas d'escape du xml
                //case 41: /* ) */
                // pas d'escape du xml
                //case 93: /* ] */
            case 176: /* ° */
                // pas d'escape du xml
                //case 43: /* + */
                // pas d'escape du xml
                //case 61: /* = */
                // pas d'escape du xml
                //case 125: /* } */
            case 168: /* ¨ */
            case 164: /* ¤ */
            case 181: /* µ */
            case 42: /* * */
                // pas d'escape du xml    
                //case 47: /* / */
            case 167: /* § */
                c = ' ';
                break;

        }
        return c;
    }

  public static String getFileContent(Resources resources, int rawId) throws IOException
  {
    InputStream is = resources.openRawResource(rawId);

    // We guarantee that the available method returns the total
    // size of the asset... of course, this does mean that a single
    // asset can't be more than 2 gigs.
    int size = is.available();

    // Read the entire asset into a local byte buffer.
    byte[] buffer = new byte[size];
    is.read(buffer);
    is.close();

    // Convert the buffer into a string.
    return new String(buffer);
  }

  public static String pad(int hour, int minute)
  {
    return new StringBuilder(pad(hour)).append(":").append(pad(minute)).toString();
  }

  public static String pad(int c)
  {
    if (c >= 10)
      return String.valueOf(c);
    else
      return "0" + String.valueOf(c);
  }
}
