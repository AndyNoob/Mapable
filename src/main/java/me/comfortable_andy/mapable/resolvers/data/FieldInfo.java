package me.comfortable_andy.mapable.resolvers.data;

import lombok.Data;
import me.comfortable_andy.mapable.util.ClassUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Data
public class FieldInfo {
    @NotNull
    private final Class<?> type;
    @Nullable
    private Class<?> generics = null;

    public FieldInfo(@NotNull Class<?> type) {
        this.type = type;
    }

    @Contract("_ -> this")
    public FieldInfo setGenericsType(@NotNull final Type type) {
        if (!(type instanceof ParameterizedType)) return this;
        final ParameterizedType parameterizedType = (ParameterizedType) type;
        if (parameterizedType.getActualTypeArguments().length == 0) return this;
        this.generics = ClassUtil.fromNameOrNull(parameterizedType.getActualTypeArguments()[0].getTypeName());
        return this;
    }
}
