package JavaRenderer;

public class Matrix3 {

	double[][] values;
	
	public Matrix3(double[][] values){
		
		this.values = values;
		
	}
	
	public double[][] getValues() {
		return values;
	}
	public void setValues(double[][] values) {
		this.values = values;
	}



	public Matrix3 multiply(Matrix3 b) {
		
		double[][] bCopy = b.getValues();
		double[][] result = new double[values.length][bCopy[0].length];
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < bCopy[0].length; j++) {
				for (int k = 0; k < values[i].length; k++) {
					result[i][j] += values[i][k] * bCopy[k][j];
				}
			}
		}
		
		return new Matrix3(result);
	}
	
	public Vertex transform(Vertex in) {
		
		return new Vertex(
				in.x * values[0][0] + in.y * values[1][0] + in.z * values[2][0],
				in.x * values[0][1] + in.y * values[1][1] + in.z * values[2][1],
				in.x * values[0][2] + in.y * values[1][2] + in.z * values[2][2]);
		
	}
	
}
