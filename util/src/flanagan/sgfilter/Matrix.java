package flanagan.sgfilter;

class Matrix {

    private int numberOfRows = 0;                   // number of rows
    private int numberOfColumns = 0;                // number of columns
    private double[][] matrix = null;                // 2-D  Matrix as double
    private String[][] matrixS = null;                // 2-D  Matrix as String

    private int permutationIndex[] = null;          // row permutation index
    private double rowSwapIndex = 1.0D;             // row swap index

    private Matrix(int numberOfRows, int numberOfColumns) {
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        this.matrix = new double[this.numberOfRows][this.numberOfColumns];
        this.matrixS = new String[this.numberOfRows][this.numberOfColumns];
        this.permutationIndex = new int[this.numberOfRows];
        for (int i = 0; i < this.numberOfRows; i++) {
            this.permutationIndex[i] = i;
            for (int j = 0; j < this.numberOfColumns; j++) this.matrixS[i][j] = "0.0";
        }
    }

    Matrix(double[][] twoD) {
        this.numberOfRows = twoD.length;
        this.numberOfColumns = twoD[0].length;
        this.matrix = new double[this.numberOfRows][this.numberOfColumns];
        this.matrixS = new String[this.numberOfRows][this.numberOfColumns];
        this.permutationIndex = new int[this.numberOfRows];
        for (int i = 0; i < this.numberOfRows; i++) {
            this.permutationIndex[i] = i;
            for (int j = 0; j < this.numberOfColumns; j++) {
                this.matrix[i][j] = twoD[i][j];
                this.matrixS[i][j] = convertDoubleToString(twoD[i][j]);
            }
        }
    }

    private int getNumberOfRows() {
        return this.numberOfRows;
    }

    private int getNumberOfColumns() {
        return this.numberOfColumns;
    }

    private double[][] getArrayReference() {
        return this.matrix;
    }

    double[][] getArrayCopy() {
        double[][] c = new double[this.numberOfRows][this.numberOfColumns];
        for (int i = 0; i < numberOfRows; i++) {
            System.arraycopy(this.matrix[i], 0, c[i], 0, numberOfColumns);
        }
        return c;
    }

    private String convertDoubleToString(double x) {
        return Double.toString(x).trim();
    }

    private static Matrix copy(Matrix a) {
        if (a == null) {
            return null;
        } else {
            int nr = a.getNumberOfRows();
            int nc = a.getNumberOfColumns();
            double[][] aarray = a.getArrayReference();
            Matrix b = new Matrix(nr, nc);
            b.numberOfRows = nr;
            b.numberOfColumns = nc;
            double[][] barray = b.getArrayReference();
            for (int i = 0; i < nr; i++) {
                System.arraycopy(aarray[i], 0, barray[i], 0, nc);
            }
            System.arraycopy(a.permutationIndex, 0, b.permutationIndex, 0, nr);
            return b;
        }
    }

    public Object clone() {
        int nr = this.numberOfRows;
        int nc = this.numberOfColumns;
        Matrix b = new Matrix(nr, nc);
        double[][] barray = b.getArrayReference();
        b.numberOfRows = nr;
        b.numberOfColumns = nc;
        for (int i = 0; i < nr; i++) {
            System.arraycopy(this.matrix[i], 0, barray[i], 0, nc);
        }
        System.arraycopy(this.permutationIndex, 0, b.permutationIndex, 0, nr);
        return b;
    }

    Matrix times(Matrix bmat) {
        if (this.numberOfColumns != bmat.numberOfRows) throw new IllegalArgumentException("Nonconformable matrices");

        Matrix cmat = new Matrix(this.numberOfRows, bmat.numberOfColumns);
        double[][] carray = cmat.getArrayReference();
        double sum;

        for (int i = 0; i < this.numberOfRows; i++) {
            for (int j = 0; j < bmat.numberOfColumns; j++) {
                sum = 0.0D;
                for (int k = 0; k < this.numberOfColumns; k++) {
                    sum += this.matrix[i][k] * bmat.matrix[k][j];
                }
                carray[i][j] = sum;
            }
        }
        return cmat;
    }

