package socs.network.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import java.io.File;


public class RouterConfiguration {

  /**
   * Underlying Java representation of router configuration file.
   */
  private Config routerConfig = null;

  /**
   * Constructor for RouterConfiguration instance based on input path to "router#.conf" file.
   */
  public RouterConfiguration(String path) {
    routerConfig = ConfigFactory.parseFile(new File(path));
  }

  /**
   * Fetch string data from routerConfig via input key.
   */
  public String getString(String key) {
    return routerConfig.getString(key);
  }

  /**
   * Fetch boolean data from routerConfig via input key.
   */
  public Boolean getBoolean(String key) {
    return routerConfig.getBoolean(key);
  }

  /**
   * Fetch int data from routerConfig via input key.
   */
  public int getInt(String key) {
    return routerConfig.getInt(key);
  }

  /**
   * Fetch short data from routerConfig via input key.
   */
  public short getShort(String key) {
    return (short) routerConfig.getInt(key);
  }

  /**
   * Fetch double data from routerConfig via input key.
   */
  public double getDouble(String key) {
    return routerConfig.getDouble(key);
  }

  /**
   * Programatically add a record to the configuration state for this router.
   */
  public void addEntry(String key, String value) {
    routerConfig = routerConfig.withValue(key, ConfigValueFactory.fromAnyRef(value));
  }
}
