# Mapable - Everything can be a MAP!
[![](https://jitpack.io/v/AndyNoob/Mapable.svg)](https://jitpack.io/#AndyNoob/Mapable)
## Why? 🤔
Why did I make this? TBH, I don't even know anymore. But it was probably because I wanted to for some reason handle my own serializing as opposed to just using GSON.
## How it worky
The `Mapable` class has two main methods, `asMap` and `fromMap`. If we're talking mathematics, these two methods would be inverses of each other. Each can take the output of the other, outputting the original input. This means you could, if you don't care about your stuff breaking, use this like any other serializing library (of course needing an extra step of handling saving the maps).

Behind the back, all fields in the inputted object are ran through what I call resolvers, which is just a modular way to be able to handle different immediately resolvable (e.g. primitives, arrays, lists, enums) types (this also means that you can make a resolver for any class you want).
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
