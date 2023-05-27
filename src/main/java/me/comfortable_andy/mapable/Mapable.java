package me.comfortable_andy.mapable;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import me.comfortable_andy.mapable.resolvers.ResolverRegistry;
import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;
import me.comfortable_andy.mapable.util.ClassUtil;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static me.comfortable_andy.mapable.MapableConstants.CLAZZ_KEY;
import static me.comfortable_andy.mapable.util.ClassUtil.isPrimitiveOrString;

/**
 * Will NOT attempt to convert maps into other maps
 *
 * @author AndyNoob
 */
@SuppressWarnings("unchecked")
@ToString
@Log
public final class Mapable {

    private final boolean needAnnotation;
    private final boolean dontMapSerializable;
    private final boolean shouldLog;

    public Mapable(boolean needAnnotation, boolean mapSerializable, boolean log) {
        this.needAnnotation = needAnnotation;
        this.dontMapSerializable = !mapSerializable;
        this.shouldLog = log;
    }

    public Map<String, Object> asMap(@NonNull final Object toMap) throws ReflectiveOperationException {
        return asMap(toMap, null);
    }

    /**
     * Uses reflection to retrieve all fields from the parameter and uses recursion in the case
     * of arrays or un-serializable fields.
     *
     * @param toMap The object used to map (null if clazz is an interface or annotation).
     * @return Serialized data in map.
     */
    public <T> Map<String, Object> asMap(final T toMap, Class<T> clazz) throws ReflectiveOperationException {
        if (toMap == null) return null;
        if (clazz == null) clazz = (Class<T>) toMap.getClass();

        final Map<String, Object> map = new HashMap<>();

        map.put(CLAZZ_KEY, clazz.getName());

        for (final Map.Entry<String, Field> entry : findApplicableFields(clazz, new HashMap<>()).entrySet()) {
            final String name = entry.getKey();
            final Field javaField = entry.getValue();

            final FieldInfo info = new FieldInfo(javaField.getType()).setGenericsType(javaField.getGenericType());
            final ResolvableField field = new ResolvableField(info, javaField.get(toMap), this);

            if (field.getValue() == null) continue;
            if (field.getValue() instanceof Serializable && this.dontMapSerializable) {
                debug(() -> "Mapped serializable " + field.getValue());
                map.put(name, field.getValue());
                continue;
            }

            final ResolvedField resolvedField = ResolverRegistry.getInstance().resolve(javaField.getType(), field);

            if (resolvedField == null) debug(() -> "Couldn't resolve " + javaField);

            map.put(name, resolvedField == null ? asMap(field.getValue()) : resolvedField.getResolved());
        }

        return map;
    }

    public Object fromMap(final @NonNull Map<String, Object> map) throws ReflectiveOperationException {
        return fromMap(map, null);
    }

    /**
     * Uses reflection to retrieve all fields from the designated class (the <code>===classname</code>)
     * and uses recursion in the case of arrays or other serializable fields.
     *
     * @param map Serialized object in <code>Mapable#asMap</code>.
     * @return Deserialized object.
     * @see #asMap(Object)
     */
    public <T> T fromMap(final @NonNull Map<String, Object> map, Class<T> clazz) throws ReflectiveOperationException {
        if (clazz == null) clazz = ClassUtil.fromNameOrNull(String.valueOf(map.get(CLAZZ_KEY)));
        if (clazz == null) throw new IllegalStateException("Couldn't identify class!");

        if (isPrimitiveOrString(clazz) || clazz.isArray() || clazz.isAnnotation() || clazz.isEnum() || clazz.isAnonymousClass() || clazz.getPackageName().startsWith("java") || clazz.getPackageName().startsWith("sun"))
            throw new IllegalArgumentException(clazz + " is not supported (try wrapping it in your own class)!");

        final Map<Field, ResolvableField> resolvables = new LinkedHashMap<>();

        for (final Map.Entry<String, Field> entry : findApplicableFields(clazz, new LinkedHashMap<>()).entrySet()) {
            final String name = entry.getKey();
            final Field javaField = entry.getValue();

            if (!map.containsKey(name)) {
                resolvables.put(javaField, null);
                continue;
            }

            Object value = map.get(name);
            final FieldInfo info = new FieldInfo(javaField.getType()).setGenericsType(javaField.getGenericType());

            if (dontMapSerializable &&
                    Serializable.class.isAssignableFrom(javaField.getType()) &&
                    javaField.getType().isAssignableFrom(value.getClass())) {
                debug(() -> name + " is serializable!");
                resolvables.put(javaField, new ResolvableField(info, value, this));
                continue;
            }

            if (value instanceof Map) {
                debug(() -> name + " is a map!");
                resolvables.put(javaField, new ResolvableField(info, fromMap((Map<String, Object>) value), this));
            } else { // Attempt to resolve
                debug(() -> "Resolving " + javaField);
                final ResolvedField field = new ResolvedField(javaField.getType(), map.get(name), this);
                final ResolvableField resolvableField = ResolverRegistry.getInstance().unresolve(javaField.getType(), field, info);

                debug(() -> "    Done! " + resolvableField);

                resolvables.put(javaField, resolvableField);
            }
        }

        final T object;

        if (ClassUtil.isRecord(clazz)) {
            object = (T) clazz.getDeclaredConstructors()[0].newInstance(resolvables.values().stream().map(field -> field == null ? null : field.getValue()).toArray());
        } else {
            object = ClassUtil.construct(clazz, true);

            if (object == null) throw new IllegalStateException("Couldn't initialize a desired object!");

            for (final Map.Entry<Field, ResolvableField> entry : resolvables.entrySet()) {
                if (entry.getValue() == null) continue;
                entry.getValue().applyToJavaField(object, entry.getKey());
            }
        }

        return object;
    }

    @NonNull
    private Map<String, Field> findApplicableFields(final @NonNull Class<?> clazz, final @NonNull Map<String, Field> fields) {
        debug(() -> "Searching for " + clazz);

        for (final Field field : clazz.getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
            if (!field.trySetAccessible()) continue;

            final MapMe mapTo = field.getAnnotation(MapMe.class);

            if (mapTo == null && this.needAnnotation) continue;

            fields.put(mapTo != null && !mapTo.mapName().isEmpty() ? mapTo.mapName() : field.getName(), field);
        }

        debug(() -> "Done");
        debug(() -> "    Total " + fields);

        return clazz.getSuperclass() == null || clazz.getSuperclass() == Object.class ? fields : findApplicableFields(clazz.getSuperclass(), fields);
    }

    public void debug(Supplier<String> str) {
        if (shouldLog) log.info(("[" + this + "] ") + str.get());
    }

    /**
     * Required for the serializing and deserializing process.
     *
     * @author AndyNoob
     * @see #asMap(Object, Class)
     * @see #fromMap(Map, Class)
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MapMe {
        /**
         * Default variable name
         */
        String mapName() default "";
    }

}