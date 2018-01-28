package socs.network.node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;
import socs.network.message.LinkStateAdvertisement;
import socs.network.message.SospfPacket;

/**
 * Static utilities class to assist Router instances.
 */
public final class RouterUtils {

  /**
   * Int constant to flag that no port is available at this time.
   */
  static final int NO_PORT_AVAILABLE_FLAG = -1;

  /**
   * Int constant to flag attempt at duplicate attachment.
   */
  static final int DUPLICATE_ATTACHMENT_ATTEMPT_FLAG = -2;

  /**
   * Private constructor to restrict class instantiation.
   */
  private RouterUtils() {
  }

  /**
   * Static helper method to create a RouterDescription via input params.
   */
  public static RouterDescription createRouterDescription(String processIp,
      short processPortNumber, String simulatedIp) {
    RouterDescription rd = new RouterDescription();
    rd.processIpAddress = processIp;
    rd.processPortNumber = processPortNumber;
    rd.simulatedIpAddress = simulatedIp;
    rd.status = RouterStatus.UNKNOWN;
    return rd;
  }

  /**
   * Static helper method to create a SospfPacket via input params.
   */
  public static SospfPacket creatSospfPacket(
      String srcProcessIp, short srcProcessPort,
      String srcSimulatedIp, String destSimulatedIp,
      short sospfType, String routerId,
      String neighborId, Vector<LinkStateAdvertisement> lsaArray) {
    SospfPacket packet = new SospfPacket();
    packet.srcProcessIp = srcProcessIp;
    packet.srcProcessPort = srcProcessPort;
    packet.srcIp = srcSimulatedIp;
    packet.dstIp = destSimulatedIp;
    packet.sospfType = sospfType;
    packet.routerId = routerId;
    packet.neighborId = neighborId;
    packet.lsaArray = lsaArray;
    return packet;
  }

  /**
   * Finds unoccupied port in input Link[] array (or returns flag indicating error).
   */
  public static int findIndexOfFreePort(Link[] ports, String simulatedIpOfTarget) {
    int indexOfFreePort = NO_PORT_AVAILABLE_FLAG;
    int curIndex;
    // iterate over all ports to verify against duplicate attachment
    for (curIndex = 0; curIndex < Router.NUM_PORTS_PER_ROUTER; curIndex++) {
      Link curLink = ports[curIndex];
      if (ports[curIndex] == null) {
        // found a free port (continue scanning to verify against duplicate attachment)
        indexOfFreePort = curIndex;
      } else if (curLink.router2.simulatedIpAddress.equals(simulatedIpOfTarget)) {
        // return immediately to flag duplicate attachment attempt
        return DUPLICATE_ATTACHMENT_ATTEMPT_FLAG;
      }
    }
    return indexOfFreePort;
  }

  /**
   * Finds index of the port in input Link[] array attached to the input remote router (or returns
   * flag indicating error).
   */
  public static int findIndexOfPortAttachedTo(Link[] ports, String simulatedIpOfTarget) {
    int curIndex;
    // iterate over all ports seeking attachment with simulatedIpOfTarget
    for (curIndex = 0; curIndex < Router.NUM_PORTS_PER_ROUTER; curIndex++) {
      Link curLink = ports[curIndex];
      if (ports[curIndex] != null
          && curLink.router2.simulatedIpAddress.equals(simulatedIpOfTarget)) {
        // found the port linked to the simulated IP address of our target router
        return curIndex;
      }
    }
    return NO_PORT_AVAILABLE_FLAG;
  }

  /**
   * Close client-server I/O socket connection.
   */
  public static boolean closeIoSocketConnection(
      Socket ioSocket, ObjectInputStream inFromServer, ObjectOutputStream outToServer) {
    try {
      if (inFromServer != null) {
        inFromServer.close();
      }
      if (outToServer != null) {
        outToServer.close();
      }
      if (ioSocket != null) {
        ioSocket.close();
      }
      return true;
    } catch (Exception e) {
      System.out.println(
          "\n\nError: Failed to correctly close connection for socket: "
              + ioSocket + "\n\n");
      e.printStackTrace();
      System.out.println("\n\n");
      return false;
    }
  }

  /**
   * Static method to format output of process-process communication exception.
   */
  public static void alertInterprocessException(Exception e, String customMessage) {
    System.out.println(customMessage);
    System.out.println("\n\nFailed with Exception: \n\n");
    e.printStackTrace();
    System.out.println("\n\n");
  }

  /**
   * Static method to alert response that no ports are available at remote.
   */
  public static void alertNoPortsAvailableAtRemote(SospfPacket responsePacket) {
    String alertMessageOfNoPortsAvailableAtTargetRouter =
        "\n\n Error: Received response that no ports are available to establish a "
            + "connection at remote with (SimulatedIP = "
            + responsePacket.srcIp + " ) \n\n";
    System.out.println(alertMessageOfNoPortsAvailableAtTargetRouter);
  }

