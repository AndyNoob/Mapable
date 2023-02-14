package me.comfortable_andy.mapable.resolvers.data;

import me.comfortable_andy.mapable.Mapable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleResolvedField extends ResolvedField {

    private final Object value;

    public SingleResolvedField(@Nullable final Class<?> original, @NotNull final Object value, @NotNull final Mapable instance) {
        super(original, instance);
        this.value = value;
    }

    @Override
    public Object getResolved() {
        return value;
    }
}
