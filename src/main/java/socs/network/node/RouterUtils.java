package socs.network.node;

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
   * Static utilities class to assist Router instances.
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
   * Finds unoccupied port in input Link[] array.
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
}
