package me.comfortable_andy.mapable.resolvers.data;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class FieldInfo {
    @NotNull
    private final Class<?> type;

    public FieldInfo(@NotNull Class<?> type) {
        this.type = type;
    }
}
