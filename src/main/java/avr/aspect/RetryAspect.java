package avr.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RetryAspect {
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
