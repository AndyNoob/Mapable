# Mapable - Everything can be a MAP!
[![](https://jitpack.io/v/AndyNoob/Mapable.svg)](https://jitpack.io/#AndyNoob/Mapable)
## How it worky
### `Mapable#asMap`
1. Receives input object `toMap`, and `@Nullable clazz` (the class of the object is inferred)
2. Collects all fields (including superclasses) via Reflection
   1. Checks if a `IResolver` is registered for this type
      1. **Yes**
         1. Calls [`IResolver#resolve`](src/main/java/me/comfortable_andy/mapable/resolvers/IResolver.java)
      2. **No**
         1. Calls [`Mapable#asMap`](#mapableasmap);
3. Done!