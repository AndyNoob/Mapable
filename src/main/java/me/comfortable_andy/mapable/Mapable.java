package me.comfortable_andy.mapable;

import lombok.ToString;
import me.comfortable_andy.mapable.resolvers.ResolverRegistry;
import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;
import me.comfortable_andy.mapable.resolvers.data.SingleResolvedField;
import me.comfortable_andy.mapable.util.ClassUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static me.comfortable_andy.mapable.MapableConstants.CLAZZ_KEY;
import static me.comfortable_andy.mapable.util.ClassUtil.isPrimitiveOrString;

/**
 * Will NOT attempt to convert maps into other maps
 *
 * @author AndyNoob
 */
@SuppressWarnings("unchecked")
@ToString
public final class Mapable {

    private final boolean needAnnotation;
    private final boolean mapSerializable;
    private final boolean log;

    public Mapable(boolean needAnnotation, boolean mapSerializable, boolean log) {
        this.needAnnotation = needAnnotation;
        this.mapSerializable = mapSerializable;
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
        if (clazz == null) clazz = (Class<T>) toMap.getClass();

        final Map<String, Object> map = new HashMap<>();

        map.put(CLAZZ_KEY, clazz.getName());

        for (final Map.Entry<String, Field> entry : findApplicableFields(clazz, new HashMap<>()).entrySet()) {
            final String name = entry.getKey();
            final Field javaField = entry.getValue();

            final FieldInfo info = new FieldInfo(javaField.getType());
            final ResolvableField field = new ResolvableField(info, javaField.get(toMap), this);

            if (field.getValue() == null) continue;
            if (field.getValue() instanceof Serializable && !this.mapSerializable) {
                debug("Mapped serializable " + field.getValue());
                map.put(name, field.getValue());
                continue;
            }

            final ResolvedField resolvedField = ResolverRegistry.getInstance().resolve(javaField.getType(), field);

            if (resolvedField == null) debug("Couldn't resolve " + javaField);

            map.put(name, resolvedField == null ? asMap(field.getValue()) : resolvedField.getResolved());
        }

        return map;
    }

    public Object fromMap(final @NotNull Map<String, Object> map) throws ReflectiveOperationException {
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
    public <T> T fromMap(final @NotNull Map<String, Object> map, @Nullable Class<T> clazz) throws ReflectiveOperationException {
        if (clazz == null) clazz = ClassUtil.fromName(String.valueOf(map.get(CLAZZ_KEY)));
        if (clazz == null) throw new IllegalStateException("Couldn't identify class!");

        if (isPrimitiveOrString(clazz) || clazz.isArray() || clazz.isAnnotation() || clazz.isEnum() || clazz.isAnonymousClass() || clazz.getPackageName().startsWith("java") || clazz.getPackageName().startsWith("sun")) throw new IllegalArgumentException(clazz + " is not supported (try wrapping it in your own class)!");

        final T object = ClassUtil.construct(clazz, true);

        for (final Map.Entry<String, Field> entry : findApplicableFields(clazz, new HashMap<>()).entrySet()) {
            final String name = entry.getKey();
            final Field javaField = entry.getValue();

            if (!map.containsKey(name)) continue;

            Object value = map.get(name);

            if (value instanceof Map) {
                try {
                    value = fromMap((Map<String, Object>) value);
                } catch (ReflectiveOperationException ignored) {
                }
            } else if (!(value instanceof Serializable && !this.mapSerializable)) {
                final FieldInfo info = new FieldInfo(javaField.getType());
                final SingleResolvedField field = new SingleResolvedField(javaField.getType(), map.get(name), this);
                value = ResolverRegistry.getInstance().unresolve(javaField.getType(), field, info);
            }

            javaField.set(object, value);
        }

        return object;
    }

    @NotNull
    private Map<String, Field> findApplicableFields(final @NotNull Class<?> clazz, final @NotNull Map<String, Field> fields) {
        debug("Searching for " + clazz);
        for (final Field field : clazz.getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
            if (!field.trySetAccessible()) continue;

            final MapMe mapTo = field.getAnnotation(MapMe.class);

            if (mapTo == null && this.needAnnotation) continue;

            fields.put(mapTo != null && !mapTo.mapName().isEmpty() ? mapTo.mapName() : field.getName(), field);
        }

        return clazz.getSuperclass() == null || clazz.getSuperclass() == Object.class ? fields : findApplicableFields(clazz.getSuperclass(), fields);
    }

    private void debug(String str) {
        if (log) System.out.println(("[" + this.toString() + "] ") + str);
    }

    /**
     * Required for the serializing and deserializing process.
     *
     * @author AndyNoob
     * @see #asMap(Object, Class)
     * @see #fromMap(Map, Class)
     */
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface MapMe {
        /**
         * Default variable name
         */
        String mapName() default "";
    }

}