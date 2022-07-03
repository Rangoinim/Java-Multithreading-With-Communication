//******************************************************************************
//
//  Developer:     Cory Munselle
//
//  Project #:     Project 5
//
//  File Name:     CalculateValues.java
//
//  Course:        COSC 4301 - Modern Programming
//
//  Due Date:      3/13/2022
//
//  Instructor:    Fred Kumi
//
//  Description:   Does the calculations for sum, mean, and standard deviation.
//                 Now with multithreading!
//
//  Notes:         The idea to have one class for all three calculations
//                 through multiple overloaded constructors is entirely Michael's.
//                 The implementation is entirely mine, however.
//                 This just cuts down on the number of files needed in the package
//                 while still performing the functionality individually.
//                 This lets me standardize the returns and just assign one variable
//                 repeatedly.
//
//******************************************************************************

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class CalculateValues implements Callable<Double> {

    private ArrayList<Integer> numbers;
    private double mean;
    private double sum;
    private int count;
    private final int operation;

    //***************************************************************
    //
    //  Method:       Constructor
    //
    //  Description:  Defines variables for use in the program
    //
    //  Parameters:   ArrayList<Integer> numbers
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public CalculateValues(ArrayList<Integer> numbers) {
        this.numbers = numbers;
        operation = 0;
    }

    //***************************************************************
    //
    //  Method:       Constructor
    //
    //  Description:  Defines variables for use in the program
    //
    //  Parameters:   double sum, int count
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public CalculateValues(double sum, int count) {
        this.sum = sum;
        this.count = count;
        operation = 1;
    }

    //***************************************************************
    //
    //  Method:       Constructor
    //
    //  Description:  Defines variables for use in the program
    //
    //  Parameters:   ArrayList<Integer> numbers, double mean
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public CalculateValues(ArrayList<Integer> numbers, double mean) {
        this.mean = mean;
        this.numbers = numbers;
        operation = 2;
    }

    //***************************************************************
    //
    //  Method:       call
    //
    //  Description:  runs the task
    //
    //  Parameters:   None
    //
    //  Returns:      Double
    //
    //**************************************************************
    @Override
    public Double call() throws Exception{
        double returnval = 0.0;
        if (operation == 0) {
            returnval = getSum();
        }
        else if (operation == 1) {
            returnval = getMean();
        }
        else if (operation == 2) {
            returnval = getStdDev();
        }
        return returnval;
    }

    //***************************************************************
    //
    //  Method:       getSum
    //
    //  Description:  calculates the sum of a list
    //
    //  Parameters:   None
    //
    //  Returns:      double sum
    //
    //**************************************************************
    public double getSum() {
        synchronized (numbers) {
            for (Integer number : numbers) {
                sum += number;
            }
        }
        return sum;
    }

    //***************************************************************
    //
    //  Method:       getMean
    //
    //  Description:  calculates the average based on sum and amount of numbers
    //
    //  Parameters:   None
    //
    //  Returns:      double mean
    //
    //**************************************************************
    public double getMean() {
        return sum / count;
    }

    //***************************************************************
    //
    //  Method:       getStdDev
    //
    //  Description:  calculates the standard deviation of a list using mean
    //
    //  Parameters:   None
    //
    //  Returns:      double stddev
    //
    //**************************************************************
    public double getStdDev() {
        double stddev = 0.0;
        synchronized (numbers) {
            for (Integer number : numbers) {
                stddev += Math.pow(number - mean, 2);
            }

            if (stddev != 0) {
                stddev = Math.sqrt(stddev / numbers.size());
            }
        }
        return stddev;
    }
}
