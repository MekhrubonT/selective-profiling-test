package org.jetbrains.test.profiller;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jetbrains.test.calltree.CallTree;


@Aspect
public class TestAspect {
    private String getName(Signature signature, Profiling profiling) {
        switch (profiling.type()) {
            case MID:
                return signature.toString();
            case LONG:
                return signature.toLongString();
            case SHORT:
                return signature.toShortString();
            default:
                return profiling.ownName();
        }
    }

    @Before("execution(* org.jetbrains.test..*(..))&&@annotation(profiling)")
    public void beforeProfilingHandler(JoinPoint jp, Profiling profiling) {
        CallTree.getInstance().addMethodCall(getName(jp.getSignature(), profiling), jp.getArgs());
    }

    @After("execution(* org.jetbrains.test..*(..))&&@annotation(profiling)")
    public void afterProfilingHandler(JoinPoint jp, Profiling profiling) {
        CallTree.getInstance().finishMethodCall();
    }
}
