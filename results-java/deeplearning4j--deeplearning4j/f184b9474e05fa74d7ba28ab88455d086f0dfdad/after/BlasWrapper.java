package org.deeplearning4j.linalg.jblas;

import org.deeplearning4j.linalg.api.complex.IComplexFloat;
import org.deeplearning4j.linalg.jblas.complex.ComplexFloat;
import org.deeplearning4j.linalg.jblas.complex.ComplexNDArray;
import org.jblas.JavaBlas;
import org.jblas.NativeBlas;
import org.jblas.exceptions.*;


import static org.jblas.util.Functions.log2;
import static org.jblas.util.Functions.max;
import static org.jblas.util.Functions.min;

/**
 * Copy of SimpleBlas to handle offsets implementing
 * an interface for library neutral
 * jblas operations
 * @author Adam Gibson
 */
public class BlasWrapper implements org.deeplearning4j.linalg.factory.BlasWrapper<NDArray,ComplexNDArray,ComplexFloat> {
    /***************************************************************************
     * BLAS Level 1
     */

    /**
     * Compute x <-> y (swap two matrices)
     */
    @Override
    public NDArray swap(NDArray x, NDArray y) {
        //NativeBlas.dswap(x.length(), x.data(), 0, 1, y.data(), 0, 1);
        JavaBlas.rswap(x.length(), x.data(), 0, 1, y.data(), 0, 1);
        return y;
    }

    /**
     * Compute x <- alpha * x (scale a matrix)
     */
    @Override
    public NDArray scal(float alpha, NDArray x) {
        NativeBlas.sscal(x.length(), alpha, x.data(), 0, 1);
        return x;
    }

    @Override
    public ComplexNDArray scal(ComplexFloat alpha, ComplexNDArray x) {
        NativeBlas.cscal(x.length(),
                new ComplexFloat(alpha.realComponent(), alpha.imaginaryComponent()),
                x.data(), x.offset(), 1);
        return x;
    }




    /**
     * Compute y <- x (copy a matrix)
     */
    @Override
    public NDArray copy(NDArray x, NDArray y) {
        //NativeBlas.dcopy(x.length(), x.data(), 0, 1, y.data(), 0, 1);
        JavaBlas.rcopy(x.length(), x.data(), 0, 1, y.data(), 0, 1);
        return y;
    }

    @Override
    public ComplexNDArray copy(ComplexNDArray x, ComplexNDArray y) {
        NativeBlas.scopy(x.length(), x.data(), 0, 1, y.data(), 0, 1);
        return y;
    }

    /**
     * Compute y <- alpha * x + y (elementwise addition)
     */
    @Override
    public NDArray axpy(float da, NDArray dx, NDArray dy) {
        //NativeBlas.daxpy(dx.length(), da, dx.data(), 0, 1, dy.data(), 0, 1);
        JavaBlas.raxpy(dx.length(), da, dx.data(), 0, 1, dy.data(), 0, 1);

        return dy;
    }

    @Override
    public ComplexNDArray axpy(ComplexFloat da, ComplexNDArray dx, ComplexNDArray dy) {
        NativeBlas.caxpy(dx.length(), new org.jblas.ComplexFloat(da.realComponent(), da.imaginaryComponent()), dx.data(), 0, 1, dy.data(), 0, 1);
        return dy;
    }



    /**
     * Compute x^T * y (dot product)
     */
    @Override
    public float dot(NDArray x, NDArray y) {
        //return NativeBlas.ddot(x.length(), x.data(), 0, 1, y.data(), 0, 1);
        return JavaBlas.rdot(x.length(), x.data(), 0, 1, y.data(), 0, 1);
    }

    /**
     * Compute x^T * y (dot product)
     */
    public ComplexFloat dotc(ComplexNDArray x, ComplexNDArray y) {
        return new ComplexFloat(NativeBlas.cdotc(x.length(), x.data(), 0, 1, y.data(), 0, 1));
    }

    /**
     * Compute x^T * y (dot product)
     */
    @Override
    public ComplexFloat dotu(ComplexNDArray x, ComplexNDArray y) {
        return new ComplexFloat(NativeBlas.cdotu(x.length(), x.data(), 0, 1, y.data(), 0, 1));
    }

