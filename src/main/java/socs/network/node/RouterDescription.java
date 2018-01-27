package socs.network.node;

/**
 * Description summarizing state of router (ie. single node) in our network.
 */
public class RouterDescription {

  /**
   * Process IP tied to this router's exposed socket instance.
   */
  String processIpAddress;

  /**
   * Process port number tied to this router's exposed socket instance.
   */
  short processPortNumber;


  /**
   * Unique IP address to identify the router in our simulated network space.
   */
  String simulatedIpAddress;


  /**
   * Router's current discrete category of RouterStatus Enum.
   */
  RouterStatus status;
}
