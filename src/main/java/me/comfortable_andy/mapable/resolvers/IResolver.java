package me.comfortable_andy.mapable.resolvers;

import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IResolver {

    @NotNull
    List<Class<?>> getResolvableTypes();

    @Nullable
    ResolvedField resolve(@NotNull final ResolvableField field);

    @Nullable Object unresolve(@NotNull ResolvedField field, @NotNull FieldInfo info);
}
