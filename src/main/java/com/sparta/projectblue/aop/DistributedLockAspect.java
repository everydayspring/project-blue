package com.sparta.projectblue.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.sparta.projectblue.config.AopForTransaction;
import com.sparta.projectblue.config.CustomSpringELParser;
import com.sparta.projectblue.config.DistributedLock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {
    // Redis 에 저장된 락 키를 식별할 때 사용
    private static final String REDISSON_LOCK_PREFIX = "lock:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(com.sparta.projectblue.config.DistributedLock)")
    public Object distributedLock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key =
                REDISSON_LOCK_PREFIX
                        + CustomSpringELParser.getDynamicValue(
                                methodSignature.getParameterNames(),
                                joinPoint.getArgs(),
                                distributedLock.key());
        RLock lock = redissonClient.getLock(key); // 지정된 키에 락 개체 생성

        lock.lock(); // 락 시작

        try {
            boolean available =
                    lock.tryLock(
                            distributedLock.waitTime(),
                            distributedLock.leaseTime(),
                            distributedLock.timeUnit());
            if (!available) {
                throw new IllegalStateException("락을 획득하지 못했습니다.");
            } else {
                log.info("락을 획득");
            }
            return aopForTransaction.proceed(joinPoint); // 락 획득 시 메서드 호출하여 실제 작업 수행
        } catch (InterruptedException e) {
            log.warn("인터럽트 발생", e);
            throw e;
        } finally {
            lock.unlock();
            log.info("lock 해제 완료");
        }
    }
}
