package me.comfortable_andy.mapable.resolvers;

import lombok.NonNull;
import me.comfortable_andy.mapable.resolvers.data.FieldInfo;
import me.comfortable_andy.mapable.resolvers.data.ResolvableField;
import me.comfortable_andy.mapable.resolvers.data.ResolvedField;

import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EnumResolver implements IResolver {

    @Override
    public @NonNull List<Class<?>> getResolvableTypes() {
        return Collections.singletonList(Enum.class);
    }

    @Override
    public ResolvedField resolve(@NonNull ResolvableField field) {
        if (field.getValue() == null) return null;
        return new ResolvedField(field.getInfo().getType(), ((Enum) field.getValue()).name(), field.getInstance());
    }

    @Override
    public ResolvableField unresolve(@NonNull ResolvedField field, @NonNull FieldInfo info) {
        return new ResolvableField(info, Enum.valueOf((Class<? extends Enum>) info.getType(), field.getResolved().toString()), field.getInstance());
    }
}
