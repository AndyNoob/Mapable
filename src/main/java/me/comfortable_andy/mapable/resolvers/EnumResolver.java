package me.comfortable_andy.mapable.resolvers;

import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EnumResolver implements IResolver {

    @Override
    public @NotNull List<Class<?>> getResolvableTypes() {
        return Collections.singletonList(Enum.class);
    }

    @Override
    public @Nullable ResolvedField resolve(@NotNull ResolvableField field) {
        if (field.getValue() == null) return null;
        return new ResolvedField(field.getInfo().getType(), ((Enum) field.getValue()).name(), field.getInstance());
    }

    @Override
    public @Nullable ResolvableField unresolve(@NotNull ResolvedField field, @NotNull FieldInfo info) {
        return new ResolvableField(info, Enum.valueOf((Class<? extends Enum>) info.getType(), field.getResolved().toString()), field.getInstance());
    }
}
