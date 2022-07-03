//******************************************************************************
//
//  Developer:     Cory Munselle
//
//  Project #:     Project 5
//
//  File Name:     ServerInstance.java
//
//  Course:        COSC 4301 - Modern Programming
//
//  Due Date:      3/13/2022
//
//  Instructor:    Fred Kumi
//
//  Description:   Handles all the processing that server did before for each
//                 client. Each client gets one server instance for input/output,
//                 and processing is handed to any spare threads for calculations.
//
//  Notes:         I'm going to be calling a generic IOException quite frequently.
//                 This isn't exactly by choice, but what I see as a necessary evil
//                 due to my inexperience with client/server communications and the
//                 extensive list of possible exceptions that could be thrown in the
//                 event of any issues that arise. They all just happen to be derived
//                 from IOException, so I stuck with that.
//
//******************************************************************************

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.*;

public class ServerInstance implements Runnable{

    private DataInputStream inStream = null;
    private DataOutputStream outStream = null;
    private final ExecutorService executorService;
    private Socket clientConnection;
    private final Project5Server mainServer;

    private int number1;
    private int number2;
    private final boolean disconnect;
    private final ArrayList<Integer> numbers;

    //***************************************************************
    //
    //  Method:       Constructor
    //
    //  Description:  Defines variables for use in the program
    //
    //  Parameters:   Socket connection: current connection with client
    //                ExecutorServer executorService: for thread control when processing primes and stats
    //                Project5Server mainServer: so the instance can send connects and disconnects to the main server
    //                boolean disconnect: tells the server instance whether to accept or reject a connection
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public ServerInstance(Socket connection, ExecutorService executorService, Project5Server mainServer, boolean disconnect) {
        this.number1 = 0;
        this.number2 = 0;

        this.disconnect = disconnect;

        this.executorService = executorService;

        this.mainServer = mainServer;

        numbers = new ArrayList<>();
        clientConnection = connection;
    }

    //***************************************************************
    //
    //  Method:       run
    //
    //  Description:  The code to be executed in the task
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    @Override
    public void run() {
        // gets input/output set up for the client
        getClientIO();

        String str = "";
        // if the client doesn't type "Bye" and server gives permission to connect
        while (!str.matches("Bye") && !disconnect) {
            try {
                str = inStream.readUTF();
                if (!str.matches("Bye")) {

                    // basic output so anybody viewing the server console (me) knows what is being sent
                    System.out.println("The string sent from client " + clientConnection.getPort() + " is: " + str);
                    // if the provided string was successfully parsed, go ahead and process
                    if (parseInput(str)) {
                        processInput();
                    }
                }
            }
            // For handling errors
            catch(IOException io) {
                System.err.println("Failed to receive input from client. Closing the thread...");
                // I don't want to mess with disconnect so I'm using the other argument to disconnect
                str = "Bye";
            }
        }
        if (!disconnect) {
            sendToClient("Server is closing the connection...\n", true);
        }
        closeSession();
    }

