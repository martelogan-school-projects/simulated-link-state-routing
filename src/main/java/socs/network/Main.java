package socs.network;

import java.util.Timer;
import java.util.TimerTask;
import socs.network.node.HeartbeatTask;
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
    // instantiate our router
    Router r = new Router(new RouterConfiguration(args[0]));
    // init scheduled jobs for periodic heartbeats
    TimerTask heartbeatTask = new HeartbeatTask(r);
    Timer timer = new Timer();
    timer.schedule(heartbeatTask, 0, Router.HEARTBEAT_WAIT_TIME);
    // then display the router's console for user input
    r.terminal();
  }
}
