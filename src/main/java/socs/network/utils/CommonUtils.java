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
package socs.network.utils;

/**
 * Static utilities class common across sub-packages of socs.network.
 */
public final class CommonUtils {

  /**
   * Private constructor to restrict class instantiation.
   */
  private CommonUtils() {
  }

  /**
   * Static method to verify non-nullity and non-emptiness of an input string.
   */
  public static boolean isNullOrEmptyString(String inputString) {
    return inputString == null || inputString.trim().length() <= 0;
  }

}
