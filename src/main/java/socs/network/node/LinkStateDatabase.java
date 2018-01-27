package socs.network.node;

import java.util.HashMap;
import socs.network.message.LinkDescription;
import socs.network.message.LinkStateAdvertisement;

/**
 * Encapsulating class to maintain data on LSA's broadcasted by the routers.
 */
public class LinkStateDatabase {

  /**
   * Data store to map linkID (ie. simulated IP) => LinkStateAdvertisement instance.
   */
  HashMap<String, LinkStateAdvertisement> dataStore = new HashMap<String, LinkStateAdvertisement>();

  /**
   * Private description for the router maintaining this Link State Database.
   */
  private RouterDescription rd = null;

  /**
   * Initialize database with single entry for this router.
   */
  private LinkStateAdvertisement initLinkStateDatabase() {
    LinkStateAdvertisement lsa = new LinkStateAdvertisement();
    lsa.linkStateId = rd.simulatedIpAddress;
    lsa.lsaSeqNumber = Integer.MIN_VALUE;
    LinkDescription ld = new LinkDescription();
    ld.linkId = rd.simulatedIpAddress;
    ld.processPortNum = -1;
    ld.tosMetrics = 0;
    lsa.links.add(ld);
    return lsa;
  }

  /**
   * Construct LinkStateDatabase based on routerDescription instance.
   */
  public LinkStateDatabase(RouterDescription routerDescription) {
    rd = routerDescription;
    LinkStateAdvertisement l = initLinkStateDatabase();
    dataStore.put(l.linkStateId, l);
  }

  /**
   * Output the shortest path from this router to the destination with the given IP address.
   */
  String getShortestPath(String destinationIp) {
    //TODO: fill the implementation here
    return null;
  }

  /**
   * Convey string representation of Link State Database instance.
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (LinkStateAdvertisement lsa : dataStore.values()) {
      sb.append(lsa.linkStateId).append("(").append(lsa.lsaSeqNumber).append(")").append(":\t");
      for (LinkDescription ld : lsa.links) {
        sb.append(ld.linkId).append(",").append(ld.processPortNum).append(",")
            .append(ld.tosMetrics).append("\t");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
