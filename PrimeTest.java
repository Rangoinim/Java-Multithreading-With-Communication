//********************************************************************
//
//  Developer:           Instructor & Cory Munselle
//
//  Project #:           Five
//
//  File Name:           PrimeTest.java
//
//  Course:              COSC 4301 - Modern Programming
//
//  Due Date:            3/13/2022 
//
//  Instructor:          Fred Kumi 
//
//  Description:         PrimeTest class
//
//  Notes:               This is Prof. Kumi's PrimeTest.java file, but
//                       converted into a Callable with main removed
//                       (scanner wasn't really necessary). isPrime
//                       remains unmodified, save for the method parameter and
//                       returning a Boolean object instead of a primitive.
//
//********************************************************************

import java.util.concurrent.Callable;

public class PrimeTest implements Callable<Boolean>
{
   int primeNum;

   public PrimeTest(int number) {
      primeNum = number;
   }

   //***************************************************************
   //
   //  Method:       main
   // 
   //  Description:  The main method of the program
   //
   //  Parameters:   String array
   //
   //  Returns:      N/A 
   //
   //**************************************************************
   @Override
   public Boolean call() throws Exception {
      return isPrime();
   }

   //***************************************************************
   //
   //  Method:       isPrime (Non Static)
   // 
   //  Description:  This method determines whether a positive integer is
   //                a prime number.  It returns true if the integer a prime
   //                number, and false if it is not.
   //
   //  Parameters:   None
   //
   //  Returns:      Boolean
   //
   //**************************************************************
   public Boolean isPrime()
   {
      boolean rtnValue = true;

      if (primeNum < 2)            // Integers < 2 cannot be prime
         rtnValue = false;
      else if (primeNum == 2)      // Special case: 2 is the only even prime number
         rtnValue = true;
      else if (primeNum % 2 == 0)  // Other even numbers are not prime
         rtnValue = false;
      else {
         // Test odd divisors up to the square root of number
         // If any of them divide evenly into it, then number is not prime
         for (int divisor = 3; divisor <= Math.sqrt(primeNum); divisor += 2)
         {
            if (primeNum % divisor == 0)
               rtnValue = false;
         }
      }
      return rtnValue;
   }
}

