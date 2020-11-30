import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class ImageHistogram extends Frame implements ActionListener {
	BufferedImage input;
	BufferedImage output;
	int width, height;
	TextField texRad, texThres;
	ImageCanvas source, target;
	PlotCanvas plot;
	
	// Constructor
	public ImageHistogram(String name) {
		super("Image Histogram");
		// load image
		try {
			input = ImageIO.read(new File( name ));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		plot = new PlotCanvas();
		output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		target = new ImageCanvas(output);
		main.setLayout(new GridLayout(1, 3, 10, 10));
		main.add(source);
		main.add(plot);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Display Histogram");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Histogram Stretch");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Cutoff fraction:"));
		texThres = new TextField("10", 2);
		controls.add(texThres);
		button = new Button("Aggressive Stretch");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Histogram Equalization");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+400, height+100);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
		
		//Display Histogram.
		if ( ((Button)e.getSource()).getLabel().equals("Display Histogram") ) {
			int[] rHist = new int[257];
			int[] gHist = new int[257];
			int[] bHist = new int[257];
			int[][] Rmatrix = new int[height][width];
			int[][] Gmatrix = new int[height][width];
			int[][] Bmatrix = new int[height][width];
			for ( int y = 0; y < height; y++ ) {
				for ( int x = 0; x < width; x++ ){
					Color clr = new Color(input.getRGB(x,  y));
					int r = clr.getRed();
					int g = clr.getGreen();
					int b = clr.getBlue();
					Rmatrix[y][x] = r;
					Gmatrix[y][x] = g;
					Bmatrix[y][x] = b;
					rHist[r]++;
					gHist[g]++;
					bHist[b]++;
				}
			}
			plot.setHistogram(rHist, gHist, bHist);
			int[][] rgb = new int [height][width];
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					rgb[y][x] = Rmatrix[y][x]<<16 | Gmatrix[y][x]<<8 | Bmatrix[y][x];
				}
			}	
			for ( int x = 0; x < height; x++){
				for ( int y = 0; y < width; y++ ){
					output.setRGB(x, y, rgb[y][x]);
				}
			}
			target.resetImage(output);
		}
		
		//Histogram Stretch.
		if ( ((Button)e.getSource()).getLabel().equals("Histogram Stretch") ) {
			int rmin = 0, gmin = 0, bmin = 0;
			int gray_level = 255;
			int rmax = gray_level - 1, gmax = gray_level - 1, bmax = gray_level - 1;
			int[][] r_matrix = new int [height][width];
			int[][] g_matrix = new int [height][width];
			int[][] b_matrix = new int [height][width];
			int[]  output_red= new int[257];
			int[]  output_green= new int[257];
			int[]  output_blue= new int[257];
			int[] Hist_red = new int[257];
			int[] Hist_green = new int[257];
			int[] Hist_blue = new int[257];
			for ( int y = 0; y < height; y++ ) {
				for ( int x = 0; x < width; x++ ){
					Color clr = new Color(input.getRGB(x,  y));
					int r2 = clr.getRed();
					int g2 = clr.getGreen();
					int b2 = clr.getBlue();
					r_matrix[y][x] = r2; 
					g_matrix[y][x] = g2;
					b_matrix[y][x] = b2;
					Hist_red[r2]++;
					Hist_green[g2]++;
					Hist_blue[b2]++;
				}
			}
			//calculate histogram stretch
			while ( Hist_red[rmin] == 0) { rmin ++; }
			while ( Hist_red[rmax] == 0) { rmax --; }
			while ( Hist_green[gmin] == 0) { gmin ++; }
			while ( Hist_green[gmax] == 0) { gmax --; }
			while ( Hist_blue[bmin] == 0) { bmin ++; }
			while ( Hist_blue[bmax] == 0) { bmax --; }
			for ( int y = 0; y < height; y++ ) {
				for ( int x = 0; x < width; x++ ){
					r_matrix[y][x] = (r_matrix[y][x]-rmin)*gray_level/(rmax-rmin); 
					g_matrix[y][x] = (g_matrix[y][x]-gmin)*gray_level/(gmax-gmin);
					b_matrix[y][x] = (b_matrix[y][x]-bmin)*gray_level/(bmax-bmin);
				}
			}
			//convert pixel 2D array into 1D array.
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					output_red[r_matrix[y][x]]++;
					output_green[g_matrix[y][x]]++;
					output_blue[b_matrix[y][x]]++;
				}
			}
			int[][] rgb = new int [height][width];
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					rgb[y][x] = r_matrix[y][x]<<16 | g_matrix[y][x]<<8 | b_matrix[y][x];
				}
			}	
			for ( int x = 0; x < height; x++){
				for ( int y = 0; y < width; y++ ){
					output.setRGB(x, y, rgb[y][x]);
				}
			}
			plot.setHistogram(output_red, output_green, output_blue);
			target.resetImage(output);
		}
		
		//Aggressive Stretch
		if ( ((Button)e.getSource()).getLabel().equals("Aggressive Stretch") ) {
			int Gray_level = 255;
			int[] red = new int[257];
			int[] green = new int[257];
			int[] blue = new int[257];
			int[] out_red = new int[257];
			int[] out_green = new int[257];
			int[] out_blue = new int[257];
			for ( int y = 0; y < height; y++ ) {
				for ( int x = 0; x < width; x++ ){
					int r3 = new Color(input.getRGB(x,  y)).getRed();
					int g3 = new Color(input.getRGB(x,  y)).getGreen();
					int b3 = new Color(input.getRGB(x,  y)).getBlue();
					red[r3]++;
					green[g3]++;
					blue[b3]++;
				}
			}
			int rmin1 = (int)Integer.parseInt(texThres.getText())*255/100, gmin1 = (int)Integer.parseInt(texThres.getText())*255/100, bmin1 = (int)Integer.parseInt(texThres.getText())*255/100;
			int rmax1 = 255-rmin1, gmax1 = 255-gmin1, bmax1 = 255-bmin1;
			while( red[rmin1] == 0 ) { rmin1++; if ( rmin1 == 255 ){ break;} }
			while( red[rmax1] == 0 ) { rmax1--; if ( rmax1 == 0 ){ break;}}
			while( green[gmin1] == 0 ) { gmin1++; if ( gmin1 == 255 ){ break;}}
			while( green[gmax1] == 0 ) { gmax1--; if ( gmax1 == 0 ){ break;}}
			while( blue[bmin1] == 0 ) { bmin1++; if ( bmin1 == 255 ){ break;}}
			while( blue[bmax1] == 0 ) { bmax1--; if ( bmax1 == 0 ){ break;}}
			for ( int y = 0; y < height; y++ ) {
				for ( int x = 0; x < width; x++ ){
					int r5 = (new Color(input.getRGB(y, x))).getRed();
					int g5 = (new Color(input.getRGB(y, x))).getGreen();
					int b5 = (new Color(input.getRGB(y, x))).getBlue();
					output.setRGB(y, x, (r5-rmin1)*Gray_level/(rmax1-rmin1)<<16 | (g5-bmin1)*Gray_level/(gmax1-gmin1)<<8 | (b5-bmin1)*Gray_level/(bmax1-bmin1));
				}
			}
			for ( int y = 0; y < height; y++ ) {
				for ( int x = 0; x < width; x++ ){
					int r6 = ((new Color(input.getRGB(y, x))).getRed()-rmin1)*Gray_level/(rmax1-rmin1);
					int g6 = ((new Color(input.getRGB(y, x))).getGreen()-gmin1)*Gray_level/(gmax1-gmin1);
					int b6 = ((new Color(input.getRGB(y, x))).getBlue()-bmin1)*Gray_level/(bmax1-bmin1);
					r6 = clamp(r6, 0, 255);
					g6 = clamp(g6, 0, 255);
					b6 = clamp(b6, 0, 255);
					out_red[r6]++;
					out_green[g6]++;
					out_blue[b6]++;
				}
			}
			plot.setHistogram(out_red, out_green, out_blue);
			target.resetImage(output);

		}
			
		//Image equalization
		if ( ((Button)e.getSource()).getLabel().equals("Histogram Equalization") ) {
			int[] rHist1 = new int[257];
			int[] gHist1 = new int[257];
			int[] bHist1 = new int[257];
			float[] rHist_percent = new float[257];
			float[] gHist_percent = new float[257];
			float[] bHist_percent = new float[257];
			float[] rchist = new float [257];
			float[] gchist = new float [257];
			float[] bchist = new float [257];
			int[] rout = new int[257];
			int[] gout = new int[257];
			int[] bout = new int[257];
			float[][] rmatrix = new float[height][width];
			float[][] gmatrix = new float[height][width];
			float[][] bmatrix = new float[height][width];
			for ( int y = 0; y < input.getHeight(); y++ ){
				for ( int x = 0; x < input.getWidth(); x++ ){
					Color clr = new Color(input.getRGB(x,  y));
					int r4 = clr.getRed();
					int g4 = clr.getGreen();
					int b4 = clr.getBlue();
					rmatrix[y][x] = r4;
					gmatrix[y][x] = g4;
					bmatrix[y][x] = b4;
					rHist1[r4]++;
					gHist1[r4]++;
					bHist1[b4]++;
				}
			}
			for( int i = 0; i < 257; i++ ){
				rHist_percent[i] = (float) rHist1[i]/(height*width);
				gHist_percent[i] = (float) gHist1[i]/(height*width);
				bHist_percent[i] = (float) bHist1[i]/(height*width);
			}
			
			rchist[0] = rHist_percent[0];
			gchist[0] = gHist_percent[0];
			bchist[0] = bHist_percent[0];
			for ( int i = 1; i < 257; i++ ){
				rchist[i] = rchist[i-1] + rHist_percent[i];
				gchist[i] = gchist[i-1] + gHist_percent[i];
				bchist[i] = bchist[i-1] + bHist_percent[i];
			}
			for ( int y = 0; y < height; y++ ){
				for ( int x = 0; x < width; x++ ){
					rmatrix[y][x] = rchist[(int)rmatrix[y][x]]*255;
					gmatrix[y][x] = gchist[(int)gmatrix[y][x]]*255;
					bmatrix[y][x] = bchist[(int)bmatrix[y][x]]*255;
				}
			}
			for ( int y = 0; y < height; y++ ){
				for ( int x = 0; x < width; x++ ){
					rout[(int)rmatrix[y][x]]++;
					gout[(int)gmatrix[y][x]]++;
					bout[(int)bmatrix[y][x]]++;
				}
			}
			
			plot.setHistogram(rout, gout, bout);
			int[][] rgb = new int [height][width];
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					rgb[y][x] = (int)rmatrix[y][x]<<16 | (int)gmatrix[y][x]<<8 | (int)bmatrix[y][x];
				}
			}
			for ( int x = 0; x < height; x++){
				for ( int y = 0; y < width; y++ ){
					output.setRGB(x, y, rgb[y][x]);
				}
			}
			target.resetImage(output);
		}
		
		
	}
	
	public int clamp(int val, int min, int max){
		if ( val > max ){ return max; }
		if ( val < min ){ return min; }
		return val;
	}
	
	public static void main(String[] args) {
		new ImageHistogram(args.length==1 ? args[0] : "lena.png");
	}
	
}

