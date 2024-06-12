package ch.furchert.iotapp.config;

import ch.furchert.iotapp.model.LogEntry;
import ch.furchert.iotapp.repository.LogEntryRepository;
import ch.furchert.iotapp.service.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    @Autowired
    private LogEntryRepository logEntryRepository;

    @AfterReturning(pointcut = "execution(* ch.furchert.iotapp.controller.*.*(..))", returning = "result")
    public void logMethodCall(JoinPoint joinPoint, Object result) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String username = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetailsImpl) {
                username = ((UserDetailsImpl) principal).getUsername();
            }
        }

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logHttpRequest(joinPoint, result, request, username);
        } else {
            logNonHttpRequest(joinPoint, result, username);
        }
    }

    private void logHttpRequest(JoinPoint joinPoint, Object result, HttpServletRequest request, String username) {
        LogEntry logEntry = new LogEntry();
        logEntry.setUsername(username);
        logEntry.setMethodName(joinPoint.getSignature().getName());
        logEntry.setEndpoint(request.getRequestURI());
        logEntry.setTimestamp(LocalDateTime.now());
        logEntryRepository.save(logEntry);

        logger.debug("HTTP Request - Method {} returned with value {}", joinPoint.getSignature(), result);
    }

    private void logNonHttpRequest(JoinPoint joinPoint, Object result, String username) {
        // Logging logic for non-HTTP contexts, possibly WebSocket or scheduled tasks
        logger.debug("Non-HTTP Request - User: {}, Method {} executed", username, joinPoint.getSignature());
    }
}
