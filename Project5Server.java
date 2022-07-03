//******************************************************************************
//
//  Developer:     Cory Munselle
//
//  Project #:     Project 5
//
//  File Name:     Project5Server.java
//
//  Course:        COSC 4301 - Modern Programming
//
//  Due Date:      3/13/2022
//
//  Instructor:    Fred Kumi
//
//  Description:   Opens a socket and waits for client connection. Once connected,
//                 valid data sent from the client is processed and sent back
//                 to the client. If the client disconnects the server stays open
//                 and waits for connection.
//
//  Notes:         Not much to say here, most of the functionality has been moved
//                 to ServerInstance.java.
//
//******************************************************************************

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Project5Server
{
    private int clientCount;
    private final int coreCount;
    private final ExecutorService executorService;
    private final int port;
    private Socket connection = null;
    private ServerSocket server = null;

    //***************************************************************
    //
    //  Method:       Constructor
    //
    //  Description:  Defines variables for use in the program
    //
    //  Parameters:   int port
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public Project5Server(int port) {
        developerInfo();

        coreCount = Runtime.getRuntime().availableProcessors();

        executorService = Executors.newFixedThreadPool(coreCount);

        clientCount = 0;

        this.port = port;

        startServer(port);
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
        Project5Server mainServer = new Project5Server(4301);
    }

    //***************************************************************
    //
    //  Method:       startServer
    //
    //  Description:  Attempts to start the server
    //
    //  Parameters:   int port
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void startServer(int port) {
        System.out.println("Attempting to start the server...");
        try {
            server = new ServerSocket(port);
        }
        catch (IOException e) {
            System.err.println("Unable to start server. Printing stack trace and terminating...");
            e.printStackTrace();
        }
        if (server != null) {
            System.out.println("Server started.");
            System.out.println("Opening threads for client and waiting for connections...");
        }
        acceptConnection();
    }

    //***************************************************************
    //
    //  Method:       acceptConnection
    //
    //  Description:  Attempts to open the server to connections
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void acceptConnection() {
        // I don't think this should ever happen, but if the connection attempt
        // throws an exception, we just restart the server by forcing creation
        // of another socket
        boolean restart = false;

        // this is still the main driver of the program, but now it's not a while true loop.
        // very nice.
        while (!restart) {
            if (server != null) {
                try {
                    connection = server.accept();
                    runServerInstance();
                } catch (IOException e) {
                    System.err.println("Unknown error occurred. Printing stack trace and restarting server...");
                    e.printStackTrace();
                    server = null;
                }
            }
            else {
                restart = true;
            }
        }
        startServer(port);
    }

    //***************************************************************
    //
    //  Method:       runServerInstance
    //
    //  Description:  Executes a single instance of the server for a client
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void runServerInstance() {
        // Don't know if this was the correct move, but we realized that
        // if you try to connect more clients than there are threads it hangs
        // until another connection is terminated. Worse still, since the
        // threads are being shared between instances and processing primes,
        // if there's the same number of clients as threads connected no processing occurs.
        // This is why there's a restriction to half of the total threads to allow for efficient
        // processing.
        if (clientCount < coreCount/2) {
            executorService.execute(new ServerInstance(connection, executorService, this, false));
        }
        // If another client tries to connect past the limit, execute but with the disconnect flag set to true
        // this lets the server send a quick message to the client to show that it's closed and then terminate
        // the connection
        else {
            executorService.execute(new ServerInstance(connection, executorService, this, true));
        }
    }

    //***************************************************************
    //
    //  Method:       disconnected
    //
    //  Description:  decrements the number of clients connected by 1
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void disconnected() {
        clientCount -= 1;
    }

    //***************************************************************
    //
    //  Method:       connected
    //
    //  Description:  increments the number of clients connected by 1
    //
    //  Parameters:   None
    //
    //  Returns:      N/A
    //
    //**************************************************************
    public void connected() {
        clientCount += 1;
    }

    //***************************************************************
    //
    //  Method:       getConnections
    //
    //  Description:  returns number of current clients
    //
    //  Parameters:   None
    //
    //  Returns:      int clientCount
    //
    //**************************************************************
    public int getConnections() {
        return clientCount;
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