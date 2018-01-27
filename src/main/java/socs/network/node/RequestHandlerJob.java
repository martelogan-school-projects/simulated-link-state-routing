package socs.network.node;

import java.net.Socket;

/**
 * Job to manage lifecycle of an active connection with client router.
 */
public class RequestHandlerJob implements Runnable {

  /**
   * Router for which our RequestHandlerJob is serving requests.
   */
  private final Router router;

  /**
   * Active socket for client-server connection handled by this job.
   */
  private final Socket activeSocket;

  /**
   * Instantiate RequestHandlerJob to handle a single request on behalf of our input Router.
   */
  public RequestHandlerJob(Router router, Socket activeSocket) {
    this.router = router;
    this.activeSocket = activeSocket;
  }

  /**
   * RequestHandlerJob's execution routine to handle a single request.
   */
  public void run() {
    // TODO: handle some requests!
  }

}
