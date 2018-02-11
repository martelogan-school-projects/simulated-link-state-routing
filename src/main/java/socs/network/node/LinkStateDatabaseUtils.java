package socs.network.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import socs.network.message.LinkDescription;
import socs.network.message.LinkStateAdvertisement;

/**
 * Static utilities class to assist LinkStateDatabase instances.
 */
final class LinkStateDatabaseUtils {

  /**
   * String constant of arrow for formatting a path list as a path string.
   */
  private static final String ARROW_STRING = " ->";

  /**
   * Private constructor to restrict class instantiation.
   */
  private LinkStateDatabaseUtils() {
  }

  /**
   * Private helper method to initialize state required for Dijkstra's algorithm.
   */
  private static void initializeDijkstraStructures(
      LinkStateDatabase linkStateDatabase,
      Queue<String> ipAddressQueue, Map<String, Integer> dist,
      Map<String, String> prev, String sourceNodeId) {

    // we'll start by iterating over each LSA record in our database
    Collection<LinkStateAdvertisement> lsaRecords = linkStateDatabase.getValuesVector();
    String curNodeId;
    for (LinkStateAdvertisement curLsa : lsaRecords) {
      // for each LSA record, we need to also consider each of its link descriptions
      LinkedList<LinkDescription> linkDescriptionsOfCurLsa = curLsa.links;
      for (LinkDescription curLinkDescription : linkDescriptionsOfCurLsa) {
        // our "node" is given by the link id of this link description
        curNodeId = curLinkDescription.linkId;
        // skip nodes that are either already seen or equal to the source
        if (curNodeId.equals(sourceNodeId) || ipAddressQueue.contains(curNodeId)) {
          continue;
        }
        // otherwise, we'll add this nodeId to each of our data structures
        dist.put(curNodeId, Integer.MAX_VALUE);
        prev.put(curNodeId, null);
      }
      // be sure that we've also included the lsa node itself
      curNodeId = curLsa.linkStateId;
      // skip nodes that are either already seen or equal to the source
      if (curNodeId.equals(sourceNodeId) || ipAddressQueue.contains(curNodeId)) {
        continue;
      }
      // otherwise, we'll add this nodeId to each of our data structures
      dist.put(curNodeId, Integer.MAX_VALUE);
      prev.put(curNodeId, null);
    }

    // ** at this stage, all nodes except the source should be setup as required **

    // let's setup our source node
    dist.put(sourceNodeId, RouterDescription.TRANSMISSION_WEIGHT_TO_SELF);
    prev.put(sourceNodeId, null);
    ipAddressQueue.add(sourceNodeId);
  }

  private static List<LinkDescription> getNeighboringLinkDescriptionsOfNodeId(
      LinkStateDatabase linkStateDatabase, String nodeId
  ) {
    // attempt to get the latest stored lsa of this node
    LinkStateAdvertisement lastLsaOfCurNode =
        linkStateDatabase.getLastLinkStateAdvertisement(nodeId);

    // get the latest link description neighbors of this node
    return lastLsaOfCurNode.links;
  }

