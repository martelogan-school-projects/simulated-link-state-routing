package socs.network.message;

import java.io.Serializable;

/**
 * Describes link to a target router (link origin shall be implicit in Link Description usage).
 */
public class LinkDescription implements Serializable {

  /**
   * Simulated IP of destination router targeted by this link.
   */
  public String linkId;

  /**
   * Process port number of destination router targeted by this link.
   */
  public int processPortNum;

  /**
   * Weight of this link.
   */
  public int tosMetrics;

  /**
   * Convey string representation of Link Description instance.
   */
  public String toString() {
    return linkId + "," + processPortNum + "," + tosMetrics;
  }
}
