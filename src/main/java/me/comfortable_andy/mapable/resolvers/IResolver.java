package me.comfortable_andy.mapable.resolvers;

import lombok.NonNull;
import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;

import java.util.List;

public interface IResolver {

    @NonNull List<Class<?>> getResolvableTypes();

    default boolean canResolve(@NonNull final Class<?> clazz) {
        return getResolvableTypes().stream().anyMatch(resolvableClazz -> resolvableClazz.isAssignableFrom(clazz));
    }

    @NonNull default ResolverRegistry.ResolverPriority getPriority() {
        return ResolverRegistry.ResolverPriority.DEFAULT;
    }

    ResolvedField resolve(@NonNull final ResolvableField field);

    ResolvableField unresolve(@NonNull ResolvedField field, @NonNull FieldInfo info);

}
