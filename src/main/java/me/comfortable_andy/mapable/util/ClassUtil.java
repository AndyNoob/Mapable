package me.comfortable_andy.mapable.util;

import sun.reflect.ReflectionFactory;

import java.util.List;

@SuppressWarnings("unchecked")
public class ClassUtil {

    public static final List<Class<?>> PRIMITIVES = List.of(double.class, float.class, long.class, int.class, short.class, char.class, byte.class, boolean.class);
    public static final List<Class<?>> WRAPPERS = List.of(Double.class, Float.class, Long.class, Integer.class, Short.class, Character.class, Byte.class, Boolean.class, String.class);

    private static final Class<?> RECORD_CLASS;

    static {
        RECORD_CLASS = fromNameOrNull("java.lang.Record");
    }

    public static <T> Class<T> fromNameOrNull(final String name) {
        if (name == null) return null;
        try {
            return (Class<T>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T construct(final Class<T> clazz, final boolean bruteForce) {
        T object = null;

        try {
            object = clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            if (bruteForce) try {
                object = (T) ReflectionFactory.getReflectionFactory()
                        .newConstructorForSerialization(clazz, Object.class.getDeclaredConstructor())
                        .newInstance();
            } catch (ReflectiveOperationException ignored) {
            }
        }

        return object;
    }

    public static boolean isPrimitiveOrString(final Class<?> type) {
        return (type.isPrimitive() && type != void.class) ||
                type == Double.class || type == Float.class || type == Long.class ||
                type == Integer.class || type == Short.class || type == Character.class ||
                type == Byte.class || type == Boolean.class || type == String.class;
    }

    public static boolean isRecord(final Class<?> clazz) {
        if (RECORD_CLASS == null) return false;
        return RECORD_CLASS.isAssignableFrom(clazz);
    }

    public static boolean isASuperclassOfB(final Class<?> a, final Class<?> b) {
        return a.isAssignableFrom(b);
    }

}
