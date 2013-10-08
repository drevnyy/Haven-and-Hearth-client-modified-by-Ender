package pathfinder;
/** FILE was created by Aghmed and Xcom
	* using source without permission forbidden
	* pretending to be creator of code forbidden
*/

import java.util.ArrayList;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import haven.Coord;
import haven.Gob;
import haven.Resource;
import haven.KinInfo;
import haven.Item;


public class PathFinder{
	HavenUtil m_util;
	PathWalker walker;
	
	public PathFinder(HavenUtil h, PathWalker p){
		m_util = h;
		walker = p;
	}
	
	boolean touchWalkableBoat(Coord tile)
	{
	if(walker.forceStopPF)return false;
	Coord test;
	for(int i=-1;i<=1; i++)
				for(int j=-1;j<=1; j++){
					test=new Coord(tile.x+i*11,tile.y+j*11);
					int id=m_util.getTileID(test.div(11));
						if(id==0 || id==1)
							return true;
	}
	
	return false;
	}
	
	boolean touchWalkable(Coord tile)
	{
	if(walker.forceStopPF)return false; 
	Coord test;
	for(int i=-1;i<=1; i++)
				for(int j=-1;j<=1; j++)
				{
					test=new Coord(tile.x+i*11,tile.y+j*11);
					int id=m_util.getTileID(test.div(11));
						if(id!=0 && id!=255)
						return true;
				}
	
	return false;
	}	
	
	ArrayList<Rectangle> unwalkableTiles ()
	{
	if(walker.forceStopPF)return new ArrayList<Rectangle>();
	ArrayList <Rectangle> result=new ArrayList<Rectangle>();
	int ID;
	Coord tile;
	Coord me=m_util.getPlayerCoord();
	me=subtileCorner(me);
	if(boatTest())
		{
			for(int i=-50;i<50; i++)
				for(int j=-50;j<50; j++){
					tile=new Coord(me.x+i*11,me.y+j*11);
					ID=m_util.getTileID(tile.div(11));
					if(walker.forceStopPF)return new ArrayList<Rectangle>();
					if(ID!=0 && ID!=1 && !walker.forceStopPF)
						if(touchWalkableBoat(tile))
						{
						result.add(new Rectangle(tile.x-1,tile.y-1,13,13));
						}
				}
		}
		else
		{
			for(int i=-50;i<50; i++)
				for(int j=-50;j<50; j++){
					tile=new Coord(me.x+i*11,me.y+j*11);
					ID=m_util.getTileID(tile.div(11));
					if(walker.forceStopPF)return new ArrayList<Rectangle>();
					if(ID==0 || ID==255)
						if(touchWalkable(tile))
						{
						result.add(new Rectangle(tile.x-1,tile.y-1,13,13));
						}
						
				}
		}
	return result;
	}
	ArrayList<Rectangle> removeShitRects(ArrayList<Rectangle> rects, int dist,Coord me)
	{
		if(walker.forceStopPF)return new ArrayList<Rectangle>();
		ArrayList<Rectangle> rects2=new ArrayList<Rectangle>();
		for(Rectangle x:rects)
			if((distance(me,new Coord(x.x+x.width/2,x.y+x.height/2))>dist)&& x.width<35 && x.height<35)
			continue;
			else
				rects2.add(x);
		return rects2;
	}
	
	Coord subtileCorner(Coord x)
	{
	if(walker.forceStopPF)return new Coord();
		Coord corner=new Coord();
		if(x==null) return x;
		corner.x=x.x/11;
		if(x.x<0)
			{
			corner.x-=1;
			corner.x*=11;
			}
		else
			corner.x*=11;
		
		corner.y=x.y/11;
		if(x.y<0)
			{
			corner.y-=1;
			corner.y*=11;
			}
		else
			corner.y*=11;
		
		return corner;
	}	
	
	Coord subtileCenter(Coord x)
	{
	return subtileCorner(x).add(5,5);
	}
	
	Rectangle smaller(Rectangle r1, Rectangle r2)
	{
		if(r1.width<r2.width) return r1;
		if(r1.height<r2.height) return r1;
		return r2;
	}
	
