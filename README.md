# Mapable - Everything can be a MAP!
## How it worky
### `Mapable#asMap`
1. Receives input object `toMap`, and `@Nullable clazz` (the class of the object is inferred)
2. Collects all fields (including superclasses) via Reflection
   1. Checks if a `IResolver` is registered for this type
      1. **Yes**
         1. Calls [`IResolver#resolve`](https://github.com/AndyNoob/Mapable/blob/710e3afd64374a0f444c7c0946546c157b0d7c53/src/main/java/me/comfortable_andy/mapable/resolvers/IResolver.java#L17)
      2. **No**
         1. Calls [`Mapable#asMap`](#mapableasmap);
3. Done!