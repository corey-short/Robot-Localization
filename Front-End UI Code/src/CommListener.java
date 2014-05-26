
/**
 * interface for generic GridNavigationController
 * 
 * @author glassey
 * 
 */
public interface CommListener {
	public void setMessage(String s);

	public void drawRobotPath(int x, int y, int heading);

	public void drawObstacle(int x, int y);
}
