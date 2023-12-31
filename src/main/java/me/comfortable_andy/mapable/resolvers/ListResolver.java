package me.comfortable_andy.mapable.resolvers;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;

import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ListResolver implements IResolver {
    @Override
    public @NonNull List<Class<?>> getResolvableTypes() {
        return Collections.singletonList(Collection.class);
    }

    @Override
    @SneakyThrows
    public ResolvedField resolve(@NonNull ResolvableField field) {
        if (field.getValue() == null) return null;
        final Collection collection = (Collection) field.getValue();
        final Collection<Object> newCollection = this.make(field.getValue().getClass(), collection);

        for (final Object o : collection) {
            final ResolvedField resolvedField = ResolverRegistry.getInstance().resolve(o.getClass(), new ResolvableField(new FieldInfo(o.getClass()), o, field.getInstance()));
            newCollection.add(resolvedField == null ? field.getInstance().asMap(o) : resolvedField.getResolved());
        }

        return new ResolvedField(field.getInfo().getType(), newCollection, field.getInstance());
    }

    @Override
    public ResolvableField unresolve(@NonNull ResolvedField field, @NonNull FieldInfo info) {
        final Class<?> elementType = info.getGenerics()[0];

        field.getInstance().debug(() -> "Element type is " + elementType);

        if (elementType == null) return null;

        final Collection collection = (Collection) field.getResolved();
        final Collection newCollection = make(collection.getClass(), collection);

        for (Object o : collection) {
            final ResolvedField resolvedField = new ResolvedField(elementType, o, field.getInstance());
            final ResolvableField resolvableField = ResolverRegistry.getInstance().unresolve(elementType, resolvedField, new FieldInfo(elementType));

            Object unresolved = resolvableField == null ? null : resolvableField.getValue();

            if (unresolved == null && o instanceof Map) {
                try {
                    unresolved = field.getInstance().fromMap((Map<String, Object>) o);
                } catch (ReflectiveOperationException ignored) {
                }
            }

            newCollection.add(unresolved);
        }

        return new ResolvableField(info, newCollection, field.getInstance());
    }

    private Collection make(final Class clazz, final @NonNull Collection old) {
        try {
            return (Collection<Object>) clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            return new ArrayList<>(old.size());
        }
    }

}
