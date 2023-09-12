package me.comfortable_andy.mapable.resolvers;

import lombok.NonNull;
import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;

public class ArrayResolver implements IResolver {
    @Override
    public @NonNull List<Class<?>> getResolvableTypes() {
        return Collections.singletonList(Object[].class);
    }

    @Override
    public boolean canResolve(@NonNull Class<?> clazz) {
        return clazz.isArray();
    }

    @Override
    public ResolvedField resolve(@NonNull ResolvableField field) {
        final Object array = field.getValue();
        if (array == null) return null;
        final int length = Array.getLength(array);
        final Object newArray = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array));
        for (int i = 0; i < length; i++) {
            final Object unresolved = Array.get(array, i);
            final ResolvableField currentItem = new ResolvableField(
                    new FieldInfo(unresolved.getClass()),
                    unresolved,
                    field.getInstance()
            );
            Array.set(
                    newArray, i,
                    ResolverRegistry.getInstance().resolve(unresolved.getClass(), currentItem).getResolved()
            );
        }
        return new ResolvedField(field.getInfo().getType(), newArray, field.getInstance());
    }

    @Override
    public ResolvableField unresolve(@NonNull ResolvedField field, @NonNull FieldInfo info) {
        final Object array = field.getResolved();
        final int length = Array.getLength(array);
        final Object newArray = Array.newInstance(info.getType().getComponentType(), length);

        for (int i = 0; i < length; i++) {
            final Object resolved = Array.get(array, i);
            ResolverRegistry.getInstance().unresolve(
                    info.getType().getComponentType(),
                    new ResolvedField(info.getType().getComponentType(), resolved, field.getInstance()),
                    new FieldInfo(info.getType().getComponentType())
            ).applyToArray(newArray, i);
        }

        return new ResolvableField(info, newArray, field.getInstance());
    }
}
