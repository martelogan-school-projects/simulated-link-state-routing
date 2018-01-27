package socs.network.node;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Job to manage lifecycle of Router's exposed ServerSocket.
 */
public class RouterServerJob implements Runnable {

  /**
   * Router for which our RouterServerJob is responsible.
   */
  private final Router router;

  /**
   * ServerSocket at which we will persistently listen for incoming requests.
   */
  private final ServerSocket serverSocket;

  /**
   * Instantiate RouterServerJob to listen for incoming requests on behalf of input Router.
   */
  public RouterServerJob(Router router, ServerSocket serverSocket) {
    this.router = router;
    this.serverSocket = serverSocket;
  }

  /**
   * RouterServerJob's execution routine to indefinitely handle incoming requests.
   */
  public void run() {
    try {
      // infinite loop to handle incoming messages
      while (true) {
        // perform blocking wait to accept an incoming connection
        Socket activeSocket = serverSocket.accept();

        // reaching here means we have accepted an incoming message
        // let's create an active connection thread to handle the incoming data
        RequestHandlerJob requestHandlerJob = new RequestHandlerJob(router, activeSocket);
        Thread activeConnectionLifecycle = new Thread(requestHandlerJob);
        activeConnectionLifecycle.start();
      }
    } catch (Exception e) {
      System.out.println(
          "\n\nRouterServerJob crashed for router IP " + router.rd.simulatedIpAddress + " \n\n");
      e.printStackTrace();
    }
  }
}
