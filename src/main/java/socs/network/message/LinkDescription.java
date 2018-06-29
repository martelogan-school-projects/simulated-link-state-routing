/*
 * simulated-link-state-routing
 * Copyright (C) 2018, Logan Martel
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
  public final short tosMetrics;

  /**
   * Constructor to instantiate an SospfPacket with required input parameters.
   */
  public LinkDescription(String linkId, int processPortNum, short tosMetrics) {
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
