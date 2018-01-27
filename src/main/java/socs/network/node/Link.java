package socs.network.node;

/**
 * Specifies link between two routers.
 */
public class Link {

  /**
   * Description of origin router (client) which instantiated the link.
   */
  RouterDescription router1;

  /**
   * Description for target router (server) of link.
   */
  RouterDescription router2;

  public Link(RouterDescription r1, RouterDescription r2) {
    router1 = r1;
    router2 = r2;
  }
}
