package pathfinder;

/** FILE was created by Aghmed and Xcom
	* using source without permission forbidden
	* pretending to be creator of code forbidden
*/


import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Color;
import java.lang.Math;
import java.awt.geom.Point2D;

import haven.HavenPanel;
import haven.Coord;
import haven.Gob;
import haven.Glob;
import haven.Sprite;
import haven.RemoteUI;
import haven.Resource;
import haven.ResDrawable;
import haven.Window;
import haven.Widget;
import haven.Inventory;
import haven.Img;
import haven.Button;
import haven.ISBox;
import haven.Item;
import haven.IMeter;
import haven.IMeter.Meter;
import haven.Moving;
import haven.UI;
import haven.Label;
import haven.IButton;
import haven.Buff;
import haven.Makewindow;
import haven.LoginScreen;
import haven.Charlist;
import haven.MapView;
import haven.VMeter;
import haven.HackThread;
import haven.Progress;
import haven.Config;
import haven.GOut;
import haven.Utils;
import haven.KinInfo;
import haven.CharWnd;
import haven.CharWnd.Study;
import haven.MCache;

public class HavenUtil{
	
	HavenPanel m_hPanel;
	public static Glob glob = null;
	
	public static final int MOUSE_LEFT_BUTTON = 1;
	public static final int MOUSE_RIGHT_BUTTON = 3;
	
	public static final int SHIFT_MOD = 1;
	public static final int CTRL_MOD = 2;
	public static final int ALT_MOD = 4;
	public static final int SHIFT_CTRL_MOD = 2;
	public static final int ALT_CTRL_MOD = 2;
	
	public static final int ACTIONBAR_NUMPAD = 0;
	public static final int ACTIONBAR_F = 1;
	public static final int ACTIONBAR_DIGIT = 2;
	
	public static int TravelWeariness = 0;
	public static int HourglassID = -1;
	public static int HourGlassValue = -1;
	
	public HavenUtil(HavenPanel hp){
		m_hPanel = hp;
	}
	
	public Coord getPlayerCoord(){
		try{
			return getPlayerGob().getc();
		}catch(Exception e){
			while(getPlayerGob() == null) wait(100);
			//if(mainPF.stop) return null;
		}
		
		return getPlayerGob().getc();
	}
	
	public boolean checkPlayerWalking(){
		Gob g = getPlayerGob();
		if (g!=null && g.checkWalking()){
			return true;
		}
		
		return false;
	}
	
	public void clickWorldObject(int button, Gob object){
		if(object == null)
		return;
		m_hPanel.ui.mainview.wdgmsg("click", new Coord(200,150), object.getc(), button, 0, object.id, object.getc());
	}
	
	public void clickWorld(int button, Coord c){
		m_hPanel.ui.mainview.wdgmsg("click", new Coord(0, 0), c, button, 0);
	}
	
	public void clickWorld(int button, Coord c, int mod){
		m_hPanel.ui.mainview.wdgmsg("click", new Coord(0, 0), c, button, mod);
	}
	
	boolean getSwiming(){
		String swimName = "gfx/hud/skills/swim";
		
		for(Buff b : m_hPanel.ui.sess.glob.buffs.values()) {
			if(swimName.equals(b.res.get().name) ){
				return true;
			}
		}
		return false;
	}
	
	ArrayList<Rectangle> voidFilter2(Coord player, boolean waterPath){
		//System.out.println("Tile size. " + tiles.size() );
		int[][] tileList = new int[300][300];
		ArrayList<Coord> filter = new ArrayList<Coord>();
		
		Coord gc = player.div(1100).add(-1,-1);
		
		for(int My = 0; My < 3; My++){
			for(int Mx = 0; Mx < 3; Mx++){
				synchronized(m_hPanel.ui.mainview.map.grids){
					MCache.Grid gd = m_hPanel.ui.mainview.map.grids.get(gc.add(Mx,My) );
					
					for(int i = 0; i < 100; i++){
						for(int j = 0; j < 100; j++){
							if(gd != null){
								tileList[j+(Mx*100)][i+(My*100)] = gd.tiles[j][i];
							}else{
								tileList[j+(Mx*100)][i+(My*100)] = -1;
							}
							//System.out.println("tileList: "+ gd.tiles[j][i]);
						}
					}
				}
			}
		}
		
		for(int Ny = 1; Ny < 299; Ny++){
			for(int Nx = 1; Nx < 299; Nx++){
			//for(Coord c : tiles){
				//int test = getTileID(c);
				int test = tileList[Nx][Ny];
				if(waterPath && test < 2) continue;
				else if(!waterPath && test < 255 && test > 0) continue;
				//if(!filter.contains(c) ) continue;
				//System.out.println("test: "+ test);
				
				int a = -1;
				int b = -1;
				for(int i = 0; i < 8; i++){
					//int id = getTileID(c.add(a,b) );
					int Bx = Nx+a;
					int By = Ny+b;
					
					/*if(Bx < 0 || By < 0 || Bx > 299 || By > 299){
						a++;
						if(a == 0 && b == 0) a++;
						if(a > 1){ b++; a = -1;}
						continue;
					}*/
					
					int id = tileList[Bx][By];
					//System.out.println("id: "+ id);
					
					if(waterPath && id >= 0 && id < 2){
						Coord c = gc.mul(100).add(Nx,Ny);
						filter.add(c);
						break;
					}else if(!waterPath && id < 255 && id > 0){
						Coord c = gc.mul(100).add(Nx,Ny);
						filter.add(c);
						break;
					}
					
					a++;
					if(a == 0 && b == 0) a++;
					if(a > 1){ b++; a = -1;}
				}
			}
		}
		
		
		//System.out.println("filter size: "+filter.size() );
	ArrayList<Rectangle> tileRects=new ArrayList<Rectangle>();	
		 for(Coord c : filter){
   Rectangle rect = new Rectangle(c.x * 11 - 1, c.y * 11 - 1, 12, 12);
   
   tileRects.add(rect);
  }
		return tileRects;
	}
	public int getTileID(Coord tc){
		return m_hPanel.ui.mainview.map.gettilen(tc);
	}
	
