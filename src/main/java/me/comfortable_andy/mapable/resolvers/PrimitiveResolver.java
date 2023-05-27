package me.comfortable_andy.mapable.resolvers;

import lombok.NonNull;
import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.comfortable_andy.mapable.util.ClassUtil.PRIMITIVES;
import static me.comfortable_andy.mapable.util.ClassUtil.WRAPPERS;

public class PrimitiveResolver implements IResolver {
    private static final List<Class<?>> TARGET = Stream.concat(PRIMITIVES.stream(), WRAPPERS.stream()).collect(Collectors.toList());

    @Override
    public @NonNull List<Class<?>> getResolvableTypes() {
        return TARGET;
    }

    @Override
    public ResolvedField resolve(@NonNull ResolvableField field) {
        return new ResolvedField(field.getInfo().getType(), field.getValue(), field.getInstance());
    }

    @Override
    public ResolvableField unresolve(@NonNull ResolvedField field, @NonNull FieldInfo info) {
        return new ResolvableField(info, field.getResolved(), field.getInstance());
    }
}
