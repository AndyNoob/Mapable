package me.comfortable_andy.mapable.resolvers.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.comfortable_andy.mapable.Mapable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public abstract class ResolvedField {

    @Getter
    @Nullable
    protected final Class<?> original;

    @Getter
    @NotNull
    protected final Mapable instance;

    public abstract Object getResolved();

}
