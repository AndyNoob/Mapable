package me.comfortable_andy.mapable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.reflect.ReflectionFactory;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Supports multi-dimension array.
 *
 * @author AndyNoob
 */
public final class Mapable {

    private static final Logger LOGGER = Logger.getLogger("Mapable");
    static final String CLAZZ_KEY = "==", PRIMITIVE_KEY = "value", ENUM_NAME_KEY = "name", ENUM_ORDINAL_KEY = "ordinal";

    private final boolean needAnnotation, mapSerializable, useEnumName, log;
    private int stage;

    public Mapable(boolean needAnnotation, boolean mapSerializable, boolean useEnumName, boolean log) {
        this.needAnnotation = needAnnotation;
        this.mapSerializable = mapSerializable;
        this.useEnumName = useEnumName;
        this.log = log;
    }

    public Map<String, Object> asMap(@NotNull final Object toMap) throws ReflectiveOperationException {
        return asMap(toMap, null);
    }

    /**
     * Uses reflection to retrieve all fields from the parameter and uses recursion in the case
     * of arrays or un-serializable fields.
     *
     * @param toMap The object used to map (null if clazz is an interface or annotation).
     * @return Serialized data in map.
     */
    @Nullable
    public <T> Map<String, Object> asMap(@Nullable final T toMap, @Nullable Class<T> clazz) throws ReflectiveOperationException {
        if (toMap == null) return null;
        this.stage = 1;

        //noinspection unchecked
        clazz = clazz == null ? (Class<T>) toMap.getClass() : clazz; // WTF java so weird

        if (clazz.isInterface() || clazz.isAnnotation()) return null;

        final Map<String, Object> map = new HashMap<>();

        map.put(CLAZZ_KEY, clazz.getName());

        log("Mapping " + clazz);

        if (clazz.isArray()) {
            log(clazz + " is array!");
            final Object[] array = (Object[]) toMap;

            for (int i = 0; i < array.length; i++) map.put(String.valueOf(i), asMap(array[i]));

            return map;
        } else if (clazz.isEnum()) {
            log(clazz + " is enum!");
            if (this.useEnumName) map.put(ENUM_NAME_KEY, toMap);
            else map.put(ENUM_ORDINAL_KEY, Arrays.asList(clazz.getEnumConstants()).indexOf(toMap));
            return map;
        } else if (isPrimitiveOrString(clazz)) {
            log(clazz + " is primitive!");
            map.put(PRIMITIVE_KEY, toMap);
            return map;
        }

        log("Saving fields...");

        for (final Map.Entry<String, Field> entry : findApplicableFields(clazz, new HashMap<>()).entrySet()) {
            final String name = entry.getKey();
            final Field field = entry.getValue();
            final Object value = field.get(toMap);

            if (value == null) continue;
            if (isPrimitiveOrString(field.getType())) {
                map.put(name, value);
                continue;
            }
            if (value instanceof Serializable && this.mapSerializable) {
                map.put(name, value);
                continue;
            }

            log(field + " is not serializable, attempting to map...");

            try {
                map.put(name, asMap(value));
            } catch (Exception e) {
                throw new IllegalStateException("Couldn't map " + toMap + " (" + clazz + ")", e);
            }

        }

        return map;
    }

