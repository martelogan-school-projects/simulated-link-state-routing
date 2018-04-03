package socs.network.node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.TimerTask;
import socs.network.message.LinkStateAdvertisement;
import socs.network.message.SospfPacket;

/**
 * Task to coordinate pinging each of a given router's neighbors to check for life.
 */
public class HeartbeatTask extends TimerTask {

  /**
   * Router for which this heartbeat task is responsible.
   */
  private final Router routerToNotify;

  /**
   * Constructor of HeartbeatTask (requiring a router at which to attach).
   */
  public HeartbeatTask(Router routerToNotify) {
    this.routerToNotify = routerToNotify;
  }

  /**
   * Scheduled task of our heartbeat mechanism.
   */
  @Override
  public void run() {
    try {
      initHeartbeatCycle();
    } catch (Exception e) {
      String alertMessageOfFailedHeartbeatCycle =
          "\n\nError: Heartbeat cycle failed unexpectedly. \n\n";
      RouterUtils.alertExceptionToConsole(e, alertMessageOfFailedHeartbeatCycle);
    }
  }

  /**
   * Method to initiate pinging each of our neighbors to check for life.
   */
  private void initHeartbeatCycle() throws Exception {
    // flag to check whether any state changed due to heartbeats
    boolean heartbeatCycleHasChangedLsdState = false;

    // prepare some reusable variables
    Socket clientSocket = null;
    ObjectInputStream inFromRemoteServer = null;
    ObjectOutputStream outToRemoteServer = null;
    RouterDescription remoteRouterDescription;
    String remoteProcessIp;
    int remoteProcessPortNumber;
    short curLinkWeight;
    // iterate over each link in our ports array to identify each neighbour
    int portIndexOfCurLink;
    Link[] ports = routerToNotify.ports;
    for (portIndexOfCurLink = 0; portIndexOfCurLink < ports.length; portIndexOfCurLink++) {
      Link curLink = ports[portIndexOfCurLink];
      if (curLink != null) {
        remoteRouterDescription = curLink.targetRouter;
        if (remoteRouterDescription.status == RouterStatus.TWO_WAY) {
          // found a neighbor: let's try to ping it
          int numRetries = 0;
          // retry as often as allowed
          while (numRetries < Router.HEARTBEAT_MAX_RETRY) {
            try {
              // let's prepare what we need to request a connection
              remoteProcessIp = remoteRouterDescription.processIpAddress;
              remoteProcessPortNumber = remoteRouterDescription.processPortNumber;
              curLinkWeight = curLink.weight;

              SospfPacket heartbeatPacket = null;

              try {
                try {
                  // let's attempt a connection
                  clientSocket = new Socket(remoteProcessIp, remoteProcessPortNumber);
                  // IMPORTANT: must establish output stream first to enable input stream setup
                  outToRemoteServer = new ObjectOutputStream(clientSocket.getOutputStream());
                  inFromRemoteServer = new ObjectInputStream(clientSocket.getInputStream());

                  // successfully connected, let's get our SospfPacket ready

                  heartbeatPacket = RouterUtils.buildSospfPacketFromRouterDescriptions(
                      routerToNotify.rd, remoteRouterDescription,
                      SospfPacket.SOSPF_HEARTBEAT, null, curLinkWeight
                  );

                  // time to send our HEARTBEAT packet!
                  outToRemoteServer.writeObject(heartbeatPacket);

                } catch (Exception e) {
                  // important to raise exception here to defer control flow & close connections
                  throw e;
                }

                // having made it this far, we now proceed to wait for a reply

                // blocking wait to deserialize SospfPacket response
                SospfPacket responseFromRemote =
                    RouterUtils.deserializeSospfPacketFromInputStream(inFromRemoteServer,
                        clientSocket, true);

                try {
                  if (responseFromRemote.sospfType != SospfPacket.SOSPF_HEARTBEAT) {
                    throw new Exception("\n\nReceived invalid response packet.\n\n");
                  }
                } catch (Exception e) {
                  // important to raise exception here to defer control flow
                  throw e;
                }
              } catch (Exception e) {
                RouterUtils.closeIoSocketConnection(clientSocket,
                    inFromRemoteServer, outToRemoteServer);
                throw e;
              } finally {
                RouterUtils.closeIoSocketConnection(clientSocket,
                    inFromRemoteServer, outToRemoteServer);
              }
            } catch (Exception e) {
              numRetries += 1;
              continue;
            }
            // break out of the loop if we survive without exceptions
            break;
          }
          // verify whether we failed to ping our neighbor
          if (numRetries >= Router.HEARTBEAT_MAX_RETRY) {

            if (ports[portIndexOfCurLink] == null) {
              // link has already been explicitly detached...let's not worry about it
              continue;
            }

            String neighborIpAddress = remoteRouterDescription.simulatedIpAddress;

            System.out.println("\n\nNo heartbeat heard for neighbor with IP: "
                + neighborIpAddress + "\n\n");
            routerToNotify.detachLinkAtPortIndex(portIndexOfCurLink);
            System.out.print(">> ");

            // update our link state database with the results of this conversation
            routerToNotify.writeLinkStateOfThisRouterToDatabase();

            // get the latest lsa for the dead neighbor
            LinkStateAdvertisement lastLsaOfNeighbor =
                routerToNotify.getLastLinkStateAdvertisement(neighborIpAddress);

            // flag that the neighbor has died
            lastLsaOfNeighbor.hasShutdown = true;

            // increment the lsa seq number
            lastLsaOfNeighbor.lsaSeqNumber = lastLsaOfNeighbor.lsaSeqNumber + 1;

            // write this to our lsd
            routerToNotify.putLinkStateAdvertisement(neighborIpAddress, lastLsaOfNeighbor);

            heartbeatCycleHasChangedLsdState = true;
          }
        }
      }
    }
    if (heartbeatCycleHasChangedLsdState) {
      // notify our live neighbors of any state changes
      routerToNotify.broadcastLsaUpdateToAllNeighbors();
    }
  }
}
