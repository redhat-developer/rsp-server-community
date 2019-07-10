package com.example.wonka;

import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        if( args.length == 0 ) {
            runServer();
        } else if( "shutdown".equals(args[0])) {
            shutdownServer();
        }
    }

    public static void shutdownServer() {
        File shutdownMarker = new java.io.File( ".", "shutdown.marker" );
        try {
            shutdownMarker.createNewFile();
        } catch(IOException ioe) {
            ioe.printStackTrace();;
        }
    }

    public static void runServer() {
        System.out.println("Wonka Runtime Running.  Waiting for shutdown marker:");
        File shutdownMarker = new java.io.File( ".", "shutdown.marker" );
        while(true ) {
            if( shutdownMarker.isFile()) {
                shutdownMarker.delete();
                System.out.println("Shutdown Marker Found. Shutting Down Wonka Runtime.");
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
