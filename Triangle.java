package JavaRenderer;

import javafx.scene.paint.Color;

public class Triangle {

	Vertex v1;
	Vertex v2;
	Vertex v3;
	Color color;
	
	

	public Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.color = color;
	}
	
	public Vertex getV1() {
		return v1;
	}

	public void setV1(Vertex v1) {
		this.v1 = v1;
	}

	public Vertex getV2() {
		return v2;
	}

	public void setV2(Vertex v2) {
		this.v2 = v2;
	}

	public Vertex getV3() {
		return v3;
	}

	public void setV3(Vertex v3) {
		this.v3 = v3;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
}
