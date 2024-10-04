package cz.osu.vbap.favUrls.lib.aop;

import cz.osu.vbap.favUrls.services.AppService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ControllerAspect {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Pointcut("execution(* cz.osu.vbap.favUrls.controllers..*(..))")
  public void controllerMethods(){}

  @Before("controllerMethods()")
  public void logBefore(JoinPoint joinPoint) {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String methodArgs = Arrays.toString(joinPoint.getArgs());
    logger.info("AOP-C:: {}.{}() invoked with arguments: {}", className, methodName, methodArgs);
  }

  @AfterReturning(pointcut = "controllerMethods()", returning = "result")
  public void logAfterReturning(JoinPoint joinPoint, Object result) {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String methodArgs = Arrays.toString(joinPoint.getArgs());
    logger.info("AOP-C:: {}.{}() completed with arguments: {} and result: {}", className, methodName, methodArgs, result);
  }
}
