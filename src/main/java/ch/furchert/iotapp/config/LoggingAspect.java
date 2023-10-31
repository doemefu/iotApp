package ch.furchert.iotapp.config;

import ch.furchert.iotapp.model.LogEntry;
import ch.furchert.iotapp.model.User;
import ch.furchert.iotapp.repository.LogEntryRepository;
import ch.furchert.iotapp.repository.UserRepository;
import ch.furchert.iotapp.security.jwt.JwtUtils;
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

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

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
        UserDetailsImpl userDetails = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            userDetails = (UserDetailsImpl) principal;
            logEntry.setUsername(userDetails.getUsername());
        } else {
            logEntry.setUsername("anonymous");
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