    /**
     * Compute || x ||_2 (2-norm)
     */
    @Override
    public double nrm2(NDArray x) {
        return NativeBlas.snrm2(x.length(), x.data(), 0, 1);
    }
    @Override
    public double nrm2(ComplexNDArray x) {
        return NativeBlas.scnrm2(x.length(), x.data(), 0, 1);
    }

    /**
     * Compute || x ||_1 (1-norm, sum of absolute values)
     */
    @Override
    public double asum(NDArray x) {
        return NativeBlas.sasum(x.length(), x.data(), 0, 1);
    }

    @Override
    public double asum(ComplexNDArray x) {
        return NativeBlas.scasum(x.length(), x.data(), 0, 1);
    }

    /**
     * Compute index of element with largest absolute value (index of absolute
     * value maximum)
     */
    @Override
    public int iamax(NDArray x) {
        return NativeBlas.isamax(x.length(), x.data(), 0, 1) - 1;
    }

    /**
     * Compute index of element with largest absolute value (complex version).
     *
     * @param x matrix
     * @return index of element with largest absolute value.
     */
    @Override
    public int iamax(ComplexNDArray x) {
        return NativeBlas.icamax(x.length(), x.data(), 0, 1) - 1;
    }

    /***************************************************************************
     * BLAS Level 2
     */

    /**
     * Compute y <- alpha*op(a)*x + beta * y (general matrix vector
     * multiplication)
     */
    @Override
    public NDArray gemv(float alpha, NDArray a,
                        NDArray x, float beta, NDArray y) {
        if (false) {
            NativeBlas.sgemv('N', a.rows(), a.columns(), alpha, a.data(), 0, a.rows(), x.data(), 0,
                    1, beta, y.data(), 0, 1);
        } else {
            if (beta == 0.0) {
                for (int j = 0; j < a.columns(); j++) {
                    double xj = x.get(j);
                    if (xj != 0.0) {
                        for (int i = 0; i < a.rows(); i++) {
                            y.putScalar(i,y.get(i) + a.get(i,j) * xj);
                        }
                    }
                }
            } else {
                for (int j = 0; j < a.columns(); j++) {
                    double byj = beta * y.data()[j];
                    double xj = x.get(j);
                    for (int i = 0; i < a.rows(); i++) {
                        y.putScalar(j,a.get(i,j) * xj + byj);
                    }
                }
            }
        }
        return y;
    }

    /**
     * Compute A <- alpha * x * y^T + A (general rank-1 update)
     */
    @Override
    public NDArray ger(float alpha, NDArray x,
                       NDArray y, NDArray a) {
        NativeBlas.sger(a.rows(), a.columns(), alpha, x.data(), 0, 1, y.data(), 0, 1, a.data(),
                0, a.rows());
        return a;
    }

    /**
     * Compute A <- alpha * x * y^T + A (general rank-1 update)
     *
     * @param alpha
     * @param x
     * @param y
     * @param a
     */
    @Override
    public ComplexNDArray geru(ComplexFloat alpha, ComplexNDArray x, ComplexNDArray y, ComplexNDArray a) {
        NativeBlas.cgeru(a.rows(), a.columns(),
                new ComplexFloat(alpha.realComponent(), alpha.imaginaryComponent()),
                x.data(), x.offset(), 1, y.data(), y.offset(), 1, a.data(),
                a.offset(), a.rows());
        return a;
    }





    /**
     * Compute A <- alpha * x * y^H + A (general rank-1 update)
     */
    @Override
    public ComplexNDArray gerc(ComplexFloat alpha, ComplexNDArray x,
                               ComplexNDArray y, ComplexNDArray a) {
        NativeBlas.cgerc(a.rows(), a.columns(), alpha, x.data(), 0, 1, y.data(), 0, 1, a.data(),
                0, a.rows());
        return a;
    }

    /***************************************************************************
     * BLAS Level 3
     */

    /**
     * Compute c <- a*b + beta * c (general matrix matrix
     * multiplication)
     */
    @Override
    public NDArray gemm(float alpha, NDArray a,
                        NDArray b, float beta, NDArray c) {
        NativeBlas.sgemm('N', 'N', c.rows(), c.columns(), a.columns(), alpha, a.data(), 0,
                a.rows(), b.data(), 0, b.rows(), beta, c.data(), 0, c.rows());

        return c;
    }



