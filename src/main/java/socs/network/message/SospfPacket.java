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
import java.util.Vector;
import socs.network.utils.CommonUtils;

/**
 * Specifies message packet transmitted between routers.
 */
public class SospfPacket implements Serializable {

  /**
   * Int constant to flag that SospfPacket is responding to client that no ports are available.
   */
  public static final short SOSPF_NO_PORTS_AVAILABLE = -1;

  /**
   * Int constant to flag that SospfPacket is a HELLO request.
   */
  public static final short SOSPF_HELLO = 0;

  /**
   * Int constant to flag that SospfPacket is an LSAUPDATE request.
   */
  public static final short SOSPF_LSAUPDATE = 1;

  /**
   * Int constant to flag that SospfPacket is a CONNECT request.
   */
  public static final short SOSPF_CONNECT = 2;

  /**
   * Int constant to flag that SospfPacket is a DISCONNECT request.
   */
  public static final short SOSPF_DISCONNECT = 3;

  /**
   * Int constant to flag that SospfPacket is a HEARTBEAT request.
   */
  public static final short SOSPF_HEARTBEAT = 4;

  /**
   * Int constant to flag that the SospfPacket weightOfTransmission is irrelevant.
   */
  public static final short IRRELEVANT_TRANSMISSION_WEIGHT = Short.MIN_VALUE;

  /**
   * Process IP address of source router.
   */
  public final String srcProcessIp;

  /**
   * Process port of source router.
   */
  public final short srcProcessPort;

  /**
   * Simulated IP address of source router.
   */
  public final String srcIp;

  /**
   * Simulated IP address of destination router.
   */
  public final String dstIp;

  /**
   * SospfType identifies type of message: 0 = HELLO 1 = LSAUPDATE .
   */
  public final short sospfType;

  /**
   * (Not clear what this is). FIXME: do we need this at all (seems covered by neighborId)?
   */
  public final String routerId;

  /**
   * Simulated IP address of neighbour router.
   *
   * <p> Used by HELLO message to identify the sender of the message (e.g. when router A sends HELLO
   * to its neighbor, it has to fill this field with its own simulated IP address) </p>
   */
  public final String neighborId;

  /**
   * Array of Link State Advertisements over which we will iterate during LSAUPDATE.
   */
  public final Vector<LinkStateAdvertisement> lsaArray;

  /**
   * Stored weight of the cost to transmit a packet between the source and destination router.
   */
  public final short weightOfTransmission;

  /**
   * Constructor to instantiate an SospfPacket with required input parameters.
   */
  public SospfPacket(String srcProcessIp, short srcProcessPort, String srcIp,
      String dstIp, short sospfType, String routerId, String neighborId,
      Vector<LinkStateAdvertisement> lsaArray, short weightOfTransmission) {
    if (CommonUtils.isNullOrEmptyString(srcProcessIp)) {
      throw new IllegalArgumentException(
          "Cannot instantiate SOSPF Packet with null or empty process IP address."
      );
    }
    if (CommonUtils.isNullOrEmptyString(srcIp)) {
      throw new IllegalArgumentException(
          "Cannot instantiate SOSPF Packet with null or empty simulated source IP address."
      );
    }
    if (CommonUtils.isNullOrEmptyString(dstIp)) {
      throw new IllegalArgumentException(
          "Cannot instantiate SOSPF Packet with null or empty simulated destination IP address."
      );
    }
    if (CommonUtils.isNullOrEmptyString(routerId)) {
      throw new IllegalArgumentException(
          "Cannot instantiate SOSPF Packet with null or empty router id."
      );
    }
    if (CommonUtils.isNullOrEmptyString(neighborId)) {
      throw new IllegalArgumentException(
          "Cannot instantiate SOSPF Packet with null or empty neighbor id."
      );
    }
    if (sospfType != SOSPF_NO_PORTS_AVAILABLE
        && sospfType != SOSPF_HELLO && sospfType != SOSPF_LSAUPDATE
        && sospfType != SOSPF_CONNECT && sospfType != SOSPF_DISCONNECT
        && sospfType != SOSPF_HEARTBEAT) {
      throw new IllegalArgumentException(
          "Tried to instantiate SOSPF Packet with invalid SOSPF Type = '" + sospfType + "'."
      );
    }
    this.srcProcessIp = srcProcessIp;
    this.srcProcessPort = srcProcessPort;
    this.srcIp = srcIp;
    this.dstIp = dstIp;
    this.sospfType = sospfType;
    this.routerId = routerId;
    this.neighborId = neighborId;
    this.lsaArray = lsaArray;
    this.weightOfTransmission = weightOfTransmission;
  }

}
