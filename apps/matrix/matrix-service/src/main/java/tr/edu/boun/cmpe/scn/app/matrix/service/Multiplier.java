package tr.edu.boun.cmpe.scn.app.matrix.service;

import tr.edu.boun.cmpe.scn.api.common.Tool;

import java.util.Random;

/**
 * Created by esinka on 3/22/2017.
 */
public class Multiplier {
    private static final Random random = new Random();

    private static byte getNext() {
        //return (long) Tool.generateRandomInteger(100, 5000, random);
        return (byte)Tool.getRandomlyFromRange(Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public static byte[][] multiplyMatrices(int dimension) {
        int aRows = dimension;
        int aColumns = dimension;
        int bRows = dimension;
        int bColumns = dimension;

        //init A
        byte[][] A = new byte[aRows][bColumns];
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                A[i][j] = getNext();
            }
        }
        //init B
        /*byte[][] B = new byte[aRows][bColumns];
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                B[i][j] = getNext();
            }
        }*/

        byte[][] C = new byte[aRows][bColumns];

        //do the multiplication 10000 time for testing purposes
        for (int x = 0; x < 1; x++) {
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    C[i][j] = 0;
                }
            }

            for (int i = 0; i < aRows; i++) { // aRow
                for (int j = 0; j < bColumns; j++) { // bColumn
                    for (int k = 0; k < aColumns; k++) { // aColumn
                        //C[i][j] += A[i][k] * B[k][j];
                        C[i][j] += A[i][k] * A[k][j];
                    }
                }
            }
        }


        A = null;
        return C;
    }

    public static void main(String[] args) {
        int dimension = 10;
        out(5);
        out(5);
        out(5);
    }

    private static void out (int dimension) {
        byte[][] result = multiplyMatrices(dimension);

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                System.out.print(result[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("----------------------------");
    }
}
