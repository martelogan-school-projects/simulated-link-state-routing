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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import java.io.File;

/**
 * Configuration class to wrap state of Router setup.
 */
public class RouterConfiguration {

  /**
   * String constant of key to fetch Router's simulated IP address.
   */
  private static final String SIMULATED_IP_KEY = "socs.network.router.ip";

  /**
   * Underlying Java representation of router configuration file.
   */
  private Config routerConfig = null;

  /**
   * Fetch string data from routerConfig via input key.
   */
  private String getString(String key) {
    return routerConfig.getString(key);
  }

  /**
   * Fetch boolean data from routerConfig via input key.
   */
  private Boolean getBoolean(String key) {
    return routerConfig.getBoolean(key);
  }

  /**
   * Fetch int data from routerConfig via input key.
   */
  private int getInt(String key) {
    return routerConfig.getInt(key);
  }

  /**
   * Fetch short data from routerConfig via input key.
   */
  private short getShort(String key) {
    return (short) routerConfig.getInt(key);
  }

  /**
   * Fetch double data from routerConfig via input key.
   */
  private double getDouble(String key) {
    return routerConfig.getDouble(key);
  }

  /**
   * Programatically add a record to the configuration state for this router.
   */
  private void addEntry(String key, String value) {
    routerConfig = routerConfig.withValue(key, ConfigValueFactory.fromAnyRef(value));
  }

  /**
   * Constructor for RouterConfiguration instance based on input path to "router#.conf" file.
   */
  public RouterConfiguration(String path) {
    routerConfig = ConfigFactory.parseFile(new File(path));
  }

  /**
   * Public getter for Router's simulated IP Address.
   */
  public String getSimulatedIpAddress() {
    return getString(SIMULATED_IP_KEY);
  }
}
