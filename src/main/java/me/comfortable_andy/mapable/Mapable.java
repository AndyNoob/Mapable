package me.comfortable_andy.mapable;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Supports multi-dimension array.
 *
 * @author AndyNoob
 * @apiNote Requires an empty constructor for the deserializing process
 */
public class Mapable {

    /**
     * Uses reflection to retrieve all fields from the parameter and uses recursion in the case
     * of arrays or un-serializable fields.
     *
     * @param toMap The object used to map.
     * @return Serialized data in map.
     * @throws ReflectiveOperationException
     */
    public static Map<String, Object> asMap(Object toMap) throws ReflectiveOperationException {
        if (toMap == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("==", toMap.getClass().getName());

        if (toMap.getClass().isArray()) {
            Object[] array = (Object[]) toMap;

            for (int i = 0; i < array.length; i++) {
                map.put(i + "", asMap(array[i]));
            }

            return map;
        }

        for (Field field : toMap.getClass().getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            boolean accessible = field.isAccessible();
            field.setAccessible(true);

            MapMe mapTo = field.getAnnotation(MapMe.class);

            if (mapTo == null) {
                continue;
            }

            String name = !mapTo.mapName().equals("") ? mapTo.mapName() : field.getName();
            Object value = field.get(toMap);

            if (!(value instanceof Serializable)) {
                value = asMap(value);
            }

            map.put(name, value);

            field.setAccessible(accessible);
        }

        return map;
    }

    /**
     * Uses reflection to retrieve all fields from the designated class (the <code>===classname</code>)
     * and uses recursion in the case of arrays or other serializable fields.
     *
     * @param map Serialized object in <code>Mapable#asMap</code>.
     * @return Deserialized object.
     * @throws ReflectiveOperationException
     * @see #asMap(Object)
     */
    public static Object fromMap(Map<String, Object> map) throws ReflectiveOperationException {
        if (!map.containsKey("==")) {
            return null;
        }

        Class<?> clazz = Class.forName(map.getOrDefault("==", "java.lang.Object").toString());

        if (clazz.isArray()) {
            Object array = Array.newInstance(clazz.getComponentType(), map.size() - 1);
            Class<?> accepted = array.getClass().getComponentType();

            for (int i = 0; i < map.size(); i++) {
                Object o = map.get(i + "");

                if (!(o instanceof Map)) {
                    continue;
                }

                o = fromMap((Map<String, Object>) o);
                Array.set(array, i, accepted.cast(o));
            }

            return array;
        }

        Object object = clazz.newInstance();

        for (Field field : object.getClass().getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            boolean accessible = field.isAccessible();
            field.setAccessible(true);

            MapMe mapTo = field.getAnnotation(MapMe.class);

            if (mapTo == null) {
                continue;
            }

            String name = !mapTo.mapName().equals("") ? mapTo.mapName() : field.getName();
            Object value = map.get(name);

            if (value instanceof Map) {
                Map<?, ?> valueMap = (Map<?, ?>) value;
                value = fromMap((Map<String, Object>) valueMap);
            }

            if (value == null) {
                continue;
            }

            field.set(object, value);

            field.setAccessible(accessible);
        }

        return object;
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
}