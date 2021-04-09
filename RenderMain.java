package JavaRenderer;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

import java.util.*;

public class RenderMain extends Application {

    int WIN_WIDTH = 500;
    int WIN_HEIGHT = 500;
    Canvas c;
    GraphicsContext gc;
    Timer t;
    TimerTask tt;
    Slider lR;
    Slider uD;
    Group center;
    Circle ci;
    Path[] pArr;
    double fakeHeading = 0;
    double fakePitch = 0;
    boolean isTimerRun =false;
    Rotate hRotate = new Rotate(0, new javafx.geometry.Point3D(0,1,0));
    Rotate pRotate = new Rotate(0,new javafx.geometry.Point3D(1,0,0));

    ArrayList<Triangle> tris;
    Image img;

    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
        center = new Group();
        root.setCenter(center);
        c = new Canvas(WIN_WIDTH, WIN_HEIGHT);

        lR = new Slider(-180,180,0);
        uD = new Slider(-90,90,0);
        uD.setOrientation(Orientation.VERTICAL);
        root.setBottom(lR);
        root.setRight(uD);

        lR.valueProperty().addListener((observableValue, oldNum, newNum) -> paint());
        uD.valueProperty().addListener((observableValue, number, t1) -> paint());



        tris = new ArrayList<Triangle>();
        tris.add(new Triangle(new Point3D(100, 100, 100),
                new Point3D(-100, -100, 100),
                new Point3D(-100, 100, -100),
                Color.WHITE));
        tris.add(new Triangle(new Point3D(100, 100, 100),
                new Point3D(-100, -100, 100),
                new Point3D(100, -100, -100),
                Color.RED));
        tris.add(new Triangle(new Point3D(-100, 100, -100),
                new Point3D(100, -100, -100),
                new Point3D(100, 100, 100),
                Color.LIMEGREEN));
        tris.add(new Triangle(new Point3D(-100, 100, -100),
                new Point3D(100, -100, -100),
                new Point3D(-100, -100, 100),
                Color.MEDIUMBLUE));

        pArr = new Path[tris.size()];
        for (int i = 0; i < pArr.length; i++) {
            pArr[i] = new Path();

            pArr[i].getElements().add(new MoveTo(tris.get(i).getV1().getX(),tris.get(i).getV1().getY()));
            pArr[i].getElements().add(new LineTo(tris.get(i).getV2().getX(),tris.get(i).getV2().getY()));
            pArr[i].getElements().add(new LineTo(tris.get(i).getV3().getX(),tris.get(i).getV3().getY()));
            pArr[i].getElements().add(new ClosePath());
            pArr[i].setFill(tris.get(i).getColor());

            center.getChildren().add(pArr[i]);
        }
        ci = new Circle(0,0,2);
        center.getChildren().add(ci);

        System.out.println(hRotate.getAxis());

        //rotate();
        paint();

        stage.setHeight(WIN_HEIGHT);
        stage.setWidth(WIN_WIDTH);
        stage.setTitle("Renderer");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(event ->{
            if(isTimerRun){
                t.cancel();
            }
        });
    }

    public void rotate() {

        tt = new TimerTask() {

            @Override
            public void run() {

                isTimerRun = true;
                paint(fakeHeading,fakePitch);
                fakeHeading += 1.5;
                fakePitch += 1.5;

            }
        };
        t = new Timer();
        t.schedule(tt, 0, 34);
    }

    public void paint() {

        double heading = lR.getValue();
        double pitch = uD.getValue();
        paint(heading,pitch);
    }

    public void paint(double hDegrees, double pDegrees) {

        hRotate.setAngle(-hDegrees);
        pRotate.setAngle(-pDegrees);
        Transform trans = hRotate.createConcatenation(pRotate);
        double[][] newOrder = new double[tris.size()][2];
        double average;
        double sum;

        for (int i = 0; i < tris.size(); i++) {


            Point3D v1 = trans.transform(tris.get(i).getV1());
            Point3D v2 = trans.transform(tris.get(i).getV2());
            Point3D v3 = trans.transform(tris.get(i).getV3());

            sum = v1.getZ() + v2.getZ() + v3.getZ();
            average = sum/3;
            newOrder[i] = new double[]{average, i};

            Path p = pArr[i];
            p.getElements().clear();

            p.getElements().add(new MoveTo(v1.getX(),v1.getY()));
            p.getElements().add(new LineTo(v2.getX(),v2.getY()));
            p.getElements().add(new LineTo(v3.getX(),v3.getY()));
            p.getElements().add(new ClosePath());
            //p.getTransforms().addAll(hRotate,pRotate);

            System.out.println(v1);
        }
        System.out.println(Arrays.deepToString(newOrder));
        Arrays.sort(newOrder, Comparator.comparingDouble(o -> o[0]));
        System.out.println(Arrays.deepToString(newOrder));
        for (int i = 0; i < newOrder.length; i++) {
            pArr[(int)newOrder[i][1]].toFront();
        }
        ci.toFront();
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        launch();

    }

}
