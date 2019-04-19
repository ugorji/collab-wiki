# collab-wiki

This is an extremely extensible wiki product.

See the embedded help information for further details:
`src/main/webapp/WEB-INF/oxywiki/pages/help/`

## Dependencies

This is typically used within a multi-project `gradle` build.
However, this package doesn't depend on any others.

It is used by my java-markup, java-facade and java-web packages, 
and whatever other modules depend on them.

## Building

```sh
gradle clean build
```

# Running

```sh
gradle appRunWar
```

