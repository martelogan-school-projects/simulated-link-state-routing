package socs.network.node;

/**
 * Status of the router. In this simplified version of routing protocol, we only define INIT and
 * TWO_WAY for database synchronization, you can choose to keep other status by adding more fields
 * in router class or you can add more options here.
 */
public enum RouterStatus {
  INIT,
  TWO_WAY,
}