	ArrayList<Rectangle> removeDuplicated(ArrayList<Rectangle> input)
	{
		ArrayList<Rectangle> removeList=new ArrayList<Rectangle>();
		ArrayList<Rectangle> output=new ArrayList<Rectangle>();
		ArrayList<Coord> temporary=new ArrayList<Coord>();
		boolean unique=true;
		
		for(Rectangle r1:input)
		{
			for(Rectangle r2:input)
			if(unique)
			{
				if((r1.x==r2.x && r1.y==r2.y) || ((r1.x+r1.width)==(r2.x+r2.width) && (r1.y+r1.height)==(r2.y+r2.height)))
					if(r1!=r2)
				{
					unique=false;
					removeList.add(smaller(r1,r2));
				}
			}
			unique=true;
		}
		
		for(Rectangle r:input)
			if(!removeList.contains(r) || temporary.contains(new Coord(r.x,r.y)))
				output.add(r);
			else
				temporary.add(new Coord(r.x,r.y));
		
		return output;
	}
	
	ArrayList<Rectangle> getRects (boolean gobclick)
	{
		
		Coord me=m_util.getPlayerCoord();
		if(walker.forceStopPF)return new ArrayList<Rectangle>();
		ArrayList<Rectangle> tileRect=new ArrayList<Rectangle>();
		if(!m_util.getSwiming())tileRect.addAll(m_util.voidFilter2(me,boatTest()));
		
		int maxRectangleAmount=120;
		if(mainPF.dynamicPathing)maxRectangleAmount=60;
		ArrayList<Rectangle> gobRect=new ArrayList<Rectangle>();
		gobRect=m_util.getAllNegs2(boatTest());
		tileRect.addAll(gobRect);
		tileRect.addAll(mainPF.pool);
		ArrayList<Rectangle> allRects=new ArrayList<Rectangle>(tileRect);
		if(!gobclick)allRects=merge(allRects);
		allRects=removeDuplicated(allRects);
		ArrayList<Rectangle> mergedRects=new ArrayList<Rectangle>();
		gobRect.clear();
		if(walker.forceStopPF)return new ArrayList<Rectangle>();
		int area=40;
		if(!gobclick)
			do
			{
				if(walker.forceStopPF) return new ArrayList<Rectangle>();
				allRects=removeShitRects(allRects,area*11,me);
				mergedRects=new ArrayList<Rectangle>(merge(allRects));
				area-=2;
			}
			while(mergedRects.size()>maxRectangleAmount && area>10);
		else
		{
			do
			{
				if(walker.forceStopPF) return new ArrayList<Rectangle>();
				allRects=trimtosize(allRects,area*11,me);
				area-=5;
			}
			while(allRects.size()>maxRectangleAmount && area>10);
			
			mergedRects.addAll(allRects);
		
		}
		
		mergedRects=removeOnPlayerRects(me,mergedRects);
		addToPool(mergedRects);
		return mergedRects;
	}
	
	void addToPool(ArrayList<Rectangle> merged)
	{
	for(Rectangle rect:merged)
		if(!mainPF.pool.contains(rect))
			if(big(rect))
			mainPF.pool.add(rect);
	
	}
	
	boolean big(Rectangle rect)
	{
	if(rect.width>33 || rect.height>33) 
		return true;
	
	return false;
	
	}
	
	ArrayList<Rectangle> trimtosize(ArrayList<Rectangle> rects,int dist,Coord me)
	{
	if(walker.forceStopPF)return new ArrayList<Rectangle>();
	ArrayList<Rectangle> rects2=new ArrayList<Rectangle>();
	for(Rectangle x:rects)
		if(distance(me,new Coord(x.x+x.width/2,x.y+x.height/2))<dist)
			rects2.add(x);
	return rects2;
	}
	
	boolean boatTest()
	{
		if(walker.forceStopPF)return false;
		Gob boat = m_util.findClosestWorldObject("boat");
		Gob player = m_util.getPlayerGob();
		if(boat == null) return false;
		return (player.checkSitting() && player.getc().equals(boat.getc()) );
	}
	/**
	boolean tooClose(Rectangle r1, Rectangle r2, boolean y)
	{
		if(y)
			if(r1.x+r1.width>=r2.x )
				return true;
		if(!y)
			if(r1.y+r1.height>=r2.y)
				return true;
		
	return false;
	}*/
	
