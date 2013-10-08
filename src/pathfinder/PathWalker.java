package pathfinder;
/** FILE was created by Aghmed and Xcom
	* using source without permission forbidden
	* pretending to be creator of code forbidden
*/

import java.util.ArrayList;
import java.awt.Rectangle;
import java.awt.Point;

import haven.Coord;
import haven.Gob;
import haven.Resource;
import haven.KinInfo;
import haven.Item;
import haven.Config;

public class PathWalker extends Thread{
	HavenUtil m_util;
	PathFinder pf;
	Gob m_gob;
	Coord m_c;
	int m_oldFrame = 0;
	ArrayList<Gob> m_memGobs = new ArrayList<Gob>();
	ArrayList<Rectangle> m_memRect = new ArrayList<Rectangle>();
	
	public boolean pathing = true;
	public boolean forceStopPF = false;
	boolean PFlocked = false;
	int button;
	//double m_nextDist;
	
	public PathWalker(HavenUtil h){
		m_util = h;
		//pf = new PathFinder(m_util);
		//m_nextDist = nextDist;
	}
	
	public PathWalker(HavenUtil h, Gob g, int b){
		m_util = h;
		button = b;
		m_gob = g;
		m_c = null;
	}
	
	public PathWalker(HavenUtil h, Coord c, int b){
		m_util = h;
		button = b;
		m_gob = null;
		m_c = c;
	}
	
	public void run(){
		pf = new PathFinder(m_util, this);
		
		if(m_gob != null){
			to(m_gob);
		}else{
			to(m_c);
		}
	}
	
	void to(Coord c)
	{
	mainPF.pool.clear();
	forceStopPF=false;
	if(m_util.m_hPanel.ui.modshift)
	c=pf.subtileCenter(c);
	
		while(pathing)
		{
		if(forceStopPF){setPFfalse(); return;}
		
			ArrayList<Coord> pathCoord = new ArrayList<Coord>();
			ArrayList<Rectangle> allRect = new ArrayList<Rectangle>();
			PFlocked=true;
			if(pathing) pathCoord = pf.generate(c,false);
			PFlocked=false;
			if(forceStopPF){setPFfalse(); return;}
			
			if(m_util.checkPlayerCarry() && button == 3)
				pathCoord.remove(pathCoord.size()-1);
			
			if(forceStopPF){setPFfalse(); return;}
			if(pathCoord.size()>0)
			{
				showPath(pathCoord, pf.getRects(false));
				
				if(walkPath(pathCoord, 0))pathing=false;
			}
			else
			{
					setPFfalse();
					m_util.sendSlenMessage("can't find path");
					return;
			}
			if(m_util.checkPlayerCarry() && button == 3)
				m_util.clickWorld(3, c);
				
		}
			
		
		
		setPFfalse();
		
	}
	
	void to(Gob g)
	{
	mainPF.pool.clear();
		if(g != null){
			Coord c = g.getc();
			Coord Target=new Coord(c);
						if(g.resname().contains("door"))
				if(m_util.findClosestWorldObject("gfx/arch/inn") != null || m_util.findClosestWorldObject("gfx/arch/cabin-log-big") != null || m_util.findClosestWorldObject("gfx/arch/cabin-log2") != null )
				{
					Target.x+=3;
					Target.y+=3;
				}
				else
				{
					Target.x-=3;
					Target.y-=3;
				}
				if(forceStopPF){setPFfalse(); return;}
			int ignoreLast = 1;
			boolean waterPath = boatTest();
			ArrayList<Coord> pathCoord = new ArrayList<Coord>();
			while(pathing && !forceStopPF){
			if(forceStopPF){setPFfalse(); return;}
			
				if(pathing)
					pathCoord = pf.generate(Target,true);
					
				if(pathCoord.size()>1)
				{
					String name=g.resname();
					if( (name.contains("/gates/") && g.GetBlob(0) == 2) || name.contains("/sign") )
						pathCoord.remove(pathCoord.size()-1);
					
					if(forceStopPF){setPFfalse(); return;}
					showPath(pathCoord, pf.getRects(true));
					if(walkPath(pathCoord, 1))pathing=false;
					if(!forceStopPF)	
					{	
						m_util.clickWorld(1, g.getc());
						m_util.clickWorldObject(3, g);
						if(mainPF.shift){
						int x=0;
						while(x<7 && !m_util.flowerMenuReady()){
						if(forceStopPF){setPFfalse(); return;}
						m_util.wait(100);
						x++;}
						//if(m_util.checkFlowerSize()==1 && !m_util.checkFlowerMenu("Chop"))
							m_util.flowerMenuSelectInt(0);
							}
					}
					
				}
				else
				{
					setPFfalse();
					m_util.sendSlenMessage("can't find path");
					return;
				}
			}
			
		setPFfalse();
		}
	else
		setPFfalse();
	}
					/*if(pathCoord.size()>1 && mainPF.PFrunning){
					if(!pf.see(m_util.getPlayerCoord(),pf.endpoint(merged,c),merged)){pathing=true; System.out.println("i have to repath"); mainPF.publicLineBoolean = false;}
					else
						pathing=false;
				}*/
	

	
	boolean boatTest(){
	if(forceStopPF){return false;}
		Gob boat = m_util.findClosestWorldObject("boat");
		Gob player = m_util.getPlayerGob();
		if(boat == null) return false;
		return (player.checkSitting() && player.getc().equals(boat.getc()) );
	}
	