    @Override
    public ComplexNDArray gemm(ComplexFloat alpha, ComplexNDArray a, ComplexNDArray b, ComplexFloat beta, ComplexNDArray c) {
        NativeBlas.cgemm(
                'N',
                'N',
                c.rows(),
                c.columns(),
                a.columns(),
                new ComplexFloat(alpha.realComponent().floatValue(), alpha.imaginaryComponent().floatValue()),
                a.data(), a.offset() / 2, a.rows(),
                b.data(), b.offset() / 2, b.rows(),
                new ComplexFloat(beta.realComponent().floatValue(), beta.imaginaryComponent().floatValue())
                , c.data(), c.offset() / 2, c.rows());
        return c;

    }



    /***************************************************************************
     * LAPACK
     */

    public NDArray gesv(NDArray a, int[] ipiv,
                        NDArray b) {
        int info = NativeBlas.sgesv(a.rows(), b.columns(), a.data(), 0, a.rows(), ipiv, 0,
                b.data(), 0, b.rows());
        checkInfo("DGESV", info);

        if (info > 0)
            throw new LapackException("DGESV",
                    "Linear equation cannot be solved because the matrix was singular.");

        return b;
    }

//STOP

    public  void checkInfo(String name, int info) {
        if (info < -1)
            throw new LapackArgumentException(name, info);
    }
//START

    public NDArray sysv(char uplo, NDArray a, int[] ipiv,
                        NDArray b) {
        int info = NativeBlas.ssysv(uplo, a.rows(), b.columns(), a.data(), 0, a.rows(), ipiv, 0,
                b.data(), 0, b.rows());
        checkInfo("SYSV", info);

        if (info > 0)
            throw new LapackSingularityException("SYV",
                    "Linear equation cannot be solved because the matrix was singular.");

        return b;
    }

    public int syev(char jobz, char uplo, NDArray a, NDArray w) {
        int info = NativeBlas.ssyev(jobz, uplo, a.rows(), a.data(), 0, a.rows(), w.data(), 0);

        if (info > 0)
            throw new LapackConvergenceException("SYEV",
                    "Eigenvalues could not be computed " + info
                            + " off-diagonal elements did not converge");

        return info;
    }

    @Override
    public int syevx(char jobz, char range, char uplo, NDArray a,
                     float vl, float vu, int il, int iu, float abstol,
                     NDArray w, NDArray z) {
        int n = a.rows();
        int[] iwork = new int[5 * n];
        int[] ifail = new int[n];
        int[] m = new int[1];
        int info;

        info = NativeBlas.ssyevx(jobz, range, uplo, n, a.data(), 0, a.rows(), vl, vu, il,
                iu, abstol, m, 0, w.data(), 0, z.data(), 0, z.rows(), iwork, 0, ifail, 0);

        if (info > 0) {
            StringBuilder msg = new StringBuilder();
            msg
                    .append("Not all eigenvalues converged. Non-converging eigenvalues were: ");
            for (int i = 0; i < info; i++) {
                if (i > 0)
                    msg.append(", ");
                msg.append(ifail[i]);
            }
            msg.append(".");
            throw new LapackConvergenceException("SYEVX", msg.toString());
        }

        return info;
    }

    public int syevd(char jobz, char uplo, NDArray A,
                     NDArray w) {
        int n = A.rows();

        int info = NativeBlas.ssyevd(jobz, uplo, n, A.data(), 0, A.rows(), w.data(), 0);

        if (info > 0)
            throw new LapackConvergenceException("SYEVD", "Not all eigenvalues converged.");

        return info;
    }

    @Override
    public int syevr(char jobz, char range, char uplo, NDArray a,
                     float vl, float vu, int il, int iu, float abstol,
                     NDArray w, NDArray z, int[] isuppz) {
        int n = a.rows();
        int[] m = new int[1];

        int info = NativeBlas.ssyevr(jobz, range, uplo, n, a.data(), 0, a.rows(), vl, vu,
                il, iu, abstol, m, 0, w.data(), 0, z.data(), 0, z.rows(), isuppz, 0);

        checkInfo("SYEVR", info);

        return info;
    }

    @Override
    public void posv(char uplo, NDArray A, NDArray B) {
        int n = A.rows();
        int nrhs = B.columns();
        int info = NativeBlas.sposv(uplo, n, nrhs, A.data(), 0, A.rows(), B.data(), 0,
                B.rows());
        checkInfo("DPOSV", info);
        if (info > 0)
            throw new LapackArgumentException("DPOSV",
                    "Leading minor of order i of A is not positive definite.");
    }

