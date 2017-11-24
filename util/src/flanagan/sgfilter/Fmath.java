/*
*   Class   Fmath
*
*   USAGE:  Mathematical class that supplements java.lang.Math and contains:
*               the main physical constants
*               trigonemetric functions absent from java.lang.Math
*               some useful additional mathematical functions
*               some conversion functions
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    June 2002
*   AMENDED: 6 January 2006, 12 April 2006, 5 May 2006, 28 July 2006, 27 December 2006,
*            29 March 2007, 29 April 2007, 2,9,15 & 26 June 2007, 20 October 2007, 4-6 December 2007
*            27 February 2008, 25 April 2008, 26 April 2008, 13 May 2008, 25/26 May 2008, 3-7 July 2008
*            11 November 2010, 9-18 January 2011, 13 August 2011, 27 July 2012, 13 September 2012
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Fmath.html
*
*   Copyright (c) 2002 - 2011
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
import java.util.HashMap;
import java.util.Map;

public class Fmath{

        // PHYSICAL CONSTANTS


    // HashMap for 'arithmetic integer' recognition nmethod
        private static final Map<Object,Object> integers = new HashMap<Object,Object>();
        static{
            integers.put(Integer.class, BigDecimal.valueOf(Integer.MAX_VALUE));
            integers.put(Long.class, BigDecimal.valueOf(Long.MAX_VALUE));
            integers.put(Byte.class, BigDecimal.valueOf(Byte.MAX_VALUE));
            integers.put(Short.class, BigDecimal.valueOf(Short.MAX_VALUE));
            integers.put(BigInteger.class, BigDecimal.valueOf(-1));
        }

        // METHODS


    // SQUARES
        // Square of a double number
        public static double square(double a){
            return a*a;
        }


    // ADDITIONAL TRIGONOMETRIC FUNCTIONS

        // Returns the length of the hypotenuse of a and b
        // i.e. sqrt(a*a+b*b) [without unecessary overflow or underflow]
        // double version
        public static double hypot(double aa, double bb){
            double amod=Math.abs(aa);
            double bmod=Math.abs(bb);
            double cc = 0.0D, ratio = 0.0D;
            if(amod==0.0){
                cc=bmod;
            }
            else{
                if(bmod==0.0){
                    cc=amod;
                }
                else{
                    if(amod>=bmod){
                        ratio=bmod/amod;
                        cc=amod*Math.sqrt(1.0 + ratio*ratio);
                    }
                    else{
                        ratio=amod/bmod;
                        cc=bmod*Math.sqrt(1.0 + ratio*ratio);
                    }
                }
            }
            return cc;
        }

    // Cosine of angle between sides sideA and sideB given all side lengths of a triangle
        public static double cos(double sideAC, double sideBC, double sideAB){
            return 0.5D*(sideAC/sideBC + sideBC/sideAC - (sideAB/sideAC)*(sideAB/sideBC));
        }

    // Inverse cosine
        // Fmath.asin Checks limits - Java Math.asin returns NaN if without limits
        public static double acos(double a){
            if(a<-1.0D || a>1.0D) throw new IllegalArgumentException("Fmath.acos argument (" + a + ") must be >= -1.0 and <= 1.0");
            return Math.acos(a);
        }

    // Cotangent
        public static double cot(double a){
            return 1.0D/Math.tan(a);
        }

        // Inverse cotangent
        public static double acot(double a){
            return Math.atan(1.0D/a);
        }

    // Secant
        public static double sec(double a){
            return 1.0/Math.cos(a);
        }

        // Inverse secant
        public static double asec(double a){
            if(a<1.0D && a>-1.0D) throw new IllegalArgumentException("asec argument (" + a + ") must be >= 1 or <= -1");
            return Math.acos(1.0/a);
        }

        // Cosecant
        public static double csc(double a){
            return 1.0D/Math.sin(a);
        }

        // Inverse cosecant
        public static double acsc(double a){
            if(a<1.0D && a>-1.0D) throw new IllegalArgumentException("acsc argument (" + a + ") must be >= 1 or <= -1");
            return Math.asin(1.0/a);
        }

        // Exsecant
        public static double exsec(double a){
            return (1.0/Math.cos(a)-1.0D);
        }

        // Inverse exsecant
        public static double aexsec(double a){
            if(a<0.0D && a>-2.0D) throw new IllegalArgumentException("aexsec argument (" + a + ") must be >= 0.0 and <= -2");
            return Math.asin(1.0D/(1.0D + a));
        }

        // Versine
        public static double vers(double a){
            return (1.0D - Math.cos(a));
        }

        // Inverse  versine
        public static double avers(double a){
            if(a<0.0D && a>2.0D) throw new IllegalArgumentException("avers argument (" + a + ") must be <= 2 and >= 0");
            return Math.acos(1.0D - a);
        }

        // Coversine
        public static double covers(double a){
            return (1.0D - Math.sin(a));
        }

        // Inverse coversine
        public static double acovers(double a){
            if(a<0.0D && a>2.0D) throw new IllegalArgumentException("acovers argument (" + a + ") must be <= 2 and >= 0");
            return Math.asin(1.0D - a);
        }

        // Haversine
        public static double hav(double a){
            return 0.5D*Fmath.vers(a);
        }

        // Inverse haversine
        public static double ahav(double a){
            if(a<0.0D && a>1.0D) throw new IllegalArgumentException("ahav argument (" + a + ") must be >= 0 and <= 1");
            return Fmath.acos(1.0D - 2.0D*a);
        }

    //Hyperbolic sine of a double number
        public static double sinh(double a){
            return 0.5D*(Math.exp(a)-Math.exp(-a));
        }

        // Inverse hyperbolic sine of a double number
        public static double asinh(double a){
            double sgn = 1.0D;
            if(a<0.0D){
                sgn = -1.0D;
                a = -a;
            }
            return sgn*Math.log(a+Math.sqrt(a*a+1.0D));
        }

        //Hyperbolic cosine of a double number
        public static double cosh(double a){
            return 0.5D*(Math.exp(a)+Math.exp(-a));
        }

        // Inverse hyperbolic cosine of a double number
        public static double acosh(double a){
            if(a<1.0D) throw new IllegalArgumentException("acosh real number argument (" + a + ") must be >= 1");
            return Math.log(a+Math.sqrt(a*a-1.0D));
        }

    //Hyperbolic secant of a double number
        public static double sech(double a){
                return 1.0D/cosh(a);
        }

        // Inverse hyperbolic secant of a double number
        public static double asech(double a){
            if(a>1.0D || a<0.0D) throw new IllegalArgumentException("asech real number argument (" + a + ") must be >= 0 and <= 1");
            return 0.5D*(Math.log(1.0D/a + Math.sqrt(1.0D/(a*a) - 1.0D)));
        }

        //Hyperbolic cosecant of a double number
        public static double csch(double a){
                return 1.0D/sinh(a);
        }

        // Inverse hyperbolic cosecant of a double number
        public static double acsch(double a){
            double sgn = 1.0D;
            if(a<0.0D){
                sgn = -1.0D;
                a = -a;
            }
            return 0.5D*sgn*(Math.log(1.0/a + Math.sqrt(1.0D/(a*a) + 1.0D)));
        }


    // MANTISSA ROUNDING (TRUNCATING)
    // returns a value of xDouble truncated to trunc decimal places
    public static double truncate(double xDouble, int trunc){
        double xTruncated = xDouble;
        if(!Fmath.isNaN(xDouble)){
            if(!Fmath.isPlusInfinity(xDouble)){
                if(!Fmath.isMinusInfinity(xDouble)){
                    if(xDouble!=0.0D){
                        String xString = ((new Double(xDouble)).toString()).trim();
                        xTruncated = Double.parseDouble(truncateProcedure(xString, trunc));
                    }
                }
            }
        }
        return xTruncated;
    }

    // private method for truncating a float or double expressed as a String
    private static String truncateProcedure(String xValue, int trunc){

        String xTruncated = xValue;
        String xWorking = xValue;
        String exponent = " ";
        String first = "+";
        int expPos = xValue.indexOf('E');
        int dotPos = xValue.indexOf('.');
        int minPos = xValue.indexOf('-');

        if(minPos!=-1){
            if(minPos==0){
                xWorking = xWorking.substring(1);
                first = "-";
                dotPos--;
                expPos--;
            }
        }
        if(expPos>-1){
            exponent = xWorking.substring(expPos);
            xWorking = xWorking.substring(0,expPos);
        }
        String xPreDot = null;
        String xPostDot = "0";
        String xDiscarded = null;
        String tempString = null;
        double tempDouble = 0.0D;
        if(dotPos>-1){
            xPreDot = xWorking.substring(0,dotPos);
            xPostDot = xWorking.substring(dotPos+1);
            int xLength = xPostDot.length();
            if(trunc<xLength){
                xDiscarded = xPostDot.substring(trunc);
                tempString = xDiscarded.substring(0,1) + ".";
                if(xDiscarded.length()>1){
                    tempString += xDiscarded.substring(1);
                }
                else{
                    tempString += "0";
                }
                tempDouble = Math.round(Double.parseDouble(tempString));

                if(trunc>0){
                    if(tempDouble>=5.0){
                        int[] xArray = new int[trunc+1];
                        xArray[0] = 0;
                        for(int i=0; i<trunc; i++){
                            xArray[i+1] = Integer.parseInt(xPostDot.substring(i,i+1));
                        }
                        boolean test = true;
                        int iCounter = trunc;
                        while(test){
                            xArray[iCounter] += 1;
                            if(iCounter>0){
                                if(xArray[iCounter]<10){
                                    test = false;
                                }
                                else{
                                    xArray[iCounter]=0;
                                    iCounter--;
                                }
                            }
                            else{
                                test = false;
                            }
                        }
                        int preInt = Integer.parseInt(xPreDot);
                        preInt += xArray[0];
                        xPreDot = (new Integer(preInt)).toString();
                        tempString = "";
                        for(int i=1; i<=trunc; i++){
                            tempString += (new Integer(xArray[i])).toString();
                        }
                        xPostDot = tempString;
                    }
                    else{
                        xPostDot = xPostDot.substring(0, trunc);
                    }
                }
                else{
                    if(tempDouble>=5.0){
                        int preInt = Integer.parseInt(xPreDot);
                        preInt++;
                        xPreDot = (new Integer(preInt)).toString();
                    }
                    xPostDot = "0";
                }
            }
            xTruncated = first + xPreDot.trim() + "." + xPostDot.trim() + exponent;
        }
        return xTruncated.trim();
    }

        // Returns true if x is infinite, i.e. is equal to either plus or minus infinity
        // x is double
        public static boolean isInfinity(double x){
            boolean test=false;
            if(x==Double.POSITIVE_INFINITY || x==Double.NEGATIVE_INFINITY)test=true;
            return test;
        }

    // Returns true if x is plus infinity
        // x is double
        public static boolean isPlusInfinity(double x){
            boolean test=false;
            if(x==Double.POSITIVE_INFINITY)test=true;
            return test;
        }

    // Returns true if x is minus infinity
        // x is double
        public static boolean isMinusInfinity(double x){
            boolean test=false;
            if(x==Double.NEGATIVE_INFINITY)test=true;
            return test;
        }


    // Returns true if x is 'Not a Number' (NaN)
        // x is double
        public static boolean isNaN(double x){
            boolean test=false;
            if(x!=x)test=true;
            return test;
        }


    // IS EVEN
        // Returns true if x is an even number, false if x is an odd number
        // x is int
        public static boolean isEven(int x){
            boolean test=false;
            if(x%2 == 0.0D)test=true;
            return test;
        }

    // DEPRECATED METHODS
        // Several methods have been revised and moved to classes ArrayMaths, Conv or PrintToScreen


    // UNIT CONVERSIONS (deprecated - see Conv class)

}

