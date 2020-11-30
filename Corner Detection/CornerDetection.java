import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JFrame;


// Main class
public class CornerDetection extends Frame implements ActionListener {
	BufferedImage input;
	int width, height;
	double sensitivity=.1;
    int threshold=20;
    ArrayList<Integer> x_points;
    ArrayList<Integer> y_points;
	ImageCanvas source, target;
	CheckboxGroup metrics = new CheckboxGroup();

	// Constructor
	public CornerDetection(String name) {
		super("Corner Detection");
		// load image
		try {
			input = ImageIO.read(new File(name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();

		// set up array and list
		Image = new double[height][width];
		x_points = new ArrayList<>();
		y_points = new ArrayList<>();

		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		target = new ImageCanvas(width, height);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);

		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Derivatives");
		button.addActionListener(this);
		controls.add(button);

		// Use a slider to change sensitivity
		JLabel label1 = new JLabel("sensitivity=" + sensitivity);
		controls.add(label1);
		JSlider slider1 = new JSlider(1, 25, (int)(sensitivity*100));
		slider1.setPreferredSize(new Dimension(50, 20));
		controls.add(slider1);
		slider1.addChangeListener(changeEvent -> {
			sensitivity = slider1.getValue() / 100.0;
			label1.setText("sensitivity=" + (int)(sensitivity*100)/100.0);
		});
		button = new Button("Corner Response");
		button.addActionListener(this);
		controls.add(button);
		JLabel label2 = new JLabel("threshold=" + threshold);
		controls.add(label2);
		JSlider slider2 = new JSlider(0, 100, threshold);
		slider2.setPreferredSize(new Dimension(50, 20));
		controls.add(slider2);
		slider2.addChangeListener(changeEvent -> {
			threshold = slider2.getValue();
			label2.setText("threshold=" + threshold);
		});
		button = new Button("Thresholding");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Non-max Suppression");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Display Corners");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(Math.max(width*2+100,850), height+110);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
    }
    
    
    
	double[][] Image;
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
        // generate Moravec corner detection result
        boolean ifImagechange = false;
		if ( ((Button)e.getSource()).getLabel().equals("Derivatives") ){
			derivatives();
		}

		if ( ((Button)e.getSource()).getLabel().equals("Non-max Suppression") ){
			BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			nonMaxSuppression();
			double r, g, b;
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					r = clamp(Image[y][x]);
					g = clamp(Image[y][x]);
					b = clamp(Image[y][x]);
					newImage.setRGB(y, x, new Color((int)r,(int)g,(int)b).getRGB());
				}
			}
			target.resetImage(newImage);
		}
		

		if ( ((Button)e.getSource()).getLabel().equals("Corner Response") ){
			BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			cornerResponse();
			
			double r, g, b;
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					r = clamp(Image[y][x]);
					g = clamp(Image[y][x]);
					b = clamp(Image[y][x]);
					newImage.setRGB(y, x, new Color((int)r,(int)g,(int)b).getRGB() );
				}
			}
			target.resetImage(newImage);
			ifImagechange = true;
		}
		else if(((Button)e.getSource()).getLabel().equals("Thresholding") ) {
			if(ifImagechange) {
			}
			
			int thres = threshold * (255/100);
			System.out.println(thres);
			BufferedImage newImage = new BufferedImage(width, height, target.image.getType());
			int r, g, b, value;
			int valR =0;
			int valG = 0;
			int valB = 0;
			for(int p=0; p<width; p++) {
				for(int q=0; q<height; q++) {
					//
					Color clr = new Color(target.image.getRGB(p,q));
					r = clr.getRed();
					g = clr.getGreen();
					b = clr.getBlue();
					int alpha = new Color(target.image.getRGB(p, q)).getAlpha();
					
					if(r > thres) {
						valR = 255;
					}
                    else{
                        valR = 0;
                    }
					if(g > thres) {
						valG = 255;
					}
                    else{
                        valG = 0;
                    }
					if(b > thres) {
						valB = 255;
					}
					else {
						valB = 0;
					}
					value = GetRGB(alpha, valR, valG, valG);
					newImage.setRGB(p, q, value);
				}
			}
			target.resetImage(newImage);
		}
		else if(((Button)e.getSource()).getLabel().equals("Display Corners") ) 
		{
			int r = 5; // radius of circle
			int r0 = 1; // radius of centre
			Graphics2D gr = input.createGraphics();
			gr.drawImage(input, 0, 0, null);
			gr.setClip(null);
			Stroke s = new BasicStroke(2);
			gr.setStroke(s);
			gr.setColor(new Color(255, 0, 0));
			Color red = new Color(255, 0, 0);
			for(int p=0; p<width; p++) {
				for(int q=0; q<height; q++) {
					Color clr = new Color(target.image.getRGB(p,q));
					int red2 = clr.getRed();
					if(red2 != 0) {
						int x = p - r/2;
						int y = q - r/2;
						//circle circumference
						gr.drawOval(x, y, r, r);
					}
				}
			}
			source.resetImage(input);
		}
    }
    
    void resVal(){
		double mean = findMean();
		double scale = 255/mean;
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
					Image[y][x] = (int)(Image[y][x]*0.01);
			}
		}
    }
    

	// find mean value for the image pixels
	double findMean(){
		double sum = 0;
		double mean = 0;
		double count = 0;
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				if ( Image[y][x] > 0){
					sum += Image[y][x];
					count++;
				}
			}
		}
		mean = sum/count;
		return mean;
	}

	
    
    void thresholding(){

		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				if ( Image[y][x] > threshold*5 ) {
					continue;
				}
				else {
					Image[y][x] = 0;
				}
			}
		}	
	}
	
	public int GetRGB(int alpha, int red, int green, int blue) {

        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red; newPixel = newPixel << 8;
        newPixel += green; newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;
    }

    void initializeImage(){
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				Image[y][x] = 0;
			}
		}
    }

    double clamp(double t){
		if ( t < 0 ){
			 t = 0; 
		}
		if ( t > 255 ){ 
			t = 255; 
		}
		return t;
	}

	void nonMaxSuppression(){
		int suppression = 3;
		int indexA = 0;
		int indexB = 0;
		
		for ( int y = suppression, maxY = height - y; y < maxY; y++ ){
			for ( int x = suppression, maxX = width - x; x < maxX; x++ ){
				double currentValue = Image[y][x];
				double max = Image[y][x];
				for ( int i = -suppression; (currentValue != 0) && (i <= suppression); i++ ){
					for ( int j = -suppression; j <= suppression; j++ ){
						
						if ( Image[y+i][x+j] < currentValue ){
							Image[y+i][x+j] = 0;
						}
						else if ( Image[y+i][x+j] > currentValue){
							indexA = x+j;
							indexB = y+i;
						}
					}
				}
				x_points.add(indexB);
				y_points.add(indexA);
			}
		}
	}

	// calculate three derivative products 
	void derivatives() {
        int pos = 2;
		int gray;
		int[] valA = new int[25];
		int[] valB = new int[25];
		int[] valC = new int[25];
		int dA = 0;
		int dB = 0;
		int dC = 0;
		double dX, dY, dXY;
		double dX2, dY2;

		// set up sample 2d guassian data at grid
		int[] gaussianX  = { -1, -2 , 0, 2 , 1, 
						     -4, -10, 0, 10, 4, 
						     -7, -17, 0, 17, 7,
						     -4, -10, 0, 10, 4,
						     -1, -2 , 0, 2 , 1 };
		
		int[] gaussianY = { -1, -4 , -7 , -4 , -1,
				            -2, -10, -17, -10, -2, 
				             0,  0 ,  0 ,  0 ,  0, 
				             2,  10,  17,  10,  2,
				             1,  4 ,  7 ,  4 ,  1 };
		
		int[] gaussianXY = { 1, 4 , 6 , 4 , 1,
							 4, 16, 24, 16, 1,
							 6, 24, 36, 24, 6, 
							 4, 16, 24, 16, 4,
							 1, 4 , 6 , 4 , 1 };
		
		
		
		for ( int q = pos; q < height - pos; q++ ){
			for ( int p = pos; p < width - pos; p++ ){
				int i = 0;
				dA = 0;
				dB = 0;
				dC = 0;
				for ( int v = -pos; v <= pos; v++ ){
					for ( int u = -pos; u <= pos; u++ ){
						Color clr = new Color(source.image.getRGB(q+v,p+u));
						gray = (clr.getRed() + clr.getGreen() + clr.getBlue())/3;
						valA[i] = gray*gaussianX[i];
						valB[i] = gray*gaussianY[i];
						valC[i] = gray*gaussianXY[i];
						i++;
					}
				}
				for ( int t = 0; t < gaussianX.length; t++ ){
					dA += valA[t];
				}
				for ( int t = 0; t < gaussianY.length; t++ ){
					dB += valB[t];
				}
				for ( int t = 0; t < gaussianXY.length; t++ ){
					dC += valC[t];
				}

				dX = dA/58;
				dX2 = dX*dX*0.05;

				if ( dX2 > 255 ) { 
					dX2 = 255; 
				}

				dY = dB/58;
				dY2 = dY*dY*0.05;

				if ( dY2 > 255 ) { 
					dY2 = 255; 
				}
				dXY = dX*dY*0.09;
				if ( dXY < 0 ) { 
					dXY = 0; 
				}
				if ( dXY > 255 ) { 
					dXY = 255; 
				}
				target.image.setRGB(q, p, new Color((int)dX2, (int)dY2, (int)dXY).getRGB());
			}
		}
		target.repaint();
	}

	

	// calculate corner response value
	void cornerResponse(){
        initializeImage();
        int pos = 2;
		double R;
		Color clr;
		double gray;
		double[] valA = new double[25];
		double[] valB = new double[25];
		double[] valC = new double[25];
		double dA = 0;
		double dB = 0;
		double dC = 0;
		double dX, dY, dXY;
		double dX2, dY2;

		int[] gaussianX  = { -1, -2 , 0, 2 , 1, 
			     			 -4, -10, 0, 10, 4, 
			     			 -7, -17, 0, 17, 7,
			     			 -4, -10, 0, 10, 4,
			     			 -1, -2 , 0, 2 , 1 };

		int[] gaussianY = { -1, -4 , -7 , -4 , -1,
	            			-2, -10, -17, -10, -2, 
	            			 0,  0 ,  0 ,  0 ,  0, 
	            			 2,  10,  17,  10,  2,
	            			 1,  4 ,  7 ,  4 ,  1 };

		int[] gaussianXY = { 1, 4 , 6 , 4 , 1,
				 			 4, 16, 24, 16, 1,
				 			 6, 24, 36, 24, 6, 
				 			 4, 16, 24, 16, 4,
				 			 1, 4 , 6 , 4 , 1 };

		double[] A = new double[4];
		
		
		
		for ( int q = pos; q < height - pos; q++ ){
			for ( int p = pos; p < width - pos; p++ ){
				int i = 0;
				dA = 0;
				dB = 0;
				dC = 0;
				for ( int v = -pos; v <= pos; v++ ){
					for ( int u = -pos; u <= pos; u++ ){
						clr = new Color(source.image.getRGB(q+v,p+u));
						gray = (clr.getRed() + clr.getGreen() + clr.getBlue())/3;
						valA[i] = gray*gaussianX[i];
						valB[i] = gray*gaussianY[i];
						valC[i] = gray*gaussianXY[i];
						i++;
					}
				}
				for ( int t = 0; t < gaussianX.length; t++ ){
					dA += valA[t];
				}
				for ( int t = 0; t < gaussianY.length; t++ ){
					dB += valB[t];
				}
				for ( int t = 0; t < gaussianXY.length; t++ ){
					dC += valC[t];
				}
				dX = (dA/58);
				dX2 = dX*dX;
				if ( dX2 > 255 ) { dX2 = 255; }
				dY = (dB/58);
                dY2 = dY*dY;
                
				if ( dY2 > 255 ) { dY2 = 255; }
                dXY = dC/256;
                
				if ( dXY < 0 ) { dXY = 0; }
				A[0] = dX2;
				A[1] = dXY;
				A[2] = dXY;
				A[3] = dY2;
				R = ((A[0]*A[3]-A[1]*A[2]) - sensitivity*Math.pow(A[0]+A[3], 2));
				if ( R < 0 ){ R = 0; }
				Image[q][p] = R;
			}
		}
		resVal();
	}

	
    
    public static void main(String[] args) {
		new CornerDetection(args.length==1 ? args[0] : "pepper.png");
	}

}