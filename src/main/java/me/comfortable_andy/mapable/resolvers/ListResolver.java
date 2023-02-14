package me.comfortable_andy.mapable.resolvers;

import lombok.SneakyThrows;
import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;
import me.comfortable_andy.mapable.resolvers.data.SingleResolvedField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ListResolver implements IResolver {
    @Override
    public @NotNull List<Class<?>> getResolvableTypes() {
        return Collections.singletonList(Collection.class);
    }

    @Override
    @SneakyThrows
    public @Nullable ResolvedField resolve(@NotNull ResolvableField field) {
        final Collection collection = (Collection) field.getValue();
        final Collection<Object> newCollection = this.make(field.getValue().getClass(), collection);

        for (final Object o : collection) {
            final ResolvedField resolvedField = ResolverRegistry.getInstance().resolve(o.getClass(), new ResolvableField(new FieldInfo(o.getClass()), o, field.getInstance()));
            newCollection.add(resolvedField == null ? field.getInstance().asMap(o) : resolvedField.getResolved());
        }

        return new SingleResolvedField(field.getInfo().getType(), newCollection, field.getInstance());
    }

    @Override
    public @Nullable Object unresolve(@NotNull ResolvedField field, @NotNull FieldInfo info) {
        final Collection collection = (Collection) field.getResolved();
        final Collection newCollection = make(collection.getClass(), collection);

        for (Object o : collection) {
            final ResolvedField resolvedField = new SingleResolvedField(o.getClass(), o, field.getInstance());

            Object unresolved = ResolverRegistry.getInstance().unresolve(o.getClass(), resolvedField, new FieldInfo(o.getClass()));

            System.out.println(o);

            if (unresolved == null && o instanceof Map) {
                try {
                    System.out.println(o);
                    unresolved = field.getInstance().fromMap((Map<String, Object>) o);
                } catch (ReflectiveOperationException ignored) {
                }
            }

            newCollection.add(unresolved);
        }

        return newCollection;
    }

    private Collection make(final Class clazz, final @NotNull Collection old) {
        try {
             return  (Collection<Object>) clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            System.out.println("Couldn't make a new collection from the existing value's class, defaulting with ArrayList");
            return new ArrayList<>(old.size());
        }
    }

}