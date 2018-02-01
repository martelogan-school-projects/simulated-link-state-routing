package socs.network.message;

import java.io.Serializable;
import java.util.LinkedList;
import socs.network.utils.CommonUtils;

/**
 * Broadcast conveying updates to Link Description relations between routers.
 */
public class LinkStateAdvertisement implements Serializable {

  /**
   * Int constant to flag the status of an initial Link State Advertisement.
   */
  public static final int NO_PREVIOUS_ADVERTISEMENTS_FLAG = Integer.MIN_VALUE;

  /**
   * Int constant of the minimum Link State Advertisement sequence number.
   */
  public static final int MIN_SEQ_NUMBER = 0;

  /**
   * Simulated IP address of the router originating this LinkStateAdvertisement.
   */
  public final String linkStateId;

  /**
   * Version number imposing total order on sequence of broadcasts for this LSA.
   */
  public final int lsaSeqNumber;

  /**
   * List of Link Descriptions per link associated to this LSA.
   */
  public final LinkedList<LinkDescription> links;

  /**
   * Constructor to instantiate a LinkStateAdvertisement with required input parameters.
   */
  public LinkStateAdvertisement(String linkStateId,
      int lsaSeqNumber, LinkedList<LinkDescription> links) {

    if (CommonUtils.isNullOrEmptyString(linkStateId)) {
      throw new IllegalArgumentException(
          "Cannot instantiate LinkStateAdvertisement with a null or empty link state id."
      );
    }
    if (lsaSeqNumber != NO_PREVIOUS_ADVERTISEMENTS_FLAG && lsaSeqNumber < MIN_SEQ_NUMBER) {
      throw new IllegalArgumentException(
          "Tried to instantiate LinkStateAdvertisement with invalid link state id '"
              + linkStateId + "'.");
    }
    if (links == null) {
      throw new IllegalArgumentException(
          "Cannot instantiate LinkStateAdvertisement with null list of link descriptions."
      );
    }

    this.linkStateId = linkStateId;
    this.lsaSeqNumber = lsaSeqNumber;
    this.links = links;

  }

  /**
   * Convey string representation of Link State Advertisement instance.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(linkStateId).append(":").append(lsaSeqNumber).append("\n");
    for (LinkDescription ld : links) {
      sb.append(ld);
    }
    sb.append("\n");
    return sb.toString();
  }
}
