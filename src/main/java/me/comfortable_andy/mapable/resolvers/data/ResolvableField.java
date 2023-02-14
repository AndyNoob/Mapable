package me.comfortable_andy.mapable.resolvers.data;

import lombok.Getter;
import me.comfortable_andy.mapable.Mapable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ResolvableField {

    private final FieldInfo info;
    private final @Nullable Object value;
    private final Mapable instance;

    public ResolvableField(@NotNull final FieldInfo info, @Nullable final Object value, @NotNull final Mapable instance) {
        this.info = info;
        this.value = value;
        this.instance = instance;
    }
}
