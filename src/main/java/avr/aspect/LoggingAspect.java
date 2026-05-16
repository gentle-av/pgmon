package avr.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(avr.controller..*)")
    public void controllerMethods() {}

    @Pointcut("within(avr.service..*)")
    public void serviceMethods() {}

    @Pointcut("within(avr.repository..*)")
    public void repositoryMethods() {}

    @Around("controllerMethods()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("→ {}.{}() called with args: {}", className, methodName, joinPoint.getArgs());

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        log.info("← {}.{}() returned: {} ({}ms)", className, methodName, result, duration);

        return result;
    }

    @Around("serviceMethods()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();

        if (log.isDebugEnabled()) {
            log.debug("Executing service: {}", methodName);
        }

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            if (duration > 1000) {
                log.warn("Slow service method: {} took {}ms", methodName, duration);
            }

            return result;
        } catch (Exception e) {
            log.error("Service method failed: {}", methodName, e);
            throw e;
        }
    }

    @Around("repositoryMethods()")
    public Object logRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        if (duration > 500) {
            log.warn("Slow query: {} took {}ms", methodName, duration);
        }

        return result;
    }
}
