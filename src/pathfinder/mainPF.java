package pathfinder;
/** FILE was created by Aghmed and Xcom
	* using source without permission forbidden
	* pretending to be creator of code forbidden
*/

import java.awt.Rectangle;
import java.util.ArrayList;

import haven.HavenPanel;
import haven.Coord;
import haven.Gob;
import haven.Config;
 

public class mainPF{
	HavenPanel m_havenPanel;
	public static HavenUtil m_util;
	
	///buttons
	/*
	public static boolean pathMouse = false;
	public static boolean showPath = Config.pathFinder;
	public static boolean showRectangles = false;
	*/
	public static boolean dynamicPathing = true;
	public static boolean shift = true;
	
	public static ArrayList<Rectangle> publicRects = new ArrayList<Rectangle>();
	public static ArrayList<Rectangle> pool = new ArrayList<Rectangle>();
	public static ArrayList<Coord> publicPoints = new ArrayList<Coord>();
	public static ArrayList<Coord> publicPoints2 = new ArrayList<Coord>();
	public static boolean publicLineBoolean = false;
	
	public mainPF(HavenPanel havenPanel){
		m_util = new HavenUtil(havenPanel);
	}
}