	void setPFfalse()
	{
	pathing=false;
	forceStopPF=true;
	mainPF.publicLineBoolean = false;
	}
	
	int pathLength(ArrayList<Coord> pathCoord)
	{
	if(forceStopPF){return 0;}
		return pf.distance(pathCoord.get(0),pathCoord.get(pathCoord.size()-1));
	}
	
	boolean walkPath(ArrayList<Coord> pathCoord, int ignoreLast)
	{
	if(pathCoord.size()<1) {return true;}
	if(pathLength(pathCoord)<3)
	{
		m_util.clickWorld(1, pathCoord.get(pathCoord.size()-1));
		pathing=false;
		return true;
		
	}
	if(forceStopPF){ return true;}	
	ArrayList<Coord> newPath=new ArrayList<Coord>(pathCoord);
	boolean boatTest=boatTest();
	if(mainPF.dynamicPathing)
	{
	ArrayList<Rectangle> merged=new ArrayList<Rectangle>();
	
		if(pathCoord.size()<1){return true;}
	int ID=m_util.getTileID(pathCoord.get(pathCoord.size()-1).div(11));
	if(boatTest && ID>1) {return true;}
	if(!boatTest && ID==0 && !m_util.getSwiming()){return true;}
		for(int i = 1; i < (pathCoord.size()); i++){
		if(forceStopPF){ return true;}	
			Coord c = pathCoord.get(i);
			if(!pf.see(m_util.getPlayerCoord(),c,merged))
				{
					showPath(newPath,merged);
					walkPath(removeWalked(newPath,pathCoord.get(i-1)),ignoreLast);
					pathing=false;
					return true;
				}
			else
				{
					m_util.clickWorld(1, c);	
				}
			if(forceStopPF){ return true;}	
			Gob boat=m_util.findClosestWorldObject("boat");
			if(boatTest)
				while(!m_util.gobMoving(boat) && !forceStopPF)
					m_util.wait(10);
			else
				while(!m_util.checkPlayerWalking() && !forceStopPF )
					m_util.wait(10);
			if(forceStopPF){return true;}
			while((m_util.checkPlayerWalking() || (boatTest && m_util.gobMoving(boat))) || !m_util.getPlayerCoord().equals(c) && !forceStopPF)
			{
				if(forceStopPF) {return true;}
				if(pathCoord.size()<1) return true;
				
				if((pathCoord.size()>1 && (m_util.getPlayerCoord().equals(pathCoord.get(pathCoord.size()-1)) && !m_util.checkPlayerWalking())) || m_util.flowerMenuReady())
				{
					//setPFfalse();
					return true;
				}
				
				if(ignoreLast==0)
					merged=new ArrayList<Rectangle>(pf.getRects(false));
				else
					merged=new ArrayList<Rectangle>(pf.getRects(true));
				
				Coord me=m_util.getPlayerCoord();
				
				if(repath(i,newPath,merged,me))
				{
				PFlocked=true;
					if(ignoreLast==0)
					newPath=pf.generate(pathCoord.get(pathCoord.size()-1),false);
					else
					newPath=pf.generate(pathCoord.get(pathCoord.size()-1),true);
				PFlocked=false;
				if(forceStopPF){return true;}
					//showPath(newPath,merged);
					if(newPath.contains(c))
					{
						if(!pf.see(m_util.getPlayerCoord(),c,merged))
						{
							showPath(newPath,merged);
							walkPath(newPath,ignoreLast);
							pathing=false;
							return true;
						}
					}
					else
					{
						showPath(newPath,merged);
						walkPath(newPath,ignoreLast);
						pathing=false;
						return true;
					}
				}
				if(forceStopPF){return true;}
				int u=0;
				while(!forceStopPF && u<15){
				u++;
				m_util.wait(10);
				}
				if(forceStopPF){return true;}
				boolean changed=false;
				for(int j=0; j<pathCoord.size()-2; j++)
				{
					if(pf.see(pathCoord.get(j),pathCoord.get(j+2),merged))
					{
						pathCoord.remove(j+1);
						changed=true;
						if(forceStopPF){return true;}
					}
				}
				
				if(changed)
				{
					m_util.clickWorld(1,c);
					changed=false;
				}
			}
			if(!m_util.getPlayerCoord().equals(c))
				i--;
			if(forceStopPF){ return true;}	

		}
		
		return true;
	
	}
	else
	{
	ArrayList<Rectangle> merged=new ArrayList<Rectangle>();
	if(forceStopPF){ return true;}	
	if(pathCoord.size()<1) return true;
	int ID=m_util.getTileID(pathCoord.get(pathCoord.size()-1).div(11));
	if(boatTest() && ID>1) {return true;}
	if(!boatTest() && ID==0 && !m_util.getSwiming()){return true;}
		for(int i = 1; i < (pathCoord.size()); i++)
		{
			if(pathCoord.size()<1) return true;
			Coord c = pathCoord.get(i);
			m_util.clickWorld(1, c);
			if(forceStopPF){ return true;}	
			if(ignoreLast==0)
				merged=new ArrayList<Rectangle>(pf.getRects(false));
			else
				merged=new ArrayList<Rectangle>(pf.getRects(true));
				
			Coord me=new Coord(m_util.getPlayerCoord());
			if(repath(i,pathCoord,merged,me))
				{
					PFlocked=true;
					if(ignoreLast==0)
					pathCoord=pf.generate(pathCoord.get(pathCoord.size()-1),false);
					else
					pathCoord=pf.generate(pathCoord.get(pathCoord.size()-1),true);
					PFlocked=false;
					if(forceStopPF){return true;}
					showPath(pathCoord,merged);
					walkPath(pathCoord,ignoreLast);
					pathing=false;
					return true;
				}
			
				
				
			
			Gob boat=m_util.findClosestWorldObject("boat");
			if(forceStopPF){return true;}
			if(boatTest)
				while(!m_util.gobMoving(boat) && !forceStopPF)
					m_util.wait(20);
			else
				while(!m_util.checkPlayerWalking() && !forceStopPF )
					m_util.wait(20);
			if(forceStopPF){setPFfalse();return true;}
			
			while((m_util.checkPlayerWalking() || (boatTest && m_util.gobMoving(boat))) && !forceStopPF )
			{
			m_util.wait(30);
			}
			
			if(forceStopPF){return true;}
			me=new Coord(m_util.getPlayerCoord());
			if(!me.equals(c) && !m_util.checkPlayerWalking())
			{
			//System.out.println(pf.distance(me,c));
			PFlocked=true;
					if(ignoreLast==0)
					pathCoord=pf.generate(pathCoord.get(pathCoord.size()-1),false);
					else
					pathCoord=pf.generate(pathCoord.get(pathCoord.size()-1),true);
					PFlocked=false;
					if(forceStopPF){return true;}
					showPath(pathCoord,merged);
					walkPath(pathCoord,ignoreLast);
					pathing=false; 
					return true;
			}
			
		if(forceStopPF){return true;}
		}
			
	}
	return true;
	}
	