  /**
   * Static method to compute dijkstra's shortest path on input setup.
   */
  static List<String> computeShortestPathByDijkstra(
      LinkStateDatabase linkStateDatabase,
      Queue<String> ipAddressQueue, Map<String, Integer> dist,
      Map<String, String> prev, String sourceNodeId, String destinationNodeId) throws Exception {

    // first, let's initialize our data structures
    LinkStateDatabaseUtils.initializeDijkstraStructures(
        linkStateDatabase, ipAddressQueue, dist, prev, sourceNodeId
    );

    // prepare some reusable variables
    String curNodeId;
    String neighborNodeId;
    int weightOfLink;
    int weightOfAltPathToNeighbor;

    // and iterate over each node in our graph
    while (!ipAddressQueue.isEmpty()) {

      // get & remove the node with minimum distance in our queue
      curNodeId = ipAddressQueue.poll();

      // break iff we found the node we are looking for!
      if (curNodeId.equals(destinationNodeId)) {
        break;
      }

      // otherwise, get the latest link description neighbors of this node
      List<LinkDescription> neighborLinkDescriptions =
          getNeighboringLinkDescriptionsOfNodeId(linkStateDatabase, curNodeId);

      // and, for each neighbor, update the state of our shortest paths
      for (LinkDescription neighborLinkDescription : neighborLinkDescriptions) {
        // caching the link description fields
        neighborNodeId = neighborLinkDescription.linkId;
        weightOfLink = neighborLinkDescription.tosMetrics;
        // from which we can compute a path to this node
        weightOfAltPathToNeighbor = dist.get(curNodeId) + weightOfLink;
        // comparing this path with the best path we've found so far
        if (weightOfAltPathToNeighbor < dist.get(neighborNodeId)) {
          // and updating our shortest path if we found a better alternative
          dist.put(neighborNodeId, weightOfAltPathToNeighbor);
          prev.put(neighborNodeId, curNodeId);
          // add the neighbor to our queue if not yet seen
          if (!ipAddressQueue.contains(neighborNodeId)) {
            ipAddressQueue.add(neighborNodeId);
          }
        }
      }
    }

    // we'll use an arrayList to guarantee O(1) prepend
    List<String> shortestPath = new ArrayList<String>();

    // working our way back from the destination
    curNodeId = destinationNodeId;
    String prevNodeId = prev.get(curNodeId);
    // we'll iterate until there are no predecessor nodes
    while (prevNodeId != null) {
      // prepending each new node at the start of our path
      shortestPath.add(0, curNodeId);
      // setting its predecessor as our next node
      curNodeId = prevNodeId;
      // and updating our predecessor node accordingly
      prevNodeId = prev.get(curNodeId);
    }

    // the last node we encountered should definitely be the source!
    if (!curNodeId.equals(sourceNodeId)) {
      throw new Exception(
          "Dijikstra's algorithm failed: \n"
              + "(path root id = '" + curNodeId + "') does not equal "
              + "expected (source IP address = '" + sourceNodeId + "')."
      );
    }

    // surviving the above, prepend the source node to our path
    shortestPath.add(0, curNodeId);

    // and return this path
    return shortestPath;
  }

  /**
   * Static method to format an input path to the conventional string format.
   */
  static String getFormattedStringFromRouterIpPath(
      LinkStateDatabase linkStateDatabase, List<String> path
  ) {

    // ready a list iterator to iterate over the nodes in our path
    ListIterator<String> pathIterator = path.listIterator();

    // ready a string builder to efficiently construct our path string
    StringBuilder sb = new StringBuilder();

    // prepare some reusable variables
    String curNodeId;
    String nextNodeId;
    String currentEdgeWeightString;
    int weightOfCurrentEdge = Integer.MIN_VALUE;

    // and iterate over each node in our path
    while (pathIterator.hasNext()) {

      // taking one node at a time
      curNodeId = pathIterator.next();

      // break iff we've reached the destination
      if (!pathIterator.hasNext()) {
        // appending the destination node to our path string
        sb.append(curNodeId);
        break;
      }

      // otherwise, we'll get the latest link description neighbors of this node
      List<LinkDescription> neighborLinkDescriptions =
          getNeighboringLinkDescriptionsOfNodeId(linkStateDatabase, curNodeId);

      // get the next node temporarily to seek it in the current node's neighbors
      nextNodeId = pathIterator.next();

      // iterating over each neighboring link descriptions to seek the next node
      for (LinkDescription ld : neighborLinkDescriptions) {
        // breaking iff we find the next node
        if (ld.linkId.equals(nextNodeId)) {
          // and setting our edge weight when we do
          weightOfCurrentEdge = ld.tosMetrics;
          break;
        }
      }

      // from which, we can format an edge weight string
      currentEdgeWeightString = "(" + weightOfCurrentEdge + ") ";

      // and append this information to the path string we are building
      sb.append(curNodeId).append(ARROW_STRING).append(currentEdgeWeightString);

      // ** ESSENTIAL STATE UPDATE **
      // importantly, resetting the iterator state
      // so our neighbor is handled next
      pathIterator.previous();
    }

    // and, finally, returning the path string we've constructed
    return sb.toString();
  }
}
