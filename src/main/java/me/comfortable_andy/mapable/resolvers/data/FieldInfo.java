package me.comfortable_andy.mapable.resolvers.data;

import lombok.Data;
import lombok.NonNull;
import me.comfortable_andy.mapable.util.ClassUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

@Data
public class FieldInfo {
    @NonNull
    private final Class<?> type;
    private Class<?>[] generics = null;

    public FieldInfo(@NonNull Class<?> type) {
        this.type = type;
    }

    public FieldInfo setGenericsType(@NonNull final Type type) {
        if (!(type instanceof ParameterizedType)) return this;
        final ParameterizedType parameterizedType = (ParameterizedType) type;
        if (parameterizedType.getActualTypeArguments().length == 0) return this;
        this.generics = Arrays.stream(parameterizedType.getActualTypeArguments()).map(Type::getTypeName).map(ClassUtil::fromNameOrNull).toArray(Class[]::new);
        return this;
    }
}
