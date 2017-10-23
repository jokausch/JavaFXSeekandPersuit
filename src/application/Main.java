package application;


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import java.util.ArrayList;


public class Main extends Application {

    static Canvas canvas = new Canvas();
    static float HEIGHT, WIDTH;
    static GraphicsContext g;
    static ArrayList<Boid> boidList = new ArrayList();
    static ArrayList<BoidHunter> boidHunterList = new ArrayList();
    static DropShadow ds;
    double frameRate;
    double old;
    static boolean debug=false;
    int cycleCount;
    double elapsedTime,lastNanoTime=System.nanoTime();

    
    /* (non-Javadoc)
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Pane centerPane = new Pane();
        centerPane.autosize();
        centerPane.setStyle("-fx-background-color: #ADD8E6;");
        centerPane.getChildren().add(canvas);
        root.setCenter(centerPane);

        canvas.widthProperty().bind(centerPane.widthProperty());
        canvas.heightProperty().bind(centerPane.heightProperty());

        Scene scene = new Scene(root, 1024, 768);
        //Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Seek and Persuit");
        //primaryStage.setFullScreen(true);
        primaryStage.setResizable(false);
        primaryStage.show();

        HEIGHT = (float) canvas.getHeight();
        WIDTH = (float) canvas.getWidth();



        ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setColor(Color.color(0.4f, 0.4f, 0.4f));

        g = canvas.getGraphicsContext2D();

        for (int i = 0; i < 250; i++) {

            boidList.add(new Boid(new PVector(Utils.randomFloatNumberInRange(Boid.distanceToBoarder,
                    Main.WIDTH), Utils.randomFloatNumberInRange(Boid.distanceToBoarder, Main.HEIGHT)),
                    new PVector(Utils.randomFloatNumberInRange(100, 300), Utils.randomFloatNumberInRange(100, 300))));
        }

        boidHunterList.add(new BoidHunter(new PVector(200,200),new PVector(200,200)));
        boidHunterList.add(new BoidHunter(new PVector(300,100),new PVector(-200,200)));


        canvas.setOnMouseDragged((MouseEvent event) -> {


        });

        canvas.setOnMouseClicked((MouseEvent event) -> {
        		if (debug) debug=false;
        		else debug=true;
        		boidList.stream().forEach(e-> e.col=Color.rgb(0, 0, 0, 1));

        });
        
     



        AnimationTimer animator = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {

                elapsedTime = (currentNanoTime - lastNanoTime) / 1000000000.0;
                lastNanoTime = currentNanoTime;

                cycleCount++;

                if (currentNanoTime - old >= 1000000000) {
                    frameRate = 1000000000 / ((currentNanoTime - old) / cycleCount);
                    cycleCount = 0;
                    old = System.nanoTime();
                }

                g.clearRect(0, 0, WIDTH, HEIGHT);

                boidHunterList.stream().forEach(e->{
                    e.updateAndDraw(elapsedTime);
                    if (debug) e.viewAndPersuit(boidList);
                    else e.persuit(boidList,0.3f);
                    e.boundaries(WIDTH,HEIGHT,5);
                    e.separate(boidHunterList, 2f);
                });

                boidList.stream().forEach(e-> e.draw());
                boidList.parallelStream().forEach(e -> {
                	if (e.isInSight==false) e.col=Color.rgb(0, 0, 0, 1);
                    e.update(elapsedTime);
                    e.boundaries(WIDTH, HEIGHT,5f);
                    e.separate(boidList, 1.9f);
                    e.fleeHunter(boidHunterList,4f);
                    e.align(boidList,0.06f);
                    e.cohesion(boidList,0.75f);
                    //e.flock(boidList, 1f);
                });

                boidList.removeIf(e-> e.isDead);

                g.setFill(Color.rgb(255, 0, 0, 1));
                g.setFont(new Font(14));
                g.fillText("Curent Framerate: " + Math.round(frameRate), 10, Main.HEIGHT - 10);
                g.fillText("Num Of Boids: " + boidList.size(), 200, Main.HEIGHT - 10);
                
            }
        };
        animator.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
