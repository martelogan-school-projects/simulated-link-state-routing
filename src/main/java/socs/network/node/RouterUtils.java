/*
 * simulated-link-state-routing
 * Copyright (C) 2018, Logan Martel
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package socs.network.node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Vector;
import socs.network.message.LinkDescription;
import socs.network.message.LinkStateAdvertisement;
import socs.network.message.SospfPacket;

/**
 * Static utilities class to assist Router instances.
 */
final class RouterUtils {

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
   * Static method to instantiate Sospf Packet via router descriptions.
   */
  static SospfPacket buildSospfPacketFromRouterDescriptions(
      RouterDescription srcRouter, RouterDescription destRouter, short sospfType,
      Vector<LinkStateAdvertisement> lsaArray, short weightOfTransmission) {
    // FIXME: is below the intended use case of neighbor and router id?
    String srcSimulatedIpAndNeighbourIdAndRouterId = srcRouter.simulatedIpAddress;
    return new SospfPacket(
        srcRouter.processIpAddress, srcRouter.processPortNumber,
        srcSimulatedIpAndNeighbourIdAndRouterId, destRouter.simulatedIpAddress,
        sospfType, srcSimulatedIpAndNeighbourIdAndRouterId, srcSimulatedIpAndNeighbourIdAndRouterId,
        lsaArray, weightOfTransmission

    );
  }

  /**
   * Static helper method to construct LinkedList of LinkDescriptions from advertiser's Link[] ports
   * array.
   */
  static LinkedList<LinkDescription> getListOfLinkDescriptions(
      RouterDescription rd, Link[] ports) {
    LinkDescription linkDescriptionOfActiveRouter = new LinkDescription(
        rd.simulatedIpAddress,
        rd.processPortNumber,
        (short) RouterDescription.TRANSMISSION_WEIGHT_TO_SELF
    );
    LinkedList<LinkDescription> linkedListOfLinkDescriptions = new LinkedList<LinkDescription>();
    linkedListOfLinkDescriptions.add(linkDescriptionOfActiveRouter);
    for (Link curLink : ports) {
      // skip any null links
      if (curLink == null) {
        continue;
      }

      // prepare some details about the remote router
      RouterDescription remoteRouterDescription = curLink.targetRouter;
      RouterStatus remoteRouterStatus = remoteRouterDescription.status;
      // skip any links with an unclear router status
      if (remoteRouterStatus == null || remoteRouterStatus == RouterStatus.UNKNOWN) {
        continue;
      }

      // create the link description to summarize this connection
      LinkDescription linkDescription = new LinkDescription(
          remoteRouterDescription.simulatedIpAddress,
          remoteRouterDescription.processPortNumber,
          curLink.weight
      );

      // add the linkDescription to our running list
      linkedListOfLinkDescriptions.add(linkDescription);
    }

    return linkedListOfLinkDescriptions;
  }

  /**
   * Static helper method to create a LinkStateAdvertisement for the input router.
   */
  static LinkStateAdvertisement createLinkStateAdvertisement(Router advertisingRouter)
      throws Exception {

    // set link state id to the advertising router's simulated IP address
    String linkStateId = advertisingRouter.getSimulatedIpAddress();

    // retrieve the advertiser's last LSA from its link state database
    LinkStateAdvertisement lastLsa = advertisingRouter.getLastLinkStateAdvertisement();

    if (lastLsa == null) {
      throw new Exception(
          "Router should have a sentinel LSA in the database before calling this method."
      );
    }

    int prevSeqNumber = lastLsa.lsaSeqNumber;
    int newSeqNumber;
    if (prevSeqNumber == LinkStateAdvertisement.NO_PREVIOUS_ADVERTISEMENTS_FLAG) {
      // this link state advertisement is the advertiser's first
      newSeqNumber = LinkStateAdvertisement.MIN_SEQ_NUMBER;
    } else {
      // there was a prior link state advertisement by this router
      // increment the LSA's sequence number
      newSeqNumber = prevSeqNumber + 1;
    }

    // construct LinkedList of LinkDescriptions from advertiser's Link[] ports array
    LinkedList<LinkDescription> newLinks = getListOfLinkDescriptions(
        advertisingRouter.rd, advertisingRouter.ports
    );

    return new LinkStateAdvertisement(linkStateId, newSeqNumber, newLinks);
  }

  /**
   * Static method to verify Link[] ports array and target IP address input combination.
   */
  private static void verifyPortsAndTargetIpNotNull(Link[] ports, String simulatedIpOfTarget) {
    if (ports == null) {
      throw new IllegalArgumentException("Cannot index into null ports array!");
    }
    if (simulatedIpOfTarget == null) {
      throw new IllegalArgumentException("Cannot establish link with null target IP!");
    }
  }

  /**
   * Static boolean helper method to check validity of port index.
   */
  static boolean isPortIndexInvalid(int portIndex) {
    return portIndex < 0 || portIndex > Router.NUM_PORTS_PER_ROUTER;
  }

