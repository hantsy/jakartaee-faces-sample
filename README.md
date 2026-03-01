#  Jakarta EE Faces Sample

![Compile and build](https://github.com/hantsy/jakartaee-faces-sample/workflows/build/badge.svg)

A Jakarta Faces example application demonstrates the latest Jakarta EE tech stack for classic web application development.

![home](./home.png)

## Documentation

[Building a Jatarka Server Faces application](/docs/guide.md)

## Build

1. Clone a copy of the source code.

   ```bash
   git clone https://github.com/hantsy/jakartaee-faces-sample
   ```

2. Run on Glassfish.

   ```bash
   mvn clean package cargo:run -pglassfish
   ```

   Run the test on the GlassFish managed adapter.

   ```bash
   mvn clean verify -Parg-glassfish-managed
   ```

 > [!WARNING]
 > To reduce maintenance costs, I have removed the configuration for running the applications on WildFly, OpenLiberty, Payara, etc. If you need to run it on these application servers, please refer to [jakartaee9-starter-boilerplate](https://github.com/hantsy/jakartaee9-starter-boilerplate) or  [jakartaee10-starter-boilerplate](https://github.com/hantsy/jakartaee10-starter-boilerplate) and add the configuration yourself.
   
## Reference

* [Testing HTML and JSF-Based UIs with Arquillian](https://blogs.oracle.com/javamagazine/testing-html-and-jsf-based-uis-with-arquillian)
* [Functional Testing using Drone and Graphene](http://arquillian.org/guides/functional_testing_using_graphene/)
* [Arquillian Drone Reference Documentation](http://arquillian.org/arquillian-extension-drone)
* [Arquillian Graphene  Reference Documentation](http://arquillian.org/arquillian-graphene)
