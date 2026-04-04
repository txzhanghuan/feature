/*
 * Copyright (C) 2026 zhanghuan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package top.volusus.engine.clz;

import javassist.*;

import java.lang.reflect.Parameter;

/**
 * Generates dynamic classes that implement {@link IFeature} using Javassist.
 * <p>
 * This class is responsible for creating executable wrappers around annotated feature methods.
 * For each {@code @Feature} annotated method, it generates a new class that:
 * <ul>
 *   <li>Extends {@link AbstractFeature}</li>
 *   <li>Implements the {@link IFeature#execute(Object[])} method</li>
 *   <li>Delegates to the original annotated method with proper type casting</li>
 * </ul>
 *
 * <p>
 * <b>Thread Safety:</b> The static class pool is shared but Javassist handles
 * concurrent class generation safely.
 * </p>
 *
 * @author zhanghuan
 * @since 2020/01/27
 * @see IFeature
 * @see AbstractFeature
 * @see top.volusus.engine.processor.NativeFeatureProcessor
 */
public class FeatureClassGenerator {

    /**
     * Template for the execute method that casts and delegates to the original method.
     */
    private static final String TEMPLATE_METHOD = "public Object execute(Object[] args){\n"
            + "return ((%s)bean).%s(%s);\n"
            + "}\n";
    /**
     * Template for the constructor that passes the bean to the superclass.
     */
    private static final String TEMPLATE_CONSTRUCT = "public %s(Object bean){\n" +
            "super(bean);\n" +
            "}";

    /**
     * Shared Javassist class pool with the thread context class loader.
     */
    private static final ClassPool classPool;

    static {
        classPool = new ClassPool();
        classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }


    /**
     * Generates a dynamic class that implements {@link IFeature} for the given method.
     * <p>
     * The generated class extends {@link AbstractFeature} and overrides the execute method
     * to call the original feature method with properly cast arguments.
     * </p>
     *
     * @param parameterTypes       the types of the method parameters
     * @param parameters           the parameter objects (for names)
     * @param catalogClassName     the fully qualified name of the class containing the method
     * @param featureComponentName the Spring bean name (unused but kept for compatibility)
     * @param featureName          the name to use for the generated class
     * @param realMethodName       the actual method name to invoke
     * @return the generated class implementing IFeature
     * @throws NotFoundException      if a required class is not found in the class pool
     * @throws CannotCompileException if the generated code cannot be compiled
     */
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

    /**
     * Constructs the parameter casting expressions for the execute method body.
     * <p>
     * Generates code like: {@code (Type1)args[0], (Type2)args[1], ...}
     * </p>
     *
     * @param parameterTypes the types to cast each argument to
     * @param parameters     the parameters (must match length of parameterTypes)
     * @return the formatted parameter string for method invocation
     * @throws CannotCompileException if parameter counts don't match
     */
    private String constructByParam(Class<?>[] parameterTypes, Parameter[] parameters) throws CannotCompileException {
        int parameterTypesLength = parameterTypes.length;
        int parametersLength = parameters.length;
        if (parameterTypesLength != parametersLength) {
            throw new CannotCompileException("Parameter type count and parameter count do not match");
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

    /**
     * Constructs method parameter declarations (currently unused).
     * <p>
     * Generates code like: {@code Type1 param1, Type2 param2, ...}
     * </p>
     *
     * @param parameterTypes the parameter types
     * @param parameters     the parameter objects for names
     * @return the formatted parameter declaration string
     * @throws CannotCompileException if parameter counts don't match
     */
    private String constructByMethodParam(Class<?>[] parameterTypes, Parameter[] parameters) throws CannotCompileException {
        int parameterTypesLength = parameterTypes.length;
        int parametersLength = parameters.length;
        if (parameterTypesLength != parametersLength) {
            throw new CannotCompileException("Parameter type count and parameter count do not match");
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
