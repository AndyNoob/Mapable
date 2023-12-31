package me.comfortable_andy.mapable.resolvers;

import lombok.NonNull;
import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;

import java.util.*;

@SuppressWarnings("unchecked")
public class MapResolver implements IResolver {

    @Override
    public @NonNull List<Class<?>> getResolvableTypes() {
        return Collections.singletonList(Map.class);
    }

    @Override
    public ResolvedField resolve(@NonNull ResolvableField field) {
        if (field.getValue() == null) return null;
        final Map<Object, Object> originalMap = (Map<Object, Object>) field.getValue();
        final Map<Object, Object> newMap = this.make(originalMap);

        for (Object key : originalMap.keySet()) {
            final ResolvedField keyField = ResolverRegistry
                    .getInstance()
                    .resolve(
                            key.getClass(),
                            new ResolvableField(new FieldInfo(key.getClass()), key, field.getInstance())
                    );
            final Object value = originalMap.get(key);
            final ResolvedField valueField = ResolverRegistry
                    .getInstance()
                    .resolve(
                            value.getClass(),
                            new ResolvableField(new FieldInfo(value.getClass()), value, field.getInstance())
                    );

            newMap.put(keyField == null ? key : keyField.getResolved(), valueField == null ? originalMap.get(key) : valueField.getResolved());
        }

        return new ResolvedField(originalMap.getClass(), newMap, field.getInstance());
    }

    @Override
    public ResolvableField unresolve(@NonNull ResolvedField field, @NonNull FieldInfo info) {
        if (info.getGenerics().length != 2)
            throw new IllegalArgumentException("incorrect amount of generics");
        final Class<?> keyClazz = info.getGenerics()[0];
        final Class<?> valueClazz = info.getGenerics()[1];
        final Map<Object, Object> originalMap = (Map<Object, Object>) field.getResolved();
        final Map<Object, Object> newMap = this.make(originalMap);
        for (Object key : originalMap.keySet()) {
            final ResolvableField keyField = ResolverRegistry.getInstance().unresolve(keyClazz, new ResolvedField(keyClazz, key, field.getInstance()), new FieldInfo(keyClazz));
            final Object value = originalMap.get(key);
            final ResolvableField valueField = ResolverRegistry.getInstance().unresolve(valueClazz, new ResolvedField(valueClazz, value, field.getInstance()), new FieldInfo(valueClazz));

            newMap.put(keyField == null ? key : keyField.getValue(), valueField == null ? value : valueField.getValue());
        }
        return new ResolvableField(info, newMap, field.getInstance());
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object> make(@NonNull Map<Object, Object> original) {
        try {
            return (Map<Object, Object>) original.getClass().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            return new HashMap<>();
        }
    }

}
