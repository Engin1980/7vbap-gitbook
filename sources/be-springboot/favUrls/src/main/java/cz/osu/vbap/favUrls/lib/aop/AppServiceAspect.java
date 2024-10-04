package cz.osu.vbap.favUrls.lib.aop;

import cz.osu.vbap.favUrls.services.AppService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class AppServiceAspect {
  @Pointcut("within(cz.osu.vbap.favUrls.services.AppService+))")
  public void appServiceMethods(){}

  // Before advice: runs before the method execution
  @Before("appServiceMethods()")
  public void logBefore(JoinPoint joinPoint) {
    AppService service = (AppService) joinPoint.getTarget();
    Logger logger = service.getLogger();
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String methodArgs = Arrays.toString(joinPoint.getArgs());
    logger.info("AOP:: {}.{}() invoked with arguments: {}", className, methodName, methodArgs);
  }

  // Advice that runs after a method returns successfully
  @AfterReturning(pointcut = "appServiceMethods()", returning = "result")
  public void logAfterReturning(JoinPoint joinPoint, Object result) {
    AppService service = (AppService) joinPoint.getTarget();
    Logger logger = service.getLogger();

    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String methodArgs = Arrays.toString(joinPoint.getArgs());
    logger.info("AOP:: {}.{}() completed with arguments: {} and result: {}", className, methodName, methodArgs, result);
  }
}
