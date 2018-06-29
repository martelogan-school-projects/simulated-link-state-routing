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

/**
 * Specifies link between two routers.
 */
public class Link {

  /**
   * Description of origin router (client) which instantiated the link.
   */
  RouterDescription originRouter;

  /**
   * Description for target router (server) of link.
   */
  RouterDescription targetRouter;

  /**
   * Weight of link between the origin and target router.
   */
  short weight;

  /**
   * Instantiate Link between two routers (based on description).
   */
  public Link(RouterDescription originRouterDescription,
      RouterDescription targetRouterDescription) {
    if (originRouterDescription == null) {
      throw new IllegalArgumentException("Cannot instantiate a link with a null origin router.");
    }
    if (targetRouterDescription == null) {
      throw new IllegalArgumentException("Cannot instantiate a link with a null target router.");
    }
    originRouter = originRouterDescription;
    targetRouter = targetRouterDescription;
    weight = targetRouterDescription.weightToAttemptTransmission;
  }
}
