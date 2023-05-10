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
## Contributing
Thank you for risking your personal sanity by considering contributions to this project!

Here's some quick steps to get you started:
1. **Fork** this repo and create a **new** branch that describes the issue/goal of your PR.
2. **Clone** this repo locally to your machine.
3. ... and you're pretty much set to start your changes!

But while making changes, please keep in mind to:
1. Keep the code **styled** in a similar manner to existing code.
2. Put an adequate amount of **comments** to explain your changes.
3. Follow the same **naming conventions** as existing code.
4. **Make tests** to cover of your changes.

And you're pretty much set to get to the commit stage, but please also keep in mind to:
1. Write clear and concise commit messages.
2. Make sure your PR is descriptive of what your changes are.

Thank you once again you risking your sanity!
