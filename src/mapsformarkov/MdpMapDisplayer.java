package mapsformarkov;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
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
import obstaclemaps.MapDisplayer;
import obstaclemaps.MapDisplayerPanel;

public class MdpMapDisplayer extends JFrame {
	private MoveToGoalOnSlidingObstacleGridMDP mdp;
	private Policy<MDPMapState, MapAction> policyToDraw = null;
	private GeneralizedValueFunction<MDPMapState, Double> valueFunction = null;
	private MDPMapState startState = null;
	private int horizon = 0;
	private final MapDisplayedPanel panel;

	private MdpMapDisplayer(MoveToGoalOnSlidingObstacleGridMDP mdp) {
		this.mdp = mdp;
		panel = new MapDisplayedPanel();
		this.add(panel);

		repaint();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(panel.getTotalPanelWidth(),
				panel.getTotalPanelHeight()));
	}

	private class MapDisplayedPanel extends JPanel{
		private final MapDisplayerPanel rawMap = MapDisplayerPanel.newInstance();
		
		public void paint(Graphics g) {
			super.paint(g);
			rawMap.paint(g);
			
			paintPolicy(g);
			paintPath((Graphics2D)g);
			paintScale((Graphics2D)g);
		}


		public int getTotalPanelHeight() {
			return rawMap.getTotalPanelHeight();
		}


		public int getTotalPanelWidth() {
			return rawMap.getTotalPanelWidth()+100;
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
				throw new Error();
			}
		}



		private void paintPolicy(Graphics g) {
			if(policyToDraw==null)return;
			for(MDPMapState s:mdp.getAllStates())
			{
				if(s instanceof GoalReachedState)continue;

				PointState p = (PointState)s;

				int centerX = MapDisplayer.LEFTTILTPANEL+p.getPoint().x*10+5;
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



		
	}

	public static MdpMapDisplayer newInstance(MoveToGoalOnSlidingObstacleGridMDP mdp) {
		return new MdpMapDisplayer(mdp);
	}

	public void setPolicyToDraw(Policy<MDPMapState, MapAction> p) {
		this.policyToDraw = p;
		repaint();
	}

	public void setBackgroundColorToDraw(GeneralizedValueFunction<MDPMapState, Double> valuePerState) {
		double minVal = mdp.getAllStates()
				.parallelStream()
				.filter(x->! (x instanceof GoalReachedState))
				.map(x->(PointState)x)
				.filter(x->!mdp.getObstacles().contains(x.getPoint()))
				.map(x->valuePerState.apply(x))
				.min(Double::compare).get();
		valueFunction = valuePerState;
		
		panel.setColorPerTile(
		 x->
		{
			if(mdp.getObstacles().contains(x)) return Color.black;
			double val = valuePerState.apply(PointState.newInstance(x, mdp.getWidth()));
			float ratio = ((float) val/((float)-minVal)) +1f; 
			return new Color(ratio, ratio, 1-ratio);
		});

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

	public void setPolicyAndFullyUpdate(Policy<MDPMapState, MapAction> pol,
			PointState s0, int h,
			GeneralizedValueFunction<MDPMapState, Double> valueFunctionFor, String fileName) {
		this.setPolicyToDraw(pol);
		this.setBackgroundColorToDraw(valueFunctionFor);
		this.setDrawingPath(s0, h);

		this.setTitle(fileName);

		//Get Window Content Panel  
		Container content=this.getContentPane();  
		//Create Buffered Picture Object  
		BufferedImage img=new BufferedImage( this.getWidth(),this.getHeight(),BufferedImage.TYPE_INT_RGB);  
		//Get Graphic Objects  
		Graphics2D g2d = img.createGraphics();  
		//Output Window Content Panel to Graphic Object  
		content.printAll(g2d);  
		//Save as Picture  
		try{
			ImageIO.write(img, "jpg", new File("output/"+ fileName+".jpg"));
		}catch (IOException e){
			e.printStackTrace();
		}
		//Release graphic objects  
		g2d.dispose();  
	}
}
