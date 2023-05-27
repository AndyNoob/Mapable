package me.comfortable_andy.mapable.resolvers.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.comfortable_andy.mapable.Mapable;

@RequiredArgsConstructor
public class ResolvedField {

    @Getter
    protected final Class<?> original;

    @Getter
    protected final Object resolved;

    @Getter
    @NonNull
    protected final Mapable instance;


}
