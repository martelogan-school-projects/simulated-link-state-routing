package socs.network.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Vector;
import socs.network.message.LinkDescription;
import socs.network.message.LinkStateAdvertisement;
import socs.network.message.SospfPacket;
import socs.network.utils.CommonUtils;
import socs.network.utils.RouterConfiguration;

/**
 * Encapsulating class for a router (ie. single node) in our network.
 */
public class Router {

  /**
   * Int constant for fixed-size of ports array.
   */
  static final int NUM_PORTS_PER_ROUTER = 4;

  /**
   * Int constant for initial attempt at port assignment.
   */
  static final short MIN_PROCESS_PORT_NUMBER = 20000;

  /**
   * Int constant for max value of attempted port assignment.
   */
  static final short MAX_PROCESS_PORT_NUMBER = Short.MAX_VALUE;

  /**
   * LSD instance for this router (captures state of this router's knowledge on LSA broadcasts).
   */
  private final LinkStateDatabase lsd;

  /**
   * Description summarizing state of this router (ie. single node) in our network.
   */
  final RouterDescription rd;

  /**
   * Fixed-size array maintaining state of ports exposed to link with other routers in our network.
   */
  final Link[] ports = new Link[NUM_PORTS_PER_ROUTER];

  /**
   * Constructor to instantiate a Router via input RouterConfiguration parameters.
   */
  public Router(RouterConfiguration config) throws Exception {

    if (config == null) {
      throw new IllegalArgumentException("Cannot instantiate router with null input config.");
    }

    // assign simulated IP address from config file
    String simulatedIpAddress = config.getSimulatedIpAddress();

    // attempt to set process IP address to localhost (fail fast if unsuccessful)
    String processIpAddress = InetAddress.getLocalHost().getHostAddress();

    ServerSocket serverSocket = null;
    short processPortNumber = RouterDescription.INVALID_PORT_NUMBER;
    short curPortNumber = MIN_PROCESS_PORT_NUMBER;
    // seek available port at which to assign a ServerSocket
    while (serverSocket == null) {
      try {
        // attempt setting socket at this port number
        serverSocket = new ServerSocket(curPortNumber);
        processPortNumber = curPortNumber;
        // on success, let's start a background listener for our router
        RouterServerJob serverJob = new RouterServerJob(serverSocket);
        Thread serverJobThread = new Thread(serverJob);
        serverJobThread.start();
      } catch (Exception e) {
        if (curPortNumber < MAX_PROCESS_PORT_NUMBER) {
          curPortNumber += 1;
        } else {
          throw new IllegalStateException(
              "\n\nNo process ports available to start router at this time.\n\n"
          );
        }
      }
    }

    // instantiate our router description (will raise exception on any invalid params)
    this.rd = new RouterDescription(processIpAddress, processPortNumber, simulatedIpAddress,
        RouterStatus.UNKNOWN, (short) RouterDescription.TRANSMISSION_WEIGHT_TO_SELF);

    // surviving the above, let's instantiate an LSD for our Router
    lsd = new LinkStateDatabase(rd);

    // notify details of our router instance
    System.out.println("\nSuccessfully started router instance at:\n");
    System.out.println("Simulated IP = " + rd.simulatedIpAddress);
    System.out.println("Process IP = " + rd.processIpAddress);
    System.out.println("Process Port Number = " + rd.processPortNumber);
    System.out.println("\n");

  }

  private synchronized void writeLinkStateOfThisRouterToDatabase() throws Exception {
    // let's create a new LSA (derived from current LSD state) for this router
    LinkStateAdvertisement myLinkStateAdvertisement =
        RouterUtils.createLinkStateAdvertisement(this);
    // write the above LSA for this router to our own database
    putLinkStateAdvertisement(myLinkStateAdvertisement);
  }

