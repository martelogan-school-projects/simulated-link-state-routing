package socs.network;

import socs.network.node.Router;
import socs.network.util.RouterConfiguration;

public class Main {

  /**
   * Main routine to drive router instantiation.
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("usage: program conf_path");
      System.exit(1);
    }
    Router r = new Router(new RouterConfiguration(args[0]));
    r.terminal();
  }
}