    /**
     * Uses reflection to retrieve all fields from the designated class (the <code>===classname</code>)
     * and uses recursion in the case of arrays or other serializable fields.
     *
     * @param map Serialized object in <code>Mapable#asMap</code>.
     * @return Deserialized object.
     * @see #asMap(Object)
     */
    public Object fromMap(final Map<String, Object> map) throws ReflectiveOperationException {
        if (!map.containsKey("==")) throw new IllegalArgumentException("Invalid map! Should contain a \"==\" property!");

        this.stage = 2;

        final Class<?> clazz = Class.forName(map.get(CLAZZ_KEY).toString());

        if (clazz.isArray()) {
            log(clazz + " is array!");
            final Object array = Array.newInstance(clazz.getComponentType(), map.size() - 1);
            final Class<?> accepted = array.getClass().getComponentType();

            for (int i = 0; i < map.size(); i++) {
                final Object o = map.get(i + "");

                if (!(o instanceof Map)) continue;

                try {
                    Array.set(array, i, accepted.cast(fromMap((Map<String, Object>) o)));
                } catch (Exception e) {
                    throw new IllegalStateException("Couldn't unmap array val " + o, e);
                }
            }

            return array;
        } else if (clazz.isEnum()) {
            log(clazz + " is enum!");
            if (useEnumName) return Enum.valueOf((Class<? extends Enum>) clazz, map.get(ENUM_NAME_KEY).toString());
            else return clazz.getEnumConstants()[Integer.parseInt(map.get(ENUM_ORDINAL_KEY).toString())];
        } else if (isPrimitiveOrString(clazz)) return map.get(PRIMITIVE_KEY);

        Object object;

        try {
            object = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log("Couldn't find empty constructor, attempting to bypass...");

            try {
                object = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(clazz, Object.class.getDeclaredConstructor()).newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException("Couldn't use ReflectionFactory to instantiate object", ex);
            }
        }

        for (final Map.Entry<String, Field> entry : findApplicableFields(clazz, new HashMap<>()).entrySet()) {
            final String name = entry.getKey();
            final Field field = entry.getValue();
            Object value = map.get(name);

            if (value == null) continue;
            if (isPrimitiveOrString(field.getType())) {
                field.set(object, value);
                continue;
            }
            if (field.getType().isAssignableFrom(Serializable.class) && value instanceof Serializable && mapSerializable) {
                field.set(object, value);
                continue;
            }
            if (value instanceof Map) {
                try {
                    value = fromMap((Map<String, Object>) value);
                } catch (Exception e) {
                    throw new IllegalStateException("Couldn't unmap " + value + " for unmapping " + clazz, e);
                }
            }

            if (value == null) continue;

            field.set(object, value);
        }

        return object;
    }

    @NotNull
    private Map<String, Field> findApplicableFields(final @NotNull Class<?> clazz, final @NotNull Map<String, Field> fields) {
        for (final Field field : clazz.getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
            if (!field.trySetAccessible()) continue;

            final MapMe mapTo = field.getAnnotation(MapMe.class);

            if (mapTo == null && this.needAnnotation) continue;

            fields.put(mapTo != null && !mapTo.mapName().isEmpty() ? mapTo.mapName() : field.getName(), field);
        }

        return clazz.getSuperclass() == null || clazz.getSuperclass() == Object.class ? fields : findApplicableFields(clazz, fields);
    }

    private void log(String str) {
        if (log) LOGGER.info((this.stage == 1 ? "[MAP] " : "[UNMAP] ") + str);
    }

    private static boolean isPrimitiveOrString(Class<?> type) {
        return (type.isPrimitive() && type != void.class) ||
                type == Double.class || type == Float.class || type == Long.class ||
                type == Integer.class || type == Short.class || type == Character.class ||
                type == Byte.class || type == Boolean.class || type == String.class;
    }

    /**
     * Required for the serializing and deserializing process.
     *
     * @author AndyNoob
     * @see #asMap(Object)
     * @see #fromMap(Map)
     */
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface MapMe {
        /**
         * Default variable name
         */
        String mapName() default "";
    }

    public static class MapableBuilder {
        private boolean needAnnotation = false;
        private boolean mapSerializable = false;
        private boolean useEnumName = true;
        private boolean log = false;

        public MapableBuilder setNeedAnnotation(boolean needAnnotation) {
            this.needAnnotation = needAnnotation;
            return this;
        }

        public MapableBuilder setMapSerializable(boolean mapSerializable) {
            this.mapSerializable = mapSerializable;
            return this;
        }

        public MapableBuilder setUseEnumName(boolean useEnumName) {
            this.useEnumName = useEnumName;
            return this;
        }

        public MapableBuilder setLog(boolean log) {
            this.log = log;
            return this;
        }

        public Mapable createMapable() {
            return new Mapable(needAnnotation, mapSerializable, useEnumName, log);
        }
    }
}