    Matrix inverse() {
        int n = this.numberOfRows;
        if (n != this.numberOfColumns) throw new IllegalArgumentException("Matrix is not square");
        Matrix invmat = new Matrix(n, n);

        if (n == 1) {
            double[][] hold = this.getArrayCopy();
            if (hold[0][0] == 0.0) throw new IllegalArgumentException("Matrix is singular");
            hold[0][0] = 1.0 / hold[0][0];
            invmat = new Matrix(hold);
        } else {
            if (n == 2) {
                double[][] hold = this.getArrayCopy();
                double det = hold[0][0] * hold[1][1] - hold[0][1] * hold[1][0];
                if (det == 0.0) throw new IllegalArgumentException("Matrix is singular");
                double[][] hold2 = new double[2][2];
                hold2[0][0] = hold[1][1] / det;
                hold2[1][1] = hold[0][0] / det;
                hold2[1][0] = -hold[1][0] / det;
                hold2[0][1] = -hold[0][1] / det;
                invmat = new Matrix(hold2);
            } else {
                double[] col = new double[n];
                double[] xvec = new double[n];
                double[][] invarray = invmat.getArrayReference();
                Matrix ludmat;

                ludmat = this.luDecomp();
                for (int j = 0; j < n; j++) {
                    for (int i = 0; i < n; i++) col[i] = 0.0D;
                    col[j] = 1.0;
                    xvec = ludmat.luBackSub(col);
                    for (int i = 0; i < n; i++) invarray[i][j] = xvec[i];
                }
            }
        }
        return invmat;
    }

    Matrix transpose() {
        Matrix tmat = new Matrix(this.numberOfColumns, this.numberOfRows);
        double[][] tarray = tmat.getArrayReference();
        for (int i = 0; i < this.numberOfColumns; i++) {
            for (int j = 0; j < this.numberOfRows; j++) {
                tarray[i][j] = this.matrix[j][i];
            }
        }
        return tmat;
    }

    private Matrix luDecomp() {
        if (this.numberOfRows != this.numberOfColumns) throw new IllegalArgumentException("A matrix is not square");
        int n = this.numberOfRows;
        int imax = 0;
        double dum, temp, big;
        double[] vv = new double[n];
        double sum;
        double dumm;

        Matrix ludmat = Matrix.copy(this);
        double[][] ludarray = ludmat.getArrayReference();

        ludmat.rowSwapIndex = 1.0D;
        for (int i = 0; i < n; i++) {
            big = 0.0D;
            for (int j = 0; j < n; j++) if ((temp = Math.abs(ludarray[i][j])) > big) big = temp;
            if (big == 0.0D) {
                for (int k = 0; k < n; k++) for (int j = 0; j < n; j++) ludarray[k][j] = Double.NaN;
                return ludmat;
            }
            vv[i] = 1.0 / big;
        }
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < j; i++) {
                sum = ludarray[i][j];
                for (int k = 0; k < i; k++) sum -= ludarray[i][k] * ludarray[k][j];
                ludarray[i][j] = sum;
            }
            big = 0.0D;
            for (int i = j; i < n; i++) {
                sum = ludarray[i][j];
                for (int k = 0; k < j; k++)
                    sum -= ludarray[i][k] * ludarray[k][j];
                ludarray[i][j] = sum;
                if ((dum = vv[i] * Math.abs(sum)) >= big) {
                    big = dum;
                    imax = i;
                }
            }
            if (j != imax) {
                for (int k = 0; k < n; k++) {
                    dumm = ludarray[imax][k];
                    ludarray[imax][k] = ludarray[j][k];
                    ludarray[j][k] = dumm;
                }
                ludmat.rowSwapIndex = -ludmat.rowSwapIndex;
                vv[imax] = vv[j];
            }
            ludmat.permutationIndex[j] = imax;

            if (ludarray[j][j] == 0.0D) {
                double tiny = 1.0e-100;
                ludarray[j][j] = tiny;
            }
            if (j != n - 1) {
                dumm = 1.0 / ludarray[j][j];
                for (int i = j + 1; i < n; i++) {
                    ludarray[i][j] *= dumm;
                }
            }
        }
        return ludmat;
    }

    private double[] luBackSub(double[] bvec) {
        int ii = 0, ip;
        int n = bvec.length;
        if (n != this.numberOfColumns)
            throw new IllegalArgumentException("vector length is not equal to matrix dimension");
        if (this.numberOfColumns != this.numberOfRows) throw new IllegalArgumentException("matrix is not square");
        double sum;
        double[] xvec = new double[n];
        System.arraycopy(bvec, 0, xvec, 0, n);
        for (int i = 0; i < n; i++) {
            ip = this.permutationIndex[i];
            sum = xvec[ip];
            xvec[ip] = xvec[i];
            if (ii == 0) {
                for (int j = ii; j <= i - 1; j++) {
                    sum -= this.matrix[i][j] * xvec[j];
                }
            } else {
                if (sum == 0.0) ii = i;
            }
            xvec[i] = sum;
        }
        for (int i = n - 1; i >= 0; i--) {
            sum = xvec[i];
            for (int j = i + 1; j < n; j++) {
                sum -= this.matrix[i][j] * xvec[j];
            }
            xvec[i] = sum / matrix[i][i];
        }
        return xvec;
    }

}