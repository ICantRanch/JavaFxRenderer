package JavaRenderer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;

public class RenderMain extends Application {

	int WIN_WIDTH = 400;
	int WIN_HEIGHT = 400;
	Canvas c;
	GraphicsContext gc;
	Timer t;
	TimerTask tt;
	Slider lR;
	Group center;
	Path[] pArr;
	
	ArrayList<Triangle> tris;
	
	public void start(Stage stage) {
		
		BorderPane root = new BorderPane();
		Scene scene = new Scene(root);
		c = new Canvas(WIN_WIDTH,WIN_HEIGHT);
		gc = c.getGraphicsContext2D();
		center = new Group();
		root.setCenter(center);
		
		lR = new Slider(0,360,180);
		root.setBottom(lR);
		lR.valueProperty().addListener(new ChangeListener<Number>() {

			public void changed(
					ObservableValue<? extends Number> observableValue,
					Number oldNum,
					Number newNum) {
				paint();
			}
		});
		
		gc.translate(WIN_WIDTH/2, WIN_HEIGHT/2);
		
		tris = new ArrayList<Triangle>();
		tris.add(new Triangle(new Vertex(100, 100, 100),
		                      new Vertex(-100, -100, 100),
		                      new Vertex(-100, 100, -100),
		                      Color.WHITE));
		tris.add(new Triangle(new Vertex(100, 100, 100),
		                      new Vertex(-100, -100, 100),
		                      new Vertex(100, -100, -100),
		                      Color.RED));
		tris.add(new Triangle(new Vertex(-100, 100, -100),
		                      new Vertex(100, -100, -100),
		                      new Vertex(100, 100, 100),
		                      Color.GREEN));
		tris.add(new Triangle(new Vertex(-100, 100, -100),
		                      new Vertex(100, -100, -100),
		                      new Vertex(-100, -100, 100),
		                      Color.BLUE));
		
		pArr = new Path[tris.size()];
		for (int i = 0; i < pArr.length; i++) {
			pArr[i] = new Path();
		}
		
		
		paint();
		
		
		
		stage.setHeight(WIN_HEIGHT);
		stage.setWidth(WIN_WIDTH);
		stage.setTitle("Renderer");
		stage.setScene(scene);
		stage.show();
		
		
	}
	
	public void paint() {
		
		gc.setFill(Color.WHITE);
		
		
		double heading = Math.toRadians(lR.getValue());
		Matrix3 transMat = new Matrix3(new double[][] {
			{Math.cos(heading),0,-Math.sin(heading)},
			{0,1,0},
			{Math.sin(heading),0,Math.cos(heading)}
			});
		
		
		for (int i = 0; i < tris.size(); i++) {
			
			Vertex v1 = transMat.transform(tris.get(i).getV1());
			Vertex v2 = transMat.transform(tris.get(i).getV2());
			Vertex v3 = transMat.transform(tris.get(i).getV3());
			
			Path p = pArr[i];
			p.getElements().clear();
			p.setFill(tris.get(i).getColor());
			p.getElements().add(new MoveTo(v1.getX(),v1.getY()));
			p.getElements().add(new LineTo(v2.getX(),v2.getY()));
			p.getElements().add(new LineTo(v3.getX(),v3.getY()));
			center.getChildren().add(p);
			
			
			
		}
	}
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch();

	}

}
