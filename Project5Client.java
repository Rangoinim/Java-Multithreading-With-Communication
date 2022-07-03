//******************************************************************************
//
//  Developer:     Cory Munselle
//
//  Project #:     Project 5
//
//  File Name:     Project5Client.java
//
//  Course:        COSC 4301 - Modern Programming
//
//  Due Date:      3/13/2022
//
//  Instructor:    Fred Kumi
//
//  Description:   Connects to the server via opened socket. Once connected,
//                 sends data to the server for processing. Valid data will be
//                 accepted and the sum, mean, and standard deviation of the data
//                 will be returned along with a list of prime numbers.
//
//  Notes:         I ultimately had to remove my attempts at reconnection. I wanted
//                 to keep them, but I feel like the scope of this project got a bit
//                 out of hand, and having the client try to constantly connect when
//                 the server is only allowing for a certain number of connections causes
//                 more complications. Also, I'm getting this in under the wire already.
//
//******************************************************************************

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Project5Client {
    // initializing socket and input output streams

    private Scanner userInput = null;
    private DataOutputStream outStream = null;
    private DataInputStream inStream = null;
    private Socket connection = null;

    //***************************************************************
    //
    //  Method:       Constructor
    //
    //  Description:  Defines variables for use in the program
    //
    //  Parameters:   String address, int port
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public Project5Client(String address, int port) {
        // Establishing connection with server
        establishConnection(address, port);
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
    public static void main(String[] argv) {
        Project5Client client = new Project5Client("127.0.0.1", 4301);
    }

    //***************************************************************
    //
    //  Method:       establishConnection
    //
    //  Description:  Attempts to connect to the server
    //
    //  Parameters:   String address, int port
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void establishConnection(String address, int port) {
        developerInfo();
        System.out.println("Attempting to establish connection...");

        // The server attempts to connect to the client
        try {
            connection = new Socket(address, port);
            outStream = new DataOutputStream(connection.getOutputStream());
            inStream = new DataInputStream(connection.getInputStream());

        }
        // if connection fails it prints out a message and ends the program gracefully
        catch (IOException e) {
            System.err.println("Unable to establish connection.");
        }
        // if the connection is assigned to something (only true if the try block above fails)
        if (connection != null) {
            // try and read messages from the server
            try {
                String messageStr = inStream.readUTF();
                // if the string sent is "no", tell the client that the server is busy and exit the client
                if (messageStr.matches("no")) {
                    System.out.println("Server is currently busy. Please try again later.");
                }
                // if the string sent is "yes", go ahead and start requesting input
                else if (messageStr.matches("yes")){
                    System.out.println("Connection established.");
                    getInput();
                }
                // this is fallback in case the connection is allowed but the datastream fails for some reason
                // just close the connection and get the client to restart
                else {
                    System.err.println("Unable to properly connect to the server. Closing...");
                    closeClient();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //***************************************************************
    //
    //  Method:       getInput
    //
    //  Description:  Constantly queries the user for input until "Bye" is supplied
    //
    //  Parameters:   String array
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void getInput() {
        userInput = new Scanner(System.in);
        String str = "";


        // This loop is responsible for the entire execution of the program
        while (!str.matches("Bye")) {
            str = "";
            System.out.println("\nPlease supply two positive integers separated by spaces. "
                    + "The first integer must be less than the second.\n"
                    + "If you wish to quit, type 'Bye'.");
            while (str.isEmpty()) {
                str = userInput.nextLine();
            }
            try {
                // Writes string to server
                outStream.writeUTF(str);

                // this is here only because if Bye was typed the program would try to get
                // input from the server when there was none and the program would crash
                //if (!str.equals("Bye")) {
                    String serverOut = "";

                    // Constantly reads input from the server until the word "stop" is sent.
                    // It was done this way because I don't know how many numbers I'll have to
                    // send for the prime list so I just keep sending them until the word "stop"
                    // is sent from the server to signify completion
                    while (!serverOut.matches("stop")) {
                        serverOut = inStream.readUTF();
                        if (!serverOut.matches("stop")) {
                            getResponse(serverOut);
                        }
                    }
                //}
            }
            // if it fails to send at any time, just get the user to restart the client
            catch(IOException e) {
                System.out.println("Failed to send string to the server.");
                closeClient();
            }
        }
        // This isn't executed until the user types Bye so there's no logic needed
        closeClient();
    }

    //***************************************************************
    //
    //  Method:       getResponse
    //
    //  Description:  Displays any messages sent by the server
    //
    //  Parameters:   String message
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void getResponse(String message) {
        System.out.print(message);
    }

    //***************************************************************
    //
    //  Method:       closeClient
    //
    //  Description:  Gracefully closes the client and all connections
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void closeClient() {
        System.out.println("Closing the client...");
        // I don't know how garbage collection works exactly, so I decided to
        // implement a closeClient method to make sure things were properly closed
        // to the best of my ability
        try {
            connection.close();
            inStream.close();
            outStream.close();
            userInput.close();
        }
        // Shouldn't get here but in case it does just terminate
        catch (IOException e) {
            System.err.println("Failed to gracefully close connections. Terminating...");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    //***************************************************************
    //
    //  Method:       developerInfo (Non Static)
    //
    //  Description:  The developer information method of the program
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void developerInfo()
    {
        System.out.println("Name:    Cory Munselle");
        System.out.println("Course:  COSC 4301 Modern Programming");
        System.out.println("Project: Four\n");

    } // End of the developerInfo method
}