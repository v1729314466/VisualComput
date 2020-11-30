// Skeletal program for the "Image Threshold" assignment
// Written by:  Minglun Gong

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class ImageThreshold extends Frame implements ActionListener {
	BufferedImage input;
	int width, height;
	TextField texThres, texOffset;
	ImageCanvas source, target;
	PlotCanvas2 plot;
	// Constructor
	public ImageThreshold(String name) {
		super("Image Histred1am");
		// load image
		try {
			input = ImageIO.read(new File(name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		plot = new PlotCanvas2(256, 200);
		target = new ImageCanvas(width, height);
		//target.copyImage(input);
        target.resetImage(input);
		main.setLayout(new GridLayout(1, 3, 10, 10));
		main.add(source);
		main.add(plot);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		controls.add(new Label("Threshold:"));
		texThres = new TextField("128", 2);
		controls.add(texThres);
		Button button = new Button("Manual Selection");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Automatic Selection");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Otsu's Method");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Offset:"));
		texOffset = new TextField("10", 2);
		controls.add(texOffset);
		button = new Button("Adaptive Mean-C");
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
		// example -- compute the average color for the image
		if ( ((Button)e.getSource()).getLabel().equals("Manual Selection") ) {
			BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			int threshold = Integer.parseInt(texThres.getText());
			int[][] imageMatrix = new int[height][width];
			int[][] red = new int[height][width];
			int[][] green = new int[height][width];
			int[][] blue = new int[height][width];

			for(int y=0; y<height; y++){
				for(int x=0; x<width; x++){
					if (new Color(source.image.getRGB(y, x)).getRed() < threshold){
						red[y][x] = 0;
					}
					if (new Color(source.image.getRGB(y, x)).getRed() >= threshold){
						red[y][x] = 255;
					}
					if (new Color(source.image.getRGB(y, x)).getGreen() < threshold){
						green[y][x] = 0;
					}
					if (new Color(source.image.getRGB(y, x)).getGreen() >= threshold){
						green[y][x] = 255;
					}
					if (new Color(source.image.getRGB(y, x)).getBlue() < threshold){
						blue[y][x] = 0;
					}
					if (new Color(source.image.getRGB(y, x)).getBlue() >= threshold){
						blue[y][x] = 255;
					}
					imageMatrix[y][x] = red[y][x]<<16 | green[y][x]<<8 | blue[y][x];
				}
			}
			// Create the threshold line
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.BLACK, threshold, 150));

			int[] redVal = new int[256];
		    int[] greVal = new int[256];
		    int[] bluVal = new int[256];

	        for(int x=0; x<input.getWidth(); x++) {
	            for(int y=0; y<input.getHeight(); y++) {
					int clrR = new Color(source.image.getRGB(x, y)).getRed();
					int clrG = new Color(source.image.getRGB(x, y)).getGreen();
					int clrB = new Color(source.image.getRGB(x, y)).getBlue();
	                redVal[clrR]++;
	                greVal[clrG]++;
	                bluVal[clrB]++;
	            }
			}

			for (int hisVal=0; hisVal<256; hisVal++){
				plot.addObject(new VerticalBar(Color.RED, hisVal, redVal[hisVal]/8));
	    		plot.addObject(new VerticalBar(Color.GREEN, hisVal, greVal[hisVal]/8));
				plot.addObject(new VerticalBar(Color.BLUE, hisVal, bluVal[hisVal]/8));
			}
			// get output image
			for (int y=0; y<height; y++){
				for (int x=0; x<width; x++){
					output.setRGB(y, x, imageMatrix[y][x]);
				}
			}
			target.resetImage(output);
		}
		if ( ((Button)e.getSource()).getLabel().equals("Automatic Selection") ) {
			BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			int[][] ImageMatrix = new int[height][width];
			int[][] red = new int[height][width];
			int[][] green = new int[height][width];
			int[][] blue = new int[height][width];
			int threshR = 128;
			int threshG = 128;
			int threshB = 128;
			threshR = thresholdSelection(threshR, "red");
			threshG = thresholdSelection(threshG, "green");
			threshB = thresholdSelection(threshB, "blue");

			// apply threshold to the image
			for (int y=0; y<height; y++){
				for(int x=0; x<width; x++){
					if (new Color(source.image.getRGB(y, x)).getRed() < threshR){
						red[y][x] = 0; 
					}
					if (new Color(source.image.getRGB(y, x)).getRed() >= threshR){
						red[y][x] = 255; 
					}
					if (new Color(source.image.getRGB(y, x)).getGreen() < threshG){
						green[y][x] = 0; 
					}
					if (new Color(source.image.getRGB(y, x)).getGreen() >= threshG){
						green[y][x] = 255; 
					}
					if (new Color(source.image.getRGB(y, x)).getBlue() < threshB){
						blue[y][x] = 0; 
					}
					if (new Color(source.image.getRGB(y, x)).getBlue() >= threshB){
						blue[y][x] = 255; 
					}
					ImageMatrix[y][x] = red[y][x] << 16 | green[y][x] << 8 | blue[y][x];
				}
			}
			//Create the threshold line
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.RED, threshR, 150));
			plot.addObject(new VerticalBar(Color.GREEN, threshG, 150));
			plot.addObject(new VerticalBar(Color.BLUE, threshB, 150));

			int[] redVal = new int[256];
		    int[] greVal = new int[256];
		    int[] bluVal = new int[256];

	        for(int x=0; x<input.getWidth(); x++) {
	            for(int y=0; y<input.getHeight(); y++) {
					int clrR = new Color(source.image.getRGB(x, y)).getRed();
					int clrG = new Color(source.image.getRGB(x, y)).getGreen();
					int clrB = new Color(source.image.getRGB(x, y)).getBlue();
	                redVal[clrR]++;
	                greVal[clrG]++;
	                bluVal[clrB]++;
	            }
			}
	
			for (int hisVal=0; hisVal<255; hisVal++){
				plot.addObject(new VerticalBar(Color.RED, hisVal, redVal[hisVal]/8));
	    		plot.addObject(new VerticalBar(Color.GREEN, hisVal, greVal[hisVal]/8));
				plot.addObject(new VerticalBar(Color.BLUE, hisVal, bluVal[hisVal]/8));
			}


			for (int y=0; y<height; y++){
				for(int x=0; x<width; x++){
					output.setRGB(y, x, ImageMatrix[y][x]);
				}
			}
			target.resetImage(output);
		}
		
		if ( ((Button)e.getSource()).getLabel().equals("Otsu's Method") ) {
			int[] histS = new int[256];
			int[] histR = new int[256];
			int[] histG = new int[256];
			int[] histB = new int[256];

			BufferedImage output = new BufferedImage(width, height, input.getType());

			//get rgb 
			for (int y=0; y<height; y++)
            {
				for (int x=0; x<width; x++)
                {
					histR[new Color(source.image.getRGB(x, y)).getRed()]++;
					histG[new Color(source.image.getRGB(x, y)).getGreen()]++;
					histB[new Color(source.image.getRGB(x, y)).getBlue()]++;

					int rgb = input.getRGB(x, y);

					int clrR = (rgb >> 16) & 0xFF;
					int clrG = (rgb >> 8) & 0xFF;
					int clrB = (rgb & 0xFF);

					int gray= input.getRGB(x, y)& 0xFF;
					histS[gray]++;
				}
			}

			//get threshold values for each color channels
			int threshR = OtusThreshold(histR);
			int threshG = OtusThreshold(histG);
			int threshB = OtusThreshold(histB);
			System.out.println(threshR);
			System.out.println(threshG);
			System.out.println(threshB);

			int clrR, clrG, clrB, val;
			int valR = 255;
			int valG = 255;
			int valB = 255;
			for(int x=0; x<width; x++) {
				for(int y=0; y<height; y++) {
					clrR = new Color(source.image.getRGB(x, y)).getRed();
					clrG = new Color(source.image.getRGB(x, y)).getGreen();
					clrB = new Color(source.image.getRGB(x, y)).getBlue();
					int alpha = new Color(input.getRGB(x, y)).getAlpha();
					if(clrR <= threshR) {
						valR = 0;
					}
					else{
						valR = 255;
					}
					if(clrG <= threshG) {
						valG = 0;
					}
					else{
						valG = 255;
					}
					if(clrB <= threshB) {
						valB = 0;
					}
					else{
						valB = 255;
					}
					val = GetRGB(alpha, valR, valG, valB);
					output.setRGB(x, y, val);
				}
			}

			plot.clearObjects();
			for (int hisVal=0; hisVal<255; hisVal++){
				plot.addObject(new VerticalBar(Color.RED, hisVal, histR[hisVal]/10));
	    			plot.addObject(new VerticalBar(Color.GREEN, hisVal, histG[hisVal]/10));
				plot.addObject(new VerticalBar(Color.BLUE, hisVal, histB[hisVal]/10));
			}
			plot.addObject(new VerticalBar(Color.RED, threshR, 150));
			plot.addObject(new VerticalBar(Color.GREEN, threshG, 150));
			plot.addObject(new VerticalBar(Color.BLUE, threshB, 150));

			target.resetImage(output);
		}

		if (((Button)e.getSource()).getLabel().equals("Adaptive Mean-C")){
			BufferedImage new_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

			int[][] red = new int[width][height];
			int[][] green = new int[width][height];
			int[][] blue = new int[width][height];

			int[] valR = new int[256];
			int[] valG = new int[256];
			int[] valB = new int[256];

			String val = texOffset.getText();
			int constantC = Integer.valueOf(val);

			for (int y = 0; y < width; y++) {
				for (int x = 0; x < height; x++) {
					red[x][y] = new Color(source.image.getRGB(x, y)).getRed();
					green[x][y] = new Color(source.image.getRGB(x, y)).getGreen();
					blue[x][y] = new Color(source.image.getRGB(x, y)).getBlue();

					valR[new Color(source.image.getRGB(x, y)).getRed()]++;
					valG[new Color(source.image.getRGB(x, y)).getGreen()]++;
					valB[new Color(source.image.getRGB(x, y)).getBlue()]++;
				}
			}

			for (int y = 0; y < width; y++) {
				for (int x = 0; x < height; x++) {
					int rVal = 0;
					int gVal = 0;
					int bVal = 0;

					int red1 = new Color(source.image.getRGB(x, y)).getRed();
					int green1 = new Color(source.image.getRGB(x, y)).getGreen();
					int blue1 = new Color(source.image.getRGB(x, y)).getBlue();

					int[][] kernalR = new int[7][7];
					int[][] kernalG = new int[7][7];
					int[][] kernalB = new int[7][7];

					for (int j = 0; j < 7; j++){
						for (int i = 0; i < 7; i++){
							int jVal = x - 3 + j;
							int iVal = y - 3 + i;

							if(jVal < 0){
								jVal = 0;
							}
							else if(jVal >= width){
								jVal = width-1;
							}

							if(iVal < 0){
								iVal = 0;
							}
							else if(iVal >= height){
								iVal = height-1;
							}

							kernalR[j][i] = red[jVal][iVal];
							kernalG[j][i] = green[jVal][iVal];
							kernalB[j][i] = blue[jVal][iVal];

							rVal += red[jVal][iVal];
							gVal += green[jVal][iVal];
							bVal += blue[jVal][iVal];
						}
					}
					rVal /= 49;
					rVal -= constantC;
					gVal /= 49;
					gVal -= constantC;
					bVal /= 49;
					bVal -= constantC;
					// Red
					if(red1 >= rVal){
						red1 = 255;
					}
					else{
						red1 = 0;
					}
					// Green
					if(green1 >= gVal){
						green1 = 255;
					}
					else{
						green1 = 0;
					}
					// Blue
					if(blue1 >= bVal){
						blue1 = 255;
					}
					else{
						blue1 = 0;
					}
			new_img.setRGB(x, y, red1 << 16 | green1 << 8 | blue1);
			}
		}
plot.clearObjects();
	for(int x = 0; x < 256; x++){
		plot.addObject(new VerticalBar(Color.RED, x, valR[x]/20));
		plot.addObject(new VerticalBar(Color.GREEN, x, valG[x]/20));
		plot.addObject(new VerticalBar(Color.BLUE, x, valB[x]/20));
	}

	target.resetImage(new_img);
	}
}	
	
	public int thresholdSelection(int threshold, String color_name){
		int i = 0;
		int sumVal1 = 0;
		int sumVal2 = 0;
		int aveVal1 = 0;
		int aveVal2 = 0;
		int count1 = 0;
		int count2 = 0;
		while (Math.abs(threshold - i) > 0.01){
			i = threshold;
			for (int y=0; y<height; y++){
				for (int x=0; x<width; x++){
					if(color_name == "red"){
						if (new Color(source.image.getRGB(y, x)).getRed() < threshold){
							sumVal1 += new Color(source.image.getRGB(y, x)).getRed();
							count1++;
						}
						if (new Color(source.image.getRGB(y, x)).getRed() >= threshold){
							sumVal2 += new Color(source.image.getRGB(y, x)).getRed();
							count2++;
						}
					}
					if (color_name == "green"){
						if (new Color(source.image.getRGB(y, x)).getGreen() < threshold){
							sumVal1 += new Color(source.image.getRGB(y, x)).getGreen();
							count1++;
						}
						if (new Color(source.image.getRGB(y, x)).getGreen() >= threshold){
							sumVal2 += new Color(source.image.getRGB(y, x)).getGreen();
							count2++;
						}
					}
					if (color_name == "blue"){
						if (new Color(source.image.getRGB(y, x)).getBlue() < threshold){
							sumVal1 += new Color(source.image.getRGB(y, x)).getBlue();
							count1++;
						}
						if (new Color(source.image.getRGB(y, x)).getBlue() >= threshold){
							sumVal2 += new Color(source.image.getRGB(y, x)).getBlue();
							count2++;
						}
					}
				}
			}
			aveVal1 = (count1 > 0) ? (int)(sumVal1/count1):0;
			aveVal2 = (count2 > 0) ? (int)(sumVal2/count2):0;

			threshold = (int)(aveVal1 + aveVal2)/2;
		}
		return threshold;
	}

	public int OtusThreshold(int[] hist) {
		double maxVal = 0;
		int threshold = 0;
		for (int i=0; i<256; i++){
			double w0 = 0;
			double w1 = 0;
			double u0 = 0;
			double u1 = 0;
			double vars = 0;
			double u = 0;
			double u0_t = 0;
			double u1_t = 0;
			for(int y=0; y<256; y++){
				if (y < i){
					w0 += hist[y];
					u0_t += y*hist[y];
				}
				if (y >= i){
					w1 += hist[y];
					u1_t += y*hist[y];
				}
			}
			u0 = u0_t/w0;
			u1 = u1_t/w1;
			u = u0_t + u1_t;
			// calculate intra-variance
			vars = (w0*Math.pow(u0-u,2)+(w1*Math.pow(u1-u,2)));
			// get the minimum vars
			if (vars > maxVal){
				maxVal = vars;
				threshold = i;
			}
		}
		return threshold;
	}

	 public int GetRGB(int alpha, int red, int green, int blue) {

        int p = 0;
        p += alpha;
        p = p << 8;
        p += red; p = p << 8;
        p += green; p = p << 8;
        p += blue;

        return p;
	}
	public static void main(String[] args) {
		new ImageThreshold(args.length==1 ? args[0] : "baboon.png");
	}
}