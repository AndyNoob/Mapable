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
    private static final Map<Class, Map<String, Field>> CACHE = new ConcurrentHashMap<>();

    private final boolean needAnnotation, mapSerializable, assumeEmptyConstructor, useEnumName, log;

    public Mapable(boolean needAnnotation, boolean mapSerializable, boolean assumeEmptyConstructor, boolean useEnumName, boolean log) {
        this.needAnnotation = needAnnotation;
        this.mapSerializable = mapSerializable;
        this.assumeEmptyConstructor = assumeEmptyConstructor;
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
    public <T> Map<String, Object> asMap(@NotNull final T toMap, @Nullable Class<T> clazz) throws ReflectiveOperationException {
        LOGGER.setLevel(log ? Level.ALL : Level.OFF);
        //noinspection unchecked
        clazz = clazz == null ? (Class<T>) toMap.getClass() : clazz; // WTF java so weird

        if (clazz.isInterface() || clazz.isAnnotation()) return null;

        final Map<String, Object> map = new HashMap<>();

        map.put("==", clazz.getName());

        if (clazz.isArray()) {
            LOGGER.info(clazz + " is array!");
            final Object[] array = (Object[]) toMap;

            for (int i = 0; i < array.length; i++) map.put(String.valueOf(i), asMap(array[i]));

            return map;
        } else if (clazz.isEnum()) {
            LOGGER.info(clazz + " is enum!");
            if (this.useEnumName) map.put("name", toMap);
            else map.put("ordinal", Arrays.asList(clazz.getEnumConstants()).indexOf(toMap));
            return map;
        } else if (clazz.isPrimitive()) {
            LOGGER.info(clazz + " is primitive!");
            map.put("value", toMap);
        }

        for (final Map.Entry<String, Field> entry : findApplicableFields(clazz, new HashMap<>()).entrySet()) {
            final String name = entry.getKey();
            final Field field = entry.getValue();

            Object value = field.get(toMap);

            if (!(value instanceof Serializable) && this.mapSerializable) {
                LOGGER.info(field + " is not serializable, attempting to map...");
                try {
                    value = asMap(value);
                } catch (Exception e) {
                    throw new IllegalStateException("Couldn't map " + toMap + " (" + clazz + ")", e);
                }
            }

            map.put(name, value);
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

        LOGGER.setLevel(log ? Level.ALL : Level.OFF);

        final Class<?> clazz = Class.forName(map.get("==").toString());

        if (clazz.isArray()) {
            LOGGER.info(clazz + " is array!");
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
            LOGGER.info(clazz + " is enum!");
            if (useEnumName) return Enum.valueOf((Class<? extends Enum>) clazz, map.get("name").toString());
            else return clazz.getEnumConstants()[Integer.parseInt(map.get("ordinal").toString())];
        }

        Object object;

        try {
            object = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
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
        if (CACHE.containsKey(clazz)) {
            LOGGER.info("Returning from cache (" + clazz + ")");
            return CACHE.get(clazz);
        }

        for (final Field field : clazz.getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
            if (!field.trySetAccessible()) continue;

            final MapMe mapTo = field.getAnnotation(MapMe.class);

            fields.put(mapTo != null && !mapTo.mapName().isEmpty() ? mapTo.mapName() : field.getName(), field);
        }

        if (clazz.getSuperclass() == null) {
            LOGGER.info("Caching field data (" + clazz + ")");
            CACHE.put(clazz, new ConcurrentHashMap<>(fields));
            return fields;
        } else return findApplicableFields(clazz, fields);
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
        private boolean mapSerializable = true;
        private boolean assumeEmptyConstructor = true;
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

        public MapableBuilder setAssumeEmptyConstructor(boolean assumeEmptyConstructor) {
            this.assumeEmptyConstructor = assumeEmptyConstructor;
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
            return new Mapable(needAnnotation, mapSerializable, assumeEmptyConstructor, useEnumName, log);
        }
    }
}