	ArrayList<Rectangle> merge (ArrayList<Rectangle> merged)
	{
	if(walker.forceStopPF)return new ArrayList<Rectangle>();
	int size=0;
	boolean combine=false;
	int id=0;
	Rectangle neew, rect,Current;
	while (id < merged.size())
	{
	if(walker.forceStopPF)return new ArrayList<Rectangle>();
	combine=false;
		Current=merged.get(id);
		for(int i=0; i<merged.size();i++)
		{
			if(walker.forceStopPF)return new ArrayList<Rectangle>();
			if(i==id && i<merged.size()-1)i++;
			rect=merged.get(i);
			
			if(Current!=rect && Current.y==rect.y && Current.height==rect.height && Current.x<=rect.x && Current.x+Current.width>rect.x)
			{
				//if(tooClose(Current,rect,true))
				//{
				if(rect.x+rect.width<Current.x+Current.width)
					size=Current.width;
				else
					size=rect.x+rect.width-Current.x;
					rect.setBounds(Current.x, Current.y,size,rect.height);
					merged.remove(Current);
					Current=rect;
					combine=true;
					if(i>0)i--;
				//}
			}
			else if(Current!=rect && Current.x==rect.x && Current.width==rect.width && Current.y<=rect.y && Current.y+Current.height>rect.y)
			{
				
				if(rect.y+rect.height<Current.y+Current.height)
					size=Current.height;
				else
					size=rect.y+rect.height-Current.y;
					
					rect.setBounds(Current.x, Current.y,rect.width,size);
					merged.remove(Current);
					Current=rect;
					combine=true;
					if(i>0)i--;
				
				}
				
			
			
		}
		if(combine && id>0)id--;
		if(!combine){ id++;}
	}
	
	return merged;
	}
	
	////////////////////////////////////////////////////////////////////////
	//  neighbour matrix generator
	
	
	ArrayList<Rectangle> removeOnPlayerRects(Coord me,ArrayList<Rectangle> merged)
	{
	if(walker.forceStopPF)return new ArrayList<Rectangle>();
	ArrayList<Rectangle> shit=new ArrayList<Rectangle>();
	for(Rectangle r:merged)
	{
		if(walker.forceStopPF)return new ArrayList<Rectangle>();
		if(r.contains(me.x,me.y))
		{
			if(distance(me,new Coord(r.x+r.width/2,r.y+r.height/2))>1)
			{
				if(lineDistance(me,new Coord(r.x,r.y),new Coord(r.x,r.y+r.height))<2)
				{
					r.x+=1;
					shit.add(r);
				}
				if(lineDistance(me,new Coord(r.x,r.y),new Coord(r.x+r.width,r.y))<2)
				{
					r.y+=1;
					shit.add(r);
				}
				if(lineDistance(me,new Coord(r.x+r.width,r.y),new Coord(r.x+r.width,r.y+r.height))<2)
				{
					r.width-=1;
					shit.add(r);
				}
				if(lineDistance(me,new Coord(r.x,r.y+r.height),new Coord(r.x+r.width,r.y+r.height))<2)
				{
					r.height-=1;
					shit.add(r);
				}
			
			}
		}
		else shit.add(r);
	}
	return shit;
	
	}
	
	double lineDistance(Coord me, Coord p1, Coord p2)
	{
		Line2D line = new Line2D.Double(p1.x,p1.y,p2.x,p2.y);
		return line.ptLineDist(me.x,me.y);
	
	}
	
	boolean rectangleTest(Coord first, Coord last, ArrayList<Rectangle> merged)
	{
		if(walker.forceStopPF)return false;
		Rectangle rFirst=ignoreMe(merged,first);
		Rectangle rLast=ignoreMe(merged,last);
		
		if(rFirst.x!=0 && rLast.y!=0)
			if(rFirst.x==rLast.x && rFirst.y==rLast.y)
				return true;
		return false;
	}
	
