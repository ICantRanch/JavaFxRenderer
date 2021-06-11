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
import javafx.stage.Stage;

import java.util.*;

public class RenderMain extends Application {

    int CAN_WIDTH = 500;
    int CAN_HEIGHT = 500;
    double stageWidth;
    double stageHeight;
    Canvas c;
    GraphicsContext gc;
    Timer t, tm;
    TimerTask tt, ttm;
    Slider lR;
    Slider uD;
    Group center;
    double[][] zBuffer;
    double fakeHeading = 0;
    double fakePitch = 0;
    double hDeg, pDeg;
    boolean isTimerRun = false;
    boolean momTimer = false;
    Rotate hRotate = new Rotate(0, new javafx.geometry.Point3D(0,1,0));
    Rotate pRotate = new Rotate(0,new javafx.geometry.Point3D(1,0,0));

    ArrayList<Triangle> tris;
    WritableImage img;
    
    double mousePrevX, mousePrevY;
    double momX, momY;
    
    
    
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
        center = new Group();
        c = new Canvas(CAN_WIDTH, CAN_HEIGHT);
        gc = c.getGraphicsContext2D();
		root.setCenter(c);
		zBuffer = new double[(int) c.getHeight()][(int) c.getWidth()];
		
		hDeg = pDeg = 0;
		
        lR = new Slider(-180,180,0);
        uD = new Slider(-180,180,0);
        uD.setOrientation(Orientation.VERTICAL);
        root.setBottom(lR);
        root.setRight(uD);

        
        //Slider Listeners
        lR.valueProperty().addListener((observableValue, oldNum, newNum) -> {

        	if(lR.getValue() != hDeg) {
        		paint();
        	}
        });
        uD.valueProperty().addListener((observableValue, number, t1) -> {

        	if(uD.getValue() != pDeg) {
        		paint();
        	}
        });
        
        
        
        //Drag Listeners
        c.setOnMousePressed(m->{
        	
        	mousePrevX = m.getSceneX();
        	mousePrevY = m.getSceneY();
        	if(momTimer) {
        		tm.cancel();
        	}
        });
        c.setOnMouseDragged(m-> {
        	
        	momX = (m.getSceneX()-mousePrevX)*(360/scene.getWidth());
        	momY = -(m.getSceneY()-mousePrevY)*(360/scene.getHeight());
        	paintInc(momX , momY);
        	mousePrevX = m.getSceneX();
        	mousePrevY = m.getSceneY();
        });
        c.setOnMouseReleased(m->{
        	
        	System.out.println("Exited");
        	rotateMom(0.96);
        });
        

        //Reference Points for Initial Triangle
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
        
        //Inflation
        for (int i = 0; i < 0; i++) {
			tris = (ArrayList<Triangle>) inflate(tris);
		}
        
        paint();
        
        stage.setTitle("Renderer");
        stage.setScene(scene);
        stage.show();

        //rotate(1.5, 0.75);
        
        stage.setOnCloseRequest(event ->{
        	if(isTimerRun){
        		t.cancel();
        	}
        	if(momTimer) {
        		tm.cancel();
        	}
        });
    }

    public void rotate(double x, double y) {

        tt = new TimerTask() {
            @Override
            public void run() {

                isTimerRun = true;
                paintInc(x, y);
            }
        };
        t = new Timer();
        t.schedule(tt, 0, 34);
    }
    
    public void rotateMom(double dec) {
    	
    	ttm = new TimerTask() {
    		
    		public void run() {
    			
    			momTimer = true;
    			paintInc(momX, momY);
    			momX *= dec;
    			momY *= dec;
    			
    			if(Math.abs(momX) <= 0.2 && Math.abs(momY) <= 0.2) {
    				tm.cancel();
    			}
    		}
    	};
    	tm = new Timer();
    	tm.schedule(ttm, 0, 34);
    }
    
    public static List<Triangle> inflate(List<Triangle> tris){
		
    	List<Triangle> result = new ArrayList<>();
    	
    	for (Triangle tri : tris) {
    		Point3D m1 =
    				new Point3D((tri.v1.getX() + tri.v2.getX())/2, (tri.v1.getY() + tri.v2.getY())/2, (tri.v1.getZ() + tri.v2.getZ())/2);
    		Point3D m2 =
    				new Point3D((tri.v2.getX() + tri.v3.getX())/2, (tri.v2.getY() + tri.v3.getY())/2, (tri.v2.getZ() + tri.v3.getZ())/2);
    		Point3D m3 =
    				new Point3D((tri.v1.getX() + tri.v3.getX())/2, (tri.v1.getY() + tri.v3.getY())/2, (tri.v1.getZ() + tri.v3.getZ())/2);
    		
    		result.add(new Triangle(tri.v1, m1, m3, tri.color));
    		result.add(new Triangle(tri.v2, m1, m2, tri.color));
    		result.add(new Triangle(tri.v3, m2, m3, tri.color));
    		result.add(new Triangle(m1, m2, m3, tri.color));
		}

    	for (Triangle t : result) {
                double b = Math.sqrt(t.v1.getX() * t.v1.getX() + t.v1.getY() * t.v1.getY() + t.v1.getZ() * t.v1.getZ()) / Math.sqrt(30000);
                t.setV1(new Point3D(t.v1.getX()/b, t.v1.getY()/b, t.v1.getZ()/b));
                
                b = Math.sqrt(t.v2.getX() * t.v2.getX() + t.v2.getY() * t.v2.getY() + t.v2.getZ() * t.v2.getZ()) / Math.sqrt(30000);
                t.setV2(new Point3D(t.v2.getX()/b, t.v2.getY()/b, t.v2.getZ()/b));
                
                b = Math.sqrt(t.v3.getX() * t.v3.getX() + t.v3.getY() * t.v3.getY() + t.v3.getZ() * t.v3.getZ()) / Math.sqrt(30000);
                t.setV3(new Point3D(t.v3.getX()/b, t.v3.getY()/b, t.v3.getZ()/b));
        }
    	return result;
    }
    

    public void paint() {

    	//Stop any momentum when adjusting rotation with sliders
    	if(momTimer) {
    		tm.cancel();
    	}	
        double heading = lR.getValue();
        double pitch = uD.getValue();
        paint(heading,pitch);
    }
    
    //Adjust rotation incrementally
    public void paintInc(double hDegrees, double pDegrees) {
    	
    	paint(hDeg + hDegrees, pDeg + pDegrees);
    }

    //Paint scene with rasterization
    public void paint(double hDegrees, double pDegrees) {

    	hDeg = hDegrees;
    	if(hDeg > 180) {hDeg -= 360;}
    	else {
    		if(hDeg < -180) {hDeg += 360;}
    	}
    	pDeg = pDegrees;
    	if(pDeg > 180) {pDeg -= 360;}
    	else {
    		if(pDeg < -180) {pDeg += 360;}
    	}
    	
    	lR.setValue(hDeg);
    	uD.setValue(pDeg);
    	hRotate.setAngle(hDeg);
    	pRotate.setAngle(pDeg);

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
