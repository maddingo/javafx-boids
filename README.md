# javafx-boids - a JavaFX implementation of the Artificial Life flocking algorithm "Boids"

![javafx-boids](https://github.com/tofti/javafx-boids/blob/master/examples/ExampleAnimated.gif "javafx-boids")


### Background
Boids is an artificial life program, which simulates the flocking behaviour of birds. https://en.wikipedia.org/wiki/Boids. It was developed by Craig Reynolds in 1986, and was published in 1987 in the proceedings of the ACM SIGGRAPH conference. http://dl.acm.org/citation.cfm?id=37406

### Using javafx-boids

javafx-boids is a plain old java application, requiring a Java 25 runtime and a mouse. The boids world is initialized with a single boid. Left mouse click adds a boid to the world, and boid parameters can be controlled by right clicking and using the sliders/buttons on the context menu. The sliders affect the precedence of the three velocity vectors used to control the boids motion. The "Color Sensitive" toggle restricts each boid to flocking only with boids of its own colour. A reset function on the menu resets the sliders, and removes all the boids bar one. The context menu also shows the current frame rate.

### Building
Build with a JDK 25 that does **not** bundle JavaFX (e.g. Temurin, or the stock OpenJDK build) — JavaFX is pulled from Maven Central instead:

```
mvn clean package
```

Building with a JavaFX-bundled JDK (Azul Zulu FX, Liberica Full, etc.) fails the jlink step with a module hash mismatch, because the JavaFX modules baked into that JDK's `java.base` conflict with the ones resolved from Maven.

### Running
The easiest way to run from a checkout:

```
mvn javafx:run
```

`mvn package` also produces a self-contained runtime image via jlink, in `target/image`. It bundles the JavaFX modules and a stripped-down JRE, so it needs no Java installation to run. Start it with the generated launcher script:

```
target/image/bin/boids
```

The image is relocatable — copy the whole `target/image` directory anywhere and run `bin/boids` from there.

### Implementation
This implementation uses the JavaFX API to render the boids (see https://openjfx.io/). JavaFX shipped with the JDK up to Java SE 10, but has been a separate distribution since Java 11, so it is declared here as an ordinary Maven dependency on `org.openjfx:javafx-controls` and wired up with the `javafx-maven-plugin`. The application is a JPMS module (`src/main/java/module-info.java`), which is what allows jlink to produce the runtime image. The implementation also includes a basic 2D vector class, and corresponding unit tests.

### TODO
- Add obstacles, and obstacle avoidance.
- Add separation of model, and view/controller.
- Add rendering of neighbourhoods with transparency (so you can see the sphere of influence).
