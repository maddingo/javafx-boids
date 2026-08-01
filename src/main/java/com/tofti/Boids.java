package com.tofti;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Boids extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    private static final int DEFAULT_WINDOW_WIDTH = 1024;
    private static final int DEFAULT_WINDOW_HEIGHT = 768;
    private static final double DEBUG_MARKER_SIZE = 5;

    static class Boid {
        public static final boolean COLOR_SENSITIVE_DEFAULT = false;
        Vector2D location;
        Vector2D velocity;

        double xBound;
        double yBound;

        double alignmentWeight;
        double separationWeight;
        double cohesionWeight;

        boolean colorSensitive;

        Sphere sphere;
        Polygon poly;
        Color color;

        static final double MAX_VELOCITY = 3;

        static final int LOC_Z = 0;
        static final int RADIUS = 5;
        static final double TRI_SIZE = 8d;

        static final double SEPARATION_DISTANCE = 10 * TRI_SIZE;
        static final double CENTER_OF_MASS_NEIGHBORHOOD = 25 * TRI_SIZE;
        static final double DEFAULT_WEIGHT = 0.5d;
        static final double MAX_WEIGHT = 3d;

        static Function<Boid, Vector2D> TO_LOCATION_VECTOR_2D = b -> b.location;
        static Function<Boid, Vector2D> TO_VELOCITY_VECTOR_2D = b -> b.velocity;

        static Function<List<Boid>, List<Vector2D>> TO_LOCATION_VECTOR_2D_LIST
                = lb -> lb.stream().map(TO_LOCATION_VECTOR_2D).collect(Collectors.toList());

        static Function<List<Boid>, List<Vector2D>> TO_VELOCITY_VECTOR_2D_LIST
                = lb -> lb.stream().map(TO_VELOCITY_VECTOR_2D).collect(Collectors.toList());

        static final Random RNG = new Random(System.currentTimeMillis());
        static final List<Color> COLORS = List.of(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);

        Boid(double locX, double locY, double xBound, double yBound) {
            this.location = new Vector2D(locX, locY);
            this.xBound = xBound;
            this.yBound = yBound;
            this.velocity = new Vector2D(RNG.nextDouble() * MAX_VELOCITY - 0.5 * MAX_VELOCITY,
                                         RNG.nextDouble() * MAX_VELOCITY- 0.5 * MAX_VELOCITY);

            this.setAlignmentWeight(DEFAULT_WEIGHT);
            this.setSeparationWeight(DEFAULT_WEIGHT);
            this.setCohesionWeight(DEFAULT_WEIGHT);
            this.setColorSensitive(COLOR_SENSITIVE_DEFAULT);

            this.sphere = new Sphere(RADIUS);
            this.poly = new Polygon();
            poly.getPoints().addAll(0.0, TRI_SIZE, TRI_SIZE, -TRI_SIZE, -TRI_SIZE, -TRI_SIZE);
            poly.setCache(true);
            poly.setCacheHint(CacheHint.SPEED);

            color = COLORS.get(RNG.nextInt(COLORS.size()));
            poly.setFill(color);
        }

        public void setAlignmentWeight(double alignmentWeight) {
            this.alignmentWeight = alignmentWeight;
        }

        public void setSeparationWeight(double separationWeight) {
            this.separationWeight = separationWeight;
        }

        public void setCohesionWeight(double cohesionWeight) {
            this.cohesionWeight = cohesionWeight;
        }

        public void setXBound(double xBound) {
            this.xBound = xBound;
        }

        public void setYBound(double yBound) {
            this.yBound = yBound;
        }

        @Override
        public String toString() {
            return String.format("location=[%s] velocity=[%s]" + System.lineSeparator(), location, velocity);
        }

        void updateAndRender(List<Boid> all) {
            update(all);
            render();
        }

        void update(List<Boid> all) {
            List<Boid> others = new ArrayList<>(all);
            others.remove(this);

            Optional<Vector2D> centreOfMassAdj = centreOfMassVelocity(others, cohesionWeight);
            centreOfMassAdj.ifPresent(vector2D -> velocity = velocity.plus(vector2D));

            Optional<Vector2D> avoidOthersAdj = avoidOthersAdj(others, separationWeight);
            avoidOthersAdj.ifPresent(vector2D -> velocity = velocity.plus(vector2D));

            Optional<Vector2D> alignmentAdj = alignmentAdj(others, alignmentWeight);
            alignmentAdj.ifPresent(vector2D -> velocity = velocity.plus(vector2D));

            if(RNG.nextDouble() < 0.01) {
                velocity = velocity.plus(new Vector2D(RNG.nextDouble() - 0.5, RNG.nextDouble() - 0.5).normalizeTo(0.5));
            }

            velocity = velocity.normalizeTo(MAX_VELOCITY);
            location = location.plus(velocity);
            location = location.wrapAround(xBound, yBound);
        }

        private Optional<Vector2D> alignmentAdj(List<Boid> boids, double magnitude) {
            if(boids.isEmpty()) {
                return Optional.empty();
            }

            List<Boid> othersWithinDistance
                    = boidsWithinDistance(boids, this, SEPARATION_DISTANCE);
            if(othersWithinDistance.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(
                    Vector2D.getArithmeticMean(TO_VELOCITY_VECTOR_2D_LIST.apply(othersWithinDistance)).normalizeTo(magnitude));
        }

        private Optional<Vector2D> avoidOthersAdj(List<Boid> boids, double magnitude) {
            if(boids.isEmpty()) {
                return Optional.empty();
            }
            List<Boid> othersWithinDistance
                    = boidsWithinDistance(boids, this, SEPARATION_DISTANCE);

            if(othersWithinDistance.isEmpty()) {
                return Optional.empty();
            }

            Vector2D thisVec = TO_LOCATION_VECTOR_2D.apply(this);

            Vector2D adj = new Vector2D(0d, 0d);
            for(Vector2D other : TO_LOCATION_VECTOR_2D_LIST.apply(othersWithinDistance)) {
                adj = adj.plus(thisVec.minus(other));
            }
            return Optional.of(adj.normalizeTo(magnitude));
        }

        private Optional<Vector2D> centreOfMassVelocity(List<Boid> all, double magnitude) {
            Optional<Vector2D> center = getCentreOfMass(all, this);
            if(center.isEmpty()) {
                return center;
            }
            Vector2D v = center.get().minus(location);
            return Optional.of(v.normalizeTo(magnitude));
        }

        static List<Boid> boidsWithinDistance(List<Boid> others, Boid thiz, double distance) {
            List<Vector2D> othersLocations = TO_LOCATION_VECTOR_2D_LIST.apply(others);
            return others.stream()
                          .filter(o -> !thiz.colorSensitive || (thiz.color == o.color))
                          .filter(o -> Vector2D.getDistanceBetween(TO_LOCATION_VECTOR_2D.apply(o),
                                                                  TO_LOCATION_VECTOR_2D.apply(thiz)) < distance)

                         .collect(Collectors.toList());
        }

        static Optional<Vector2D> getCentreOfMass(List<Boid> boids, Boid forBoid) {
            if(boids.isEmpty()) {
                return Optional.empty();
            }

            List<Boid> othersWithinDistance
                    = boidsWithinDistance(boids, forBoid, CENTER_OF_MASS_NEIGHBORHOOD);

            if(othersWithinDistance.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(
                    Vector2D.getArithmeticMean(TO_LOCATION_VECTOR_2D_LIST.apply(othersWithinDistance)));
        }

        void render() {
            sphere.setTranslateX(location.getX());
            sphere.setTranslateY(location.getY());
            sphere.setTranslateZ(-LOC_Z);

            poly.setTranslateX(location.getX());
            poly.setTranslateY(location.getY());
            double r = -90 + Math.toDegrees(Math.atan(velocity.getY() / velocity.getX()));
            r = velocity.getX() < 0.0d ? r -180d : r;
            poly.setRotate(r);
        }

        List<Node> getNodes() {
            return Collections.singletonList(poly);
        }

        public void setColorSensitive(Boolean colorSensitive) {
            this.colorSensitive = colorSensitive;
        }
    }

    static Rectangle addDebugMarkers(double x, double y, double xSize, double ySize) {
        Rectangle tl = new Rectangle(x, y, xSize, ySize);
        tl.setFill(Color.RED);
        return tl;
    }

    static  void reinitDebugMarkersInCorner( List<Rectangle> nodes, double xBound, double yBound) {
        nodes.clear();
        nodes.add(addDebugMarkers( 0, 0, DEBUG_MARKER_SIZE , DEBUG_MARKER_SIZE));
        nodes.add(addDebugMarkers( xBound-DEBUG_MARKER_SIZE, 0, DEBUG_MARKER_SIZE , DEBUG_MARKER_SIZE));
        nodes.add(addDebugMarkers( xBound-DEBUG_MARKER_SIZE, yBound-DEBUG_MARKER_SIZE, DEBUG_MARKER_SIZE , DEBUG_MARKER_SIZE));
        nodes.add(addDebugMarkers( 0, yBound-DEBUG_MARKER_SIZE, DEBUG_MARKER_SIZE , DEBUG_MARKER_SIZE));
    }

    List<Boid> initRandomBoids(final int n) {
        List<Boid> boids = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0 ; i < n ; i++) {
            boids.add(new Boid(rand.nextDouble() * DEFAULT_WINDOW_WIDTH,
                               rand.nextDouble() * DEFAULT_WINDOW_HEIGHT,
                               DEFAULT_WINDOW_WIDTH,
                               DEFAULT_WINDOW_HEIGHT ));
        }
        return boids;
    }

    void reinit(List<Boid> boids, Group root, List<Slider> sliders) {
        root.getChildren().removeAll(boids.stream().map(Boid::getNodes).flatMap(Collection::stream).toList());
        boids.clear();
        boids.addAll(initRandomBoids(1));
        root.getChildren().addAll(boids.stream().map(Boid::getNodes).flatMap(Collection::stream).toList());
        sliders.forEach(s -> s.valueProperty().set(Boid.DEFAULT_WEIGHT));
    }

    @Override
    public void start(Stage stage) {
        PerspectiveCamera camera = new PerspectiveCamera(false);

        Group root = new Group();
        boolean USE_DEPTH_BUFFER = true;
        Scene scene = new Scene(root, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT, USE_DEPTH_BUFFER);
        final ContextMenu contextMenu = new ContextMenu();
        final List<Boid> boids = new ArrayList<>();

        Slider alignmentSlider = new Slider();
        List<CustomMenuItem> alignmentControls = buildLabelAndSlider(alignmentSlider, "Alignment: %.3f", 0.1,
                Boid.MAX_WEIGHT, (ov, old_val, new_val) -> boids.forEach(b -> b.setAlignmentWeight(new_val.doubleValue())));

        Slider cohesionSlider = new Slider();
        List<CustomMenuItem> cohesionControls = buildLabelAndSlider(cohesionSlider, "Cohesion: %.3f", 0.1, Boid.MAX_WEIGHT,
                (ov, old_val, new_val) -> boids.forEach(b -> b.setCohesionWeight(new_val.doubleValue())));

        Slider seperationSlider = new Slider();
        List<CustomMenuItem> separationControls = buildLabelAndSlider(seperationSlider, "Seperation: %.3f", 0.1, Boid.MAX_WEIGHT,
                (ov, old_val, new_val) -> boids.forEach(b -> b.setSeparationWeight(new_val.doubleValue())));

        contextMenu.getItems().addAll(alignmentControls);
        contextMenu.getItems().addAll(cohesionControls);
        contextMenu.getItems().addAll(separationControls);

        final List<Slider> allSliders = Arrays.asList(alignmentSlider, cohesionSlider, seperationSlider);

        ToggleButton colorSensitive = new ToggleButton();
        colorSensitive.setSelected(Boid.COLOR_SENSITIVE_DEFAULT);
        colorSensitive.setText("Color Sensitive");
        colorSensitive.selectedProperty().addListener((observable, oldValue, newValue) -> boids.forEach(b->b.setColorSensitive(newValue)));
        contextMenu.getItems().add(new CustomMenuItem(colorSensitive));

        Button reset = new Button();
        reset.setText("Reset");
        reset.setOnAction(e -> reinit(boids, root, allSliders));
        contextMenu.getItems().add(new CustomMenuItem(reset));

        final Label fpsLabel = new Label();
        contextMenu.getItems().add(new CustomMenuItem(fpsLabel));

        reinit(boids, root, allSliders);

        scene.setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(stage, e.getScreenX(), e.getScreenY());
            }
            if(e.getButton() == MouseButton.PRIMARY) {
                Boid newBoid = new Boid(e.getSceneX(), e.getSceneY(), scene.getWidth(), scene.getHeight());
                boids.add(newBoid);
                root.getChildren().addAll(newBoid.getNodes());
            }
        });

        final List<Rectangle> debugNodes = new ArrayList<>();
        reinitDebugMarkersInCorner(debugNodes, scene.getWidth(), scene.getHeight());
        root.getChildren().addAll(debugNodes);

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            boids.forEach(b -> b.setYBound(newVal.doubleValue()));
            root.getChildren().removeAll(debugNodes);
            reinitDebugMarkersInCorner(debugNodes, scene.getWidth(), scene.getHeight());
            root.getChildren().addAll(debugNodes);

        });

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            boids.forEach(b -> b.setXBound(newVal.doubleValue()));
            root.getChildren().removeAll(debugNodes);
            reinitDebugMarkersInCorner(debugNodes, scene.getWidth(), scene.getHeight());
            root.getChildren().addAll(debugNodes);
        });

        // Add the Scene to the Stage
        scene.setCamera(camera);
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        stage.setTitle("JavaFX Boids");

        AnimationTimer t = new AnimationTimer() {
            private final long[] frameTimes = new long[100];
            private int frameIndex = 0;
            private boolean wrapped = false;

            @Override public void handle(long now) {
                boids.forEach(b -> b.updateAndRender(boids));

                long oldest = wrapped ? frameTimes[frameIndex] : frameTimes[0];
                int intervals = wrapped ? frameTimes.length : frameIndex;

                frameTimes[frameIndex] = now;
                frameIndex = (frameIndex + 1) % frameTimes.length;
                wrapped |= frameIndex == 0;

                if (intervals > 0) {
                    fpsLabel.setText(String.format("FPS: %.3f", intervals * 1_000_000_000.0 / (now - oldest)));
                }
            }
        };
        t.start();
        stage.show();
    }

    static List<CustomMenuItem> buildLabelAndSlider(Slider slider, final String labelText, double blockIncrement, double max, ChangeListener<Number> changeListener) {
        slider.setBlockIncrement(blockIncrement);
        slider.setMax(max);
        Label label = new Label();
        label.setText(String.format(labelText, slider.valueProperty().get()));

        CustomMenuItem sliderLabelMenuItem = new CustomMenuItem(label);
        CustomMenuItem sliderMenuItem = new CustomMenuItem(slider);

        slider.valueProperty().addListener(changeListener);
        slider.valueProperty().addListener((ov, old_val, new_val) -> {
            label.setText(String.format(labelText, new_val));
        });
        return Arrays.asList(sliderLabelMenuItem, sliderMenuItem);
    }
}