    @Override
    public int geev(char jobvl, char jobvr, NDArray A,
                    NDArray WR, NDArray WI, NDArray VL, NDArray VR) {
        int info = NativeBlas.sgeev(jobvl, jobvr, A.rows(), A.data(), 0, A.rows(), WR.data(), 0,
                WI.data(), 0, VL.data(), 0, VL.rows(), VR.data(), 0, VR.rows());
        if (info > 0)
            throw new LapackConvergenceException("DGEEV", "First " + info + " eigenvalues have not converged.");
        return info;
    }

    @Override
    public int sygvd(int itype, char jobz, char uplo, NDArray A, NDArray B, NDArray W) {
        int info = NativeBlas.ssygvd(itype, jobz, uplo, A.rows(), A.data(), 0, A.rows(), B.data(), 0, B.rows(), W.data(), 0);
        if (info == 0)
            return 0;
        else {
            if (info < 0)
                throw new LapackArgumentException("DSYGVD", -info);
            if (info <= A.rows() && jobz == 'N')
                throw new LapackConvergenceException("DSYGVD", info + " off-diagonal elements did not converge to 0.");
            if (info <= A.rows() && jobz == 'V')
                throw new LapackException("DSYGVD", "Failed to compute an eigenvalue while working on a sub-matrix  " + info + ".");
            else
                throw new LapackException("DSYGVD", "The leading minor of order " + (info - A.rows()) + " of B is not positive definite.");
        }
    }

    /**
     * Generalized Least Squares via *GELSD.
     *
     * Note that B must be padded to contain the solution matrix. This occurs when A has fewer rows
     * than columns.
     *
     * For example: in A * X = B, A is (m,n), X is (n,k) and B is (m,k). Now if m < n, since B is overwritten to contain
     * the solution (in classical LAPACK style), B needs to be padded to be an (n,k) matrix.
     *
     * Likewise, if m > n, the solution consists only of the first n rows of B.
     *
     * @param A an (m,n) matrix
     * @param B an (max(m,n), k) matrix (well, at least)
     */
    public void gelsd(NDArray A, NDArray B) {
        int m = A.rows();
        int n = A.columns();
        int nrhs = B.columns();
        int minmn = min(m, n);
        int maxmn = max(m, n);

        if (B.rows() < maxmn) {
            throw new SizeException("Result matrix B must be padded to contain the solution matrix X!");
        }

        int smlsiz = NativeBlas.ilaenv(9, "DGELSD", "", m, n, nrhs, 0);
        int nlvl = max(0, (int) log2(minmn/ (smlsiz+1)) + 1);

//      System.err.printf("GELSD\n");
//      System.err.printf("m = %d, n = %d, nrhs = %d\n", m, n, nrhs);
//      System.err.printf("smlsiz = %d, nlvl = %d\n", smlsiz, nlvl);
//      System.err.printf("iwork size = %d\n", 3 * minmn * nlvl + 11 * minmn);

        int[] iwork = new int[3 * minmn * nlvl + 11 * minmn];
        float[] s = new float[minmn];
        int[] rank = new int[1];
        int info = NativeBlas.sgelsd(m, n, nrhs, A.data(), 0, m, B.data(), 0, B.rows(), s, 0, -1, rank, 0, iwork, 0);
        if (info == 0) {
            return;
        } else if (info < 0) {
            throw new LapackArgumentException("DGESD", -info);
        } else if (info > 0) {
            throw new LapackConvergenceException("DGESD", info + " off-diagonal elements of an intermediat bidiagonal form did not converge to 0.");
        }
    }

    @Override
    public void geqrf(NDArray A, NDArray tau) {
        int info = NativeBlas.sgeqrf(A.rows(), A.columns(), A.data(), 0, A.rows(), tau.data(), 0);
        checkInfo("GEQRF", info);
    }

    @Override
    public void ormqr(char side, char trans, NDArray A, NDArray tau, NDArray C) {
        int k = tau.length();
        int info = NativeBlas.sormqr(side, trans, C.rows(), C.columns(), k, A.data(), 0, A.rows(), tau.data(), 0, C.data(), 0, C.rows());
        checkInfo("ORMQR", info);
    }

    @Override
    public void dcopy(int n, float[] dx, int dxIdx, int incx, float[] dy, int dyIdx, int incy) {
        NativeBlas.scopy(n,dx,dxIdx,incx,dy,dyIdx,incy);
    }

}