package at.fhooe.mc.emg.core.util.filter.sg

internal class Matrix {

    private var numberOfRows = 0                   // number of rows
    private var numberOfColumns = 0                // number of columns
    private var arrayReference: Array<DoubleArray>                // 2-D  Matrix as double
    private var matrixS: Array<Array<String>>               // 2-D  Matrix as String

    private var permutationIndex: IntArray         // row permutation index
    private var rowSwapIndex = 1.0             // row swap index

    val arrayCopy: Array<DoubleArray>
        get() {
            val c = Array(this.numberOfRows) { DoubleArray(this.numberOfColumns) }
            for (i in 0 until numberOfRows) {
                System.arraycopy(arrayReference[i], 0, c[i], 0, numberOfColumns)
            }
            return c
        }

    private constructor(numberOfRows: Int, numberOfColumns: Int) {
        this.numberOfRows = numberOfRows
        this.numberOfColumns = numberOfColumns
        arrayReference = Array(this.numberOfRows) { DoubleArray(this.numberOfColumns) }
        matrixS = Array(numberOfRows) { Array(numberOfColumns) {_ -> ""} }
        permutationIndex = IntArray(this.numberOfRows)
        for (i in 0 until this.numberOfRows) {
            permutationIndex.set(i, i)
            for (j in 0 until this.numberOfColumns) this.matrixS[i][j] = "0.0"
        }
    }

    constructor(twoD: Array<DoubleArray>) {
        numberOfRows = twoD.size
        numberOfColumns = twoD[0].size
        arrayReference = Array(this.numberOfRows) { DoubleArray(this.numberOfColumns) }
        matrixS = Array(numberOfRows) { Array(numberOfColumns) {_ -> ""} }
        permutationIndex = IntArray(this.numberOfRows)
        for (i in 0 until this.numberOfRows) {
            permutationIndex[i] = i
            for (j in 0 until this.numberOfColumns) {
                this.arrayReference[i][j] = twoD[i][j]
                this.matrixS[i][j] = convertDoubleToString(twoD[i][j])
            }
        }
    }

    private fun convertDoubleToString(x: Double): String {
        return java.lang.Double.toString(x).trim { it <= ' ' }
    }

    private fun copy(a: Matrix?): Matrix? {
        if (a == null) {
            return null
        } else {
            val nr = a.numberOfRows
            val nc = a.numberOfColumns
            val aArray = a.arrayReference
            val b = Matrix(nr, nc)
            b.numberOfRows = nr
            b.numberOfColumns = nc
            val bArray = b.arrayReference
            for (i in 0 until nr) {
                System.arraycopy(aArray[i], 0, bArray[i], 0, nc)
            }
            System.arraycopy(a.permutationIndex, 0, b.permutationIndex, 0, nr)
            return b
        }
    }

    fun clone(): Any {
        val nr = this.numberOfRows
        val nc = this.numberOfColumns
        val b = Matrix(nr, nc)
        val barray = b.arrayReference
        b.numberOfRows = nr
        b.numberOfColumns = nc
        for (i in 0 until nr) {
            System.arraycopy(this.arrayReference.get(i), 0, barray[i], 0, nc)
        }
        System.arraycopy(permutationIndex, 0, b.permutationIndex, 0, nr)
        return b
    }

    operator fun times(bmat: Matrix): Matrix {
        if (this.numberOfColumns != bmat.numberOfRows) throw IllegalArgumentException("Nonconformable matrices")

        val cmat = Matrix(this.numberOfRows, bmat.numberOfColumns)
        val carray = cmat.arrayReference
        var sum: Double

        for (i in 0 until this.numberOfRows) {
            for (j in 0 until bmat.numberOfColumns) {
                sum = (0 until this.numberOfColumns).sumByDouble { this.arrayReference[i][it] * bmat.arrayReference[it][j] }
                carray[i][j] = sum
            }
        }
        return cmat
    }