	ArrayList<Coord> removeWalked(ArrayList<Coord> newPath,Coord c)
	{
	if(forceStopPF){return new ArrayList<Coord>();}
	if(!newPath.contains(c)) return newPath;
	int x=0;
	while(!newPath.get(x).equals(c) && x<newPath.size())
		x++;
	if(x==newPath.size()) return newPath;
	for(int i=0; i<x; i++)
		newPath.remove(0);
		
	return newPath;
	}
	
	boolean repath(int i, ArrayList<Coord> pathCoord,ArrayList<Rectangle> merged, Coord me )
	{
	if(forceStopPF){return false;}
		if(i<pathCoord.size() && !pf.see(me,pathCoord.get(i),merged)) return true;
		if(pathCoord.size()-1>i)
		for(i=i; i<pathCoord.size()-1; i++)
			if(!pf.see(pathCoord.get(i),pathCoord.get(i+1),merged)) return true;
		
		return false;
	}
	
	void showPath(ArrayList<Coord> pathCoord, ArrayList<Rectangle> r){
		mainPF.publicLineBoolean = false;
		if(forceStopPF){return;}
		m_util.wait(50);
		mainPF.publicRects.clear();
		mainPF.publicPoints.clear();
		if(Config.pathfinderHitboxes)mainPF.publicRects = new ArrayList<Rectangle>(r);
		ArrayList<Coord> draw = new ArrayList<Coord>(pathCoord);
		if(draw.size()==0){draw.add(m_util.getPlayerCoord().add(5,5) );draw.add(new Coord(0,0));}
		if(Config.pathfinderLines)mainPF.publicPoints = draw;
		
		mainPF.publicLineBoolean = true;
	}
}