	public static boolean flowerMenuReady(){
		return UI.flowerMenu != null;
	}
	
	public Gob findClosestWorldObject(String name){
		Gob closest = null;
		double min = 0;
		
		synchronized(m_hPanel.ui.mainview.glob.oc){
			for(Gob g : m_hPanel.ui.mainview.glob.oc){
				if(g.resname().contains(name)){
					double dist = getPlayerCoord().dist(g.getc() );
					if(closest == null){
						closest = g;
						min = dist;
						}else if(dist < min){
						closest = g;
						min = dist;
					}
				}
			}
		}
		return closest;
	}
	
	public Gob getPlayerGob(){
		return m_hPanel.ui.mainview.glob.oc.getgob(m_hPanel.ui.mainview.playergob);
	}
	
	public void sendSlenMessage(String str){
		m_hPanel.ui.slen.error(str);
	}
	
	public void wait(int time){
		try{
			Thread.sleep(time);
		}
		catch(Exception e){}
	}
	
	ArrayList<Rectangle> getAllNegs2(boolean boatTravel){
		//long time = System.currentTimeMillis();
		
		int meOffcet = -2;
		int meSize = 4;
		
		if(boatTravel){
			meOffcet = -14;
			meSize = 26;
		}
		
		Gob player = getPlayerGob();
		ArrayList<Rectangle> negRec = new ArrayList<Rectangle>();
		synchronized(m_hPanel.ui.mainview.glob.oc){
			for(Gob g : m_hPanel.ui.mainview.glob.oc){
				String name = g.resname();
				
				if( g == player ) continue;
				
				if( name.contains("/plants/") ) continue;
				if( name.contains("/items/") ) continue;
				if( name.equals("gfx/terobjs/trees/log") ) continue;
				if( name.equals("gfx/terobjs/blood") ) continue;
				//if( name.equals("gfx/terobjs/herbs/chantrelle") ) continue;
				if( name.equals("gfx/terobjs/claim") ) continue;
				if( name.equals("gfx/terobjs/anthill-r") ) continue;
				if( (name.equals("gfx/terobjs/hearth-play") && g.getattr(KinInfo.class) == null) ) continue;
				if( (name.contains("/gates/") && g.GetBlob(0) == 2) ) continue;
				
				Coord offcet = new Coord();
				Coord size = new Coord();
				
				if(!kritterFix(g, offcet, size) ){
					Resource.Neg neg = g.getneg();
					if(neg == null){/*System.out.println("Error neg");*/ continue;}
					
					offcet = neg.bc;
					size = neg.bs;
				}
				
				if(size.x != 0){
					if(g.resname().equals("gfx/arch/door-inn") ){
						Resource.Neg neg = g.getneg();
						offcet = neg.bc;
						size = neg.bs;
						
						Rectangle rect = new Rectangle(g.getc().x + offcet.x + -2, 
						g.getc().y + offcet.y + -2, 
						size.x + 4, 
						size.y + 4);
						
						if(findClosestWorldObject("gfx/arch/inn") != null ){
							rect.height = rect.height-6;
							rect.y = rect.y + 6;
							}else{
							rect.x = rect.x - 1;
							rect.y = rect.y - 4;
						}
						negRec.add(rect);
					}
					else
					{
						Rectangle rect = new Rectangle(g.getc().x + offcet.x + meOffcet, 
						g.getc().y + offcet.y + meOffcet, 
						size.x + meSize, 
						size.y + meSize);
						negRec.add(rect);
					}
				}
			}
		}
		
		return negRec;
	}
	
	public boolean checkPlayerCarry(){
		Gob g = getPlayerGob();
		if (g.checkCarry()){
			return true;
		}
		
		return false;
	}
		public boolean gobMoving(Gob g){
		Moving m = (Moving)g.getattr(Moving.class);
		
			if (m != null){
				return true;
			}
			
		return false;
	}
	
	public void flowerMenuSelectInt(int index){
		if(!flowerMenuReady())
			return;
		
		UI.flowerMenu.SelectOptint(index);
	}
	
	public boolean checkFlowerMenu(String name){
		if(!flowerMenuReady())
			return false;
		
		return UI.flowerMenu.checkFlower(name);
	}
	
	public int checkFlowerSize(){
		if(!flowerMenuReady())
			return 0;
		
		return UI.flowerMenu.checkFlowerSize();
	}
	
	boolean kritterFix(Gob g, Coord offcet, Coord size){ // fix for those pesky bugs
		
		if(g.resname().contains("gfx/borka/s") && !g.isHuman()){
			offcet.x = 0;
			offcet.y = 0;
			size.x = 1;
			size.y = 1;
			
			return true;
			}else if(g.resname().contains("gfx/arch/sign") ){
			offcet.x = -5;
			offcet.y = -5;
			size.x = 10;
			size.y = 10;
			
			return true;
			}else if(g.resname().contains("gfx/kritter/rat/s") ){
			for(String s : g.resnames() ){
				if(s.contains("gfx/kritter/dragonfly") || s.contains("gfx/kritter/moth") ){
					offcet.x = 0;
					offcet.y = 0;
					size.x = 0;
					size.y = 0;
					
					return true;
				}
			}
		}
		
		return false;
	}
	
}