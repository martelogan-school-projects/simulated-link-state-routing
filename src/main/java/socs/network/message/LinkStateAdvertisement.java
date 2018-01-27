package socs.network.message;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Broadcast conveying updates to Link Description relations between routers.
 */
public class LinkStateAdvertisement implements Serializable {

  /**
   * Simulated IP address of the router originating this LinkStateAdvertisement.
   */
  public String linkStateId;

  /**
   * Version number imposing total order on sequence of broadcasts for this LSA.
   */
  public int lsaSeqNumber = Integer.MIN_VALUE;

  /**
   * List of Link Descriptions per link associated to this LSA.
   */
  public LinkedList<LinkDescription> links = new LinkedList<LinkDescription>();

  /**
   * Convey string representation of Link State Advertisement instance.
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(linkStateId + ":").append(lsaSeqNumber + "\n");
    for (LinkDescription ld : links) {
      sb.append(ld);
    }
    sb.append("\n");
    return sb.toString();
  }
}
