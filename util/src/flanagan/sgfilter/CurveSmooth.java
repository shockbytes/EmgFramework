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

public class CurveSmooth {

    private double[] xData = null;                                  // original x data, y = f(x)  
    private double[] yData = null;                                  // original y data, y = f(x)  
    private int nPoints = 0;                                        // number of data points
    private double[] yDataSavGol = null;                            // Savitzky-Golay smoothed y data, y = f(x)

    public CurveSmooth(double[] y) {
        int n = y.length;
        this.yData = y;
        this.xData = new double[n];
        for (int i = 0; i < n; i++) this.xData[i] = i;
        this.check();
    }

    private void check() {
        this.nPoints = this.xData.length;
        int m = this.yData.length;
        if (m != this.nPoints)
            throw new IllegalArgumentException("The length of the x data array, " + this.nPoints + ", must be the same as the length of the y data array, " + m);
        if (m < 5) throw new IllegalArgumentException("There must be at least five data points");
    }

    private int windowLength(int width) {
        return isEven(width) ? (width + 1) : width;
    }

    public double[] savitzkyGolay(int sgWindowWidth) {
        this.yDataSavGol = new double[this.nPoints];
        int sgWindowWidth1 = this.windowLength(sgWindowWidth);
        this.savitzkyGolayCommon(sgWindowWidth1);
        return copy(this.yDataSavGol);
    }

    private double[] savitzkyGolayCommon(int width) {
        int ww = (width - 1) / 2;
        double[] coeff = (this.savitzkyGolayFilter(ww, ww))[0];
        double[] psData = this.padData(this.yData, ww);
        for (int i = ww; i < this.nPoints + ww; i++) {
            double sum = 0.0;
            int counter = 0;
            for (int k1 = i - ww; k1 <= i + ww; k1++) {
                sum += psData[k1] * coeff[counter++];
            }
            this.yDataSavGol[i - ww] = sum;
        }
        return this.yDataSavGol;
    }

    private double[] padData(double[] data, int ww) {
        int nn = data.length;
        double[] psData = new double[nn + 2 * ww];
        System.arraycopy(data, 0, psData, ww, nn);
        for (int i = 0; i < ww; i++) {
            psData[i] = psData[ww];
        }
        for (int i = nn + ww; i < nn + 2 * ww; i++) {
            psData[i] = psData[nn + ww - 1];
        }
        return psData;
    }

    private double[][] savitzkyGolayFilter(int bp, int fp) {

        int ww = bp + fp + 1;
        int[] values = new int[ww];
        for (int i = 0; i < ww; i++) {
            values[i] = i - bp;
        }

        int sgPolyDeg = 4;
        double[][] x = new double[ww][sgPolyDeg + 1];
        for (int i = 0; i < ww; i++) {
            for (int j = 0; j < sgPolyDeg + 1; j++) {
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

    private boolean isEven(int x) {
        boolean test = false;
        if (x % 2 == 0.0D) test = true;
        return test;
    }

    private double[] copy(double[] array) {
        if (array == null) return null;
        int n = array.length;
        double[] copy = new double[n];
        System.arraycopy(array, 0, copy, 0, n);
        return copy;
    }
}
