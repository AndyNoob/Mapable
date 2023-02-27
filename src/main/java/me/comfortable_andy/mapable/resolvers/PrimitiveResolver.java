package me.comfortable_andy.mapable.resolvers;

import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;
import me.comfortable_andy.mapable.resolvers.data.SingleResolvedField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrimitiveResolver implements IResolver {
    
    private static final List<Class<?>> PRIMITIVES = Arrays.asList(double.class, float.class, long.class, int.class, short.class, char.class, byte.class, boolean.class);
    private static final List<Class<?>> WRAPPERS = Arrays.asList(Double.class, Float.class, Long.class, Integer.class, Short.class, Character.class, Byte.class, Boolean.class, String.class);
    private static final List<Class<?>> TARGET = Stream.concat(PRIMITIVES.stream(), WRAPPERS.stream()).collect(Collectors.toList());

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
        if (field.getResolved().getClass() != info.getType()) {
            if (!PRIMITIVES.contains(info.getType())) return field.getResolved(); // Too bad

            for (int i = 0; i < PRIMITIVES.size(); i++) {
                final Class<?> primitive = PRIMITIVES.get(i);
                if (info.getType() != primitive) continue;
                try {
                    return WRAPPERS.get(i).getMethod("valueOf", String.class).invoke(null, field.getResolved().toString());
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return field.getResolved();
    }
}
