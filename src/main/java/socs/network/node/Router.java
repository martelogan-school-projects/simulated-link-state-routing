package socs.network.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import socs.network.net_utils.RouterConfiguration;

/**
 * Encapsulating class for a router (ie. single node) in our network.
 */
public class Router {

  /**
   * Int constant for fixed-size of ports array.
   */
  static final int NUM_PORTS_PER_ROUTER = 4;

  /**
   * LSD instance for this router (captures state of this router's knowledge on LSA broadcasts).
   */
  protected LinkStateDatabase lsd;

  /**
   * Description summarizing state of this router (ie. single node) in our network.
   */
  RouterDescription rd = new RouterDescription();

  /**
   * Fixed-size array maintaining state of ports exposed to link with other routers in our network.
   */
  Link[] ports = new Link[NUM_PORTS_PER_ROUTER];

  /**
   * Constructor to instantiate a Router via input RouterConfiguration parameters.
   */
  public Router(RouterConfiguration config) {
    rd.simulatedIpAddress = config.getSimulatedIpAddress();
    lsd = new LinkStateDatabase(rd);

  }

  /**
   * Output the shortest path to the given destination ip.
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIp the ip address of the destination simulated router
   */
  private void processDetect(String destinationIp) {

  }

  /**
   * Disconnect with the router identified by the given destination ip address. Notice: this command
   * should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) {

  }

  /**
   * Attach the link to the remote router, which is identified by the given simulated ip. To
   * establish the connection via socket, you need to identify the process IP and process Port.
   * Additionally, weight is the cost of transmitting data through the link.
   * <p/>
   * NOTE: This command should not trigger link database synchronization
   */
  private void processAttach(String remoteProcessIp, short remoteProcessPort,
      String remoteSimulatedIp, short linkWeight) {

    // verify that we are not attempting self-attachment
    if (remoteSimulatedIp.equals(this.rd.simulatedIpAddress)) {
      System.out.println("\n\nError: cannot input IP of current router.");
      System.out.println("Please enter a valid remote IP address at which to attach.\n\n");
      return;
    }

    // find free port at which to link remote router
    int indexOfFreePort = RouterUtils.findIndexOfFreePort(ports, remoteSimulatedIp);

    // verify that a valid port was indeed available (else, return immediately)
    switch (indexOfFreePort) {
      case RouterUtils.NO_PORT_AVAILABLE_FLAG:
        System.out.println("\n\nNo free port available on current router at this time.\n\n");
        return;
      case RouterUtils.DUPLICATE_ATTACHMENT_ATTEMPT_FLAG:
        System.out.println("\n\nError: cannot attach twice to a given remote IP.");
        System.out.println("Please enter a valid remote IP address at which to attach.\n\n");
        return;
    }

    // a port was available, let's create a description for our remote router
    RouterDescription remoteRouterDescription =
        RouterUtils.createRouterDescription(remoteProcessIp, remoteProcessPort, remoteSimulatedIp);

    // attach the link to the free port of our array
    ports[indexOfFreePort] = new Link(this.rd, remoteRouterDescription);

  }

  /**
   * Broadcast Hello to neighbors.
   */
  private void processStart() {

    // TODO:

  }

  /**
   * Attach the link to the remote router, which is identified by the given simulated ip. To
   * establish the connection via socket, you need to identify the process IP and process Port.
   * Additionally, weight is the cost to transmitting data through the link
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIp, short processPort,
      String simulatedIp, short weight) {

  }

  /**
   * Output the neighbors of the routers.
   */
  private void processNeighbors() {

  }

  /**
   * Disconnect with all neighbors and quit the program.
   */
  private void processQuit() {

  }

  /**
   * Interpret user input from the command line.
   */
  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
        if (command.startsWith("detect ")) {
          String[] cmdLine = command.split(" ");
          processDetect(cmdLine[1]);
        } else if (command.startsWith("disconnect ")) {
          String[] cmdLine = command.split(" ");
          processDisconnect(Short.parseShort(cmdLine[1]));
        } else if (command.startsWith("quit")) {
          processQuit();
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
              cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("start")) {
          processStart();
        } else if (command.equals("connect ")) {
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
              cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("neighbors")) {
          // output neighbors
          processNeighbors();
        } else {
          // invalid command
          break;
        }
        System.out.print(">> ");
        command = br.readLine();
      }
      isReader.close();
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