// Canvas for plotting histogram
class PlotCanvas extends Canvas {
	// lines for plotting axes and mean color locations
	LineSegment x_axis, y_axis;
	LineSegment red, green, blue, line;
	LineSegment[] rarray = new LineSegment [256];
	LineSegment[] garray = new LineSegment [256];
	LineSegment[] barray = new LineSegment [256];
	
	boolean showMean = false;
	boolean display = false, displayStretch = false;
	

	public PlotCanvas() {
		x_axis = new LineSegment(Color.BLACK, -10, 0, 256+10, 0);
		y_axis = new LineSegment(Color.BLACK, 0, -10, 0, 200+10);
	}
	
	//Set up the lines of the histogram
	public void setHistogram( int[] red, int[] green, int[] blue) {
		display = true;
		for ( int i = 0; i < red.length-1; i++ ) {
			double init = red[i]/5;
			int init_int = (int) init;
			double next = red[i+1]/5;
			int next_int = (int) next;
			rarray[i] = new LineSegment(Color.RED, i, init_int, i+1, next_int);
		}
		for ( int j = 0; j < green.length-1; j++ ) {
			double init = green[j]/5;
			int init_int = (int) init;
			double next = green[j+1]/5;
			int next_int = (int) next;
			garray[j] = new LineSegment(Color.GREEN, j, init_int, j+1, next_int);
		}
		for ( int k = 0; k < blue.length-1; k++ ) {
			double init = blue[k]/5;
			int init_int = (int) init;
			double next = blue[k+1]/5;
			int next_int = (int) next;
			barray[k] = new LineSegment(Color.BLUE, k, init_int, k+1, next_int);
		}	
		repaint();
	}
	
	
	// redraw the canvas
	public void paint(Graphics g) {
		// draw axis
		int xoffset = (getWidth() - 256) / 2;
		int yoffset = (getHeight() - 200) / 2;
		x_axis.draw(g, xoffset, yoffset, getHeight());
		y_axis.draw(g, xoffset, yoffset, getHeight());
		
		/**if ( showMean ) {
			red.draw(g, xoffset, yoffset, getHeight());
			green.draw(g, xoffset, yoffset, getHeight());
			blue.draw(g, xoffset, yoffset, getHeight());
		}*/
		
		if ( display ) {
			for ( int i = 0; i < rarray.length; i++ ){
				rarray[i].draw(g, xoffset, yoffset, getHeight());
			}
			for ( int i = 0; i < garray.length; i++ ){
				garray[i].draw(g, xoffset, yoffset, getHeight());
			}
			for ( int i = 0; i < barray.length; i++ ){
				barray[i].draw(g, xoffset, yoffset, getHeight());
			}
		}

	}

}
	

// LineSegment class defines line segments to be plotted
class LineSegment {
	// location and color of the line segment
	int x0, y0, x1, y1;
	Color color;
	// Constructor
	public LineSegment(Color clr, int x0, int y0, int x1, int y1) {
		color = clr;
		this.x0 = x0; this.x1 = x1;
		this.y0 = y0; this.y1 = y1;
	}
	public void draw(Graphics g, int xoffset, int yoffset, int height) {
		g.setColor(color);
		g.drawLine(x0+xoffset, height-y0-yoffset, x1+xoffset, height-y1-yoffset);
	}
}
