package socs.network.node;

/**
 * Specifies link between two routers.
 */
public class Link {

  /**
   * Description of origin router (client) which instantiated the link.
   */
  RouterDescription originRouter;

  /**
   * Description for target router (server) of link.
   */
  RouterDescription targetRouter;

  /**
   * Weight of link between the origin and target router.
   */
  short weight;

  /**
   * Instantiate Link between two routers (based on description).
   */
  public Link(RouterDescription originRouterDescription,
      RouterDescription targetRouterDescription) {
    if (originRouterDescription == null) {
      throw new IllegalArgumentException("Cannot instantiate a link with a null origin router.");
    }
    if (targetRouterDescription == null) {
      throw new IllegalArgumentException("Cannot instantiate a link with a null target router.");
    }
    originRouter = originRouterDescription;
    targetRouter = targetRouterDescription;
    weight = targetRouterDescription.weightToAttemptTransmission;
  }
}