  /**
   * Static method to raise exception on receiving invalid response packet at client.
   */
  public static void raiseInvalidResponseException(short packetType) throws Exception {
    String exceptionMessageOfInvalidSospfType =
        "Received packet at client "
            + "with invalid (SospfPacketType = " + packetType + " ) ";
    throw new Exception(exceptionMessageOfInvalidSospfType);
  }

  /**
   * Static method to raise exception on receiving invalid response packet at client.
   */
  public static void raiseUnknownResponseException(short packetType) throws Exception {
    String exceptionMessageOfFailedResponseHandling =
        "Received packet with unknown (SospfPacketType = " + packetType + " ) ";
    throw new Exception(exceptionMessageOfFailedResponseHandling);
  }

  /**
   * Static method to deserialize SospfPacket from given input stream.
   */
  public static SospfPacket deserializeSospfPacketFromInputStream(
      ObjectInputStream inFromRemoteServer, Socket activeSocket) throws Exception {
    try {
      // attempt to read raw binary of packet as generic object
      Object inputRequestRaw = inFromRemoteServer.readObject();
      if (inputRequestRaw == null) {
        throw new Exception("Received empty input packet!");
      }
      // attempt to deserialize packet to expected SospfPacket object
      return (SospfPacket) inputRequestRaw;
    } catch (Exception e) {
      String alertMessageOfFailedInputStreamParsing =
          "\n\nError: Failed to parse input stream to SospfPacket at socket "
              + activeSocket + " \n\n";
      RouterUtils.alertInterprocessException(e, alertMessageOfFailedInputStreamParsing);
      // important to raise exception here to defer control flow & close connections
      throw e;
    }
  }

  /**
   * Static method to respond at client to HELLO broadcast reply.
   */
  private static void respondAtClientToHelloReply(Link curLink, SospfPacket responsePacket) {
    String remotedSimulatedIpAddress = responsePacket.srcIp;
    // ** ESSENTIAL PRINT STATEMENT FOR PA1 DELIVERABLE **
    System.out.println("\nreceived HELLO from " + remotedSimulatedIpAddress + ";");
    // importantly, we can now finally set remote RouterStatus to TWO_WAY
    curLink.router2.status = RouterStatus.TWO_WAY;
    // ** ESSENTIAL PRINT STATEMENT FOR PA1 DELIVERABLE **
    System.out.println("\nset " + remotedSimulatedIpAddress + " state to TWO_WAY;\n");
  }

  /**
   * Static helper method to route and handle reply to HELLO broadcast at client.
   */
  public static void handleHelloReplyAtClient(SospfPacket responsePacket, Link curLink)
      throws Exception {
    short packetType = responsePacket.sospfType;
    switch (packetType) {
      case SospfPacket.SOSPF_NO_PORTS_AVAILABLE:
        //TODO: should we also disconnect the attachment locally here?
        RouterUtils.alertNoPortsAvailableAtRemote(responsePacket);
        break;
      case SospfPacket.SOSPF_HELLO:
        RouterUtils.respondAtClientToHelloReply(curLink, responsePacket);
        break;
      case SospfPacket.SOSPF_LSAUPDATE:
        RouterUtils.raiseInvalidResponseException(packetType);
        break;
      default:
        RouterUtils.raiseUnknownResponseException(packetType);
    }
  }

  /**
   * Static method to respond at remote to HELLO broadcast reply.
   */
  private static void respondAtRemoteToHelloReply(Link curLink, SospfPacket responsePacket) {
    String remotedSimulatedIpAddress = responsePacket.srcIp;
    // ** ESSENTIAL PRINT STATEMENT FOR PA1 DELIVERABLE **
    System.out.println("\nreceived HELLO from " + remotedSimulatedIpAddress + ";");
    // importantly, we can now finally set remote RouterStatus to TWO_WAY
    curLink.router2.status = RouterStatus.TWO_WAY;
    // ** ESSENTIAL PRINT STATEMENT FOR PA1 DELIVERABLE **
    System.out.println("\nset " + remotedSimulatedIpAddress + " state to TWO_WAY;\n");
    // add the prompt back
    System.out.print(">> ");
  }

  /**
   * Static helper method to route and handle reply to HELLO broadcast at remote router.
   */
  public static void handleHelloReplyAtRemote(SospfPacket responsePacket, Link curLink)
      throws Exception {
    short packetType = responsePacket.sospfType;
    switch (packetType) {
      case SospfPacket.SOSPF_HELLO:
        RouterUtils.respondAtRemoteToHelloReply(curLink, responsePacket);
        break;
      case SospfPacket.SOSPF_LSAUPDATE:
      case SospfPacket.SOSPF_NO_PORTS_AVAILABLE:
        RouterUtils.raiseInvalidResponseException(packetType);
        break;
      default:
        RouterUtils.raiseUnknownResponseException(packetType);
    }
  }

}
