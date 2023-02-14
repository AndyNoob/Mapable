package me.comfortable_andy.mapable.resolvers;

import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;
import me.comfortable_andy.mapable.resolvers.data.SingleResolvedField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class PrimitiveResolver implements IResolver {
    
    private static final List<Class<?>> TARGET = Arrays.asList(double.class, float.class, long.class, int.class, short.class, char.class, byte.class, boolean.class, Double.class, Float.class, Long.class, Integer.class, Short.class, Character.class, Byte.class, Boolean.class, String.class);

    @Override
    public @NotNull List<Class<?>> getResolvableTypes() {
        return TARGET;
    }

    @Override
    public @Nullable ResolvedField resolve(@NotNull ResolvableField field) {
        return new SingleResolvedField(field.getInfo().getType(), field.getValue(), field.getInstance());
    }

    @Override
    public @Nullable Object unresolve(@NotNull ResolvedField field, @NotNull FieldInfo info) {
        return field.getResolved();
    }
}
