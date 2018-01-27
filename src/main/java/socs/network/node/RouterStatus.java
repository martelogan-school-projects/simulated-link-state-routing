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
