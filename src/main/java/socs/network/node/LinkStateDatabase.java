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
package socs.network.node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
  private final HashMap<String, LinkStateAdvertisement> dataStore;

  /**
   * Private description for the router maintaining this Link State Database.
   */
  private final RouterDescription rd;

  /**
   * Initialize database with single entry for this router.
   */
  private LinkStateAdvertisement initLinkStateDatabase() {
    // TODO: here, originally: (portNum, tosMetrics) = (-1, 0) - worth verifying my change later
    LinkDescription linkDescriptionOfActiveRouter = new LinkDescription(
        rd.simulatedIpAddress,
        rd.processPortNumber,
        (short) RouterDescription.TRANSMISSION_WEIGHT_TO_SELF
    );
    LinkedList<LinkDescription> linkedListOfLinkDescriptions = new LinkedList<>();
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
    dataStore = new HashMap<>();
    dataStore.put(initialLsaRecord.linkStateId, initialLsaRecord);
  }

  /**
   * Synchronized helper method to construct & return vector of database values.
   */
  synchronized Vector<LinkStateAdvertisement> getValuesVector() {
    Vector<LinkStateAdvertisement> lsaArray = new Vector<>();
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
   * <p><br></p> Attribution: <p><br></p> Derived from pseudocode described at
   * https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
   */
  synchronized String getShortestPath(String destinationIp) throws Exception {

    LinkStateAdvertisement lastLsaOfDestination = dataStore.get(destinationIp);

    if (lastLsaOfDestination == null || lastLsaOfDestination.hasShutdown) {
      System.out.println(
          "\n\nCannot detect shortest path to IP '" + destinationIp
              + "' because there is no path to it in our network.\n\n"
      );
      return null;
    }

    // lock the data store so no one touches it while we're reading from it
    synchronized (dataStore) {
      // first, let's prepare our data structures
      Map<String, Integer> dist = new HashMap<>();
      Map<String, String> prev = new HashMap<>();

      // and, of course, make note of our source node id
      String sourceNodeId = rd.simulatedIpAddress;

      // from which, we will apply dijkstra's algorithm to compute the shortest path
      List<String> shortestPath = LinkStateDatabaseUtils.computeShortestPathByDijkstra(
          this, new HashSet<>(), dist, prev, sourceNodeId, destinationIp
      );

      // and return our shortest path in the expected string format
      return LinkStateDatabaseUtils.getFormattedStringFromRouterIpPath(
          this, shortestPath
      );
    }
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
