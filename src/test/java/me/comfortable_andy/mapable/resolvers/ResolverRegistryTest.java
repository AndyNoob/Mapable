package me.comfortable_andy.mapable.resolvers;

import lombok.Setter;
import me.comfortable_andy.mapable.Mapable;
import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResolverRegistryTest {

    @Setter
    public static class AlwaysThrowResolver implements IResolver {

        private ResolverRegistry.ResolverPriority priority = ResolverRegistry.ResolverPriority.HIGH;

        @Override
        public List<Class<?>> getResolvableTypes() {
            return Collections.singletonList(Object.class);
        }

        @Override
        public ResolverRegistry.ResolverPriority getPriority() {
            return priority;
        }

        @Override
        public ResolvedField resolve(ResolvableField field) {
            throw new RuntimeException();
        }

        @Override
        public ResolvableField unresolve(ResolvedField field, FieldInfo info) {
            throw new RuntimeException();
        }
    }

    private static final AlwaysThrowResolver RESOLVER = new AlwaysThrowResolver();
    private static final ResolvableField FIELD = new ResolvableField(new FieldInfo(int.class), 4, new Mapable(false, false, false));

    @BeforeAll
    static void setUp() {
        ResolverRegistry.getInstance().register(RESOLVER);
    }

    @Test
    void ifPresent() {
        RESOLVER.setPriority(ResolverRegistry.ResolverPriority.HIGH);
        // int.class doesn't extend Object???????
        assertThrowsExactly(RuntimeException.class, () -> ResolverRegistry.getInstance().ifPresent(Integer.class, iResolvers -> {
            assertEquals(iResolvers.size(), 2);
            assertEquals(RESOLVER, iResolvers.get(0));
            return iResolvers.get(0).resolve(FIELD);
        }));
        RESOLVER.setPriority(ResolverRegistry.ResolverPriority.LOW);
        assertDoesNotThrow(() -> ResolverRegistry.getInstance().ifPresent(Integer.class, iResolvers -> {
            assertEquals(iResolvers.size(), 2);
            assertEquals(RESOLVER, iResolvers.get(1));
            return null;
        }));
    }

    @Test
    void resolve() {
        RESOLVER.setPriority(ResolverRegistry.ResolverPriority.HIGH);
        assertThrowsExactly(RuntimeException.class, () -> System.out.println(ResolverRegistry.getInstance().resolve(Integer.class, FIELD)));
        RESOLVER.setPriority(ResolverRegistry.ResolverPriority.LOW);
        assertDoesNotThrow(() -> System.out.println(ResolverRegistry.getInstance().resolve(Integer.class, FIELD)));
    }

}