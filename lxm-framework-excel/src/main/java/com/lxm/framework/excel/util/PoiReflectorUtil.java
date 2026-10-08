package com.lxm.framework.excel.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射工具类,缓存读取的class信息,省的一直获取
 * 参考的 mybatis的Reflector
 *
 * @author twenty2
 */
@Slf4j
public final class PoiReflectorUtil {

    private static final Map<Class<?>, PoiReflectorUtil> CACHE_REFLECTOR =
            new ConcurrentHashMap<>();

    private Map<String, Method> getMethods = new HashMap<>();
    private Map<String, Method> setMethods = new HashMap<>();
    private Map<String, Method> enumMethods = new HashMap<>();
    private List<Field> fieldList = new ArrayList<>();

    private Class<?> type;

    private PoiReflectorUtil(Class<?> clazz) {
        this.type = clazz;
        addGetMethods(clazz);
        addFields(clazz);
        addSetMethods(clazz);
    }

    public static PoiReflectorUtil forClass(Class<?> clazz) {
        return new PoiReflectorUtil(clazz);
    }

    public static PoiReflectorUtil fromCache(Class<?> clazz) {
        if (!CACHE_REFLECTOR.containsKey(clazz)) {
            CACHE_REFLECTOR.put(clazz, new PoiReflectorUtil(clazz));
        }
        return CACHE_REFLECTOR.get(clazz);
    }

