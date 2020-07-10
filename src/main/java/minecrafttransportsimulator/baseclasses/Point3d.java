package minecrafttransportsimulator.baseclasses;

/**Double implementation of point class.
 *
 * @author don_bruce
 */
public class Point3d extends APoint3<Double, Point3d>{
	private static final Point3d ZERO = new Point3d(0D, 0D, 0D);
	
	public double x;
	public double y;
	public double z;
	
	public Point3d(double x, double y, double z){
		super(x, y, z);
	}
	
	@Override
	public boolean equals(Object object){
		if(object instanceof Point3d){
			Point3d otherPoint = (Point3d) object;
			return (float)x == (float)otherPoint.x && (float)y == (float)otherPoint.y && (float)z == (float)otherPoint.z;
		}else{
			return false;
		}
	}
	
	@Override
	public Point3d set(Double x, Double y, Double z){
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	@Override
	public Point3d setTo(Point3d point){
		this.x = point.x;
		this.y = point.y;
		this.z = point.z;
		return this;
	}
	
	@Override
	public Point3d add(Double x, Double y, Double z){
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	@Override
	public Point3d add(Point3d point){
		this.x += point.x;
		this.y += point.y;
		this.z += point.z;
		return this;
	}
	
	@Override
	public Point3d subtract(Point3d point){
		this.x -= point.x;
		this.y -= point.y;
		this.z -= point.z;
		return this;
	}
	
	@Override
	public Point3d multiply(Double scale){
		this.x *= scale;
		this.y *= scale;
		this.z *= scale;
		return this;
	}

	@Override
	public Point3d multiply(Point3d point){
		this.x *= point.x;
		this.y *= point.y;
		this.z *= point.z;
		return this;
	}
	
	@Override
	public Double distanceTo(Point3d point){
		double deltaX = point.x - this.x;
		double deltaY = point.y - this.y;
		double deltaZ = point.z - this.z;
		return Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ);
	}
	
	public Double distanceTo(Point3i point){
		double deltaX = point.x - this.x;
		double deltaY = point.y - this.y;
		double deltaZ = point.z - this.z;
		return Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ);
	}
	
	@Override
	public Double dotProduct(Point3d point){
		return this.x*point.x + this.y*point.y + this.z*point.z;
	}
	
	@Override
	public Point3d crossProduct(Point3d point){
		return new Point3d(this.y*point.z - this.z*point.y, this.z*point.x - this.x*point.z, this.x*point.y - this.y*point.x);
	}
	
	@Override
	public Double length(){
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	@Override
	public Point3d normalize(){
		Double length = length();
		if(length > 1.0E-4D){
			x /= length;
			y /= length;
			z /= length;
		}
		return this;
	}
	
	@Override
	public Point3d copy(){
		return new Point3d(this.x, this.y, this.z);
	}
	
	@Override
	public boolean isZero(){
		return this.equals(ZERO);
	}
	
	private static final Double[] sinTable = new Double[361];
	private static final Double[] cosTable = new Double[361];
	private static double xRot;
	private static double yRot;
	private static double zRot;
	public Point3d rotateCoarse(Point3d angles){
		//Init sin and cos tables, if they aren't ready.
		if(sinTable[0] == null){
			for(int i=0; i<=360; ++i){
				sinTable[i] = Math.sin(Math.toRadians(i));
				cosTable[i] = Math.cos(Math.toRadians(i));
			}
		}
		
		//Clamp values to 0-360;
		xRot = (angles.x%360 + 360)%360;
		yRot = (angles.y%360 + 360)%360;
		zRot = (angles.z%360 + 360)%360;
		
		//Rotate based on tabled values.
		d1 = cosTable[(int) xRot];//A
		d2 = sinTable[(int) xRot];//B
		d3 = cosTable[(int) yRot];//C
		d4 = sinTable[(int) yRot];//D
		d5 = cosTable[(int) zRot];//E
		d6 = sinTable[(int) zRot];//F
		x = x*(d3*d5-d2*-d4*d6) + y*(-d2*-d4*d5-d3*d6) + z*(-d1*-d4);
		y = x*(d1*d6)           + y*(d1*d5)            + z*(-d2);
		z = x*(-d4*d5+d2*d3*d6) + y*(d2*d3*d5+d4*-d6)  + z*(d1*d3);
		return this;
	}
	
	private static double d1;
	private static double d2;
	private static double d3;
	private static double d4;
	private static double d5;
	private static double d6;
	public Point3d rotateFine(Point3d angles){
		d1 = Math.cos(Math.toRadians(angles.x));//A
		d2 = Math.sin(Math.toRadians(angles.x));//B
		d3 = Math.cos(Math.toRadians(angles.y));//C
		d4 = Math.sin(Math.toRadians(angles.y));//D
		d5 = Math.cos(Math.toRadians(angles.z));//E
		d6 = Math.sin(Math.toRadians(angles.z));//F
		x = x*(d3*d5-d2*-d4*d6) + y*(-d2*-d4*d5-d3*d6) + z*(-d1*-d4);
		y = x*(d1*d6)           + y*(d1*d5)            + z*(-d2);
		z = x*(-d4*d5+d2*d3*d6) + y*(d2*d3*d5+d4*-d6)  + z*(d1*d3);
		return this;
	}
	
	/*For reference, here are the rotation matrixes.
	 * Note that the resultant rotation matrix follows the Yaw*Pitch*Roll format.
	 * Rx=[[1,0,0],[0,cos(P),-sin(P)],[0,sin(P),cos(P)]]
	 * Ry=[[cos(Y),0,sin(Y)],[0,1,0],[-sin(Y),0,cos(Y)]]
	 * Rz=[[cos(R),-sin(R),0],[sin(R),cos(R),0],[0,0,1]]
	 * {[C,0,-D],[0,1,0],[D,0,C]}*{[1,0,0],[0,A,-B],[0,B,A]}*{[E,-F,0],[F,E,0],[0,0,1]}
	 */
}
