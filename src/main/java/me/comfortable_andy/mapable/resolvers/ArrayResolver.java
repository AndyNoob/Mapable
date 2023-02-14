package me.comfortable_andy.mapable.resolvers;

import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;
import me.comfortable_andy.mapable.resolvers.data.SingleResolvedField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;

public class ArrayResolver implements IResolver {
    @Override
    public @NotNull List<Class<?>> getResolvableTypes() {
        return Collections.singletonList(Object[].class);
    }

    @Override
    public @Nullable ResolvedField resolve(@NotNull ResolvableField field) {
        final Object[] array = (Object[]) field.getValue();
        final Object newArray = Array.newInstance(field.getInfo().getType().getComponentType(), array.length);
        for (int i = 0; i < array.length; i++) {
            final Object unresolved = array[i];
            Array.set(newArray, i, ResolverRegistry.getInstance().resolve(unresolved.getClass(), new ResolvableField(new FieldInfo(unresolved.getClass()), unresolved, field.getInstance())));
        }
        return new SingleResolvedField(field.getInfo().getType(), newArray, field.getInstance());
    }

    @Override
    public @Nullable Object unresolve(@NotNull ResolvedField field, @NotNull FieldInfo info) {
        final Object[] array = (Object[]) field.getResolved();
        final Object newArray = Array.newInstance(info.getType().getComponentType(), array.length);

        for (int i = 0; i < array.length; i++) {
            final Object resolved = array[i];
            Array.set(newArray, i, ResolverRegistry.getInstance().unresolve(info.getType().getComponentType(), new SingleResolvedField(info.getType().getComponentType(), resolved, field.getInstance()), new FieldInfo(info.getType().getComponentType())));
        }

        return newArray;
    }
}
