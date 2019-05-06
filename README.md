# collab-wiki

This is an extremely extensible wiki product.

See the embedded help information for further details:
`src/main/webapp/WEB-INF/oxywiki/pages/help/`

## Dependencies

This repository is part of a multi-project `gradle` build.

It has the following dependencies:

- [java-common](https://github.com/ugorji/java-common)
- [java-markup](https://github.com/ugorji/java-markup)
- [java-web](https://github.com/ugorji/java-web)
- [web-common](https://github.com/ugorji/web-common)

Before building:

- clone the dependencies into adjacent folders directly under same parent folder
- download [`settings.gradle`](https://gist.githubusercontent.com/ugorji/2a338462e63680d117016793989847fa/raw/settings.gradle) into the parent folder

## Building

```sh
gradle clean
gradle build
```

# Running

```sh
gradle appRunWar
```

