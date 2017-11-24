/*
*   CLASS:      CurveSmooth
*
*   USAGE:      Class for smoothing a curve and obtaining the maxima and minima of a curve
*               Smoothing methods: moving average window or Savitzky-Golay filter
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       February 2012
*   AMENDED:    26-27 February 2012, 3-17 March 2012 
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Smooth.html
*
*   Copyright (c) 2012  Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*
* Permission to use, copy and modify this software and its documentation for NON-COMMERCIAL purposes is granted, without fee,
* provided that an acknowledgement to the author, Dr Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies
* and associated documentation or publications.
*
* Redistributions of the source code of this source code, or parts of the source codes, must retain the above copyright notice, this list of conditions
* and the following disclaimer and requires written permission from the Michael Thomas Flanagan:
*
* Redistribution in binary form of all or parts of this class must reproduce the above copyright notice, this list of conditions and
* the following disclaimer in the documentation and/or other materials provided with the distribution and requires written permission from the Michael Thomas Flanagan:
*
* Dr Michael Thomas Flanagan makes no representations about the suitability or fitness of the software for any or for a particular purpose.
* Dr Michael Thomas Flanagan shall not be liable for any damages suffered as a result of using, modifying or distributing this software
* or its derivatives.
*
***************************************************************************************/

package flanagan.sgfilter;

public class CurveSmooth{
    
    private double[] xData = null;                                  // original x data, y = f(x)  
    private double[] yData = null;                                  // original y data, y = f(x)  
    private int nPoints = 0;                                        // number of data points
    private double[] yDataSavGol = null;                            // Savitzky-Golay smoothed y data, y = f(x)

    // Constructor - data as double - no x data, y = f(x)
    public CurveSmooth(double[] y){ 
        int n = y.length;
        this.yData = y;
        this.xData = new double[n];
        for(int i=0; i<n; i++)this.xData[i] = i;
        this.check();
    }

    // Check for correct dimensions, y = f(x) 
    private void check(){
        // Dimension arrays
        this.nPoints = this.xData.length;
        int m = this.yData.length;
        if(m!=this.nPoints)throw new IllegalArgumentException("The length of the x data array, " + this.nPoints + ", must be the same as the length of the y data array, " + m);       
        if(m<5)throw new IllegalArgumentException("There must be at least five data points");
    }
    
    // order data in ascending x-values, y = f(x)
    private void ascend() {
        boolean test1 = true;
        int ii = 1;
        while (test1) {
            if (this.xData[ii] < this.xData[ii - 1]) {
                test1 = false;
            } else {
                ii++;
                if (ii >= this.nPoints) test1 = false;
            }
        }
    }

    // Adust width to an odd number of points
    private int windowLength(int width){
    
        int ww = 0;
        if(Fmath.isEven(width)){
            ww = width+1;
        }
        else{
           ww = width;
        }
        return ww;
    }

    public double[] savitzkyGolay(int sgWindowWidth){
        this.yDataSavGol = new double[this.nPoints];
        // adjust window width to an odd number of points
        int sgWindowWidth1 = this.windowLength(sgWindowWidth);
        // Apply filter 
        this.savitzkyGolayCommon(sgWindowWidth1);
        return Conv.copy(this.yDataSavGol);
    }
    
     // Common method for smoothing with a Savitzky-Golay filter with a window width
    private double[] savitzkyGolayCommon(int width){
        
        // Set filter dimension term
        int ww = (width - 1)/2;
        // Calculate filter coefficients
        double[] coeff = (this.savitzkyGolayFilter(ww, ww))[0];
        // Padout the data to solve edge effects
        double[] psData = this.padData(this.yData, ww);
        // Apply filter       
        for(int i=ww; i<this.nPoints+ww; i++){ 
            double sum = 0.0;
            int counter = 0;
             for(int k1=i-ww; k1<=i+ww; k1++){
                sum += psData[k1]*coeff[counter++];
             }
             this.yDataSavGol[i-ww] = sum;
        }
        return this.yDataSavGol;
    }
    
    private double[] padData(double[] data, int ww){
        
        // Pad out to solve edge effects
        // Set dimensions
        int nn = data.length;
        
        
        // Create array for padding
        double[] psData = new double[nn+2*ww];
        
        // fill central array with true data
        for(int i=0; i<nn; i++){ 
            psData[i+ww] = data[i];
        }
    
        // pad out leading elements
        for(int i=0; i<ww; i++){ 
            psData[i] = psData[ww];
        }
        
        // pad out trailing elements
        for(int i=nn+ww; i<nn+2*ww; i++){ 
            psData[i] = psData[nn+ww-1];
        }
        
        return psData;
    }

    private double[][] savitzkyGolayFilter(int bp, int fp){
        
        int ww = bp + fp + 1;                   //filter  length 
        double[] coeff = new double[ww];        // Savitzky-Golay coefficients 

        // Assign 'x' values
        int[] values = new int[ww];
        for(int i = 0; i<ww; i++){
            values[i] = i-bp;
        }

        int sgPolyDeg = 4;
        double[][] x = new double[ww][sgPolyDeg +1];
        for(int i=0; i<ww; i++){
            for(int j = 0; j< sgPolyDeg +1; j++){
                x[i][j] = Math.pow(values[i], j);
            }
        }            
              
        Matrix matX = new Matrix(x);
        Matrix matT = matX.transpose();
        Matrix matTX = matT.times(matX);        
        Matrix matI = matTX.inverse();      
        Matrix matC = matI.times(matT);
        return matC.getArrayCopy();
    }

}
