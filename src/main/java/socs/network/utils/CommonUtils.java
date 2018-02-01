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
