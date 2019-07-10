package com.example.wonka;

import java.io.File;
import java.io.IOException;
import java.util.Random;
/**
 * Hello world!
 *
 */
public class App {
    public static final File shutdownMarker = new java.io.File( ".", "shutdown.marker" );
    public static final File startedMarker = new java.io.File( ".", "wonka.started" );

    public static void main( String[] args ) {
        shutdownMarker.delete();
        startedMarker.delete();

        if( args.length == 0 ) {
            runServer();
        } else if( "shutdown".equals(args[0])) {
            shutdownServer();
        }
    }

    public static void shutdownServer() {
        try {
            shutdownMarker.createNewFile();
        } catch(IOException ioe) {
            ioe.printStackTrace();;
        }
    }

    public static void runServer() {
        System.out.print("Server is starting up...");
        Random rand = new Random(); 
        int value = rand.nextInt(60);
        for( int i = 0; i < value+20; i++ ) {
            System.out.print(".");
            try {
                Thread.sleep(100);
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        try {
            startedMarker.createNewFile();
        } catch(IOException ioe) {
            ioe.printStackTrace();;
        }

        System.out.println("\nWonka Runtime Running.  Waiting for shutdown marker:");

        while(true ) {
            if( shutdownMarker.isFile()) {
                System.out.println("Shutdown Marker Found. Shutting Down Wonka Runtime.");
                shutdownMarker.delete();
                startedMarker.delete();
                return;
            }
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ie) {
                // Ignore
            }
        }

    }
}
