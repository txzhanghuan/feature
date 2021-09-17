package com.github.zh.engine.clz;

import javassist.*;

import java.lang.reflect.Parameter;

/**
 * @author zhanghuan
 * @created 2020/01/27
 */
public class FeatureClassGenerator {

    private static final String TEMPLATE_METHOD = "public Object execute(Object[] args){\n"
            + "return ((%s)bean).%s(%s);\n"
            + "}\n";
    private static final String TEMPLATE_CONSTRUCT = "public %s(Object bean){\n" +
            "super(bean);\n" +
            "}";

    private static final ClassPool classPool;

    static {
        classPool = new ClassPool();
        classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }


    public Class<?> generateClass(Class<?>[] parameterTypes, Parameter[] parameters,
                                  String catalogClassName, String featureComponentName,
                                  String featureName, String realMethodName)
            throws NotFoundException, CannotCompileException {

        String params = constructByParam(parameterTypes, parameters);
        String compileMethod = String.format(TEMPLATE_METHOD, catalogClassName, realMethodName, params);
        String className = featureName;
        CtClass clz = classPool.makeClass(className,
                classPool.get(AbstractFeature.class.getName()));
        CtMethod ctMethod = CtNewMethod.make(compileMethod, clz);
        clz.addMethod(ctMethod);
        CtConstructor ctConstructor = CtNewConstructor.make(String.format(TEMPLATE_CONSTRUCT, featureName), clz);
        clz.addConstructor(ctConstructor);
        Class<?> klz = clz.toClass();
        clz.detach();
        return klz;
    }

    private String constructByParam(Class<?>[] parameterTypes, Parameter[] parameters) throws CannotCompileException {
        int parameterTypesLength = parameterTypes.length;
        int parametersLength = parameters.length;
        if (parameterTypesLength != parametersLength) {
            throw new CannotCompileException("参数类型个数和参数个数不匹配");
        }
        StringBuilder paramsStr = new StringBuilder();
        String template = "(%s)args[%s]";
        for (int i = 0; i < parametersLength; i++) {
            paramsStr.append(String.format(template, parameterTypes[i].getName(), i));
            if (i < parametersLength - 1) {
                paramsStr.append(",");
            }
        }
        return paramsStr.toString();
    }

    private String constructByMethodParam(Class<?>[] parameterTypes, Parameter[] parameters) throws CannotCompileException {
        int parameterTypesLength = parameterTypes.length;
        int parametersLength = parameters.length;
        if (parameterTypesLength != parametersLength) {
            throw new CannotCompileException("参数类型个数和参数个数不匹配");
        }
        StringBuilder paramsStr = new StringBuilder();
        for (int i = 0; i < parameterTypesLength; i++) {
            paramsStr.append(parameterTypes[i].getName());
            paramsStr.append(" ");
            paramsStr.append(parameters[i].getName());
            if (i < parameterTypesLength - 1) {
                paramsStr.append(", ");
            }
        }
        return paramsStr.toString();
    }
}