	ArrayList<Coord> generate (Coord Target, boolean gobclick)
	{
		if(walker.forceStopPF)return new ArrayList<Coord>();
		long time = System.currentTimeMillis();
		ArrayList<Rectangle> merged=new ArrayList<Rectangle>(getRects(gobclick));
		Coord me=m_util.getPlayerCoord();
		Rectangle rectt;
		ArrayList<Coord> temp=new ArrayList<Coord>();
		
		if(walker.forceStopPF)return new ArrayList<Coord>();
		
		if(rectangleTest(me,Target,merged))
			{
			temp.add(me);
			temp.add(me);
			//long time2 = System.currentTimeMillis();
			//System.out.println("shitty same rectangle situation time: "+(time2-time));
			}
		ArrayList<Rectangle2D> smaller=new ArrayList<Rectangle2D>();
		double delta=0.000001;
		for(Rectangle rect:merged)
			smaller.add(new Rectangle2D.Double(rect.x+delta,rect.y+delta,rect.width-2*delta,rect.height-2*delta));
		
		if(walker.forceStopPF)return new ArrayList<Coord>();
		
		if(see2D(me,Target,smaller))
		{
			temp.add(me);
			temp.add(Target);
			
			//long time2 = System.currentTimeMillis();
			//System.out.println("you don't need pathfinder to go there! time: "+(time2-time));
			
			return temp;
		}
		
		rectt=ignoreMe(merged,Target);
		if(rectt.x!=0 && rectt.y!=0)
		Target=endpoint(merged,Target);
		rectt=ignoreMe(merged,me);
		if(walker.forceStopPF)return new ArrayList<Coord>();
		ArrayList<Coord> corners=new ArrayList<Coord>();
		corners.add(me);
		
		if(walker.forceStopPF)return new ArrayList<Coord>();
		
			for(Rectangle rect:merged)
			{
				corners.add(new Coord(rect.x,rect.y));
				corners.add(new Coord(rect.x+rect.width,rect.y));
				corners.add(new Coord(rect.x,rect.y+rect.height));
				corners.add(new Coord(rect.x+rect.width,rect.y+rect.height));
			}
			corners.add(Target);
		
		int [][] x=new int[corners.size()][corners.size()];
		
			for(int i=1; i<corners.size()-1; i++)
				for(int j=i+1; j<corners.size()-1; j++)
				{
		if(walker.forceStopPF)return new ArrayList<Coord>();
					if(see2D(corners.get(i),corners.get(j),smaller))
						x[i][j]=distance(corners.get(i),corners.get(j));
						
					x[j][i]=x[i][j];
				}
			if(walker.forceStopPF)return new ArrayList<Coord>();
			
				int size=corners.size();
		for(int i=0; i<corners.size()-1; i++)
			if(seeEnd(corners.get(i),corners.get(0),merged,rectt)){
						x[i][0]=distance(corners.get(i),corners.get(0));
						x[0][i]=x[i][0];
					}
					rectt=ignoreMe(merged,Target);
			if(walker.forceStopPF)return new ArrayList<Coord>();
		for(int i=0; i<corners.size()-1; i++)
			if(seeEnd(corners.get(i),corners.get(size-1),merged,rectt)){
						x[i][size-1]=distance(corners.get(i),corners.get(size-1));
						x[size-1][i]=x[i][size-1];
					}
		
		if(walker.forceStopPF)return new ArrayList<Coord>();
		
		corners=getPath(x,corners,Target);
		Coord asd;
		size=corners.size();
		if(size>1)
		{
		Line2D line = new Line2D.Double(corners.get(size-1).x, corners.get(size-1).y, corners.get(size-2).x, corners.get(size-2).y);
					asd=getIntersectionPoint(line, rectt);
					if(asd.x!=0 && asd.y!=0)
					{
					corners.remove(size-1);
					corners.add(asd);
					}
		}
		
		boolean change=true;
		while(change)
		{
			if(walker.forceStopPF)return new ArrayList<Coord>();
			change=false;
			for(int j=0; j<corners.size()-2; j++)
			{
				if(see2D(corners.get(j),corners.get(j+2),smaller))
				{
					corners.remove(j+1);
					change=true;
				}
			}
		}
		if(walker.forceStopPF)return new ArrayList<Coord>();
		long time2 = System.currentTimeMillis();
		System.out.println("ful total time of panthing is ONLY "+(time2-time));
			return corners;
	}
	
	Coord getIntersectionPoint(Line2D line, Rectangle2D rectangle) 
	{
	if(walker.forceStopPF)return new Coord();
            // Top line
			if(line.intersectsLine(new Line2D.Double(rectangle.getX(),rectangle.getY(),rectangle.getX()+rectangle.getWidth(),rectangle.getY())))
				return getIntersectionPointY(line,rectangle.getY());
            // Bottom line
			if(line.intersectsLine(new Line2D.Double(rectangle.getX(),rectangle.getY()+rectangle.getHeight(),rectangle.getX()+rectangle.getWidth(),rectangle.getY()+rectangle.getHeight())))
				return getIntersectionPointY(line,rectangle.getY() + rectangle.getHeight());
            // Left side...
			if(line.intersectsLine(new Line2D.Double(rectangle.getX(),rectangle.getY(),rectangle.getX(),rectangle.getY()+rectangle.getHeight())))
				return getIntersectionPointX(line,rectangle.getX());

            // Right side
			if(line.intersectsLine(new Line2D.Double(rectangle.getX()+rectangle.getWidth(),rectangle.getY(),rectangle.getX()+rectangle.getWidth(),rectangle.getY()+rectangle.getHeight())))
				return getIntersectionPointX(line,rectangle.getX() + rectangle.getWidth());

		return new Coord(0,0);
        }
	
