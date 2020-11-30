import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

// Main class
public class HoughTransform extends Frame implements ActionListener {
	BufferedImage input;
	int width, height, diagonal;
	ImageCanvas source, target;
	TextField texRad, texThres;
	// Constructor
	public HoughTransform(String name) {
		super("Hough Transform");
		// load image
		try {
			input = ImageIO.read(new File(name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		diagonal = (int)Math.sqrt(width * width + height * height);
		// prepare the panel for two images.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		target = new ImageCanvas(input);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Line Transform");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Radius:"));
		texRad = new TextField("10", 3);
		controls.add(texRad);
		button = new Button("Circle Transform");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Threshold:"));
		texThres = new TextField("75", 3);
		controls.add(texThres);
		button = new Button("Search");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(diagonal*2+100, Math.max(height,360)+100);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener
	public void actionPerformed(ActionEvent e) {
		// perform one of the Hough transforms if the button is clicked.
		if ( ((Button)e.getSource()).getLabel().equals("Line Transform") ) {
			int[][] g = new int[diagonal][360];
			int threshold = Integer.parseInt(texThres.getText());
			int[][] ImageMatrix = new int[height][width];
			for ( int x = 0; x < width; x++ ){
				for ( int y = 0; y < height; y++ ){
					ImageMatrix[x][y] = (new Color(source.image.getRGB(x, y))).getRed()<<16 | (new Color(source.image.getRGB(x, y))).getGreen()<<8 | (new Color(source.image.getRGB(x, y))).getBlue();
				}
			}

			for ( int theta = 0; theta < 360; theta++ ){
				for ( int x = 0; x < width; x++ ){
					for( int y = 0; y < width; y++ ){
						if ( (ImageMatrix[y][x] & 0x0000ff ) < 255 ){
							int r = (int)(x*Math.cos(Math.toRadians(theta)) + y*Math.sin(Math.toRadians(theta)));
							if(r>0){
								g[r][theta]++;
							}
						}
					}
				}
			}


			BufferedImage imaged = null;
			try {
				imaged = ImageIO.read(new File("pantagon.png"));
			}
			catch ( Exception ex ) {
				ex.printStackTrace();
			}

			BufferedImage changeImage = new BufferedImage(imaged.getWidth(), imaged.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
			changeImage.getGraphics().drawImage(imaged, 0, 0, null);
			Graphics2D l = changeImage.createGraphics();
			l.drawImage(changeImage, 0, 0, null);
			l.setColor(Color.RED);
			
			for ( int theta = 0; theta < 360; theta++ ){
				for ( int r =0; r < diagonal; r++ ){
					if ( g[r][theta] > threshold ) {
						int x1 = 0;
						int y1 = (int)((r-x1)/Math.sin(Math.toRadians(theta)));
						int x2 =  width -1;
						int y2 = (int)((r-x2*Math.cos(Math.toRadians(theta)))/Math.sin(Math.toRadians(theta)));
						if ( y1 < 0 || y2 > height ){
							y1 = 0;
							y2 = height - 1;
							x1 = (int)((r-y1)/Math.cos(Math.toRadians(theta)));
							x2 = (int)((r-y2*Math.sin(Math.toRadians(theta)))/Math.cos(Math.toRadians(theta)));;
						}
						l.drawLine(x1,y1,x2,y2);
					}
				}
			}
			source.resetImage(changeImage);
			g = HouTransform(ImageMatrix, g);
			// insert your implementation for straight-line here.
			DisplayTransform(360, diagonal, g);

		}
		else if ( ((Button)e.getSource()).getLabel().equals("Circle Transform") ) {
			int[][] g = new int[height][width];
			int radius = Integer.parseInt(texRad.getText());
            int threshold = Integer.parseInt(texThres.getText());
            int diameter = radius + radius;

            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    Color clr = new Color(source.image.getRGB(x, y));
                    float red = (float)clr.getRed()/255;
                    float green = (float)clr.getGreen()/255;
                    float blue = (float)clr.getBlue()/255;

                    float max = Math.max(Math.max(red, green), blue);
                    float min = Math.min(Math.min(red, green), blue);

                    int i = Math.round((max + min) / 2 * 100);
                    if (i < 75){
                        for (float t = 0; t < Math.PI/4; t+=(Math.PI/(float)diameter)){
                            int w = (int)Math.round((float)radius * Math.cos(t));
							int h = (int)Math.round((float)radius * Math.sin(t));
                            if(y+h < height && x-w > 0){
                                g[y+h][x-w]++;
                            }
                            if(y-h > 0 && x-w > 0){
                                g[y-h][x-w]++;
                            }
                            if(y-w > 0 && x-h > 0){
                                g[y-w][x-h]++;
                            }
                            if(y+w < height && x-h > 0){
                                g[y+w][x-h]++;
                            }
                            if(y+h < height && x+w < width){
                                g[y+h][x+w]++;
                            }
                            if(y-h >= 0 && x+w < width){
                                g[y-h][x+w]++;
                            }
                            if(y-w >= 0 && x+h < width){
                                g[y-w][x+h]++;
                            }
                            if(y+w < height && x+h < width){
                                g[y+w][x+h]++;
                            }
                            
                        }
                    }
                }
            }

            int a = 0;
            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    if(g[y][x] > a){
						a = g[y][x];
					} 
                }
            }

            float multVal = 100/ (float)a;

            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    g[y][x] = (int)Math.round((float)g[y][x] * multVal);
                }
            }
			// insert your implementation for circle here.
			DisplayTransform(width, height, g);
            PaintCircles(g, radius, threshold);
            source.repaint();
		}
	}

	public void PaintCircles(int[][] g, int r, int thres){
        int diameter = r * 4;
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                if(g[y][x] >= thres){
                    for (float t = 0; t < Math.PI/4; t+=(Math.PI/(float)diameter)){
                        int w = (int)Math.round((float)r * Math.cos(t));
						int h = (int)Math.round((float)r * Math.sin(t));
                        if(y+h < height && x-w > 0){
                            source.image.setRGB(y+h, x-w, Color.RED.getRGB());
                        }
                        if(y-h > 0 && x-w > 0){
                            source.image.setRGB(y-h,x-w, Color.RED.getRGB());
                        }
                        if(y-w > 0 && x-h > 0){
                            source.image.setRGB(y-w, x-h, Color.RED.getRGB());
                        }
                        if(y+w < height && x-h > 0){
                            source.image.setRGB(y+w, x-h, Color.RED.getRGB());
                        }
                        if(y+h < height && x+w < width){
                            source.image.setRGB(y+h,x+w,Color.RED.getRGB());
                        }
                        if(y-h >= 0 && x+w < width){
                            source.image.setRGB(y-h,x+w,Color.RED.getRGB());
                        }
                        if(y-w >= 0 && x+h < width){
                            source.image.setRGB(y-w,x+h, Color.RED.getRGB());
                        }
                        if(y+w < height && x+h < width){
                            source.image.setRGB(y+w, x+h, Color.RED.getRGB());
                        }
                        
                    }
                }
            }
        }

	}
	
	// display the spectrum of the transform.
	public void DisplayTransform(int wid, int hgt, int[][] g) {
		target.resetBuffer(wid, hgt);
		for ( int y=0, i=0 ; y<hgt ; y++ )
			for ( int x=0 ; x<wid ; x++, i++ )
			{
				int value = g[y][x] > 255 ? 255 : g[y][x];
				target.image.setRGB(x, y, new Color(value, value, value).getRGB());
			}
		target.repaint();
	}

	public int[][] HouTransform(int[][] ImageMatrix, int[][] g){
		int x_center = width/2;
		int y_center = height/2;
		int maxTheta = 360;
		double thetastep = Math.PI/360;
		for ( int theta = 0; theta < maxTheta; theta++ ){
			for ( int x = 0; x < width; x++ ){
				for( int y = 0; y < width; y++ ){
					if ( (ImageMatrix[x][y] & 0x0000ff ) < 255 ){
						int r = (int)Math.round((x-x_center)*Math.cos(theta*thetastep)+((y-y_center)*Math.sin(theta*thetastep)));
						int rscaled = (int)Math.round(r+diagonal/2);
						if ( rscaled < 0) {
							rscaled = Math.abs(rscaled);
						}
						g[rscaled][theta]+=2;
					}
				}
			}
		}
		return g;
	}

	public static void main(String[] args) {
		new HoughTransform(args.length==1 ? args[0] : "pantagon.png");
	}
}