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
 * Status of the router. UKNOWN means the RouterStatus is currently undetermined. INIT means we have
 * received the initial HELLO from the origin router. TWO_WAY means we have received an ACK for the
 * initial HELLO.
 */
public enum RouterStatus {
  UNKNOWN,
  INIT,
  TWO_WAY,
}
