/*  Class Matrix
*
*   Defines a matrix and includes the methods needed
*   for standard matrix manipulations, e.g. multiplation,
*   and related procedures, e.g. solution of linear
*   simultaneous equations
*
*   See class ComplexMatrix and PhasorMatrix for complex matrix arithmetic
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	June 2002
*   UPDATES:    21 April 2004, 16 February 2006, 31 March 2006, 22 April 2006,
*               1 July 2007, 17 July 2007, 18 August 2007, 7 October 2007
*               27 February 2008, 7 April 2008, 5 July 2008, 6-15 September 2008, 7-14 October 2008, 
*               16 February 2009, 16 June 2009, 15 October 2009, 4-5 November 2009
*               12 January 2010, 19 February 2010, 14 November 2010
*               12 January 2011, 20 January 2011, 14-16 July 2011
*               7 April 2012, 26 June 2012
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Matrix.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2012  Michael Thomas Flanagan
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

public class Matrix{

	    private int numberOfRows = 0;                   // number of rows
	    private int numberOfColumns = 0;                // number of columns
	    private double[][] matrix = null; 	            // 2-D  Matrix as double
            private String[][] matrixS = null;	            // 2-D  Matrix as String
            private int entryType = -1;                     // Entry type
                                                            // = 0; double
                                                            // = 1; float
                                                            // = 2; long
                                                            // = 3; int
                                                            // = 4; BigDecimal
                                                            // = 5; BigInteger
                                                            // = x; 
                                                            // = y;
            private boolean numericalCheck = true;          // = false if no numerical data entered, e.g. only non-numerical strings entered
            
	    private double[][] hessenberg = null; 	    // 2-D  Hessenberg equivalent
	    private boolean hessenbergDone = false;         // = true when Hessenberg matrix calculated
	    private int permutationIndex[] = null;          // row permutation index
	    private double rowSwapIndex = 1.0D;             // row swap index
	    private double[] eigenValues = null;            // eigen values of the matrix
	    private double[][] eigenVector = null;          // eigen vectors of the matrix
	    private double[] sortedEigenValues = null;      // eigen values of the matrix sorted into descending order
	    private double[][] sortedEigenVector = null;    // eigen vectors of the matrix sorted to matching descending eigen value order
	    private int numberOfRotations = 0;              // number of rotations in Jacobi transformation
	    private int[] eigenIndices = null;              // indices of the eigen values before sorting into descending order
	    private int maximumJacobiIterations = 100;      // maximum number of Jacobi iterations
	    private boolean eigenDone = false;              // = true when eigen values and vectors calculated
	    private boolean matrixCheck = true;             // check on matrix status
	                                    	            // true - no problems encountered in LU decomposition
	                                    	            // false - attempted a LU decomposition on a singular matrix

	    private boolean supressErrorMessage = false;    // true - LU decompostion failure message supressed

	    private double tiny = 1.0e-100;                 // small number replacing zero in LU decomposition

	    // CONSTRUCTORS
	    // Construct a numberOfRows x numberOfColumns matrix of variables all equal to zero
            public Matrix(int numberOfRows, int numberOfColumns){
		    this.numberOfRows = numberOfRows;
		    this.numberOfColumns = numberOfColumns;
                    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
                    this.matrixS = new String[this.numberOfRows][this.numberOfColumns];
		    this.permutationIndex = new int[this.numberOfRows];
                    this.entryType = 0;
                    for(int i=0;i<this.numberOfRows;i++){
                        this.permutationIndex[i]=i;
                        for(int j=0;j<this.numberOfColumns;j++)this.matrixS[i][j] = "0.0";
                    }
                    this.eigenDone = false;
                    this.hessenbergDone = false;
            }

	// Construct matrix with a copy of an existing numberOfRows x numberOfColumns 2-D array of variables
            public Matrix(double[][] twoD){
		    this.numberOfRows = twoD.length;
		    this.numberOfColumns = twoD[0].length;
		    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
                    this.matrixS = new String[this.numberOfRows][this.numberOfColumns];
                    this.permutationIndex = new int[this.numberOfRows];
                    this.entryType = 0;
                    for(int i=0;i<this.numberOfRows;i++){
                        this.permutationIndex[i]=i;
                        for(int j=0;j<this.numberOfColumns;j++){
                            this.matrix[i][j] = twoD[i][j];
                            this.matrixS[i][j] = Conv.convert_double_to_String(twoD[i][j]);
                        }
                    }
                    this.eigenDone = false;
                    this.hessenbergDone = false;
   	    }


	// Construct matrix with a copy of the 2D matrix and permutation index of an existing Matrix bb.
            public Matrix(Matrix bb){
		    this.numberOfRows = bb.numberOfRows;
		    this.numberOfColumns = bb.numberOfColumns;
		    this.matrix = new double[this.numberOfRows][this.numberOfColumns];
                    this.matrixS = new String[numberOfRows][numberOfColumns];
                    this.entryType = bb.getEntryType();
		    for(int i=0; i<numberOfRows; i++){
		        for(int j=0; j<numberOfColumns; j++){
		            this.matrix[i][j] = bb.matrix[i][j];
                            this.matrixS[i][j] = bb.matrixS[i][j];
		        }
		    }
		    this.permutationIndex = Conv.copy(bb.permutationIndex);
                    this.rowSwapIndex = bb.rowSwapIndex;
                    this.eigenDone = false;
                    this.hessenbergDone = false;
 	    }


        // METHODS
        // GET ENTRY TYPE
        private int getEntryType(){
            return this.entryType;
        }


	// Set a sub-matrix starting with row index i, column index j
    	public void setSubMatrix(int i, int j, double[][] subMatrix){
                int k = subMatrix.length;
                int l = subMatrix[0].length;
        	if(i+k-1>=this.numberOfRows)throw new IllegalArgumentException("Sub-matrix position is outside the row bounds of this Matrix");
        	if(j+l-1>=this.numberOfColumns)throw new IllegalArgumentException("Sub-matrix position is outside the column bounds of this Matrix");

        	int m = 0;
        	int n = 0;
        	for(int p=0; p<k; p++){
        	    n = 0;
                    for(int q=0; q<l; q++){
                        this.matrix[i+p][j+q] = subMatrix[m][n];
                        n++;
                    }
                    m++;
        	}
                this.eigenDone = false;
                this.hessenbergDone = false;
    	}

	// GET VALUES
        // Return the number of rows
    	public int getNumberOfRows(){
        	return this.numberOfRows;
    	}

	// Return the number of columns
    	public int getNumberOfColumns(){
        	return this.numberOfColumns;
    	}

	// Return a reference to the internal 2-D array
    	public double[][] getArrayReference(){
        	return this.matrix;
    	}

	// Return a copy of the internal 2-D array
    	public double[][] getArrayCopy(){
        	double[][] c = new double[this.numberOfRows][this.numberOfColumns];
		    for(int i=0; i<numberOfRows; i++){
		    	for(int j=0; j<numberOfColumns; j++){
		        	c[i][j]=this.matrix[i][j];
		    	}
		    }
        	return c;
    	}


	// Return a sub-matrix
    	// row = array of row indices
   	    // col = array of column indices
    	public Matrix getSubMatrix(int[] row, int[] col){
        	int n = row.length;
        	int m = col.length;
        	Matrix subMatrix = new Matrix(n, m);
        	double[][] sarray = subMatrix.getArrayReference();
        	for(int i=0; i<n; i++){
            		for(int j=0; j<m; j++){
                		sarray[i][j]= this.matrix[row[i]][col[j]];
            		}
        	}
        	return subMatrix;
    	}

	// COPY
    	// Copy a Matrix [static method]
    	public static Matrix copy(Matrix a){
    	    if(a==null){
    	        return null;
    	    }
    	    else{
        	    int nr = a.getNumberOfRows();
        	    int nc = a.getNumberOfColumns();
        	    double[][] aarray = a.getArrayReference();
        	    Matrix b = new Matrix(nr,nc);
        	    b.numberOfRows = nr;
        	    b.numberOfColumns = nc;
        	    double[][] barray = b.getArrayReference();
        	    for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		barray[i][j]=aarray[i][j];
            		}
        	    }
        	    for(int i=0; i<nr; i++)b.permutationIndex[i] = a.permutationIndex[i];
        	    return b;
        	}
    	}

	// Clone a Matrix
    	public Object clone(){
            if(this==null){
    	        return null;
    	    }
    	    else{
    	        int nr = this.numberOfRows;
        	    int nc = this.numberOfColumns;
        	    Matrix b = new Matrix(nr,nc);
        	    double[][] barray = b.getArrayReference();
        	    b.numberOfRows = nr;
        	    b.numberOfColumns = nc;
        	    for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		barray[i][j]=this.matrix[i][j];
            		}
        	    }
        	    for(int i=0; i<nr; i++)b.permutationIndex[i] = this.permutationIndex[i];
        	    return (Object) b;
        	}
    	}


	// MULTIPLICATION
    	// Multiply this  matrix by a matrix.   [instance method]
    	// This matrix remains unaltered.
    	public Matrix times(Matrix bmat){
        	if(this.numberOfColumns!=bmat.numberOfRows)throw new IllegalArgumentException("Nonconformable matrices");

        	Matrix cmat = new Matrix(this.numberOfRows, bmat.numberOfColumns);
        	double [][] carray = cmat.getArrayReference();
        	double sum = 0.0D;

        	for(int i=0; i<this.numberOfRows; i++){
            		for(int j=0; j<bmat.numberOfColumns; j++){
                		sum=0.0D;
                		for(int k=0; k<this.numberOfColumns; k++){
                       			sum += this.matrix[i][k]*bmat.matrix[k][j];
                		}
                		carray[i][j]=sum;
            		}
        	}
        	return cmat;
    	}

	// Multiply this matrix by a matrix [equivalence of *=]
    	public void timesEquals(Matrix bmat){
        	if(this.numberOfColumns!=bmat.numberOfRows)throw new IllegalArgumentException("Nonconformable matrices");

        	Matrix cmat = new Matrix(this.numberOfRows, bmat.numberOfColumns);
        	double [][] carray = cmat.getArrayReference();
        	double sum = 0.0D;

        	for(int i=0; i<this.numberOfRows; i++){
            		for(int j=0; j<bmat.numberOfColumns; j++){
                		sum=0.0D;
                		for(int k=0; k<this.numberOfColumns; k++){
                       			sum += this.matrix[i][k]*bmat.matrix[k][j];
                		}
                		carray[i][j]=sum;
            		}
        	}

        	this.numberOfRows = cmat.numberOfRows;
	        this.numberOfColumns = cmat.numberOfColumns;
	        for(int i=0; i<this.numberOfRows; i++){
	            for(int j=0; j<this.numberOfColumns; j++){
	                this.matrix[i][j] = cmat.matrix[i][j];
	            }
	        }
    	}


	// Divide a this matrix by a matrix[equivalence of /=]
    	public void overEquals(Matrix bmat){
        	if((this.numberOfRows!=bmat.numberOfRows)||(this.numberOfColumns!=bmat.numberOfColumns)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	Matrix cmat = new Matrix(bmat);
    	    this.timesEquals(cmat.inverse());
    	}

	// INVERSE
    	// Inverse of a square matrix [instance method]
    	public Matrix inverse(){
        	int n = this.numberOfRows;
        	if(n!=this.numberOfColumns)throw new IllegalArgumentException("Matrix is not square");
        	Matrix invmat = new Matrix(n, n);

            if(n==1){
                double[][] hold = this.getArrayCopy();
                if(hold[0][0]==0.0)throw new IllegalArgumentException("Matrix is singular");
                hold[0][0] = 1.0/hold[0][0];
                invmat = new Matrix(hold);
            }
            else{
                if(n==2){
                    double[][] hold = this.getArrayCopy();
                    double det = hold[0][0]*hold[1][1] - hold[0][1]*hold[1][0];
                    if(det==0.0)throw new IllegalArgumentException("Matrix is singular");
                    double[][] hold2 = new double[2][2];
                    hold2[0][0] = hold[1][1]/det;
                    hold2[1][1] = hold[0][0]/det;
                    hold2[1][0] = -hold[1][0]/det;
                    hold2[0][1] = -hold[0][1]/det;
                    invmat = new Matrix(hold2);
                }
                else{
        	        double[] col = new double[n];
        	        double[] xvec = new double[n];
        	        double[][] invarray = invmat.getArrayReference();
        	        Matrix ludmat;

	    	        ludmat = this.luDecomp();
        	        for(int j=0; j<n; j++){
            		    for(int i=0; i<n; i++)col[i]=0.0D;
            		    col[j]=1.0;
            		    xvec=ludmat.luBackSub(col);
            		    for(int i=0; i<n; i++)invarray[i][j]=xvec[i];
        	        }
        	    }
        	}
       		return invmat;
    	}

	// TRANSPOSE
    	// Transpose of a matrix [instance method]
    	public Matrix transpose(){
        	Matrix tmat = new Matrix(this.numberOfColumns, this.numberOfRows);
        	double[][] tarray = tmat.getArrayReference();
        	for(int i=0; i<this.numberOfColumns; i++){
            		for(int j=0; j<this.numberOfRows; j++){
                		tarray[i][j]=this.matrix[j][i];
            		}
        	}
        	return tmat;
    	}

	// DETERMINANT
    	//  Returns the determinant of a square matrix [instance method]
    	public double determinant(){
        	int n = this.numberOfRows;
        	if(n!=this.numberOfColumns)throw new IllegalArgumentException("Matrix is not square");
        	double det = 0.0D;
        	if(n==2){
        	     det = this.matrix[0][0]*this.matrix[1][1] - this.matrix[0][1]*this.matrix[1][0];
        	}
        	else{
        	    Matrix ludmat = this.luDecomp();
    	    	det = ludmat.rowSwapIndex;
            	for(int j=0; j<n; j++){
            		det *= ludmat.matrix[j][j];
             	}
            }
        	return det;
    	}

	// Returns the log(determinant) of a square matrix [instance method].
    	// Useful if determinant() underflows or overflows.
    	public double logDeterminant(){
        	int n = this.numberOfRows;
        	if(n!=this.numberOfColumns)throw new IllegalArgumentException("Matrix is not square");
        	double det = 0.0D;
        	Matrix ludmat = this.luDecomp();

	    	det = ludmat.rowSwapIndex;
	    	det=Math.log(det);
        	for(int j=0; j<n; j++){
            		det += Math.log(ludmat.matrix[j][j]);
        	}
        	return det;
    	}

	// Returns the ii,jjth cofactor
    	public double cofactor(int ii, int jj){
    	    if(ii<0 || ii>=this.numberOfRows)throw new IllegalArgumentException("The entered row index, " + ii + " must lie between 0 and " + (this.numberOfRows-1) + " inclusive");
    	    if(jj<0 || jj>=this.numberOfColumns)throw new IllegalArgumentException("The entered column index, " + jj + " must lie between 0 and " + (this.numberOfColumns-1) + " inclusive");
    	    int[] rowi = new int[this.numberOfRows - 1];
    	    int[] colj = new int[this.numberOfColumns - 1];
    	    int kk = 0;
    	    for(int i=0; i<this.numberOfRows; i++){
    	        if(i!=ii){
    	            rowi[kk]=i;
    	            kk++;
    	        }
    	    }
    	    kk = 0;
    	    for(int j=0; j<this.numberOfColumns; j++){
    	        if(j!=jj){
    	            colj[kk]=j;
    	            kk++;
    	        }
    	    }
            Matrix aa = this.getSubMatrix(rowi, colj);
            double aadet = aa.determinant();
            return aadet*Math.pow(-1.0, (ii+jj));
        }


	// Check if a matrix is symmetric
    	public boolean isSymmetric(){
    	    boolean test = true;
    	    if(this.numberOfRows==this.numberOfColumns){
    	        for(int i=0; i<this.numberOfRows; i++){
    	            for(int j=i+1; j<this.numberOfColumns; j++){
    	                if(this.matrix[i][j]!=this.matrix[j][i])test = false;
    	            }
    	        }
    	    }
    	    else{
    	        test = false;
    	    }
    	    return test;
    	}


	// LU DECOMPOSITION OF MATRIX A
    	// For details of LU decomposition
    	// See Numerical Recipes, The Art of Scientific Computing
    	// by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
	    // Cambridge University Press,   http://www.nr.com/
	    // This method has followed their approach but modified to an object oriented language
	    // Matrix ludmat is the returned LU decompostion
	    // int[] index is the vector of row permutations
	    // rowSwapIndex returns +1.0 for even number of row interchanges
	    //       returns -1.0 for odd number of row interchanges
	    public Matrix luDecomp(){
        	if(this.numberOfRows!=this.numberOfColumns)throw new IllegalArgumentException("A matrix is not square");
        	int n = this.numberOfRows;
	    	int imax = 0;
	    	double dum = 0.0D, temp = 0.0D, big = 0.0D;
	    	double[] vv = new double[n];
	    	double sum = 0.0D;
	    	double dumm = 0.0D;

	    	this.matrixCheck = true;

	      	Matrix ludmat = Matrix.copy(this);
	    	double[][] ludarray = ludmat.getArrayReference();

    		ludmat.rowSwapIndex=1.0D;
	    	for (int i=0;i<n;i++) {
		    	big=0.0D;
		    	for (int j=0;j<n;j++)if  ((temp=Math.abs(ludarray[i][j])) > big) big=temp;
        		if (big == 0.0D){
        		    if(!this.supressErrorMessage){
        	    		System.out.println("Attempted LU Decomposition of a singular matrix in Matrix.luDecomp()");
         	    		System.out.println("NaN matrix returned and matrixCheck set to false");
         	        }
         	        this.matrixCheck=false;
         	    	for(int k=0;k<n;k++)for(int j=0;j<n;j++)ludarray[k][j]=Double.NaN;
         	    	return ludmat;
         		}
    			vv[i]=1.0/big;
	    	}
	    	for (int j=0;j<n;j++) {
		    	for (int i=0;i<j;i++) {
			    	sum=ludarray[i][j];
			    	for (int k=0;k<i;k++) sum -= ludarray[i][k]*ludarray[k][j];
			    	ludarray[i][j]=sum;
		    	}
		    	big=0.0D;
		    	for (int i=j;i<n;i++) {
    				sum=ludarray[i][j];
	    			for (int k=0;k<j;k++)
				    	sum -= ludarray[i][k]*ludarray[k][j];
			    		ludarray[i][j]=sum;
     					if ((dum=vv[i]*Math.abs(sum)) >= big) {
				        	big=dum;
				        	imax=i;
			    		}
		    		}
		    		if (j != imax) {
			    	for (int k=0;k<n;k++) {
				    	dumm=ludarray[imax][k];
				    	ludarray[imax][k]=ludarray[j][k];
    					ludarray[j][k]=dumm;
			    	}
			    	ludmat.rowSwapIndex = -ludmat.rowSwapIndex;
    				vv[imax]=vv[j];
	    		}
		    	ludmat.permutationIndex[j]=imax;

		    	if(ludarray[j][j]==0.0D){
		        	ludarray[j][j]=this.tiny;
		    	}
		    	if(j != n-1) {
			    	dumm=1.0/ludarray[j][j];
    				for (int i=j+1;i<n;i++){
    			    		ludarray[i][j]*=dumm;
	    	    		}
	    		}
	    	}
	    	return ludmat;
	    }

    	// Solves the set of n linear equations A.X=B using not A but its LU decomposition
    	// bvec is the vector B (input)
    	// xvec is the vector X (output)
    	// index is the permutation vector produced by luDecomp()
    	public double[] luBackSub(double[] bvec){
	    	int ii = 0,ip = 0;
	    	int n=bvec.length;
	    	if(n!=this.numberOfColumns)throw new IllegalArgumentException("vector length is not equal to matrix dimension");
	    	if(this.numberOfColumns!=this.numberOfRows)throw new IllegalArgumentException("matrix is not square");
	    	double sum= 0.0D;
	    	double[] xvec=new double[n];
	    	for(int i=0; i<n; i++){
	        	xvec[i]=bvec[i];
	    	}
	    	for (int i=0;i<n;i++) {
		    	ip=this.permutationIndex[i];
		    	sum=xvec[ip];
		    	xvec[ip]=xvec[i];
		    	if (ii==0){
			    	for (int j=ii;j<=i-1;j++){
			        	sum -= this.matrix[i][j]*xvec[j];
			    	}
			    }
		    	else{
		        	if(sum==0.0) ii=i;
		    	}
		    	xvec[i]=sum;
	    	}
	    	for(int i=n-1;i>=0;i--) {
		    	sum=xvec[i];
		    	for (int j=i+1;j<n;j++){
		        	sum -= this.matrix[i][j]*xvec[j];
		    	}
		    	xvec[i]= sum/matrix[i][i];
	    	}
	    	return xvec;
    	}


	// HESSENBERG MARTIX

        // Calculates the Hessenberg equivalant of this matrix
        public void hessenbergMatrix(){

            this.hessenberg = this.getArrayCopy();
            double pivot = 0.0D;
            int pivotIndex = 0;
            double hold = 0.0D;

            for(int i = 1; i<this.numberOfRows-1; i++){
                // identify pivot
                pivot = 0.0D;
                pivotIndex = i;
                for(int j=i; j<this.numberOfRows; j++){
                    if(Math.abs(this.hessenberg[j][i-1])> Math.abs(pivot)){
                        pivot = this.hessenberg[j][i-1];
                        pivotIndex = j;
                    }
                }

                // row and column interchange
                if(pivotIndex != i){
                    for(int j = i-1; j<this.numberOfRows; j++){
                        hold = this.hessenberg[pivotIndex][j];
                        this.hessenberg[pivotIndex][j] = this.hessenberg[i][j];
                        this.hessenberg[i][j] = hold;
                    }
                    for(int j = 0; j<this.numberOfRows; j++){
                        hold = this.hessenberg[j][pivotIndex];
                        this.hessenberg[j][pivotIndex] = this.hessenberg[j][i];
                        this.hessenberg[j][i] = hold;
                    }

                    // elimination
                    if(pivot!=0.0){
                        for(int j=i+1; j<this.numberOfRows; j++){
                            hold = this.hessenberg[j][i-1];
                            if(hold!=0.0){
                                hold /= pivot;
                                this.hessenberg[j][i-1] = hold;
                                for(int k=i; k<this.numberOfRows; k++){
                                    this.hessenberg[j][k] -= hold*this.hessenberg[i][k];
                                }
                                for(int k=0; k<this.numberOfRows; k++){
                                    this.hessenberg[k][i] += hold*this.hessenberg[k][j];
                                }
                            }
                        }
                    }
                }
            }
            for(int i = 2; i<this.numberOfRows; i++){
                for(int j = 0; j<i-1; j++){
                    this.hessenberg[i][j] = 0.0;
                }
            }
            this.hessenbergDone = true;
        }


	// EIGEN VALUES AND EIGEN VECTORS
    	// For a discussion of eigen systems see
    	// Numerical Recipes, The Art of Scientific Computing
    	// by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
	    // Cambridge University Press,   http://www.nr.com/
	    // These methods follow their approach but modified to an object oriented language

	// Returns the eigen values and eigen vectors of a symmetric matrix
        // Follows the approach of Numerical methods but adapted to object oriented programming (see above)
        private void symmetricEigen(){

            if(!this.isSymmetric())throw new IllegalArgumentException("matrix is not symmetric");
            double[][] amat = this.getArrayCopy();
            this.eigenVector = new double[this.numberOfRows][this.numberOfRows];
            this.eigenValues = new double[this.numberOfRows];
	        double threshold = 0.0D;
	        double cot2rotationAngle = 0.0D;
	        double tanHalfRotationAngle = 0.0D;
	        double offDiagonalSum = 0.0D;
	        double scaledOffDiagonal = 0.0D;
	        double sElement = 0.0D;
	        double cElement = 0.0D;
	        double sOverC = 0.0D;
	        double vectorDifference = 0.0D;
	        double[] holdingVector1 = new double[this.numberOfRows];
	        double[] holdingVector2 = new double[this.numberOfRows];

	        for(int p=0;p<this.numberOfRows;p++){
		        for(int q=0;q<this.numberOfRows;q++) this.eigenVector[p][q] = 0.0;
		        this.eigenVector[p][p] = 1.0;
	        }
	        for(int p=0;p<this.numberOfRows;p++){
		        holdingVector1[p] = amat[p][p];
		        this.eigenValues[p] = amat[p][p];
		        holdingVector2[p] = 0.0;
	        }
	        this.numberOfRotations = 0;
	        for(int i=1;i<=this.maximumJacobiIterations;i++){
		        offDiagonalSum = 0.0;
		        for(int p=0;p<this.numberOfRows-1;p++){
			        for(int q=p+1;q<this.numberOfRows;q++){
			            offDiagonalSum += Math.abs(amat[p][q]);
			        }
		        }
                if(offDiagonalSum==0.0){
                    this.eigenDone = true;
                    this.eigenSort();
                    return;
                }
		        if (i < 4){
			        threshold = 0.2*offDiagonalSum/(this.numberOfRows*this.numberOfRows);
			    }
		        else{
			        threshold = 0.0;
			    }
		        for(int p=0;p<this.numberOfRows-1;p++){
			        for(int q=p+1;q<this.numberOfRows;q++){
				        scaledOffDiagonal = 100.0*Math.abs(amat[p][q]);
				        if (i > 4 && (Math.abs(this.eigenValues[p]) + scaledOffDiagonal) == Math.abs(this.eigenValues[p]) && (Math.abs(this.eigenValues[q]) + scaledOffDiagonal) == Math.abs(this.eigenValues[q])){
				            amat[p][q] = 0.0;
				        }
				        else if(Math.abs(amat[p][q]) > threshold){
					        vectorDifference = this.eigenValues[q] - this.eigenValues[p];
					        if ((Math.abs(vectorDifference) + scaledOffDiagonal) == Math.abs(vectorDifference))
					            sOverC = amat[p][q]/vectorDifference;
					        else{
						        cot2rotationAngle = 0.5*vectorDifference/amat[p][q];
						        sOverC = 1.0/(Math.abs(cot2rotationAngle) + Math.sqrt(1.0 + cot2rotationAngle*cot2rotationAngle));
						        if (cot2rotationAngle < 0.0) sOverC = -sOverC;
					        }
					        cElement = 1.0/Math.sqrt(1.0 + sOverC*sOverC);
					        sElement = sOverC*cElement;
					        tanHalfRotationAngle = sElement/(1.0 + cElement);
					        vectorDifference = sOverC*amat[p][q];
					        holdingVector2[p] -= vectorDifference;
					        holdingVector2[q] += vectorDifference;
			                this.eigenValues[p] -= vectorDifference;
				            this.eigenValues[q] += vectorDifference;
				            amat[p][q] = 0.0;
					        for(int j=0;j<=p-1;j++) rotation(amat, tanHalfRotationAngle, sElement, j, p, j, q);
					        for(int j=p+1;j<=q-1;j++) rotation(amat, tanHalfRotationAngle, sElement, p, j, j, q);
	                        for(int j=q+1;j<this.numberOfRows;j++) rotation(amat, tanHalfRotationAngle, sElement,p, j, q, j);
					        for(int j=0;j<this.numberOfRows;j++) rotation(this.eigenVector, tanHalfRotationAngle, sElement, j, p, j, q);
            			    ++this.numberOfRotations;
			            }
		            }
		        }
		        for(int p=0;p<this.numberOfRows;p++){
			        holdingVector1[p] += holdingVector2[p];
	                this.eigenValues[p] = holdingVector1[p];
		            holdingVector2[p] = 0.0;
	            }
	        }
	        System.out.println("Maximum iterations, " + this.maximumJacobiIterations + ", reached - values at this point returned");
	        this.eigenDone = true;
            this.eigenSort();
	    }

        // matrix rotaion required by symmetricEigen
	    private void rotation(double[][] a, double tau, double sElement, int i, int j, int k, int l){
            double aHold1 = a[i][j];
            double aHold2 = a[k][l];
            a[i][j] = aHold1 - sElement*(aHold2 + aHold1*tau);
	        a[k][l] = aHold2 + sElement*(aHold1 - aHold2*tau);
        }

        // Sorts eigen values into descending order and rearranges eigen vecors to match
        // follows Numerical Recipes (see above)
        private void eigenSort(){
	        int k = 0;
	        double holdingElement;
	        this.sortedEigenValues = Conv.copy(this.eigenValues);
	        this.sortedEigenVector = Conv.copy(this.eigenVector);
	        this.eigenIndices = new int[this.numberOfRows];

	        for(int i=0; i<this.numberOfRows-1; i++){
		        holdingElement = this.sortedEigenValues[i];
		        k = i;
		        for(int j=i+1; j<this.numberOfRows; j++){
			        if (this.sortedEigenValues[j] >= holdingElement){
			            holdingElement = this.sortedEigenValues[j];
			            k = j;
			        }
			    }
		        if (k != i){
			        this.sortedEigenValues[k] = this.sortedEigenValues[i];
			        this.sortedEigenValues[i] = holdingElement;

			        for(int j=0; j<this.numberOfRows; j++){
				        holdingElement = this.sortedEigenVector[j][i];
				        this.sortedEigenVector[j][i] = this.sortedEigenVector[j][k];
				        this.sortedEigenVector[j][k] = holdingElement;
		            }
	            }
            }
            this.eigenIndices = new int[this.numberOfRows];
            for(int i=0; i<this.numberOfRows; i++){
                boolean test = true;
                int j = 0;
                while(test){
                    if(this.sortedEigenValues[i]==this.eigenValues[j]){
                        this.eigenIndices[i] = j;
                        test = false;
                    }
                    else{
                        j++;
                    }
                }
            }
        }


	// Method not in java.lang.maths required in this Class
    	// See Fmath.class for public versions of this method
    	private static double hypot(double aa, double bb){
        	double cc = 0.0D, ratio = 0.0D;
        	double amod=Math.abs(aa);
        	double bmod=Math.abs(bb);

        	if(amod==0.0D){
         	   	cc=bmod;
        	}
        	else{
            		if(bmod==0.0D){
                		cc=amod;
            		}
            		else{
                		if(amod<=bmod){
                    			ratio=amod/bmod;
                    			cc=bmod*Math.sqrt(1.0D+ratio*ratio);
                		}
                		else{
                    			ratio=bmod/amod;
                    			cc=amod*Math.sqrt(1.0D+ratio*ratio);
                		}
            		}
        	}
        	return cc;
    	}

}




