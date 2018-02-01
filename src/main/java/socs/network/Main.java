package socs.network;

import socs.network.node.Router;
import socs.network.utils.RouterConfiguration;

/**
 * Standard 'Main' class for command-line execution.
 */
public class Main {

  /**
   * Main routine to drive router instantiation.
   */
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("usage: program conf_path");
      System.exit(1);
    }
    Router r = new Router(new RouterConfiguration(args[0]));
    r.terminal();
  }
}
