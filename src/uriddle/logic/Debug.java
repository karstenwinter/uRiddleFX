package uriddle.logic;

import static java.util.logging.Level.*;

import java.util.logging.*;
import java.util.logging.Level;

public class Debug {
  private static final Level FINE = INFO;
  //Logger log;

  public Debug(Class<?> cl) {
    //og = Logger.getLogger(cl.getName());
  }

  public static Debug getLogger(Class<?> cl) {
    return new Debug(cl);
  }

  public void debug(String s, Object... args) {
    //if (log.isLoggable(FINE)) {
    //	log.log(FINE, String.format(s, args));
    //}
    System.out.println(String.format(s, args));
  }

  public void debug(Object a) {
    //log.log(FINE, String.valueOf(a));
    System.out.println(a);
  }

  public void e(Throwable t) {
    //if (log.isLoggable(FINE)) {
    //	String msg = t.getClass().getSimpleName() + ": "
    //			+ t.getLocalizedMessage();
    //	log.log(FINE, msg, t);
    //}
    System.out.println(t.toString());
    t.printStackTrace();
  }
}