    fun inverse(): Matrix {
        val n = this.numberOfRows
        if (n != this.numberOfColumns) throw IllegalArgumentException("Matrix is not square")
        var invmat = Matrix(n, n)

        if (n == 1) {
            val hold = this.arrayCopy
            if (hold[0][0] == 0.0) throw IllegalArgumentException("Matrix is singular")
            hold[0][0] = 1.0 / hold[0][0]
            invmat = Matrix(hold)
        } else {
            if (n == 2) {
                val hold = this.arrayCopy
                val det = hold[0][0] * hold[1][1] - hold[0][1] * hold[1][0]
                if (det == 0.0) throw IllegalArgumentException("Matrix is singular")
                val hold2 = Array(2) { DoubleArray(2) }
                hold2[0][0] = hold[1][1] / det
                hold2[1][1] = hold[0][0] / det
                hold2[1][0] = -hold[1][0] / det
                hold2[0][1] = -hold[0][1] / det
                invmat = Matrix(hold2)
            } else {
                val col = DoubleArray(n)
                var xvec: DoubleArray
                val invArray = invmat.arrayReference
                val ludMat: Matrix = this.luDeComp()

                for (j in 0 until n) {
                    for (i in 0 until n) col[i] = 0.0
                    col[j] = 1.0
                    xvec = ludMat.luBackSub(col)
                    for (i in 0 until n) invArray[i][j] = xvec[i]
                }
            }
        }
        return invmat
    }

    fun transpose(): Matrix {
        val tMat = Matrix(this.numberOfColumns, this.numberOfRows)
        val tArray = tMat.arrayReference
        for (i in 0 until this.numberOfColumns) {
            for (j in 0 until this.numberOfRows) {
                tArray[i][j] = this.arrayReference[j][i]
            }
        }
        return tMat
    }

    private fun luDeComp(): Matrix {
        if (this.numberOfRows != this.numberOfColumns) throw IllegalArgumentException("A matrix is not square")
        val n = this.numberOfRows
        var imax = 0
        var dum: Double
        var temp: Double
        var big: Double
        val vv = DoubleArray(n)
        var sum: Double
        var dumm: Double

        val ludmat = copy(this)
        val ludarray = ludmat!!.arrayReference

        ludmat.rowSwapIndex = 1.0
        for (i in 0 until n) {
            big = 0.0
            for (j in 0 until n){
                temp = Math.abs(ludarray[i][j])
                if (temp > big){
                    big = temp
                }
            }
            if (big == 0.0) {
                for (k in 0 until n) for (j in 0 until n) ludarray[k][j] = java.lang.Double.NaN
                return ludmat
            }
            vv[i] = 1.0 / big
        }
        for (j in 0 until n) {
            for (i in 0 until j) {
                sum = ludarray[i][j]
                for (k in 0 until i) sum -= ludarray[i][k] * ludarray[k][j]
                ludarray[i][j] = sum
            }
            big = 0.0
            for (i in j until n) {
                sum = ludarray[i][j]
                for (k in 0 until j)
                    sum -= ludarray[i][k] * ludarray[k][j]
                ludarray[i][j] = sum
                dum = vv[i] * Math.abs(sum)
                if (dum >= big) {
                    big = dum
                    imax = i
                }
            }
            if (j != imax) {
                for (k in 0 until n) {
                    dumm = ludarray[imax][k]
                    ludarray[imax][k] = ludarray[j][k]
                    ludarray[j][k] = dumm
                }
                ludmat.rowSwapIndex = -ludmat.rowSwapIndex
                vv[imax] = vv[j]
            }
            ludmat.permutationIndex[j] = imax

            if (ludarray[j][j] == 0.0) {
                val tiny = 1.0e-100
                ludarray[j][j] = tiny
            }
            if (j != n - 1) {
                dumm = 1.0 / ludarray[j][j]
                for (i in j + 1 until n) {
                    val ludNew = ludarray[i][j] * dumm
                    ludarray[i][j] = ludNew
                }
            }
        }
        return ludmat
    }

    private fun luBackSub(bvec: DoubleArray): DoubleArray {
        var ii = 0
        var ip: Int
        val n = bvec.size
        if (n != this.numberOfColumns)
            throw IllegalArgumentException("vector length is not equal to matrix dimension")
        if (this.numberOfColumns != this.numberOfRows) throw IllegalArgumentException("matrix is not square")
        var sum: Double
        val xvec = DoubleArray(n)
        System.arraycopy(bvec, 0, xvec, 0, n)
        for (i in 0 until n) {
            ip = this.permutationIndex[i]
            sum = xvec[ip]
            xvec[ip] = xvec[i]
            if (ii == 0) {
                for (j in ii until i) {
                    sum -= this.arrayReference[i][j] * xvec[j]
                }
            } else {
                if (sum == 0.0) ii = i
            }
            xvec[i] = sum
        }
        for (i in n - 1 downTo 0) {
            sum = xvec[i]
            for (j in i + 1 until n) {
                sum -= this.arrayReference[i][j] * xvec[j]
            }
            xvec[i] = sum / arrayReference[i][i]
        }
        return xvec
    }

}