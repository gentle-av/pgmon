package avr.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RetryAspect {

    private static final Logger log = LoggerFactory.getLogger(RetryAspect.class);

    @Around("@annotation(retry)")
    public Object retry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        int attempts = 0;
        Exception lastException = null;
        while (attempts < retry.maxAttempts()) {
            try {
                attempts++;
                return joinPoint.proceed();
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} failed for {}: {}",
                    attempts, joinPoint.getSignature().toShortString(), e.getMessage());
                if (attempts < retry.maxAttempts()) {
                    long delay = retry.delay() * (long) Math.pow(2, attempts - 1);
                    log.info("Retrying in {}ms...", delay);
                    Thread.sleep(delay);
                }
            }
        }
        log.error("All {} attempts failed", retry.maxAttempts());
        throw lastException;
    }
}
