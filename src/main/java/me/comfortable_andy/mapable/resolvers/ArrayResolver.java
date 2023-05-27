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
    public ResolvedField resolve(@NonNull ResolvableField field) {
        if (field.getValue() == null) return null;
        final Object[] array = (Object[]) field.getValue();
        final Object newArray = Array.newInstance(field.getInfo().getType().getComponentType(), array.length);
        for (int i = 0; i < array.length; i++) {
            final Object unresolved = array[i];
            Array.set(newArray, i, ResolverRegistry.getInstance().resolve(unresolved.getClass(), new ResolvableField(new FieldInfo(unresolved.getClass()), unresolved, field.getInstance())));
        }
        return new ResolvedField(field.getInfo().getType(), newArray, field.getInstance());
    }

    @Override
    public ResolvableField unresolve(@NonNull ResolvedField field, @NonNull FieldInfo info) {
        final Object[] array = (Object[]) field.getResolved();
        final Object newArray = Array.newInstance(info.getType().getComponentType(), array.length);

        for (int i = 0; i < array.length; i++) {
            final Object resolved = array[i];
            Array.set(newArray, i, ResolverRegistry.getInstance().unresolve(info.getType().getComponentType(), new ResolvedField(info.getType().getComponentType(), resolved, field.getInstance()), new FieldInfo(info.getType().getComponentType())));
        }

        return new ResolvableField(info, newArray, field.getInstance());
    }
}