  /**
   * Static boolean helper method to check validity of actual process port number.
   */
  static boolean isPortNumberInvalid(int portNumber) {
    return portNumber < Router.MIN_PROCESS_PORT_NUMBER
        || portNumber > Router.MAX_PROCESS_PORT_NUMBER;
  }

  /**
   * Finds unoccupied port in input Link[] array (or returns flag indicating error).
   */
  static int findIndexOfFreePort(Link[] ports, String simulatedIpOfTarget) {
    // verify input arguments
    verifyPortsAndTargetIpNotNull(ports, simulatedIpOfTarget);
    // iterate over all ports to verify against duplicate attachment
    int indexOfFreePort = NO_PORT_AVAILABLE_FLAG;
    int curIndex;
    for (curIndex = 0; curIndex < Router.NUM_PORTS_PER_ROUTER; curIndex++) {
      Link curLink = ports[curIndex];
      if (ports[curIndex] == null) {
        // found a free port (continue scanning to verify against duplicate attachment)
        if (indexOfFreePort == NO_PORT_AVAILABLE_FLAG) {
          // only set this if we hadn't already found a free port (prefers lowest index first)
          indexOfFreePort = curIndex;
        }
      } else if (curLink.targetRouter.simulatedIpAddress.equals(simulatedIpOfTarget)) {
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
  static int findIndexOfPortAttachedTo(Link[] ports, String simulatedIpOfTarget) {
    // verify input arguments
    verifyPortsAndTargetIpNotNull(ports, simulatedIpOfTarget);
    // iterate over all ports seeking attachment with simulatedIpOfTarget
    int curIndex;
    for (curIndex = 0; curIndex < Router.NUM_PORTS_PER_ROUTER; curIndex++) {
      Link curLink = ports[curIndex];
      if (ports[curIndex] != null
          && curLink.targetRouter.simulatedIpAddress.equals(simulatedIpOfTarget)) {
        // found the port linked to the simulated IP address of our target router
        return curIndex;
      }
    }
    return NO_PORT_AVAILABLE_FLAG;
  }

  /**
   * Close client-server I/O socket connection.
   */
  static void closeIoSocketConnection(
      Socket ioSocket, ObjectInputStream inFromServer, ObjectOutputStream outToServer) {
    try {
      if (inFromServer != null) {
        inFromServer.close();
      }
      if (outToServer != null) {
        outToServer.flush();
        outToServer.close();
      }
      if (ioSocket != null) {
        ioSocket.close();
      }
    } catch (Exception e) {
      System.out.println(
          "\n\nError: Failed to correctly close connection for socket: "
              + ioSocket + "\n\n");
      e.printStackTrace();
      System.out.println("\n\n");
    }
  }

  /**
   * Static method to pretty print exception alert to console.
   */
  static void alertExceptionToConsole(Exception e, String customMessage) {
    System.out.println(customMessage);
    System.out.println("\n\nFailed with Exception: \n\n");
    e.printStackTrace(System.out);
    System.out.println("\n\n");
  }

  /**
   * Static method to alert response that no ports are available at remote.
   */
  private static void alertNoPortsAvailableAtRemote(SospfPacket responsePacket) {
    String remoteRouterIp = responsePacket.srcIp;
    String alertMessageOfNoPortsAvailableAtTargetRouter =
        "\n\nError: Received response that no ports are available to establish a "
            + "connection at remote with (SimulatedIP = "
            + remoteRouterIp + " )";
    alertMessageOfNoPortsAvailableAtTargetRouter +=
        "\nLocally disconnecting the router's attachment link...\n\n";
    System.out.println(alertMessageOfNoPortsAvailableAtTargetRouter);
  }

  /**
   * Static method to raise exception on receiving invalid response packet at client.
   */
  private static void raiseInvalidResponseException(short packetType) throws Exception {
    String exceptionMessageOfInvalidSospfType =
        "Received packet at client "
            + "with invalid (SospfPacketType = " + packetType + " ) ";
    throw new Exception(exceptionMessageOfInvalidSospfType);
  }

  /**
   * Static method to raise exception on receiving invalid response packet at client.
   */
  private static void raiseUnknownResponseException(short packetType) throws Exception {
    String exceptionMessageOfFailedResponseHandling =
        "Received packet with unknown (SospfPacketType = " + packetType + " ) ";
    throw new Exception(exceptionMessageOfFailedResponseHandling);
  }

  /**
   * Static method to deserialize SospfPacket from given input stream.
   */
  static SospfPacket deserializeSospfPacketFromInputStream(
      ObjectInputStream inFromRemoteServer, Socket activeSocket, boolean suppressEofExceptionAlert)
      throws Exception {
    try {
      if (inFromRemoteServer == null) {
        throw new IllegalArgumentException("Received null input stream!");
      }
      if (activeSocket == null) {
        throw new IllegalArgumentException("Received null socket connection!");
      }
      Object inputRequestRaw = null;
      try {
        // attempt to read raw binary of packet as generic object
        inputRequestRaw = inFromRemoteServer.readObject();
      } catch (SocketException e) {
        // FIXME: might be dangerous to fail here but there are so many connection resets...
        // for socket exceptions, we fail silently
        return null;
      }
      if (inputRequestRaw == null) {
        throw new Exception("Received empty input packet!");
      }
      // attempt to deserialize packet to expected SospfPacket object
      return (SospfPacket) inputRequestRaw;
    } catch (Exception e) {
      if (!suppressEofExceptionAlert) {
        String alertMessageOfFailedInputStreamParsing =
            "\n\nError: Failed to parse input stream to SospfPacket at socket '"
                + activeSocket + "' \n\n";
        RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedInputStreamParsing);
      }
      // important to raise exception here to defer control flow & close connections
      throw e;
    }
  }

  /**
   * Static method to verify link and packet input combination.
   */
  private static void verifyLinkAndPacketNotNull(Link link, SospfPacket packet) {
    if (link == null) {
      throw new IllegalArgumentException("Trying to message over null communication link!");
    }
    if (packet == null) {
      throw new IllegalArgumentException("Trying to send null response packet!");
    }
  }

  /**
   * Static method to respond at client to HELLO broadcast reply.
   */
  private static void respondAtClientToHelloReply(Link curLink, SospfPacket responsePacket) {
    // verify input arguments
    verifyLinkAndPacketNotNull(curLink, responsePacket);
    // ** ESSENTIAL PRINT STATEMENT FOR PA1 DELIVERABLE **
    String remotedSimulatedIpAddress = responsePacket.srcIp;
    System.out.println("\nreceived HELLO from " + remotedSimulatedIpAddress + ";");
    // importantly, we can now finally set remote RouterStatus to TWO_WAY
    curLink.targetRouter.status = RouterStatus.TWO_WAY;
    // to be safe, we'll also set our status at the same time...
    curLink.originRouter.status = RouterStatus.INIT;
    // ** ESSENTIAL PRINT STATEMENT FOR PA1 DELIVERABLE **
    System.out.println("\nset " + remotedSimulatedIpAddress + " state to TWO_WAY;\n");
  }

  /**
   * Static helper method to route and handle reply to HELLO broadcast at client.
   */
  static void handleHelloReplyAtClient(Router clientRouter,
      Link curLink, SospfPacket responsePacket) throws Exception {
    // verify input arguments
    if (clientRouter == null) {
      throw new IllegalArgumentException("Trying to handle reply at null client router!");
    }
    verifyLinkAndPacketNotNull(curLink, responsePacket);
    // handle response depending on SOSPF status
    short packetType = responsePacket.sospfType;
    switch (packetType) {
      case SospfPacket.SOSPF_NO_PORTS_AVAILABLE:
        // ** critical assumption **
        // disconnect the attachment locally
        int portIndex =
            RouterUtils.findIndexOfPortAttachedTo(clientRouter.ports, responsePacket.srcIp);
        RouterUtils.alertNoPortsAvailableAtRemote(responsePacket);
        clientRouter.detachLinkAtPortIndex(portIndex);
        break;
      case SospfPacket.SOSPF_HELLO:
      case SospfPacket.SOSPF_CONNECT:
        RouterUtils.respondAtClientToHelloReply(curLink, responsePacket);
        break;
      case SospfPacket.SOSPF_LSAUPDATE:
      case SospfPacket.SOSPF_DISCONNECT:
      case SospfPacket.SOSPF_HEARTBEAT:
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
    // verify input arguments
    verifyLinkAndPacketNotNull(curLink, responsePacket);
    // ** ESSENTIAL PRINT STATEMENT FOR PA1 DELIVERABLE **
    String remotedSimulatedIpAddress = responsePacket.srcIp;
    System.out.println("\nreceived HELLO from " + remotedSimulatedIpAddress + ";");
    // importantly, we can now finally set remote RouterStatus to TWO_WAY
    curLink.targetRouter.status = RouterStatus.TWO_WAY;
    // to be safe, we'll also set our status at the same time...
    curLink.originRouter.status = RouterStatus.TWO_WAY;
    // ** ESSENTIAL PRINT STATEMENT FOR PA1 DELIVERABLE **
    System.out.println("\nset " + remotedSimulatedIpAddress + " state to TWO_WAY;\n");
    // add the prompt back
    System.out.print(">> ");
  }

  /**
   * Static helper method to route and handle reply to HELLO broadcast at remote router.
   */
  static void handleHelloReplyAtRemote(Link curLink, SospfPacket responsePacket)
      throws Exception {
    // verify input arguments
    verifyLinkAndPacketNotNull(curLink, responsePacket);
    // handle response depending on SOSPF status
    short packetType = responsePacket.sospfType;
    switch (packetType) {
      case SospfPacket.SOSPF_HELLO:
      case SospfPacket.SOSPF_CONNECT:
        RouterUtils.respondAtRemoteToHelloReply(curLink, responsePacket);
        break;
      case SospfPacket.SOSPF_LSAUPDATE:
      case SospfPacket.SOSPF_DISCONNECT:
      case SospfPacket.SOSPF_HEARTBEAT:
      case SospfPacket.SOSPF_NO_PORTS_AVAILABLE:
        RouterUtils.raiseInvalidResponseException(packetType);
        break;
      default:
        RouterUtils.raiseUnknownResponseException(packetType);
    }
  }
}
