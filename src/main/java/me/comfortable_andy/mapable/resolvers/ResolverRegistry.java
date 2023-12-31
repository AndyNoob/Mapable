package me.comfortable_andy.mapable.resolvers;

import lombok.NonNull;
import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"unused"})
public final class ResolverRegistry {

    private static final ResolverRegistry INSTANCE = new ResolverRegistry();

    private final List<IResolver> resolvers = new CopyOnWriteArrayList<>();

    private ResolverRegistry() {
        registerAll(new PrimitiveResolver(), new EnumResolver(), new ArrayResolver(), new ListResolver(), new MapResolver());
    }


    public void register(final @NonNull IResolver resolver) {
        this.resolvers.add(resolver);
    }

    public void registerAll(final IResolver... resolvers) {
        this.resolvers.addAll(List.of(resolvers));
    }

    public boolean unregister(final @NonNull Class<?> clazz) {
        return this.resolvers.removeIf(resolver -> resolver.getResolvableTypes().stream().anyMatch(clazs -> clazs.isAssignableFrom(clazz)));
    }

    public <R> R ifPresent(final @NonNull Class<?> clazz, @NonNull final Function<List<IResolver>, R> function) {
        final List<IResolver> resolver = this.resolvers.stream().filter(iResolver -> iResolver.canResolve(clazz)).sorted(Comparator.comparing(IResolver::getPriority)).collect(Collectors.toList());
        return function.apply(resolver);
    }

    public ResolvedField resolve(final @NonNull Class<?> clazz, final @NonNull ResolvableField field) {
        return ifPresent(clazz, resolvers -> resolvers.stream().map(resolver -> resolver.resolve(field)).filter(Objects::nonNull).findFirst().orElse(null));
    }

    public <T> ResolvableField unresolve(final @NonNull Class<T> clazz, final @NonNull ResolvedField field, final @NonNull FieldInfo info) {
        return ifPresent(clazz, resolvers -> resolvers.stream().map(resolver -> resolver.unresolve(field, info)).filter(Objects::nonNull).findFirst().orElse(null));
    }

    public static ResolverRegistry getInstance() {
        return INSTANCE;
    }

    public enum ResolverPriority {
        HIGH,
        DEFAULT,
        LOW
    }

}