    //***************************************************************
    //
    //  Method:       getClientIO
    //
    //  Description:  Attempts to get the data input/output streams
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void getClientIO() {
        // if disconnect is not true
        if (!disconnect) {
            try {
                // tries to open input/output stream
                inStream = new DataInputStream(clientConnection.getInputStream());
                outStream = new DataOutputStream(clientConnection.getOutputStream());
                // prints out info to the console
                System.out.println("Connected to client " + (mainServer.getConnections() + 1));
                System.out.println("IP: " + clientConnection.getInetAddress());
                System.out.println("Port: " + clientConnection.getPort());
                // sends "yes" to the client to confirm connection is okay
                sendToClient("yes", false);
                // synchronized to make sure only one instance of the class can call it at a time
                // thread safety is important
                synchronized (mainServer) {
                    mainServer.connected();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                closeSession();
            }
        }
        // if disconnect is true
        else {
            try {
                // attempt connection
                inStream = new DataInputStream(clientConnection.getInputStream());
                outStream = new DataOutputStream(clientConnection.getOutputStream());
                // send no to the client to tell it connection has been refused
                sendToClient("no", false);
            }
            // shouldn't get here, just print the stack trace
            catch (IOException e) {
                e.printStackTrace();
            }
            sendToClient("Server is currently full. Please try again later.\n", false);
        }
    }

    //***************************************************************
    //
    //  Method:       parseInput
    //
    //  Description:  Attempts to split the client provided string
    //                into three separate integers
    //
    //  Parameters:   String line
    //
    //  Returns:      boolean success
    //
    //**************************************************************
    public boolean parseInput(String line) {
        boolean success = false;
        String[] tokens;
        // regex that splits a string by any number of spaces to account for extra spacebar presses
        tokens = line.split("\\s+");

        if (tokens.length != 2) {
            sendToClient("Invalid number of digits. Please provide exactly two numbers.\n", true);
        }
        else {
            try {
                // I did two separate int variables instead of an array because it's
                // easier to work with. I also directly referenced the indexes in the string
                // array because all the other checks should prevent anything else before this
                number1 = Integer.parseInt(tokens[0]);
                number2 = Integer.parseInt(tokens[1]);
                success = true;
            }
            catch (NumberFormatException e) {
                sendToClient("One or more supplied values is not a number. Please supply two numbers, \nseparated by spaces.\n", true);
            }
        }
        return success;
    }

    //***************************************************************
    //
    //  Method:       processInput
    //
    //  Description:  Takes the parsed integers and attempts to build
    //                an ArrayList of integers
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void processInput() {
        if (number1 <= 0 || number2 <= 0) {
            sendToClient("All numbers provided must be greater than zero.\n", true);
        }
        else if (number1 >= number2) {
            sendToClient("The first number must be less than the second.\n", true);
        }
        else {
            // ensures the arraylist is clear after each client message
            numbers.clear();
            for (int i = number1; i <= number2; i++) {
                numbers.add(i);
            }
            // once arraylist of numbers is generated, go to calculate output
            calculateOutput();
        }
    }

    //***************************************************************
    //
    //  Method:       calculateOutput
    //
    //  Description:  Calculates the sum, mean, and standard dev of the
    //                generated arraylist of integers based on client
    //                input
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void calculateOutput() {
        ArrayList<Integer> onlyPrimes = new ArrayList<>();
        ArrayList<Future<Boolean>> primeNumbers = new ArrayList<>();

        double sum = 0.0;
        double mean = 0.0;
        double stddev = 0.0;
        Future<Double> calcValue;
        int taskCounter = 0;
        boolean complete = false;
        boolean process = true;

        // generate the list of future<boolean>s for checking if a value is prime or not
        for (Integer number : numbers) {
            // make sure it's synchronized to ensure the executorService only submits one task at a time
            synchronized (executorService) {
                primeNumbers.add(executorService.submit(new PrimeTest(number)));
            }
        }

        // loops through the list of futures and all numbers concurrently, putting the value in
        // the list of primes if it's a prime number
        while (taskCounter < primeNumbers.size() && process) {
            // not required but it's a safety check (thanks Michael) that makes sure
            // the get call on the future only happens once the task is complete
            if (primeNumbers.get(taskCounter).isDone()) {
                try {
                    if (primeNumbers.get(taskCounter).get()) {
                        onlyPrimes.add(numbers.get(taskCounter));
                    }
                    taskCounter++;
                }
                // if at any point the task fails for any reason, the exception is
                // caught and the user is reprompted for input
                catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    sendToClient("Processing error occurred. Please resubmit the data.", true);
                    process = false;
                }
            }
        }

        // clears to make sure nothing is in the futures list when new values are parsed
        primeNumbers.clear();

        // submit primes list for getSum in CalculateValues
        // returned as a future<Double>
        // gated behind a boolean to ensure that calculation is stopped
        // if any of the tasks fail to return a future
        if (process && onlyPrimes.size() > 0) {
            calcValue = executorService.submit(new CalculateValues(onlyPrimes));

            //while the task is not complete
            while (!complete && process) {
                // if the task is complete
                if (calcValue.isDone()) {
                    try {
                        // try to get the value and set complete to true
                        sum = calcValue.get();
                        complete = true;
                    }
                    // if an exception is thrown halt processing
                    catch (InterruptedException | ExecutionException e) {
                        System.err.println("Sum didn't calculate correctly.");
                        e.printStackTrace();
                        sendToClient("Processing error occurred. Please resubmit the data.\n", true);
                        process = false;
                    }
                }
            }
        }

        // submit sum (calculated above) and the size of the list of primes for calculating the mean
        // same process and checks as above
        if (process && onlyPrimes.size() > 0) {
            calcValue = executorService.submit(new CalculateValues(sum, onlyPrimes.size()));
            complete = false;

            while (!complete && process) {
                if (calcValue.isDone()) {
                    try {
                        mean = calcValue.get();
                        complete = true;
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        sendToClient("Processing error occurred. Please resubmit the data.\n", true);
                        process = false;
                    }
                }
            }
        }

        // submits mean (calculated above) and the list of primes for calculating the stddev
        // same process and checks as above
        if (process && onlyPrimes.size() > 0) {
            calcValue = executorService.submit(new CalculateValues(onlyPrimes, mean));
            complete = false;

            while (!complete && process) {
                if (calcValue.isDone()) {
                    try {
                        stddev = calcValue.get();
                        complete = true;
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        sendToClient("Processing error occurred. Please resubmit the data.\n", true);
                        process = false;
                    }
                }
            }
        }

        // if any of the previous calculations failed there's no reason to send anything to the client
        // so I don't send anything to the client and just go back to the loop for input
        if (process && onlyPrimes.size() > 0) {
            sendToClient(String.format("%s%n%n", "List of primes:"), false);
            for (int i = 0; i < onlyPrimes.size(); i++) {
                sendToClient(String.format("%-7d ", onlyPrimes.get(i)), false);
                if ((i + 1) % 10 == 0) {
                    sendToClient("\n", false);
                }
            }
            sendToClient(String.format("%n%s%n%s%.0f%n%s%.3f%n%s%.3f%n", "Calculations: ", "Sum: ", sum, "Mean: ", mean, "Standard deviation: ", stddev), true);
        }

        // if the list is empty because two numbers with no primes in between were found
        // just tell the user instead of sending a bunch of extra stuff and reprompt for input
        if (onlyPrimes.size() == 0) {
            sendToClient("No primes found. Please input two different numbers.\n", true);
        }
    }