	Rectangle rectClicked(ArrayList<Rectangle> possible, Coord x)
	{
	if(walker.forceStopPF)return new Rectangle();
		int dist=99999999;
		Rectangle closest=possible.get(0);
		for(Rectangle r:possible)
			if(distance(x,new Coord(r.x+(r.width/2),r.y+(r.height/2)))<dist)
			{
				dist=distance(x,new Coord(r.x+(r.width/2),r.y+(r.height/2)));
				closest=r;
			}
		
		return closest;
	}
	
	ArrayList<Coord> getCrossCoords(ArrayList<Rectangle> possible,Rectangle clicked)
	{
		if(walker.forceStopPF)return new ArrayList<Coord>();
		ArrayList<Coord> addMe=new ArrayList<Coord>();
		ArrayList<Coord> good=new ArrayList<Coord>();
		
		addMe.add(new Coord(clicked.x,clicked.y));
		addMe.add(new Coord(clicked.x,clicked.y+clicked.height));
		addMe.add(new Coord(clicked.x+clicked.width,clicked.y));
		addMe.add(new Coord(clicked.x+clicked.width,clicked.y+clicked.height));
		
		for(Rectangle r:possible)
		{
			addMe.add(new Coord(r.x,clicked.y));
			addMe.add(new Coord(r.x,clicked.y+clicked.height));
			addMe.add(new Coord(clicked.x+clicked.width,r.y));
			addMe.add(new Coord(clicked.x+clicked.width,r.y+r.height));
		}
		
		for(Coord c:addMe)
				if(new Rectangle2D.Double(clicked.x,clicked.y,clicked.width,clicked.height).contains(c.x,c.y))
					good.add(c);
		
		return good;
	}
	
	Coord endpoint(ArrayList<Rectangle> merged, Coord x)
	{
	if(walker.forceStopPF)return new Coord();
		ArrayList<Rectangle> possibler=new ArrayList<Rectangle>();

		for(Rectangle r:merged)
			if(r.contains(x.x,x.y)) possibler.add(r);
		if(possibler.size()==0 || possibler.size()==1) return x;
		Rectangle clicked=rectClicked(possibler,x);
		if(walker.forceStopPF)return new Coord();
		ArrayList<Coord> possiblec=new ArrayList<Coord>(getCrossCoords(possibler,clicked));
		Line2D line = new Line2D.Double(x.x,x.y,m_util.getPlayerCoord().x,m_util.getPlayerCoord().y);
		possiblec.add(getIntersectionPoint(line,clicked));
		if(walker.forceStopPF)return new Coord();
		if(possiblec.size()==0) return x;
		
		Coord me=m_util.getPlayerCoord();
		int dist=99999;
		Coord that=possiblec.get(0);
		for(Coord c:possiblec)
		{if(walker.forceStopPF)return new Coord();
			int newDist=distance(c,me);
			if(newDist<dist)
			{
				dist=newDist;
				that=c;
			}
		}
		if(walker.forceStopPF)return new Coord();
		return that;

	}
	
	int distance(Coord start, Coord end)
	{
	if(walker.forceStopPF)return 0;
	int x=end.x-start.x;
	int  y=end.y-start.y;
	
	return (int)Math.sqrt(x*x+y*y);
	
	}
	
	Rectangle ignoreMe(ArrayList<Rectangle> merged, Coord x)
	{
	if(walker.forceStopPF)return new Rectangle();
	for(Rectangle rect:merged)
	{
		if (rect.contains(x.x, x.y))
				return rect;
	}
	return new Rectangle(0,0,0,0);
	}
	
	boolean seeEnd(Coord start, Coord end,ArrayList<Rectangle> merged,Rectangle Ignore)
	{
	if(walker.forceStopPF)return false;
	double delta=0.001;
	Line2D.Float daLine = new Line2D.Float(start.x,start.y,end.x,end.y);
	for(Rectangle rect:merged)
	{
		if(rect!=Ignore)
		if (daLine.intersects(rect.x+delta,rect.y+delta,rect.width-2*delta,rect.height-2*delta))
				return false;
	}
	return true;
	}