    private void addGetMethods(Class<?> cls) {
        Map<String, List<Method>> conflictingGetters = new HashMap<>(4);
        Method[] methods = getClassMethods(cls);
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("get") && name.length() > 3) {
                if (method.getParameterTypes().length == 0) {
                    name = methodToProperty(name);
                    addMethodConflict(conflictingGetters, name, method);
                }
            } else if (name.startsWith("is") && name.length() > 2) {
                if (method.getParameterTypes().length == 0) {
                    name = methodToProperty(name);
                    addMethodConflict(conflictingGetters, name, method);
                }
            }
        }
        resolveGetterConflicts(conflictingGetters);
    }

    private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
        for (String propName : conflictingGetters.keySet()) {
            List<Method> getters = conflictingGetters.get(propName);
            Iterator<Method> iterator = getters.iterator();
            Method firstMethod = iterator.next();
            if (getters.size() == 1) {
                addGetMethod(propName, firstMethod);
            } else {
                Method getter = firstMethod;
                Class<?> getterType = firstMethod.getReturnType();
                while (iterator.hasNext()) {
                    Method method = iterator.next();
                    Class<?> methodType = method.getReturnType();
                    if (methodType.equals(getterType)) {
                        throw new RuntimeException(
                                "Illegal overloaded getter method with ambiguous type for property "
                                        + propName
                                        + " in class "
                                        + firstMethod.getDeclaringClass()
                                        + ".  This breaks the JavaBeans "
                                        + "specification and can cause unpredicatble results.");
                    } else if (methodType.isAssignableFrom(getterType)) {
                        log.debug("OK getter type is descendant");
                    } else if (getterType.isAssignableFrom(methodType)) {
                        getter = method;
                        getterType = methodType;
                    } else {
                        throw new RuntimeException(
                                "Illegal overloaded getter method with ambiguous type for property "
                                        + propName
                                        + " in class "
                                        + firstMethod.getDeclaringClass()
                                        + ".  This breaks the JavaBeans "
                                        + "specification and can cause unpredicatble results.");
                    }
                }
                addGetMethod(propName, getter);
            }
        }
    }

    private void addGetMethod(String name, Method method) {
        if (isValidPropertyName(name)) {
            getMethods.put(name, method);
        }
    }

    private void addSetMethods(Class<?> cls) {
        Map<String, List<Method>> conflictingSetters = new HashMap<>(4);
        Method[] methods = getClassMethods(cls);
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("set") && name.length() > 3) {
                if (method.getParameterTypes().length == 1) {
                    name = methodToProperty(name);
                    addMethodConflict(conflictingSetters, name, method);
                }
            }
        }
        resolveSetterConflicts(conflictingSetters);
    }

    private static String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new RuntimeException(
                    "Error parsing property name '"
                            + name
                            + "'.  Didn't start with 'is', 'get' or 'set'.");
        }
        if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }
        return name;
    }

    private void addMethodConflict(
            Map<String, List<Method>> conflictingMethods, String name, Method method) {
        List<Method> list = conflictingMethods.computeIfAbsent(name, k -> new ArrayList<>());
        list.add(method);
    }

    private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
        for (String propName : conflictingSetters.keySet()) {
            List<Method> setters = conflictingSetters.get(propName);
            Method firstMethod = setters.get(0);
            if (setters.size() == 1) {
                addSetMethod(propName, firstMethod);
            } else {
                Iterator<Method> methods = setters.iterator();
                Method setter = null;
                while (methods.hasNext()) {
                    Method method = methods.next();
                    if (method.getParameterTypes().length == 1) {
                        setter = method;
                        break;
                    }
                }
                if (setter == null) {
                    throw new RuntimeException(
                            "Illegal overloaded setter method with ambiguous type for property "
                                    + propName
                                    + " in class "
                                    + firstMethod.getDeclaringClass()
                                    + ".  This breaks the JavaBeans "
                                    + "specification and can cause unpredicatble results.");
                }
                addSetMethod(propName, setter);
            }
        }
    }

    private void addSetMethod(String name, Method method) {
        if (isValidPropertyName(name)) {
            setMethods.put(name, method);
        }
    }

    private void addFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            {
                try {
                    field.trySetAccessible();
                    if (!"serialVersionUID".equalsIgnoreCase(field.getName())) {
                        this.fieldList.add(field);
                    }
                } catch (Exception e) {
                    // Ignored. This is only a final precaution, nothing we can do.
                }
            }
        }
        if (clazz.getSuperclass() != null) {
            addFields(clazz.getSuperclass());
        }
    }

    private boolean isValidPropertyName(String name) {
        return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
    }

    /**
     * This method returns an array containing all methods
     * declared in this class and any superclass.
     * We use this method, instead of the simpler Class.getMethods(),
     * because we want to look for private methods as well.
     *
     * @param cls The class
     * @return An array containing all methods in this class
     */
    private Method[] getClassMethods(Class<?> cls) {
        HashMap<String, Method> uniqueMethods = new HashMap<>(4);
        Class<?> currentClass = cls;
        while (currentClass != null) {
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

            // we also need to look for interface methods -
            // because the class may be abstract
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                addUniqueMethods(uniqueMethods, anInterface.getMethods());
            }

            currentClass = currentClass.getSuperclass();
        }
        Collection<Method> methods = uniqueMethods.values();
        int size = methods.size();
        return methods.toArray(new Method[size]);
    }

    private void addUniqueMethods(HashMap<String, Method> uniqueMethods, Method[] methods) {
        for (Method currentMethod : methods) {
            if (!currentMethod.isBridge()) {
                String signature = getSignature(currentMethod);
                // check to see if the method is already known
                // if it is known, then an extended class must have
                // overridden a method
                if (!uniqueMethods.containsKey(signature)) {
                    {
                        try {
                            currentMethod.trySetAccessible();
                        } catch (Exception e) {
                            // Ignored. This is only a final precaution, nothing we can do.
                            log.debug(
                                    "Ignored. This is only a final precaution, nothing we can do.");
                        }
                    }
                    uniqueMethods.put(signature, currentMethod);
                }
            }
        }
    }

    private String getSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        if (returnType != null) {
            sb.append(returnType.getName()).append('#');
        }
        sb.append(method.getName());
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                sb.append(':');
            } else {
                sb.append(',');
            }
            sb.append(parameters[i].getName());
        }
        return sb.toString();
    }

    Method getGetMethod(String propertyName) {
        Method method = getMethods.get(propertyName);
        if (method == null) {
            throw new RuntimeException(
                    "There is no getter for property named '"
                            + propertyName
                            + "' in '"
                            + type
                            + "'");
        }
        return method;
    }

    public Method getSetMethod(String propertyName) {
        Method method = setMethods.get(propertyName);
        if (method == null) {
            throw new RuntimeException(
                    "There is no setter for property named '"
                            + propertyName
                            + "' in '"
                            + type
                            + "'");
        }
        return method;
    }

    /**
     * 获取field 值
     *
     * @param obj      对象
     * @param property 属性
     * @return Object
     */
    public Object getValue(Object obj, String property) {
        Object value = null;
        Method m = getMethods.get(property);
        if (m != null) {
            try {
                value = m.invoke(obj);
            } catch (Exception ex) {
                log.debug("poi reflector get value error");
            }
        }
        return value;
    }

    /**
     * 设置field值
     *
     * @param obj      对象
     * @param property 属性
     * @param object   属性值
     */
    public void setValue(Object obj, String property, Object object) {
        Method m = setMethods.get(property);
        if (m != null) {
            try {
                m.invoke(obj, object);
            } catch (Exception ex) {
                log.debug("poi reflector set value error");
            }
        }
    }

    public Map<String, Method> getGetMethods() {
        return getMethods;
    }

    public List<Field> getFieldList() {
        return fieldList;
    }

    public Object execEnumStaticMethod(String staticMethod, Object params) {
        if (!enumMethods.containsKey(setMethods)) {
            try {
                enumMethods.put(staticMethod, type.getMethod(staticMethod, params.getClass()));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(
                        "There is no enum for property named '"
                                + staticMethod
                                + "' in '"
                                + type
                                + "'");
            }
        }
        try {
            return enumMethods.get(staticMethod).invoke(null, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
