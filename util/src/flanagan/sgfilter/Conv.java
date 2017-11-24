/*
*   Class   Conv
*
*   USAGE:  Methods for:
*       Recasting variable type with exception throwing not present in standard java recasts
*       Conversion of physical entities from one set of units to another
*       For copy methods now see Copy.java - those already in Conv will be retained for compatibility purposes (10 April 2012)
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    April 2008
*   AMENDED: September 2009, 9-20 January 2011, 6-11 April 2012
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Conv.html
*
*   Copyright (c) 2008 - 2012
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.sgfilter;


import java.math.BigDecimal;
import java.math.BigInteger;

public class Conv{


    private static  double max_short_as_int     = (int)Short.MAX_VALUE;
    private static  double max_byte_as_int      = (int)Byte.MAX_VALUE;

    private static boolean suppressMessage = false;    // if true lack of precision messages are suppressed


    public static String convert_double_to_String(double x){
        return Double.toString(x).trim();
    }

    public static String convert_float_to_String(float x){
        return Float.toString(x).trim();
    }

    public static String convert_long_to_String(long x){
        return Long.toString(x).trim();
    }

    public static double convert_int_to_double(int x){
        return (new Integer(x)).doubleValue();
    }


    public static String convert_BigDecimal_to_String(BigDecimal x){
        return  x.toEngineeringString().trim();
    }


    public static String convert_BigInteger_to_String(BigInteger x){
        return  x.toString().trim();
    }

    // char and Character -> . . .
    public static double convert_char_to_double(char x){
        int xx = (int)x;
        return Conv.convert_int_to_double(xx);
    }

    public static String convert_char_to_String(char x){
        return Character.toString(x).trim();
    }


    // String -> . . .
    public static double convert_String_to_double(String x){
        return  Double.parseDouble(x.trim());
    }


    // COPY

        // COPY A ONE DIMENSIONAL ARRAY OF double
        public static double[] copy(double[] array){
            if(array==null)return null;
            int n = array.length;
            double[] copy = new double[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

    // COPY A ONE DIMENSIONAL ARRAY OF int
        public static int[] copy(int[] array){
            if(array==null)return null;
            int n = array.length;
            int[] copy = new int[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }


    // COPY A TWO DIMENSIONAL ARRAY OF double
        public static double[][] copy(double[][] array){
            if(array==null)return null;
            int n = array.length;
            double[][] copy = new double[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new double[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }


    // UNIT CONVERSIONS

}
    