	boolean see(Coord start, Coord end,ArrayList<Rectangle> merged)
	{
	if(walker.forceStopPF)return false;
	double delta=0.000001;
	Line2D.Float daLine = new Line2D.Float(start.x,start.y,end.x,end.y);
	for(Rectangle rect:merged)
	{
		if (daLine.intersects(rect.x+delta,rect.y+delta,rect.width-2*delta,rect.height-2*delta))
				return false;
	}
	return true;
	}
	
	boolean see2D(Coord start, Coord end,ArrayList<Rectangle2D> merged)
	{
	if(walker.forceStopPF)return false;
	Line2D.Float daLine = new Line2D.Float(start.x,start.y,end.x,end.y);
	for(Rectangle2D rect:merged)
	{
		if (daLine.intersects(rect))
				return false;
	}
	return true;
	
	}

	ArrayList<Coord> givePath(ArrayList<Integer> shit,ArrayList<Coord> merged)
	{
	if(walker.forceStopPF)return new ArrayList<Coord>();
		ArrayList<Coord>path=new ArrayList<Coord>();
		for(int i=0; i<shit.size(); i++)
		{
		if(walker.forceStopPF)return new ArrayList<Coord>();
		path.add(merged.get(shit.get(i)));
		}
		return path;
	}
	
		@SuppressWarnings("unchecked")
	ArrayList<Coord> getPath( int[][] x, ArrayList<Coord>merged, Coord Target)
	{
		if(walker.forceStopPF)return new ArrayList<Coord>();
		
		int size=merged.size();
		ArrayList<Integer> queue=new ArrayList<Integer>();
		queue.add(0);
		ArrayList<Coord> path=new ArrayList<Coord>();
		ArrayList<Integer> pq;
		ArrayList[] parentq=new ArrayList[size];
		int [] parents=new int[size];
		for(int i=0; i<size; i++)
			parentq[i]=new ArrayList<Integer>();
		if(walker.forceStopPF)return new ArrayList<Coord>();
		while(queue.size()>0)
		{if(walker.forceStopPF)return new ArrayList<Coord>();
			int that=queue.get(0);
			for(int i=1; i<size; i++)
				if(x[that][i]>0 && i!=that)
				{
					if(walker.forceStopPF)return new ArrayList<Coord>();
					if(parentq[i].size()==0)
					{
						parentq[i].addAll(parentq[that]);
						parentq[i].add(that);
						parentq[i].add(i);
						parents[i]=parents[that]+x[that][i];
						queue.add(i);
					}
					if(parents[i]>(parents[that]+x[that][i]) && parentq[that].contains(0))
					{
						parentq[i].clear();
						parentq[i].addAll(parentq[that]);
						parentq[i].add(that);
						parentq[i].add(i);
						parents[i]=parents[that]+x[that][i];
						queue.add(i);
				}
			}
			queue.remove(0);
		}
		if(walker.forceStopPF)return new ArrayList<Coord>();
		if(parentq[size-1].size()==0)return path;
		size=size-1;
		int i=0;
		if(walker.forceStopPF)return new ArrayList<Coord>();
		path.addAll(givePath(parentq[size],merged));
	return path;
	}
	
	Coord getIntersectionPointX(Line2D lineA, double x) 
	{
	if(walker.forceStopPF)return new Coord();
            double x1 = lineA.getX1();
            double y1 = lineA.getY1();
            double x2 = lineA.getX2();
            double y2 = lineA.getY2();
			
			if(y1==y2) return new Coord((int)x,(int)y1);
			
            Coord p = null;

            double a = (y2-y1)/(x2-x1);
                double b=-a*x2+y2;
                double y=a*x+b;

                p = new Coord((int)x, (int)y);
				
            return p;
        }
	
	Coord getIntersectionPointY(Line2D lineA, double y) 
	{
	if(walker.forceStopPF)return new Coord();
            double x1 = lineA.getX1();
            double y1 = lineA.getY1();
            double x2 = lineA.getX2();
            double y2 = lineA.getY2();
			
			if(x1==x2) return new Coord((int)x1,(int)y);
			
            Coord p = null;

            double a = (y2-y1)/(x2-x1);
			double b=-a*x2+y2;
			double x=y/a-b/a;

                p = new Coord((int)x, (int)y);
				
            return p;
       }

}