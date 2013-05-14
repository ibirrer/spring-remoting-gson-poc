package ch.there.gson;

public class CallerRemoteApiVersion {

  public static final ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<>();

  public static void setVersion(int version) {
    THREAD_LOCAL.set(version);
  }

  public static Integer getVersion() {
    return THREAD_LOCAL.get();
  }
}