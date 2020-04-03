package com.github.zh.engine.clz;

import javassist.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

/**
 * @author zhanghuan
 * @created 2020/01/27
 */
@Component
public class FeatureClassGenerator {

    private static final String TEMPLATE_METHOD = "public Object execute(Object[] args){\n"
            + "return ((%s)bean).%s(%s);\n"
            + "}\n";
    private static final String TEMPLATE_CONSTRUCT = "public %s(Object bean){\n" +
            "super(bean);\n" +
            "}";

    private static ClassPool classPool = new ClassPool();

    static {
        classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }


    public Class<?> generateClass(Class<?>[] parameterTypes, Parameter[] parameters,
                                                   String catalogClassName, String featureComponentName,
                                                   String featureMethodName, String realMethodName)
            throws NotFoundException, CannotCompileException {

//        String paramsMetaStr = constructByMethodParam(parameterTypes, parameters);
        String params = constructByParam(parameterTypes, parameters);
        String compileMethod = String.format(TEMPLATE_METHOD, catalogClassName, realMethodName, params);
        String className = convert2CamelCase(featureMethodName);
        CtClass clz = classPool.makeClass(className,
                classPool.get(AbstractFeature.class.getName()));
        CtMethod ctMethod = CtNewMethod.make(compileMethod, clz);
        clz.addMethod(ctMethod);
        CtConstructor ctConstructor = CtNewConstructor.make(String.format(TEMPLATE_CONSTRUCT, featureMethodName), clz);
        clz.addConstructor(ctConstructor);
        Class<?> klz = clz.toClass();
        clz.detach();
        return klz;
    }

    private String constructByParam(Class<?>[] parameterTypes, Parameter[] parameters) throws CannotCompileException {
        int parameterTypesLength = parameterTypes.length;
        int parametersLength = parameters.length;
        if(parameterTypesLength != parametersLength){
            throw new CannotCompileException("参数类型个数和参数个数不匹配");
        }
        StringBuilder paramsStr = new StringBuilder();
        String template = "(%s)args[%s]";
        for(int i = 0; i < parametersLength; i++){
            paramsStr.append(String.format(template, parameterTypes[i].getName(), i));
            if(i < parametersLength - 1) {
                paramsStr.append(",");
            }
        }
        return paramsStr.toString();
    }

    private String constructByMethodParam(Class<?>[] parameterTypes, Parameter[] parameters) throws CannotCompileException {
        int parameterTypesLength = parameterTypes.length;
        int parametersLength = parameters.length;
        if(parameterTypesLength != parametersLength){
            throw new CannotCompileException("参数类型个数和参数个数不匹配");
        }
        StringBuilder paramsStr = new StringBuilder();
        for(int i = 0; i < parameterTypesLength; i++){
            paramsStr.append(parameterTypes[i].getName());
            paramsStr.append(" ");
            paramsStr.append(parameters[i].getName());
            if(i < parameterTypesLength - 1) {
                paramsStr.append(", ");
            }
        }
        return paramsStr.toString();
    }

    public String convert2CamelCase(String str){
        if(!str.matches("^[A-Za-z][A-Za-z0-9]*")){
            throw new IllegalArgumentException("feature名字错啦～需遵守驼峰命名法哦");
        }
        //A-Z
        if(str.charAt(0) >= 65 && str.charAt(0) <= 90){
            return str;
        }else if(str.charAt(0) >= 97 && str.charAt(0) <= 122){
            return (char) (str.charAt(0) - 32) + str.substring(1);
        } else{
            throw new IllegalArgumentException("feature名字错啦～需遵守驼峰命名法哦");
        }
    }
}
