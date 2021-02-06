package mapsformarkov;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import finitestatemachine.Action;
import finitestatemachine.State;
import markov.GeneralizedValueFunction;
import markov.MDP;
import markov.Policy;
import markov.impl.PairImpl;
import markov.impl.Policies;
import markov.impl.ValueFunctions;

public class MapDisplayer extends JFrame {
	private static final int TILEWIDTH = 10;
	private static final int TILEHEIGHT = 10;
	private static final int LEFTTILTPANEL = 1;
	
	private MoveToGoalOnSlidingObstacleGridMDP mdp;
	private Function<Point, Color> colorPerTile = (x)->Color.white;
	private Policy<MDPMapState, MapAction> policyToDraw = null;
	private GeneralizedValueFunction<MDPMapState, Double> valueFunction = null;
	private MDPMapState startState = null;
	private int horizon = 0;
	private final MapDisplayedPanel panel;
	
	private MapDisplayer(MoveToGoalOnSlidingObstacleGridMDP mdp) {
		this.mdp = mdp;
		panel = new MapDisplayedPanel();
		this.add(panel);
		
		repaint();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(panel.getTotalPanelWidth(),
				panel.getTotalPanelHeight()));
	}
	
	private class MapDisplayedPanel extends JPanel{
		public void paint(Graphics g) {
			super.paint(g);
			paintGrid(g);
			
			paintObstacles(g);
			paintPolicy(g);
			paintPath((Graphics2D)g);
			paintScale((Graphics2D)g);
		}
		
		
		public void exportToFile(String fileName) {
			BufferedImage bu = new BufferedImage(getTotalPanelWidth(), getTotalPanelHeight(), BufferedImage.TYPE_INT_ARGB);
			
			paint(bu.getGraphics());
			File outputfile = Paths.get(fileName).toFile();
			try {
				if(!outputfile.exists())
					outputfile.createNewFile();
				ImageIO.write(bu, "png", outputfile);
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error();
			}
		}

		private int getTotalPanelWidth() {
			return mdp.getWidth()*TILEWIDTH+TILEWIDTH+100;
		}
		
		private int getTotalPanelHeight() {
			return mdp.getHeight()*TILEHEIGHT+TILEHEIGHT+28;
		}

		private void paintScale(Graphics2D g) {
			
			int maxX = getTotalPanelWidth()-120;
			final int widthColorScale = 500;

			Rectangle drawingArea = new Rectangle(maxX+30, 10, 40, widthColorScale);
			if(valueFunction==null)
			{
				g.setColor(Color.black);
				g.fill(drawingArea);
				return;
			}
			
			double minVal = ValueFunctions.getWorseValue(mdp.getAllStates(),valueFunction);
			
			
			//int maxY = consideredPoints.stream().map(x->x.x).max(Integer::compare).get()*10;
		
			double fraction =  minVal/widthColorScale;
			for(int i = 0; i <= widthColorScale; i++)
			{
				double val = fraction*i;
				float ratio = ((float) val/((float)-minVal)) +1f; 
				g.setColor(new Color(ratio, ratio, 1-ratio));
				g.drawRect(maxX+30, i+10, 40, 1);
		
				g.setColor(Color.black);
				if(i%50==0) 
				{
					g.drawString("", 0, 0);
					String str = ""+(int)(fraction*i);
					g.drawString(str, maxX+80, i+15);
					g.drawLine(maxX+70, i+10, maxX+60, i+10);
				}
			}
		
			g.draw(drawingArea);
		}

		private void paintPath(Graphics2D g) {
			if(policyToDraw != null && startState != null)
			{
				List<PairImpl<MDPMapState, MapAction>> l = 
						Policies.getMostProbableTrajectoryFollowing(
								mdp,
								startState, horizon, policyToDraw);

				for(int i = 0 ; i < l.size() ; i++)
				{
					if(l.get(i).getLeft().equals(GoalReachedState.INSTANCE)) break;
					
					Point p = ((PointState)l.get(i).getLeft()).getPoint();
					int pX = LEFTTILTPANEL+p.x*10+5;
					int pY = p.y*10+5;
					int shiftX = 0;
					int shiftY = 0;
					if(l.get(i).getRight().equals(MapAction.EAST))
						shiftX = 10;
					if(l.get(i).getRight().equals(MapAction.WEST))
						shiftX = -10;
					if(l.get(i).getRight().equals(MapAction.SOUTH))
						shiftY = 10;
					if(l.get(i).getRight().equals(MapAction.NORTH))
						shiftY = -10;

					g.setStroke(new BasicStroke(3));
					g.drawLine(pX, pY, pX+shiftX, pY+shiftY);
				}
			}
		}

		private void paintObstacles(Graphics g) {
			for(int i = 0 ;  i < mdp.getWidth(); i++)
				for(int j = 0 ;  j < mdp.getHeight(); j++)
					if(mdp.getObstacles().contains(new Point(i, j)))
						paintTile(g,i,j,Color.black);
		}

		private void paintTile(Graphics g, int i, int j, Color c) {
			g.setColor(c);
			g.fillRect(LEFTTILTPANEL+i*TILEWIDTH, j*TILEHEIGHT, TILEWIDTH, TILEHEIGHT);
		}

		private void paintPolicy(Graphics g) {
			if(policyToDraw==null)return;
			for(MDPMapState s:mdp.getAllStates())
			{
				if(s instanceof GoalReachedState)continue;
				
				PointState p = (PointState)s;
				
				int centerX = LEFTTILTPANEL+p.getPoint().x*10+5;
				int centerY = p.getPoint().y*10+5;
				switch(policyToDraw.apply(s)) {
				case EAST: g.drawLine(centerX, centerY, centerX+5, centerY); break;
				case WEST: g.drawLine(centerX, centerY, centerX-5, centerY); break;
				case SOUTH: g.drawLine(centerX, centerY, centerX, centerY+5); break;
				case NORTH: g.drawLine(centerX, centerY, centerX, centerY-5); break;
				default:break;
				}
			}
			
		}

		private void paintGrid(Graphics g) {
			for(int i = 0 ;  i < mdp.getWidth(); i++)
				for(int j = 0 ;  j < mdp.getHeight(); j++)
				{
					paintTile(g, i, j, colorPerTile.apply(new Point(i, j)));
										
					g.setColor(Color.black);
					g.drawRect(LEFTTILTPANEL+i*TILEWIDTH, j*TILEHEIGHT, TILEWIDTH, TILEHEIGHT);
				}
		}
	}

	public static MapDisplayer newInstance(MoveToGoalOnSlidingObstacleGridMDP mdp) {
		return new MapDisplayer(mdp);
	}
	
	public void setPolicyToDraw(Policy<MDPMapState, MapAction> p) {
		this.policyToDraw = p;
		repaint();
	}

	public void setBackgroundColorToDraw(GeneralizedValueFunction<MDPMapState, Double> valuePerState) {
		double minVal = mdp.getAllStates()
					.stream()
					.filter(x->! (x instanceof GoalReachedState))
					.map(x->(PointState)x)
					.filter(x->!mdp.getObstacles().contains(x.getPoint()))
					.map(x->valuePerState.apply(x))
					.min(Double::compare).get();
		valueFunction = valuePerState;
		colorPerTile = x->
		{
			if(mdp.getObstacles().contains(x)) return Color.black;
			double val = valuePerState.apply(PointState.newInstance(x, mdp.getWidth()));
			float ratio = ((float) val/((float)-minVal)) +1f; 
			return new Color(ratio, ratio, 1-ratio);
		};
		
		this.repaint();

	}

	public void setDrawingPath(PointState start, int h) {
		this.startState = start;
		this.horizon = h;
		this.repaint();
	}

	public void exportToFile(String fileName) {
		panel.exportToFile(fileName);
	}

}