    //***************************************************************
    //
    //  Method:       closeSession
    //
    //  Description:  Attempts to close the server
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void closeSession() {
        System.out.println("Client on port " + clientConnection.getPort() + " disconnected.");
        try {
            inStream.close();
            outStream.close();
            clientConnection.close();
            if (!disconnect) {
                synchronized (mainServer) {
                    mainServer.disconnected();
                }
            }
        }
        // Shouldn't get here but in case it does set all connections to null
        // and the program will exit out normally
        // assuming setting them to null lets the JVM garbage collect
        catch (IOException | NullPointerException e) {
            System.err.println("Failed to gracefully close connections. Setting connections to null...");
            inStream = null;
            outStream = null;
            clientConnection = null;
            e.printStackTrace();
        }
    }

    //***************************************************************
    //
    //  Method:       sendToClient
    //
    //  Description:  Attempts to send strings to client for display
    //
    //  Parameters:   String strToSend, boolean stop
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void sendToClient(String strToSend, boolean stop) {
        try {
            if (clientConnection.isConnected()) {
                outStream.writeUTF(strToSend);
                if (stop) {
                    outStream.writeUTF("stop");
                }
            }

        }
        catch (IOException e) {
            System.out.println("Failed to send message to client. Displaying on the server...");
            System.out.println(strToSend);
        }
    }
}
