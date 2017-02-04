package tr.edu.boun.cmpe.scn.service;

/**
 * Created by esinka on 1/28/2017.
 */
public class Init {

    private static ScnServer server;

    public static void main(String args[]) throws Exception {
        server = new ScnServer();
        server.run();
    }
}
