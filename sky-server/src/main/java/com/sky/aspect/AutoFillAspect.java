package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充处理逻辑
 * @Component管理bean
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 制定切入点-对那些类的那些方法进行拦截（当前操作影响公共字段的方法）
     * mapper.*.*(..))mapper下所有类.方法(参数)  且 加了注解的
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知，在通知中进行公共字段的赋值
     * 匹配上切点表达式就会出发这个方法
     * 连接点对上值
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充...");

        //获取到当前被拦截的方法上的数据库操作类型 MethodSignature接口转化为方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库操作类型

        //获取到当前被拦截的方法的参数--实体对象。约定好所有的方法都把实体对象employee放到第一个
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }

        Object entity = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据当前不同的操作类型，为对应的属性通过反射来赋值
        if(operationType == OperationType.INSERT){
            //根据操作类型（INSERT）自动填充实体类的4个公共字段：
            try {
                //使用反射机制动态调用实体类的setter方法，避免硬编码字段名。
                //SET_CREATE_TIME在common constant里面
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值invoke(...)：动态调用方法。
                //效果等价于直接调用entity.setCreateTime(now)
                //反射可绕过访问控制（如调用私有方法）。
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(operationType == OperationType.UPDATE){
            //为2个公共字段赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 改进：封装反射逻辑为工具类
     * // 工具类
     * public class ReflectionUtils {
     *     private static final Map<Class<?>, Map<String, Method>> METHOD_CACHE = new ConcurrentHashMap<>();
     *
     *     public static void invokeSetter(Object obj, String methodName, Object value) {
     *         try {
     *             Method method = findMethod(obj.getClass(), methodName, value.getClass());
     *             method.invoke(obj, value);
     *         } catch (Exception e) {
     *             throw new RuntimeException("反射调用失败: " + methodName, e);
     *         }
     *     }
     *
     *     private static Method findMethod(Class<?> clazz, String name, Class<?> paramType) throws NoSuchMethodException {
     *         return METHOD_CACHE.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
     *                           .computeIfAbsent(name, k -> {
     *                               try {
     *                                   Method method = clazz.getDeclaredMethod(name, paramType);
     *                                   if (!Modifier.isPublic(method.getModifiers())) {
     *                                       method.setAccessible(true); // 处理非public方法
     *                                   }
     *                                   return method;
     *                               } catch (NoSuchMethodException e) {
     *                                   throw new RuntimeException("方法未找到: " + name, e);
     *                               }
     *                           });
     *     }
     * }
     *
     * // 业务代码
     * if (operationType == OperationType.INSERT) {
     *     ReflectionUtils.invokeSetter(entity, AutoFillConstant.SET_CREATE_TIME, now);
     *     ReflectionUtils.invokeSetter(entity, AutoFillConstant.SET_CREATE_USER, currentId);
     *     // ...其他setter
     * }
     */
}
