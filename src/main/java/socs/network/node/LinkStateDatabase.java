package socs.network.node;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
import socs.network.message.LinkDescription;
import socs.network.message.LinkStateAdvertisement;
import socs.network.utils.CommonUtils;

/**
 * Encapsulating class to maintain data on LSA's broadcasted by the routers.
 */
public class LinkStateDatabase {

  /**
   * Data store to map linkID (ie. simulated IP) => LinkStateAdvertisement instance.
   */
  private HashMap<String, LinkStateAdvertisement> dataStore;

  /**
   * Private description for the router maintaining this Link State Database.
   */
  private RouterDescription rd = null;

  /**
   * Initialize database with single entry for this router.
   */
  private LinkStateAdvertisement initLinkStateDatabase() {
    // TODO: here, originally: (portNum, tosMetrics) = (-1, 0) - worth verifying my change later
    LinkDescription linkDescriptionOfActiveRouter = new LinkDescription(
        rd.simulatedIpAddress,
        rd.processPortNumber,
        RouterDescription.TRANSMISSION_WEIGHT_TO_SELF
    );
    LinkedList<LinkDescription> linkedListOfLinkDescriptions = new LinkedList<LinkDescription>();
    linkedListOfLinkDescriptions.add(linkDescriptionOfActiveRouter);
    // TODO: originally, NO_PREV_ADVERT constant was just INT.MIN_VAL - careful with this
    return new LinkStateAdvertisement(
        rd.simulatedIpAddress,
        LinkStateAdvertisement.NO_PREVIOUS_ADVERTISEMENTS_FLAG,
        linkedListOfLinkDescriptions
    );
  }

  /**
   * Construct LinkStateDatabase based on routerDescription instance.
   */
  LinkStateDatabase(RouterDescription routerDescription) {
    if (routerDescription == null) {
      throw new IllegalArgumentException(
          "Cannot instantiate LinkStateDatabase with null initial RouterDescription");
    }
    rd = routerDescription;
    LinkStateAdvertisement initialLsaRecord = initLinkStateDatabase();
    dataStore = new HashMap<String, LinkStateAdvertisement>();
    dataStore.put(initialLsaRecord.linkStateId, initialLsaRecord);
  }

  /**
   * Synchronized helper method to construct & return vector of database values.
   */
  synchronized Vector<LinkStateAdvertisement> getValuesVector() {
    Vector<LinkStateAdvertisement> lsaArray = new Vector<LinkStateAdvertisement>();
    lsaArray.addAll(dataStore.values());
    return lsaArray;
  }

  /**
   * Synchronized reader of last stored LSA for a given input IP address.
   */
  synchronized LinkStateAdvertisement getLastLinkStateAdvertisement(String simulatedIpAddress) {
    if (CommonUtils.isNullOrEmptyString(simulatedIpAddress)) {
      throw new IllegalArgumentException("Cannot get LSA for null IP address string.");
    }
    return dataStore.get(simulatedIpAddress);
  }

  /**
   * Synchronized writer of LSA for a given IP address key.
   */
  synchronized void putLinkStateAdvertisement(String linkId,
      LinkStateAdvertisement linkStateAdvertisement) {
    if (CommonUtils.isNullOrEmptyString(linkId)) {
      throw new IllegalArgumentException("Cannot store LSA for null IP address string.");
    }
    if (linkStateAdvertisement == null) {
      throw new IllegalArgumentException("Cannot store null LSA to Link State Database.");
    }
    if (!linkId.equals(linkStateAdvertisement.linkStateId)) {
      throw new IllegalArgumentException("Input linkId key must equal LSA's linkId.");
    }
    dataStore.put(linkId, linkStateAdvertisement);
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
