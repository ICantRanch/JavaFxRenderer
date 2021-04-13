package JavaRenderer;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

import java.util.*;

public class RenderMain extends Application {

    int CAN_WIDTH = 500;
    int CAN_HEIGHT = 500;
    double stageWidth;
    double stageHeight;
    Canvas c;
    GraphicsContext gc;
    Timer t;
    TimerTask tt;
    Slider lR;
    Slider uD;
    Group center;
    Circle ci;
    Polygon[] pArr;
    double[][] zBuffer;
    double fakeHeading = 0;
    double fakePitch = 0;
    boolean isTimerRun =false;
    Rotate hRotate = new Rotate(0, new javafx.geometry.Point3D(0,1,0));
    Rotate pRotate = new Rotate(0,new javafx.geometry.Point3D(1,0,0));

    ArrayList<Triangle> tris;
    WritableImage img;
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
        center = new Group();
        c = new Canvas(CAN_WIDTH, CAN_HEIGHT);
        gc = c.getGraphicsContext2D();
		root.setCenter(c);
		zBuffer = new double[(int) c.getHeight()][(int) c.getWidth()];
		
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

        pArr = new Polygon[tris.size()];
        for (int i = 0; i < pArr.length; i++) {
            pArr[i] = new Polygon(new double[] {
            		tris.get(i).getV1().getX(),tris.get(i).getV1().getY(),
            		tris.get(i).getV2().getX(),tris.get(i).getV2().getY(),
            		tris.get(i).getV3().getX(),tris.get(i).getV3().getY(),		
            });
        }
        ci = new Circle(0,0,2);
        center.getChildren().add(ci);

        //rotate();
        paint();
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

        hRotate.setAngle(hDegrees);
        pRotate.setAngle(pDegrees);
        //Transform trans = hRotate.createConcatenation(pRotate);
        double[][] newOrder = new double[tris.size()][2];
        double average;
        double sum;
        img = new WritableImage((int)c.getWidth(),(int)c.getHeight());
        PixelWriter pw = img.getPixelWriter();
        for (int i = 0; i < zBuffer.length; i++) {
			for (int j = 0; j < zBuffer[i].length; j++) {
				zBuffer[i][j] = Double.NEGATIVE_INFINITY;
			}
		}
        gc.clearRect(0, 0, c.getWidth(), c.getHeight());

        for (int i = 0; i < tris.size(); i++) {

            Point3D v1 = hRotate.transform(tris.get(i).getV1());
            v1 = pRotate.transform(v1);
            Point3D v2 = hRotate.transform(tris.get(i).getV2());
            v2 = pRotate.transform(v2);
            Point3D v3 = hRotate.transform(tris.get(i).getV3());
            v3 = pRotate.transform(v3);
            
            sum = v1.getZ() + v2.getZ() + v3.getZ();
            average = sum/3;
            newOrder[i] = new double[]{average, i};
            
            v1 = new Point3D(v1.getX() + c.getWidth() / 2, v1.getY() + c.getHeight() / 2, v1.getZ());
            v2 = new Point3D(v2.getX() + c.getWidth() / 2, v2.getY() + c.getHeight() / 2, v2.getZ());
            v3 = new Point3D(v3.getX() + c.getWidth() / 2, v3.getY() + c.getHeight() / 2, v3.getZ());
            
            Point3D v12 = v1.subtract(v2);
            Point3D v13 = v1.subtract(v3);
            Point3D norm = v12.crossProduct(v13);
            norm = norm.normalize();
            double shade = Math.abs(norm.getZ());
            
            
            
            int minX = (int)Math.max(0, Math.ceil(Math.min(v1.getX(), Math.min(v2.getX(), v3.getX()))));
            int maxX = (int)Math.min(c.getWidth()-1, Math.floor(Math.max(v1.getX(), Math.max(v2.getX(), v3.getX()))));
            int minY = (int)Math.max(0, Math.ceil(Math.min(v1.getY(), Math.min(v2.getY(), v3.getY()))));
            int maxY = (int)Math.min(c.getWidth()-1, Math.floor(Math.max(v1.getY(), Math.max(v2.getY(), v3.getY()))));
            
            Polygon p = pArr[i];
            
            p.getPoints().setAll(new Double[] {
            		v1.getX(),v1.getY(),
            		v2.getX(),v2.getY(),
            		v3.getX(),v3.getY(),
            });
            
            double triangleArea =
            	       (v1.getY() - v3.getY()) * (v2.getX() - v3.getX()) + (v2.getY() - v3.getY()) * (v3.getX() - v1.getX());
            
            for (int x = minX; x < maxX; x++) {
            	for (int y = minY; y < maxY; y++) {
            		double b1 = 
            				((y - v3.getY()) * (v2.getX() - v3.getX()) + (v2.getY() - v3.getY()) * (v3.getX() - x)) / triangleArea;
            		double b2 =
            				((y - v1.getY()) * (v3.getX() - v1.getX()) + (v3.getY() - v1.getY()) * (v1.getX() - x)) / triangleArea;
            		double b3 =
            				((y - v2.getY()) * (v1.getX() - v2.getX()) + (v1.getY() - v2.getY()) * (v2.getX() - x)) / triangleArea;
            		if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
            			
            			double depth = b1 * v1.getZ() + b2 * v2.getZ() + b3 * v3.getZ();
            			if(depth > zBuffer[y][x]) {
            				
            				zBuffer[y][x] = depth;
            				Color temp = tris.get(i).getColor();
            				
            				double redLin = Math.pow(temp.getRed(), 2.4) * shade;
            				double greenLin = Math.pow(temp.getGreen(), 2.4) * shade;
            				double blueLin = Math.pow(temp.getBlue(), 2.4) * shade;
            				
            				double red = Math.pow(redLin, 1/2.4);
            				double green = Math.pow(greenLin, 1/2.4);
            				double blue =  Math.pow(blueLin, 1/2.4);
            				
            				pw.setColor(x, y, new Color(red, green, blue, 1));
            			}	
            		}
            	}	
            }
        }
        gc.drawImage(img, 0, 0);
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        launch();

    }

}
