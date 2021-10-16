# CHANGELOG

### v0.2.2

- Fixed merge issue in `v0.2.1` that overwrite updated dependencies to `akka` version `2.6.17`.

### v0.2.1

- Updated dependencies to `akka` version `2.6.17`.

## v0.2

- Shorthand for `fetch` in the `Ahql` package.
- Shorthand for instantiating `AhqlClient` in the `Ahql` package.

## v0.1

- Minimal `Ahql` functions to handle GraphQL request for `akka-http` and `spray-json`.
- GraphQL spec compliant `AhqlServer` class for handling GraphQL over HTTP for `akka-http` and `spray-json`.
- GraphQL spec compliant `AhqlClient` class for handling GraphQL over HTTP for `akka-http` and `spray-json`.
- Custom pattern matching for `GqlResponse` to easily parse GraphQL responses.
