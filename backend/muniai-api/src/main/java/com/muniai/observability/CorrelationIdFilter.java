package com.muniai.observability;
import jakarta.servlet.*; import jakarta.servlet.http.*; import java.io.IOException; import java.util.UUID;
import org.slf4j.MDC; import org.springframework.core.Ordered; import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
@Component @Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {
 @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
  String incoming=request.getHeader(CorrelationId.HEADER); String id=incoming!=null&&incoming.matches("[A-Za-z0-9._-]{1,128}")?incoming:UUID.randomUUID().toString();
  request.setAttribute(CorrelationId.ATTRIBUTE,id); response.setHeader(CorrelationId.HEADER,id); MDC.put("correlationId",id);
  try { chain.doFilter(request,response); } finally { MDC.remove("correlationId"); }
 }
}
