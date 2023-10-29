package ch.furchert.iotapp.config;

import ch.furchert.iotapp.model.LogEntry;
import ch.furchert.iotapp.model.User;
import ch.furchert.iotapp.repository.LogEntryRepository;
import ch.furchert.iotapp.repository.UserRepository;
import ch.furchert.iotapp.service.UserDetailsImpl;
import ch.furchert.iotapp.util.payload.response.MessageResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

@Aspect
@Component
public class LoggingAspect {

    @Autowired
    private LogEntryRepository logEntryRepository;

    @Autowired
    private UserRepository userRepository;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("execution(* ch.furchert.iotapp.controller.*.*(..))")
    public void authControllerMethods() {}

    @AfterReturning(pointcut = "authControllerMethods()", returning = "result")
    public void logMethodCall(JoinPoint joinPoint, Object result) {

        LogEntry logEntry = new LogEntry();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();


        if (userDetails != null) {
            logEntry.setUsername(userDetails.getUsername());
        } else {
            logEntry.setUsername("anonymous"); // Replace with actual username if available
        }

        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();


        logEntry.setMethodName(joinPoint.getSignature().getName());
        logEntry.setEndpoint(request.getRequestURI()); // Get the endpoint URL
        logEntry.setTimestamp(LocalDateTime.now());

        logEntryRepository.save(logEntry);

        log.info("Method {} returned with value {}", joinPoint.getSignature(), result);
    }
}
