package me.comfortable_andy.mapable.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.reflect.ReflectionFactory;

@SuppressWarnings("unchecked")
public class ClassUtil {

    @Nullable
    public static <T> Class<T> fromName(@Nullable final String name) {
        if (name == null) return null;
        try {
            return (Class<T>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T construct(@NotNull final Class<T> clazz, final boolean bruteForce) {
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

    public static boolean isPrimitiveOrString(@NotNull final Class<?> type) {
        return (type.isPrimitive() && type != void.class) ||
                type == Double.class || type == Float.class || type == Long.class ||
                type == Integer.class || type == Short.class || type == Character.class ||
                type == Byte.class || type == Boolean.class || type == String.class;
    }

}
