package me.comfortable_andy.mapable.resolvers;

import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IResolver {

    @NotNull List<Class<?>> getResolvableTypes();

    @NotNull default ResolverRegistry.ResolverPriority getPriority() {
        return ResolverRegistry.ResolverPriority.DEFAULT;
    }

    @Nullable ResolvedField resolve(@NotNull final ResolvableField field);

    @Nullable ResolvableField unresolve(@NotNull ResolvedField field, @NotNull FieldInfo info);

}
