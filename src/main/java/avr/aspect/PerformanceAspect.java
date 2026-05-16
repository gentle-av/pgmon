package avr.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    private final Map<String, MethodStats> stats = new ConcurrentHashMap<>();

    @Around("@annotation(avr.aspect.MonitorPerformance)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        MethodStats methodStats = stats.computeIfAbsent(methodName, k -> new MethodStats());

        methodStats.incrementCalls();
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            methodStats.addDuration(duration);

            if (duration > 2000) {
                log.warn("Performance warning: {} took {}ms, avg: {}ms",
                    methodName, duration, methodStats.getAvgDuration());
            }

            return result;
        } catch (Exception e) {
            methodStats.incrementErrors();
            throw e;
        }
    }

    public Map<String, MethodStats> getStats() {
        return new ConcurrentHashMap<>(stats);
    }

    public void resetStats() {
        stats.clear();
        log.info("Performance stats reset");
    }

    public static class MethodStats {
        private final AtomicLong calls = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicLong errors = new AtomicLong(0);

        public void incrementCalls() {
            calls.incrementAndGet();
        }

        public void incrementErrors() {
            errors.incrementAndGet();
        }

        public void addDuration(long duration) {
            totalDuration.addAndGet(duration);
        }

        public long getCalls() {
            return calls.get();
        }

        public long getTotalDuration() {
            return totalDuration.get();
        }

        public long getAvgDuration() {
            long c = calls.get();
            return c == 0 ? 0 : totalDuration.get() / c;
        }

        public long getErrors() {
            return errors.get();
        }

        public double getErrorRate() {
            long c = calls.get();
            return c == 0 ? 0 : (double) errors.get() / c * 100;
        }
    }
}
