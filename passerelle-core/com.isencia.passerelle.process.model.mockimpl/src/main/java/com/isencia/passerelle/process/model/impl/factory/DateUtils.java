package com.isencia.passerelle.process.model.impl.factory;

import java.text.*;
import java.util.*;

public final class DateUtils {

  private DateUtils() {
  }

  public static String format(long date, String pattern) {
    return format(new Date(date), pattern);
  }

  public static String format(Date date, String pattern) {
    if (date == null || pattern == null) {
      return "";
    }
    DateFormat df = createDateFormat(pattern);
    return df.format(date);
  }

  public static String formatElapsedTime(long millis) {
    long seconds = millis / 1000L;
    long minutes = seconds / 60L;
    Object args[] = { new Long(minutes), new Long(seconds % 60L) };
    return MINUTE_SECONDS.format(((Object) (args)));
  }

  private static DateFormat createDateFormat(String pattern) {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    // TimeZone gmt = TimeZone.getTimeZone("GMT");
    // sdf.setTimeZone(gmt);
    sdf.setLenient(true);
    return sdf;
  }

  private static final MessageFormat MINUTE_SECONDS;
  static {
    MINUTE_SECONDS = new MessageFormat("{0}{1}");
  }
}
