package com.muniai.observability;
import org.springframework.web.context.request.*;
public final class CorrelationId {
 public static final String HEADER = "X-Correlation-ID";
 static final String ATTRIBUTE = CorrelationId.class.getName();
 private CorrelationId() {}
 public static String current() {
  if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a) {
   Object value = a.getRequest().getAttribute(ATTRIBUTE); if (value != null) return value.toString();
  }
  return "unknown";
 }
}
