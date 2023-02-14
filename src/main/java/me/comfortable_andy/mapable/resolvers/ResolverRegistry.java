package me.comfortable_andy.mapable.resolvers;

import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class ResolverRegistry {

    private static final ResolverRegistry INSTANCE = new ResolverRegistry();

    private final List<IResolver> resolvers = new CopyOnWriteArrayList<>();

    private ResolverRegistry() {
        registerAll(new PrimitiveResolver(), new EnumResolver(), new ArrayResolver(), new ListResolver());
    }


    public void register(final @NotNull IResolver resolver) {
        this.resolvers.add(resolver);
    }

    public void registerAll(final IResolver... resolvers) {
        this.resolvers.addAll(List.of(resolvers));
    }

    public boolean unregister(final @NotNull Class<?> clazz) {
        return this.resolvers.removeIf(resolver -> resolver.getResolvableTypes().stream().anyMatch(clazs -> clazs.isAssignableFrom(clazz)));
    }

    @Nullable
    public <R> R ifPresent(final @NotNull Class<?> clazz, @NotNull final Function<List<IResolver>, R> function) {
        final List<IResolver> resolver = this.resolvers.stream().filter(iResolver -> iResolver.getResolvableTypes().stream().anyMatch(clazs -> clazs.isAssignableFrom(clazz))).collect(Collectors.toList());
        return function.apply(resolver);
    }

    @Nullable
    public ResolvedField resolve(final @NotNull Class<?> clazz, final @NotNull ResolvableField field) {
        return ifPresent(clazz, resolvers -> resolvers.stream().map(resolver -> resolver.resolve(field)).filter(Objects::nonNull).findFirst().orElse(null));
    }

    @Nullable
    public <T> T unresolve(final @NotNull Class<T> clazz, final @NotNull ResolvedField field, final @NotNull FieldInfo info) {
        return (T) ifPresent(clazz, resolvers -> resolvers.stream().map(resolver -> resolver.unresolve(field, info)).filter(Objects::nonNull).findFirst().orElse(null));
    }

    public static ResolverRegistry getInstance() {
        return INSTANCE;
    }

}
