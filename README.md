# Jakarta EE Faces Sample

[![build](https://github.com/hantsy/jakartaee-faces-sample/actions/workflows/maven.yml/badge.svg)](https://github.com/hantsy/jakartaee-faces-sample/actions/workflows/maven.yml)

This sample application demonstrates Jakarta Faces using the latest Jakarta EE stack for traditional web development.

The codebase has been upgraded to **Jakarta EE 11**.

### Older releases

* **Jakarta EE 10 with GlassFish v7.1 and Java 21:** see the [archive release](https://github.com/hantsy/jakartaee-faces-sample/releases/tag/ee10-gf7-java21).
* **Jakarta EE 10:** see the [archive release](https://github.com/hantsy/jakartaee-faces-sample/releases/tag/ee10).
* **Jakarta EE 8:** see the [archive release](https://github.com/hantsy/jakartaee-faces-sample/releases/tag/v1.0).
* **Legacy Java EE 8 sources:** browse the [javaee8-jsf-sample](https://github.com/hantsy/javaee8-jsf-sample) repository.

![home](./home.png)

## Documentation

[Building a Jakarta Server Faces application](./docs/guide.md)

## Building and running

1. Clone the repository:

   ```bash
   git clone https://github.com/hantsy/jakartaee-faces-sample
   ```

2. Start the app on GlassFish:

   ```bash
   mvn clean package cargo:run -pglassfish
   ```

3. Execute the tests using the GlassFish managed adapter:

   ```bash
   mvn clean verify -Parg-glassfish-managed
   ```

> [!WARNING]
> To lower maintenance overhead, support for WildFly, OpenLiberty, Payara, etc. has been removed. If you require those servers, consult the [jakartaee9‑starter‑boilerplate](https://github.com/hantsy/jakartaee9-starter-boilerplate) or [jakartaee10‑starter‑boilerplate](https://github.com/hantsy/jakartaee10-starter-boilerplate) projects and add the necessary configuration yourself.

## Reference

* [Testing HTML and JSF-Based UIs with Arquillian](https://blogs.oracle.com/javamagazine/testing-html-and-jsf-based-uis-with-arquillian)
* [Functional Testing using Drone and Graphene](http://arquillian.org/guides/functional_testing_using_graphene/)
* [Arquillian Drone Reference Documentation](http://arquillian.org/arquillian-extension-drone)
* [Arquillian Graphene Reference Documentation](http://arquillian.org/arquillian-graphene)
