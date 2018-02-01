package socs.network.message;

import java.io.Serializable;
import socs.network.utils.CommonUtils;

/**
 * Describes link to a target router (link origin shall be implicit in Link Description usage).
 */
public class LinkDescription implements Serializable {

  /**
   * Simulated IP of destination router targeted by this link.
   */
  public final String linkId;

  /**
   * Process port number of destination router targeted by this link.
   */
  public final int processPortNum;

  /**
   * Weight of this link.
   */
  public final int tosMetrics;

  /**
   * Constructor to instantiate an SospfPacket with required input parameters.
   */
  public LinkDescription(String linkId, int processPortNum, int tosMetrics) {
    if (CommonUtils.isNullOrEmptyString(linkId)) {
      throw new IllegalArgumentException(
          "Cannot instantiate LinkDescription with null or empty link id."
      );
    }
    this.linkId = linkId;
    this.processPortNum = processPortNum;
    this.tosMetrics = tosMetrics;
  }

  /**
   * Convey string representation of Link Description instance.
   */
  public String toString() {
    return linkId + "," + processPortNum + "," + tosMetrics;
  }
}
