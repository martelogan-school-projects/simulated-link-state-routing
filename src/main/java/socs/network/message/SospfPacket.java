package socs.network.message;

import java.io.Serializable;
import java.util.Vector;

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
   * Process IP address of source router.
   */
  public String srcProcessIp;

  /**
   * Process port of source router.
   */
  public short srcProcessPort;

  /**
   * Simulated IP address of source router.
   */
  public String srcIp;

  /**
   * Simulated IP address of destination router.
   */
  public String dstIp;

  /**
   * SospfType identifies type of message: 0 = HELLO 1 = LSAUPDATE .
   */
  public short sospfType;

  /**
   * (Not clear what this is). FIXME: do we need this at all (seems covered by neighborId)?
   */
  public String routerId;

  /**
   * Simulated IP address of neighbour router.
   *
   * <p> Used by HELLO message to identify the sender of the message (e.g. when router A sends HELLO
   * to its neighbor, it has to fill this field with its own simulated IP address) </p>
   */
  public String neighborId;

  /**
   * Array of Link State Advertisements over which we will iterate during LSAUPDATE.
   */
  public Vector<LinkStateAdvertisement> lsaArray = null;

}