  /**
   * Output the shortest path to the given destination ip.
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIp the ip address of the destination simulated router
   */
  private void processDetect(String destinationIp) {
    try {
      if (CommonUtils.isNullOrEmptyString(destinationIp)) {
        throw new IllegalArgumentException(
            "Cannot detect path to empty or null remote simulated IP.");
      }
      String pathString = this.lsd.getShortestPath(destinationIp);
      if (pathString == null) {
        System.out.println("\n\nNo shortest path to destination found.\n\n");
      } else {
        System.out.println("\n\n");
        System.out.println(pathString);
        System.out.println("\n\n");
      }
    } catch (Exception e) {
      String alertMessageOfFailedShortestPathDetection =
          "\n\nError: Failed to find shortest path to destination.\n\n";
      RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedShortestPathDetection);
    }
  }

  /**
   * Helper method to remove an attachment from the Link[] ports array.
   */
  void detachLinkAtPortIndex(int portIndex) {
    if (RouterUtils.isPortIndexInvalid(portIndex)) {
      throw new IllegalArgumentException(
          "Port index '" + portIndex + "is invalid. Unable to detach link.");
    } else {
      ports[portIndex] = null;
    }

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
   * Helper method to report if port is occupied in the Link[] ports array.
   */
  private boolean checkAndAlertPortOccupationStatus(int portIndex) {
    switch (portIndex) {
      case RouterUtils.NO_PORT_AVAILABLE_FLAG:
        System.out.println("\n\nNo free port available on current router at this time.\n\n");
        return true;
      case RouterUtils.DUPLICATE_ATTACHMENT_ATTEMPT_FLAG:
        System.out.println("\n\nError: cannot attach twice to a given remote IP.");
        System.out.println("Please enter a valid remote IP address at which to attach.\n\n");
        return true;
      default:
        if (RouterUtils.isPortIndexInvalid(portIndex)) {
          System.out.println(
              "\n\nError: cannot attach to invalid port at (index = " + portIndex + " ) \n\n");
          return true;
        } else if (this.ports[portIndex] != null) {
          System.out.println(
              "\n\nError: port is not free at (index = " + portIndex + " ) \n\n");
          return true;
        } else {
          // port is free at given index
          return false;
        }
    }
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

    if (CommonUtils.isNullOrEmptyString(remoteProcessIp)) {
      throw new IllegalArgumentException("Cannot attach to empty or null remote process IP.");
    }

    if (CommonUtils.isNullOrEmptyString(remoteSimulatedIp)) {
      throw new IllegalArgumentException("Cannot attach to empty or null remote simulated IP.");
    }

    if (RouterUtils.isPortNumberInvalid(remoteProcessPort)) {
      throw new IllegalArgumentException(
          "Cannot attach to remote process with port number '"
              + remoteProcessPort + "'.\nAll process ports in our network must fall in the range "
              + MIN_PROCESS_PORT_NUMBER + " to " + MAX_PROCESS_PORT_NUMBER + "."
      );
    }

    if (linkWeight <= RouterDescription.TRANSMISSION_WEIGHT_TO_SELF) {
      throw new IllegalArgumentException(
          "Invalid (weight of link = '"
              + linkWeight + "').\nAll attached remote routers must have weight greater than "
              + RouterDescription.TRANSMISSION_WEIGHT_TO_SELF + "."
      );
    }

    // verify that we are not attempting self-attachment
    if (remoteSimulatedIp.equals(this.rd.simulatedIpAddress)) {
      System.out.println("\n\nError: cannot input IP of current router.");
      System.out.println("Please enter a valid remote IP address at which to attach.\n\n");
      return;
    }

    // find free port at which to link remote router
    int indexOfFreePort = RouterUtils.findIndexOfFreePort(ports, remoteSimulatedIp);

    // return immediately if we were not able to find a valid port
    if (checkAndAlertPortOccupationStatus(indexOfFreePort)) {
      return;
    }

    // a port was available, let's create a description for our remote router
    RouterDescription remoteRouterDescription = new RouterDescription(remoteProcessIp,
        remoteProcessPort, remoteSimulatedIp, RouterStatus.UNKNOWN, linkWeight);

    // attach the link to the free port of our array
    ports[indexOfFreePort] = new Link(this.rd, remoteRouterDescription);

    // notify details of successful attachment
    System.out.println("\n\nSuccessfully attached to remote router at:\n");
    System.out.println("Simulated IP = " + remoteSimulatedIp);
    System.out.println("Process IP = " + remoteProcessIp);
    System.out.println("Process Port Number = " + remoteProcessPort);
    System.out.println("\n");

  }

  /**
   * A helper method to wrap logic of handling a single Lsa from the client.
   */
  private void handleSingleLsa(
      String linkId, LinkStateAdvertisement linkStateAdvertisement) {
    // synchronously retrieve the last lsa stored for this linkId
    LinkStateAdvertisement prevLsa =
        lsd.getLastLinkStateAdvertisement(linkId);
    // write the lsa we are handling iff it is either the first, or has the latest seqNumber
    if (prevLsa == null || prevLsa.lsaSeqNumber < linkStateAdvertisement.lsaSeqNumber) {
      lsd.putLinkStateAdvertisement(linkId, linkStateAdvertisement);
    }
  }

  /**
   * Helper method to process all state changes of an LSAUPDATE.
   */
  private void processStateChangesOfLsaUpdate(SospfPacket inputRequestPacket) throws Exception {
    if (inputRequestPacket == null) {
      throw new Exception("Received null input request packet!");
    }
    // ready client router properties
    String clientSimulatedIpAddress = inputRequestPacket.srcIp;
    Vector<LinkStateAdvertisement> latestLsaArrayOfClient = inputRequestPacket.lsaArray;

    // iterate over each of packet's link state advertisements
    for (LinkStateAdvertisement curLsa : latestLsaArrayOfClient) {
      // handle each lsa independently
      handleSingleLsa(curLsa.linkStateId, curLsa);
    }
    // ** at this point, the state of our link state database has been updated **
    // ** next, we will need to update the state of our local ports **

    // first, let's get the index of the port at which we are attached to the client
    int indexOfAttachmentToClient =
        RouterUtils.findIndexOfPortAttachedTo(ports, clientSimulatedIpAddress);

    // and verify that we are indeed attached to the client at all
    if (indexOfAttachmentToClient != RouterUtils.NO_PORT_AVAILABLE_FLAG) {
      // if so, get the lsa persisted by our database update
      LinkStateAdvertisement currentLinkStateAdvertisementOfClient =
          lsd.getLastLinkStateAdvertisement(clientSimulatedIpAddress);

      // specifically, we care about the updated links advertised by this lsa
      LinkedList<LinkDescription> advertisedLinks = currentLinkStateAdvertisementOfClient.links;

      // iterating over each advertised link, we seek changes directed at this router
      boolean foundChangedLink = false;
      short weightOfLink = RouterDescription.TRANSMISSION_WEIGHT_TO_SELF;
      for (LinkDescription curLink : advertisedLinks) {
        // check if the weight of a link targeted at this router has changed
        weightOfLink = curLink.tosMetrics;
        if (curLink.linkId.equals(rd.simulatedIpAddress)
            && curLink.tosMetrics != RouterDescription.TRANSMISSION_WEIGHT_TO_SELF) {
          // if so, set the appropriate flag and break from our loop
          foundChangedLink = true;
          break;
        }
      }

      if (foundChangedLink) {
        // ** if a link changed, update our local state to reflect the new weighting **

        // start by updating the weight of the relevant link
        ports[indexOfAttachmentToClient].weight = weightOfLink;

        // ** to convey this change, we will need to update & broadcast our own Lsa **

        // in order to do so, let's write the link state of our router to the database
        writeLinkStateOfThisRouterToDatabase();
      }

    }
  }

  /**
   * Helper method to broadcast an LSAUPDATE to all neighbors in our Link[] ports array.
   */
  private void broadcastLsaUpdateToAllNeighbors() {
    // simply pass in a null excludedRemoteIp so that no IP's are ignored
    broadcastLsaUpdateWithExcludedRemote(null);
  }

  /**
   * Helper method to broadcast an LSAUPDATE to all neighbors in our Link[] ports array (save
   * perhaps for an excluded, non-null, input IP address).
   */
  private void broadcastLsaUpdateWithExcludedRemote(String excludedRemoteIp) {

    // declare local variables reused over iterations
    RouterDescription remoteRouterDescription;
    String remoteProcessIp;
    short remoteProcessPortNumber;
    Socket clientSocket = null;
    ObjectOutputStream outToRemoteServer = null;
    SospfPacket lsaUpdatePacket;

    for (Link curLink : ports) {
      try {
        if (curLink == null) {
          // skip this port
          continue;
        }

        // let's prepare what we need to request a connection
        remoteRouterDescription = curLink.targetRouter;
        remoteProcessIp = remoteRouterDescription.processIpAddress;
        remoteProcessPortNumber = remoteRouterDescription.processPortNumber;

        // skip this link if it is equal to a provided exclusion IP
        if (excludedRemoteIp != null
            && remoteRouterDescription.simulatedIpAddress.equals(excludedRemoteIp)) {
          continue;
        }

        // also skip any uninitialized neighbors
        if (remoteRouterDescription.status != RouterStatus.TWO_WAY) {
          continue;
        }

        try {
          // let's attempt a connection
          clientSocket = new Socket(remoteProcessIp, remoteProcessPortNumber);
          outToRemoteServer = new ObjectOutputStream(clientSocket.getOutputStream());

          // ** successful connection **
          // ** time to prepare our lsaUpdatePacket **

          // first, get the state of our lsd as a vector of the database values
          Vector<LinkStateAdvertisement> lsaArray = lsd.getValuesVector();

          // from this, we can construct our lsaUpdatePacket
          lsaUpdatePacket = RouterUtils.buildSospfPacketFromRouterDescriptions(
              this.rd, remoteRouterDescription,
              SospfPacket.SOSPF_LSAUPDATE, lsaArray, SospfPacket.IRRELEVANT_TRANSMISSION_WEIGHT
          );

          // time to send our LSAUPDATE packet!
          outToRemoteServer.writeObject(lsaUpdatePacket);

        } catch (Exception e) {
          String failedToConnectMessage =
              "\n\nError: Failed to send data to remote IP "
                  + remoteRouterDescription.simulatedIpAddress;
          RouterUtils.alertExceptionToConsole(e, failedToConnectMessage);
          // important to raise exception here to defer control flow & close connections
          throw e;
        }

      } catch (Exception e) {
        String alertMessageOfFailedHelloBroadcast =
            "\n\nError: Failed to broadcast LsaUpdate for ( link = " + curLink + " ) \n\n";
        RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedHelloBroadcast);
      }

      // TODO: rather than leaving client to close connection, maybe close after ACK?
      //      finally {
//        RouterUtils.closeIoSocketConnection(clientSocket, null, outToRemoteServer);
//      }
    }
  }

  /**
   * Broadcast Hello to neighbors.
   */
  private void processStart() {

    // boolean to flag attempt at HELLO broadcast
    boolean attemptedHelloBroadcast = false;

    // declare local variables reused over iterations
    RouterDescription remoteRouterDescription;
    String remoteSimulatedIp;
    String remoteProcessIp;
    short remoteProcessPortNumber;
    short curLinkWeight;
    Socket clientSocket = null;
    ObjectInputStream inFromRemoteServer = null;
    ObjectOutputStream outToRemoteServer = null;

    // iterate over each link in our ports array
    for (Link curLink : ports) {

      if (curLink == null) {
        // skip this port
        continue;
      }

      // found a link!
      attemptedHelloBroadcast = true;

      // let's prepare what we need to request a connection
      remoteRouterDescription = curLink.targetRouter;
      remoteSimulatedIp = remoteRouterDescription.simulatedIpAddress;
      remoteProcessIp = remoteRouterDescription.processIpAddress;
      remoteProcessPortNumber = remoteRouterDescription.processPortNumber;
      curLinkWeight = curLink.weight;

      SospfPacket helloBroadcastPacket = null;

      try {
        try {
          // let's attempt a connection
          clientSocket = new Socket(remoteProcessIp, remoteProcessPortNumber);
          // IMPORTANT: must establish output stream first to enable input stream setup
          outToRemoteServer = new ObjectOutputStream(clientSocket.getOutputStream());
          inFromRemoteServer = new ObjectInputStream(clientSocket.getInputStream());

          // successfully connected, let's get our SospfPacket ready

          helloBroadcastPacket = RouterUtils.buildSospfPacketFromRouterDescriptions(
              this.rd, remoteRouterDescription,
              SospfPacket.SOSPF_HELLO, null, curLinkWeight
          );

          // time to send our HELLO packet!
          outToRemoteServer.writeObject(helloBroadcastPacket);

        } catch (Exception e) {
          String failedToConnectMessage =
              "\n\nError: Failed to send data to remote IP " + remoteSimulatedIp;
          RouterUtils.alertExceptionToConsole(e, failedToConnectMessage);
          // important to raise exception here to defer control flow & close connections
          throw e;
        }

        // having made it this far, we now proceed to wait for a reply

        // blocking wait to deserialize SospfPacket response
        SospfPacket responseFromRemote =
            RouterUtils.deserializeSospfPacketFromInputStream(inFromRemoteServer, clientSocket);

        try {
          // the moment of truth: handle the reply to our HELLO broadcast!
          RouterUtils.handleHelloReplyAtClient(this, curLink, responseFromRemote);
          // time to send the final HELLO packet at this link!
          outToRemoteServer.writeObject(helloBroadcastPacket);

        } catch (Exception e) {
          String alertMessageOfFailedHelloResponseHandling =
              "\n\nError: Failed to handle final HELLO over client connection at socket "
                  + clientSocket + " \n\n";
          RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedHelloResponseHandling);
          // important to raise exception here to defer control flow
          throw e;
        }

        /* ** DESIGN DECISION **
         *
         * At this stage, surviving the HELLO conversation, we must also LSAUPDATE the remote.
         *
         * By design, the client will synchronously wait for our initial LSAUPDATE before
         * attempting to send its own (as a means to avoid stepping over each other's socket setup).
         */

        try {
          // first, get the state of our lsd as a vector of the database values
          Vector<LinkStateAdvertisement> lsaArray = lsd.getValuesVector();

          // from this, we can construct our lsaUpdatePacket
          SospfPacket lsaUpdatePacket = RouterUtils.buildSospfPacketFromRouterDescriptions(
              this.rd, remoteRouterDescription,
              SospfPacket.SOSPF_LSAUPDATE, lsaArray, SospfPacket.IRRELEVANT_TRANSMISSION_WEIGHT
          );

          // time to send our LSAUPDATE packet!
          outToRemoteServer.writeObject(lsaUpdatePacket);
        } catch (Exception e) {
          String alertMessageOfFailedSendLsaUpdate =
              "\n\nError: Failed to send LSAUPDATE over client connection at socket "
                  + clientSocket + " \n\n";
          RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedSendLsaUpdate);
          // important to raise exception here to defer control flow
          throw e;
        }

        // ** analogous to our writing to the remote, we now wait on the remote's response **
        // blocking wait to deserialize another SospfPacket response
        responseFromRemote =
            RouterUtils.deserializeSospfPacketFromInputStream(inFromRemoteServer, clientSocket);

        try {
          // now, let's actually process the state changes
          processStateChangesOfLsaUpdate(responseFromRemote);
        } catch (Exception e) {
          String alertMessageOfFailedLsaUpdateResponseHandling =
              "\n\nError: Failed to handle remote's initial LSAUPDATE over active connection "
                  + "at socket " + clientSocket + " \n\n";
          RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedLsaUpdateResponseHandling);
          // important to raise exception here to defer control flow
          throw e;
        }

      } catch (Exception e) {
        String alertMessageOfFailedHelloBroadcast =
            "\n\nError: Failed to broadcast hello for ( link = " + curLink + " ) \n\n";
        RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedHelloBroadcast);
      } finally {
        RouterUtils.closeIoSocketConnection(clientSocket, inFromRemoteServer, outToRemoteServer);
      }

      // having synchronously resolved our client connection,
      // we now asynchronously update other neighbors
      broadcastLsaUpdateWithExcludedRemote(remoteSimulatedIp);
    }

    if (!attemptedHelloBroadcast) {
      System.out.println("\n\nNo attached links for which to start HELLO broadcast.\n\n");
    }
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
    // clear some space for our upcoming console output
    System.out.println("\n\n");
    // iterate over each link in our ports array to identify each neighbour
    boolean foundAtLeastOneAttached = false;
    int curPortIndex = 0;
    for (Link curLink : ports) {
      if (curLink != null) {
        // found a neighbor: output this to the console
        foundAtLeastOneAttached = true;
        RouterDescription remoteRouterDescription = curLink.targetRouter;
        if (remoteRouterDescription.status == RouterStatus.TWO_WAY) {
          System.out.println(
              "The IP Address of the TWO_WAY neighbour linked at outbound port index "
                  + curPortIndex + " is: " + curLink.targetRouter.simulatedIpAddress + "\n");
        } else {
          System.out.println(
              "An attached router (lacking TWO_WAY status) is attached at outbound port index "
                  + curPortIndex + " with IP address: "
                  + curLink.targetRouter.simulatedIpAddress + "\n");
        }
      }
      curPortIndex += 1;
    }
    if (!foundAtLeastOneAttached) {
      // no neighboring routers attached to Link[] ports array
      System.out.println(
          "No neighbouring routers are currently linked to our outbound ports.\n");
    }
    System.out.println("\n");
  }

  /**
   * Disconnect with all neighbors and quit the program.
   */

  private void processQuit() {

  }

  /**
   * Getter of the simulated IP address for this router.
   */
  String getSimulatedIpAddress() {
    return this.rd.simulatedIpAddress;
  }

  /**
   * Synchronized reader of last stored LSA for this router.
   */
  synchronized LinkStateAdvertisement getLastLinkStateAdvertisement() {
    return this.lsd.getLastLinkStateAdvertisement(this.rd.simulatedIpAddress);
  }

  /**
   * Synchronized writer of an input LSA for this writer.
   */
  private synchronized void putLinkStateAdvertisement(
      LinkStateAdvertisement linkStateAdvertisement) {
    lsd.putLinkStateAdvertisement(this.rd.simulatedIpAddress, linkStateAdvertisement);
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
      try {
        while (true) {
          try {
            if (command.startsWith("detect")) {
              String[] cmdLine = command.split(" ");
              processDetect(cmdLine[1]);
            } else if (command.startsWith("disconnect")) {
              String[] cmdLine = command.split(" ");
              processDisconnect(Short.parseShort(cmdLine[1]));
            } else if (command.startsWith("quit")) {
              processQuit();
            } else if (command.startsWith("attach")) {
              String[] cmdLine = command.split(" ");
              processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
            } else if (command.equals("start")) {
              processStart();
            } else if (command.equals("connect")) {
              String[] cmdLine = command.split(" ");
              processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
            } else if (command.equals("neighbors")) {
              // output neighbors
              processNeighbors();
            } else {
              // invalid command
              System.out.println("\n\nCommand '" + command + "' was not recognized.");
              System.out.println("Please enter a valid command.\n\n");
            }
          } catch (Exception e) {
            String alertMessageOfFailedCommandExecution =
                "\n\nError: failed to execute user input '" + command + "'.";
            RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedCommandExecution);
          } finally {
            System.out.print(">> ");
            command = br.readLine();
          }
        }
      } catch (Exception e) {
        // close streams before crashing terminal
        isReader.close();
        br.close();
        throw e;
      }
    } catch (Exception e) {
      String alertMessageOfFailedTerminal =
          "\n\nError: router's terminal interface crashed.";
      RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedTerminal);
    }
  }

  /**
   * Job to manage lifecycle of an active connection with client router.
   */
  public class RequestHandlerJob implements Runnable {

    /**
     * Active socket for client-server connection handled by this job.
     */
    private final Socket activeSocket;

    /**
     * Input stream over active socket connection.
     */
    private ObjectInputStream inFromRemoteServer;

    /**
     * Output stream over active socket connection.
     */
    private ObjectOutputStream outToRemoteServer;

    /**
     * Helper method to route inputRequestPacket to appropriate sub-handler.
     */
    private void handleRequestpacket(SospfPacket inputRequestPacket) throws Exception {
      try {
        if (inputRequestPacket == null) {
          throw new Exception("Received null input request packet!");
        }
        short packetType = inputRequestPacket.sospfType;
        switch (packetType) {
          case SospfPacket.SOSPF_NO_PORTS_AVAILABLE:
            String exceptionMessageOfInvalidSospfType =
                "Received packet at RequestJobHandler "
                    + "with invalid (SospfPacketType = " + packetType + " ) ";
            throw new Exception(exceptionMessageOfInvalidSospfType);
          case SospfPacket.SOSPF_HELLO:
            handleHelloRequest(inputRequestPacket);
            break;
          case SospfPacket.SOSPF_LSAUPDATE:
            handleLsaUpdateRequest(inputRequestPacket);
            break;
          default:
            String exceptionMessageOfFailedSospfPacketRouting =
                "Received packet with unknown (SospfPacketType = " + packetType + " ) ";
            throw new Exception(exceptionMessageOfFailedSospfPacketRouting);
        }
      } catch (Exception e) {
        String alertMessageOfFailedRequestRouting =
            "\n\nError: Failed to route input request to appropriate sub-handler for packet "
                + inputRequestPacket + " \n\n";
        RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedRequestRouting);
        // important to raise exception here to defer control flow & close connections
        throw e;
      }
    }

    /**
     * Helper method to wrap logic of handling HELLO request.
     */
    private void handleHelloRequest(SospfPacket inputRequestPacket) {
      try {
        if (inputRequestPacket == null) {
          throw new Exception("Received null input request packet!");
        }

        // ready client router properties
        String clientProcessIpAddress = inputRequestPacket.srcProcessIp;
        short clientProcessPortNumber = inputRequestPacket.srcProcessPort;
        String clientSimulatedIpAddress = inputRequestPacket.srcIp;
        short weightOfTransmission = inputRequestPacket.weightOfTransmission;

        // ** ESSENTIAL PRINT STATEMENT FOR PA1 DELIVERABLE **
        System.out.println("\n\nreceived HELLO from " + clientSimulatedIpAddress + ";");

        // find port at which to link remote router
        int indexOfPort = RouterUtils.findIndexOfFreePort(ports, clientSimulatedIpAddress);

        // let's see what we found
        switch (indexOfPort) {
          case RouterUtils.NO_PORT_AVAILABLE_FLAG:
            System.out.println("\n\nNo free port available on current router at this time.\n\n");
            // if there is no free port, we'll notify the client and terminate
            SospfPacket responsePacket = new SospfPacket(
                rd.processIpAddress, rd.processPortNumber, rd.simulatedIpAddress,
                clientSimulatedIpAddress, SospfPacket.SOSPF_NO_PORTS_AVAILABLE,
                rd.simulatedIpAddress, rd.simulatedIpAddress, null, weightOfTransmission
            );
            outToRemoteServer.writeObject(responsePacket);
            // ** terminate here! **
            return;
          case RouterUtils.DUPLICATE_ATTACHMENT_ATTEMPT_FLAG:
            // ** critical assumption **
            // act as usual on encountering duplicate attachment
            System.out.println(
                "\n(NOTE: Found existing attachment for client router at IP "
                    + clientSimulatedIpAddress
                    + ").\nWe shall proceed regardless with the HELLO conversation by design..."
            );
            // the sender was already linked with one of our ports, let's get that index
            indexOfPort =
                RouterUtils.findIndexOfPortAttachedTo(ports, clientSimulatedIpAddress);
            break;
          default:
            // first time we've seen the client router: let's create a description for it
            RouterDescription clientDescription =
                new RouterDescription(
                    clientProcessIpAddress, clientProcessPortNumber,
                    clientSimulatedIpAddress, RouterStatus.UNKNOWN,
                    weightOfTransmission
                );
            // finally: we attach (for the first time) to the client here
            ports[indexOfPort] = new Link(rd, clientDescription);
        }

        Link linkWithClient = ports[indexOfPort];

        // set the status of the client router to INIT
        // ** critical assumption **
        // do this even if link already exists
        linkWithClient.targetRouter.status = RouterStatus.INIT;
        // to be safe, we'll also set our status at the same time...
        linkWithClient.originRouter.status = RouterStatus.INIT;

        // ** ESSENTIAL PRINT STATEMENT FOR PA1 DELIVERABLE **
        System.out.println("\nset " + clientSimulatedIpAddress + " state to INIT;");

        // construct response packet (first HELLO reply)
        SospfPacket replyToClient = new SospfPacket(
            rd.processIpAddress, rd.processPortNumber, rd.simulatedIpAddress,
            clientSimulatedIpAddress, SospfPacket.SOSPF_HELLO,
            rd.simulatedIpAddress, rd.simulatedIpAddress, null, weightOfTransmission
        );

        // send response packet to client
        outToRemoteServer.writeObject(replyToClient);

        // blocking wait to deserialize SospfPacket response
        SospfPacket responseFromClient =
            RouterUtils.deserializeSospfPacketFromInputStream(inFromRemoteServer, activeSocket);

        try {
          // the moment of truth: handle the HELLO packet!
          RouterUtils.handleHelloReplyAtRemote(linkWithClient, responseFromClient);
        } catch (Exception e) {
          String alertMessageOfFailedHelloResponseHandling =
              "\n\nError: Failed to handle client's final HELLO over active connection "
                  + "at socket " + activeSocket + " \n\n";
          RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedHelloResponseHandling);
          // important to raise exception here to defer control flow
          throw e;
        }

        /* ** DESIGN DECISION **
         *
         * At this stage, surviving the HELLO conversation, we expect an LSAUPDATE from the client.
         *
         * By design, we will synchronously wait for the client's initial LSAUPDATE before
         * attempting to send our own (as a means to avoid stepping over each other's socket setup).
         */

        // blocking wait to deserialize another SospfPacket response
        responseFromClient =
            RouterUtils.deserializeSospfPacketFromInputStream(inFromRemoteServer, activeSocket);

        try {
          // now, let's actually process the state changes
          processStateChangesOfLsaUpdate(responseFromClient);
        } catch (Exception e) {
          String alertMessageOfFailedLsaUpdateResponseHandling =
              "\n\nError: Failed to handle client's initial LSAUPDATE over active connection "
                  + "at socket " + activeSocket + " \n\n";
          RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedLsaUpdateResponseHandling);
          // important to raise exception here to defer control flow
          throw e;
        }

        // ** by this point, we should have covered any case requiring updates to our LSD **

        // update our link state database with the results of this conversation
        writeLinkStateOfThisRouterToDatabase();

        // ** analogous to our waiting on the client, the client now waits on our LSAUPDATE **

        try {
          // first, get the state of our lsd as a vector of the database values
          Vector<LinkStateAdvertisement> lsaArray = lsd.getValuesVector();

          // from this, we can construct our lsaUpdatePacket
          SospfPacket lsaUpdatePacket = new SospfPacket(
              rd.processIpAddress, rd.processPortNumber, rd.simulatedIpAddress,
              clientSimulatedIpAddress, SospfPacket.SOSPF_LSAUPDATE,
              rd.simulatedIpAddress, rd.simulatedIpAddress, lsaArray,
              SospfPacket.IRRELEVANT_TRANSMISSION_WEIGHT
          );

          // time to send our LSAUPDATE packet!
          outToRemoteServer.writeObject(lsaUpdatePacket);
        } catch (Exception e) {
          String alertMessageOfFailedSendLsaUpdate =
              "\n\nError: Failed to send LSAUPDATE over client connection at socket "
                  + activeSocket + " \n\n";
          RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedSendLsaUpdate);
          // important to raise exception here to defer control flow
          throw e;
        }

        // having synchronously updated the client, we now asynchronously update other neighbors
        broadcastLsaUpdateWithExcludedRemote(clientSimulatedIpAddress);

      } catch (Exception e) {
        String alertMessageOfFailedHelloHandling =
            "\n\nError: Failed to handle HELLO request for packet '"
                + inputRequestPacket + "' \n\n";
        RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedHelloHandling);
      }
    }

    /**
     * Helper method to wrap logic of handling LSAUPDATE request.
     */
    private void handleLsaUpdateRequest(SospfPacket inputRequestPacket) {
      try {
        if (inputRequestPacket == null) {
          throw new Exception("Received null input request packet!");
        }

        // ready client router properties
        String clientSimulatedIpAddress = inputRequestPacket.srcIp;

        // before proceeding, we need to remember if this is the first time we've seen the client
        boolean firstTimeClientSeen =
            lsd.getLastLinkStateAdvertisement(clientSimulatedIpAddress) == null;

        // now, let's actually process the state changes
        processStateChangesOfLsaUpdate(inputRequestPacket);

        // ** by this point, we should have covered any case requiring updates to our LSD **

        // broadcast any changes of the LSD to our neighbors
        if (firstTimeClientSeen) {
          // including the client
          broadcastLsaUpdateToAllNeighbors();
        } else {
          // TODO: of course, we should verify the below assumption in practice...
        /* ** CRITICAL ASSUMPTION **
         *
         * Below, to avoid infinite LSAUPDATES, we exclude the client that initiated this handling.
         *
         * By doing this, the client will technically not see the corresponding LSA for changing
         * the weight of one of our own links. **However** (provided our reasoning holds), this
         * should not matter since the only information lost (ie. missing from the client's LSA)
         * will be the following:
         *
         * 1. The sequence number of this router's latest LSA will not be updated.
         * 2. A single linkDescription in that router's LSA has changed (due to the changed weight).
         *
         * With regards to (2), the client router will already know this since it was the one to
         * broadcast that information. With regards to (1), the incorrect sequence number is not an
         * issue on its own since the client router effectively has the state of the latest sequence
         * number already. Consequently, since the client router's sequence number must then be
         * lower than any sequence numbers following the missed LSA, it would receive these updates
         * instead to reflect the latest state changes.
         */

          // excluding the client
          broadcastLsaUpdateWithExcludedRemote(clientSimulatedIpAddress);
        }

      } catch (Exception e) {
        String alertMessageOfFailedLsaUpdateHandling =
            "\n\nError: Failed to handle LSAUPDATE request for packet '"
                + inputRequestPacket + "' \n\n";
        RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedLsaUpdateHandling);
      }
    }

    /**
     * Instantiate RequestHandlerJob to handle a single request on behalf of our input Router.
     */
    RequestHandlerJob(Socket activeSocket) {
      this.activeSocket = activeSocket;
    }

    /**
     * RequestHandlerJob's execution routine to handle a single request.
     */
    public void run() {
      try {
        try {
          inFromRemoteServer = new ObjectInputStream(activeSocket.getInputStream());
          outToRemoteServer = new ObjectOutputStream(activeSocket.getOutputStream());
        } catch (Exception e) {
          String alertMessageOfFailedConnectionAttempt =
              "\n\nError: Failed to establish connection to handle request at socket "
                  + activeSocket + " \n\n";
          RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedConnectionAttempt);
          // important to raise exception here to defer control flow & close connections
          throw e;
        }
        // attempt to deserialize packet to expected SospfPacket object
        SospfPacket inputRequestPacket =
            RouterUtils.deserializeSospfPacketFromInputStream(inFromRemoteServer, activeSocket);
        try {
          // fail silently if the deserialized packet was null
          if (inputRequestPacket != null) {
            // the moment of truth: handle the packet!
            handleRequestpacket(inputRequestPacket);
          }
        } catch (Exception e) {
          String alertMessageOfFailedRequestHandling =
              "\n\nError: Failed to handle request of active connection at socket "
                  + activeSocket + " \n\n";
          RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedRequestHandling);
          // important to raise exception here to defer control flow & close connections
          throw e;
        }
      } catch (Exception e) {
        String alertMessageOfCrashedRequestHandlerJob =
            "\n\nError: RequestHandlerJob crashed for active connection at socket "
                + activeSocket + " \n\n";
        RouterUtils.alertExceptionToConsole(e, alertMessageOfCrashedRequestHandlerJob);
        System.out.print(">> ");
      } finally {
        RouterUtils.closeIoSocketConnection(activeSocket, inFromRemoteServer, outToRemoteServer);
      }
    }
  }

  /**
   * Job to manage lifecycle of Router's exposed ServerSocket.
   */
  public class RouterServerJob implements Runnable {

    /**
     * ServerSocket at which we will persistently listen for incoming requests.
     */
    private final ServerSocket serverSocket;

    /**
     * Instantiate RouterServerJob to listen for incoming requests on behalf of input Router.
     */
    RouterServerJob(ServerSocket serverSocket) {
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

          if (activeSocket == null) {
            // would represent a weird edge case for which we fail silently
            continue;
          }

          // reaching here means we have accepted an incoming message
          // let's create an active connection thread to handle the incoming data
          RequestHandlerJob requestHandlerJob = new RequestHandlerJob(activeSocket);
          Thread activeConnectionLifecycle = new Thread(requestHandlerJob);
          activeConnectionLifecycle.start();
        }
      } catch (Exception e) {
        String jobCrashedMessage =
            "\n\nRouterServerJob crashed for router IP " + rd.simulatedIpAddress + " \n\n";
        RouterUtils.alertExceptionToConsole(e, jobCrashedMessage);
        System.out.print(">> ");
      }
    }
  }
}
