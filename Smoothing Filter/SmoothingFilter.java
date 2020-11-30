import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.Math;

import javax.imageio.*;

// Main class
public class SmoothingFilter extends Frame implements ActionListener {
	BufferedImage input;
	BufferedImage output;
	ImageCanvas source, target;
	TextField texSigma;
	int width, height;
	
	// Constructor
	public SmoothingFilter(String name) {
		super("Smoothing Filters");
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
		output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		target = new ImageCanvas(output);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Add noise");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 mean");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Sigma:"));
		texSigma = new TextField("1", 1);
		controls.add(texSigma);
		button = new Button("5x5 Gaussian");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 median");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 Kuwahara");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+100, height+100);
		setVisible(true);
	}
	
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
		// example -- add random noise
		if ( ((Button)e.getSource()).getLabel().equals("Add noise") ) {
			Random rand = new Random();
			int dev = 64;
			for ( int y=0, i=0 ; y<height ; y++ )
				for ( int x=0 ; x<width ; x++, i++ ) {
					Color clr = new Color(source.image.getRGB(x, y));
					int red = clr.getRed() + (int)(rand.nextGaussian() * dev);
					int green = clr.getGreen() + (int)(rand.nextGaussian() * dev);
					int blue = clr.getBlue() + (int)(rand.nextGaussian() * dev);
					red = red < 0 ? 0 : red > 255 ? 255 : red;
					green = green < 0 ? 0 : green > 255 ? 255 : green;
					blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
					source.image.setRGB(x, y, (new Color(red, green, blue)).getRGB());
				}
			source.repaint();
		}
		
		//median filter
		if ( ((Button)e.getSource()).getLabel().equals("5x5 median") ) {
			int[][] Image = new int[height][width];
			medianFilter(Image);
			//handleBoundary(Image);
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					output.setRGB(y, x, Image[y][x]);
				}
			}
			target.resetImage(output);
		}
		
		//mean filter
		if ( ((Button)e.getSource()).getLabel().equals("5x5 mean") ) {
			int[][] Image = new int[height][width];
			meanFilter(Image);
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					output.setRGB(y, x, Image[y][x]);
				}
			}
			target.resetImage(output);
		}
		
		//Gaussian filter
		if ( ((Button)e.getSource()).getLabel().equals("5x5 Gaussian") ) {
			int[][] Image = new int[height][width];
			GaussianFilter(Image);
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					output.setRGB(y, x, Image[y][x]);
				}
			}
			target.resetImage(output);
		}
		
		//Kuwahara Filter
		if ( ((Button)e.getSource()).getLabel().equals("5x5 Kuwahara") ) {
			int[][] Image = new int[height][width];
			KuwaharaFilter(Image);
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					output.setRGB(y, x, Image[y][x]);
				}
			}
			target.resetImage(output);
		}
	
	}
	
	//Gaussian filter
	public void GaussianFilter(int[][] ImageMatrix){
		int maskSize = 5;
		int pos = (maskSize-1)/2;
		double rsum, gsum, bsum;
		double sigma = Float.parseFloat(texSigma.getText());
		double[][] rmask = new double[maskSize][maskSize];
		double[][] gmask = new double[maskSize][maskSize];
		double[][] bmask = new double[maskSize][maskSize];
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				ImageMatrix[y][x] = (new Color(source.image.getRGB(y, x))).getRed()<<16 | (new Color(source.image.getRGB(y, x))).getGreen()<<8 | (new Color(source.image.getRGB(y, x))).getBlue();
			}
		}
		for ( int q = pos; q < height - pos; q++){
			for ( int p = pos; p < width - pos; p++){
				int i = 0;
				for ( int v = -pos; v <= pos; v++){
					int j = 0;
					for ( int u = -pos; u <= pos; u++){
						//int j = 0;
						rmask[i][j] = (new Color(source.image.getRGB(q+v, p+u))).getRed();
						//System.out.println(rmask[i][j]);
						gmask[i][j] = (new Color(source.image.getRGB(q+v, p+u))).getGreen();
						bmask[i][j] = (new Color(source.image.getRGB(q+v, p+u))).getBlue();
						j++;
					}
					i++;
				}
				GaussianFunction(rmask, sigma);
				GaussianFunction(gmask, sigma);
				GaussianFunction(bmask, sigma);
				rsum = clamp2(sumUp(rmask),0,255);
				//System.out.println(rsum);
				gsum = clamp2(sumUp(gmask),0,255);
				//System.out.println(gsum);
				bsum = clamp2(sumUp(bmask),0,255);
				//System.out.println(bsum);
				ImageMatrix[q][p] = (int)rsum<<16 | (int)gsum<<8 | (int)bsum;
			}
		}
		handleBoundary(ImageMatrix);
	}
	
	public double clamp2(double val, int a, int b){
		if ( val < a ) { val = a; }
		if ( val > b ) { val = b; }
		return val;
	}
	
	public void GaussianFunction(double[][] mask, double sigma){
		double[][] temp = new double[5][5];
		double sum = 0;
		double x = -2;
		for ( int i = 0; i < 5; i++ ){
			double y = -2;
			for ( int j = 0; j < 5; j++ ){
				temp[i][j] = calGaussian(x, y, sigma);
				sum += temp[i][j];
				y++;
				//System.out.println(temp[i][j]);
			}
			x++;
		}
		//System.out.println(sum);
		for ( int i = 0; i < 5; i++ ){
			for ( int j = 0; j < 5; j++){
				mask[i][j] = mask[i][j]*temp[i][j];
			}
		}
	}
	
	public double calGaussian(double x, double y, double sigma){
		double val;
		double x0 = Math.pow(x, 2);
		double y0 = Math.pow(y, 2);
		double a = 1.0/(2 * Math.PI * Math.pow(sigma, 2));
		double b = (- x0- y0)/(2.0 * Math.pow(sigma, 2));
		val = a * Math.pow(Math.E, b);
		//System.out.println(val);
		return val;
	}
	
	public double sumUp(double[][] mask) {
		double sum = 0;
		for ( int i = 0; i < 5; i++){
			for ( int j = 0; j < 5; j++){
				sum += mask[i][j];
			}
		}
		return sum;
	}
	
	//mean filter
	public void meanFilter(int[][] ImageMatrix){
		int maskSize = 5;
		int pos = (maskSize-1)/2;
		int rsum, gsum, bsum;
		int[][] red = new int[height][width];
		int[][] green = new int[height][width];
		int[][] blue = new int[height][width];
		int[][] rtemp1 = new int[height][width];
		int[][] rtemp2 = new int[height][width];
		int[][] gtemp1 = new int[height][width];
		int[][] gtemp2 = new int[height][width];
		int[][] btemp1 = new int[height][width];
		int[][] btemp2 = new int[height][width];
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				ImageMatrix[y][x] = (new Color(source.image.getRGB(y, x))).getRed()<<16 | (new Color(source.image.getRGB(y, x))).getGreen()<<8 | (new Color(source.image.getRGB(y, x))).getBlue();
				red[y][x] = (new Color(source.image.getRGB(y, x))).getRed();
				green[y][x] = (new Color(source.image.getRGB(y, x))).getGreen();
				blue[y][x] = (new Color(source.image.getRGB(y, x))).getBlue();
			}
		}
		for (int q = 0; q < height; q++){
			rsum = 3*red[q][0] + red[q][1] + red[q][2];
			rtemp1[q][0] = clamp(rsum/(2*pos+1),0,255);
			rsum = 2*red[q][0] + red[q][1] + red[q][2] + red[q][3];
			rtemp1[q][1] = clamp(rsum/(2*pos+1),0,255);
			rsum = 1*red[q][0] + red[q][1] + red[q][2] + red[q][3] + red[q][4];
			rtemp1[q][2] = clamp(rsum/(2*pos+1),0,255);
			
			gsum = 3*green[q][0] + green[q][1] + green[q][2];
			gtemp1[q][0] = clamp(gsum/(2*pos+1),0,255);
			gsum = 2*green[q][0] + green[q][1] + green[q][2] + green[q][3];
			gtemp1[q][1] = clamp(gsum/(2*pos+1),0,255);
			gsum = 1*green[q][0] + green[q][1] + green[q][2] + green[q][3] + green[q][4];
			gtemp1[q][2] = clamp(gsum/(2*pos+1),0,255);
			
			bsum = 3*blue[q][0] + blue[q][1] + blue[q][2];
			btemp1[q][0] = clamp(bsum/(2*pos+1),0,255);
			bsum = 2*blue[q][0] + blue[q][1] + blue[q][2] + blue[q][3];
			btemp1[q][1] = clamp(bsum/(2*pos+1),0,255);
			bsum = 1*blue[q][0] + blue[q][1] + blue[q][2] + blue[q][3] + blue[q][4];
			btemp1[q][2] = clamp(bsum/(2*pos+1),0,255);
			for ( int p = pos+1; p < width -pos; p++){
				rsum += red[q][p+pos] - red[q][p-pos-1];
				rtemp1[q][p] = clamp(rsum/(2*pos+1),0,255);
				gsum += green[q][p+pos] - green[q][p-pos-1];
				gtemp1[q][p] = clamp(gsum/(2*pos+1),0,255); 
				bsum += blue[q][p+pos] - blue[q][p-pos-1];
				btemp1[q][p] = clamp(bsum/(2*pos+1),0,255); 
			}
		}
		for (int p = 0; p < width; p++ ){
			rsum = 3*rtemp1[0][p] + rtemp1[1][p] + rtemp1[2][p];
			rtemp2[0][p] = clamp(rsum/(2*pos+1),0,255);
			rsum = 2*rtemp1[0][p] + rtemp1[1][p] + rtemp1[2][p] + rtemp1[3][p];
			rtemp2[1][p] = clamp(rsum/(2*pos+1),0,255);
			rsum = 1*rtemp1[0][p] + rtemp1[1][p] + rtemp1[2][p] + rtemp1[3][p] + rtemp1[4][p];
			rtemp2[2][p] = clamp(rsum/(2*pos+1),0,255);
			
			gsum = 3*gtemp1[0][p] + gtemp1[1][p] + gtemp1[2][p];
			gtemp2[0][p] = clamp(gsum/(2*pos+1),0,255);
			gsum = 2*gtemp1[0][p] + gtemp1[1][p] + gtemp1[2][p] + gtemp1[3][p];
			gtemp2[1][p] = clamp(gsum/(2*pos+1),0,255);
			gsum = 1*gtemp1[0][p] + gtemp1[1][p] + gtemp1[2][p] + gtemp1[3][p] + gtemp1[4][p];
			gtemp2[2][p] = clamp(gsum/(2*pos+1),0,255);
			
			bsum = 3*btemp1[0][p] + btemp1[1][p] + btemp1[2][p];
			btemp2[0][p] = clamp(bsum/(2*pos+1),0,255);
			bsum = 2*btemp1[0][p] + btemp1[1][p] + btemp1[2][p] + btemp1[3][p];
			btemp2[1][p] = clamp(bsum/(2*pos+1),0,255);
			bsum = 1*btemp1[0][p] + btemp1[1][p] + btemp1[2][p] + btemp1[3][p] + btemp1[4][p];
			btemp2[2][p] = clamp(bsum/(2*pos+1),0,255);
			ImageMatrix[width-1][p] = rtemp2[0][p]<<16 | gtemp2[0][p]<<8 | btemp2[0][p];
			ImageMatrix[width-2][p] = rtemp2[1][p]<<16 | gtemp2[1][p]<<8 | btemp2[1][p];
			for ( int q = pos + 1; q < height-pos; q++){
				rsum += rtemp1[q+pos][p] - rtemp1[q-pos-1][p];
				rtemp2[q][p] = clamp(rsum/(2*pos+1),0,255);
				gsum += gtemp1[q+pos][p] - gtemp1[q-pos-1][p];
				gtemp2[q][p] = clamp(gsum/(2*pos+1),0,255);
				bsum += btemp1[q+pos][p] - btemp1[q-pos-1][p];
				btemp2[q][p] = clamp(bsum/(2*pos+1),0,255);
				ImageMatrix[q][p] = rtemp2[q][p]<<16 | gtemp2[q][p]<<8 | btemp2[q][p];
			}
		}
		handleBoundary(ImageMatrix);

	}
	
	//median filter
	public void  medianFilter(int[][] ImageMatrix){
		int maskSize = 5;
		int pos = (maskSize -1)/2;
		int[] rmedianArray = new int [maskSize*maskSize];
		int[] gmedianArray = new int [maskSize*maskSize];
		int[] bmedianArray = new int [maskSize*maskSize];
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				ImageMatrix[y][x] = (new Color(source.image.getRGB(x, y))).getRed()<<16 | (new Color(source.image.getRGB(x, y))).getGreen()<<8 | (new Color(source.image.getRGB(x, y))).getBlue();
			}
		}
		for ( int q = pos; q < height - pos; q++ ) {
			for ( int p = pos; p < width - pos; p++ ) {  //(q,p) is the middle of mask
				int i = 0;
				for ( int v = -pos; v <= pos; v++ ) {
					for ( int u = -pos; u <= pos; u++ ){
						rmedianArray[i] = (new Color(source.image.getRGB(q+v, p+u))).getRed();
						gmedianArray[i] = (new Color(source.image.getRGB(q+v, p+u))).getGreen();
						bmedianArray[i] = (new Color(source.image.getRGB(q+v, p+u))).getBlue();
						i++;
					}
				}
				Arrays.sort(rmedianArray);
				Arrays.sort(gmedianArray);
				Arrays.sort(bmedianArray);
				int rmedian = findMedian(rmedianArray);
				int gmedian = findMedian(gmedianArray);
				int bmedian = findMedian(bmedianArray);
				ImageMatrix[q][p] = rmedian<<16 | gmedian<<8 | bmedian;
			}
		}
		handleBoundary(ImageMatrix);
	}
	
	public int clamp(int val, int min, int max){
		if ( val < min ){ return min; }
		if ( val > max ){ return max; }
		return val;
	}
	
	//Kuwahara filter
	public void KuwaharaFilter(int[][] ImageMatrix) {
		int maskSize = 5;
		int pos = (maskSize-1)/2;
		int[] rlist = new int[maskSize*maskSize];
		int[] glist = new int[maskSize*maskSize];
		int[] blist = new int[maskSize*maskSize];
		float rmin, gmin, bmin; 
		int rmean = 0;
		int gmean = 0;
		int bmean = 0;
		float rvar1, rvar2, rvar3, rvar4, gvar1, gvar2, gvar3, gvar4, bvar1, bvar2, bvar3, bvar4;
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				ImageMatrix[y][x] = (new Color(source.image.getRGB(x, y))).getRed()<<16 | (new Color(source.image.getRGB(x, y))).getGreen()<<8 | (new Color(source.image.getRGB(x, y))).getBlue();
			}
		}
		for ( int q = pos; q < height - pos; q++ ){
			for ( int p = pos; p < width - pos; p++){
				int i = 0;
				for ( int v = -pos; v <= pos; v++ ){
					for ( int u = -pos; u <= pos; u++ ){
							rlist[i] = (new Color(source.image.getRGB(q+v, p+u))).getRed();
							glist[i] = (new Color(source.image.getRGB(q+v, p+u))).getGreen();
							blist[i] = (new Color(source.image.getRGB(q+v, p+u))).getBlue();
							i++;
					}
				}
				rvar1 = region1(rlist); rvar2 = region2(rlist); rvar3 = region3(rlist); rvar4 = region4(rlist); gvar1 = region1(glist); gvar2 = region2(glist); gvar3 = region3(glist); gvar4 = region4(glist);
				bvar1 = region1(blist); bvar2 = region2(blist); bvar3 = region3(blist); bvar4 = region4(blist);
				rmin = findmin(rvar1, rvar2, rvar3, rvar4); gmin = findmin(gvar1, gvar2, gvar3, gvar4); bmin = findmin(bvar1, bvar2, bvar3, bvar4);
				if ( rmin == rvar1) { rmean = region1mean(rlist);} if ( rmin == rvar2 ) { rmean = region2mean(rlist); } if ( rmin == rvar3 ) { rmean = region3mean(rlist); } if ( rmin == rvar4 ) { rmean = region4mean(rlist); }
				if ( gmin == gvar1) { gmean = region1mean(glist);} if ( gmin == gvar2 ) { gmean = region2mean(glist); } if ( gmin == gvar3 ) { gmean = region3mean(glist); } if ( gmin == gvar4 ) { gmean = region4mean(glist); }
				if ( bmin == bvar1) { bmean = region1mean(blist);} if ( bmin == bvar2 ) { bmean = region2mean(blist); } if ( bmin == bvar1 ) { bmean = region3mean(blist); } if ( bmin == bvar1 ) { bmean = region4mean(blist); }
				rmean = clamp(rmean, 0, 255); gmean = clamp(gmean, 0, 255); bmean = clamp(bmean, 0, 255);
				ImageMatrix[q][p] = rmean<<16 | gmean<<8 | bmean;
			}
		}
		
		handleBoundary(ImageMatrix);
	}
	
	public float region1 (int[] list){
		int mean;
		int sumvar = 0;
		float var;
		int[] array= new int[9];
		int sum = 0;
		int a = 0;
		for ( int i = 0; i < 2; i++){
			sum += list[i];
			array[a] = list[i];
		}
		a = 3;
		for ( int i = 5; i < 8; i++){
			sum += list[i];
			array[a] = list[i];
			a++;
		}
		a = 6;
		for ( int i = 10; i < 13;i++){
			sum += list[i];
			array[a] = list[i];
			a++;
		}
		mean = sum/9;
		for ( int i = 0; i < 9; i++){
			sumvar += (array[i]-mean)*(array[i]-mean);
		}
		var = sumvar/9;
		return var;
	}
		
	public float region2 (int[] list){
		int mean;
		int sumvar = 0;
		float var;
		int[] array= new int[9];
		int sum = 0;
		int a = 0;
		for ( int i = 2; i < 5; i++){
			sum += list[i];
			array[a] = list[i];
		}
		a = 3;
		for ( int i = 7; i < 10; i++){
			sum += list[i];
			array[a] = list[i];
			a++;
		}
		a = 6;
		for ( int i = 12; i < 15;i++){
			sum += list[i];
			array[a] = list[i];
			a++;
		}
		mean = sum/9;
		for ( int i = 0; i < 9; i++){
			sumvar += (array[i]-mean)*(array[i]-mean);
		}
		var = sumvar/9;
		return var;
	}

	public float region3 (int[] list){
		int mean;
		int sumvar = 0;
		float var;
		int[] array= new int[9];
		int sum = 0;
		int a = 0;
		for ( int i = 10; i < 13; i++){
			sum += list[i];
			array[a] = list[i];
		}
		a = 3;
		for ( int i = 15; i < 18; i++){
			sum += list[i];
			array[a] = list[i];
			a++;
		}
		a = 6;
		for ( int i = 20; i < 23;i++){
			sum += list[i];
			array[a] = list[i];
			a++;
		}
		mean = sum/9;
		for ( int i = 0; i < 9; i++){
			sumvar += (array[i]-mean)*(array[i]-mean);
		}
		var = sumvar/9;
		return var;
	}
	
	public float region4 (int[] list){
		int mean;
		int sumvar = 0;
		float var;
		int[] array= new int[9];
		int sum = 0;
		int a = 0;
		for ( int i = 12; i <15; i++){
			sum += list[i];
			array[a] = list[i];
		}
		a = 3;
		for ( int i = 17; i < 20; i++){
			sum += list[i];
			array[a] = list[i];
			a++;
		}
		a = 6;
		for ( int i = 22; i <= 24;i++){
			sum += list[i];
			array[a] = list[i];
			a++;
		}
		mean = sum/9;
		for ( int i = 0; i < 9; i++){
			sumvar += (array[i]-mean)*(array[i]-mean);
		}
		var = sumvar/9;
		return var;
	}
	
	public int region1mean(int[] list){
		float sum = 0;
		int mean;
		for ( int i = 0; i < 2; i++){
			sum += list[i];
		}
		for ( int i = 5; i < 8; i++){
			sum += list[i];
		}
		for ( int i = 10; i < 13;i++){
			sum += list[i];
		}
		mean = (int)sum/9;
		return mean;
	}
	
	public int region2mean(int[] list){
		float sum = 0;
		int mean;
		for ( int i = 2; i < 5; i++){
			sum += list[i];
		}
		for ( int i = 7; i < 10; i++){
			sum += list[i];
		}
		for ( int i = 12; i < 15;i++){
			sum += list[i];
		}
		mean = (int)sum/9;
		return mean;
	}
	
	public int region3mean(int[] list){
		float sum = 0;
		int mean;
		for ( int i = 10; i < 13; i++){
			sum += list[i];
		}
		for ( int i = 15; i < 18; i++){
			sum += list[i];
		}
		for ( int i = 20; i < 23;i++){
			sum += list[i];
		}
		mean = (int)sum/9;
		return mean;
	}

	public int region4mean(int[] list){
		float sum = 0;
		int mean;
		for ( int i = 12; i < 15; i++){
			sum += list[i];
		}
		for ( int i = 17; i < 20; i++){
			sum += list[i];
		}
		for ( int i = 22; i <= 24;i++){
			sum += list[i];
		}
		mean = (int)sum/9;
		return mean;
	}
	
 	public float findmin(float a, float b, float c, float d){
		if ( b > a ){ a = b; }
		if ( c > a ){ a = c; }
		if ( d > a ){ a = d; }
 		return a;
	}
	
	public int findMedian(int[] array){
		Arrays.sort(array);
		int median;
		if ( array.length % 2 == 0 ){
			median = (int)(array[array.length/2]+array[array.length/2-1])/2;
		}
		else {
			median = (int)array[array.length/2];
		}
		return median;
	}
	
	public void handleBoundary(int[][] ImageMatrix){
		//handle boundary
		int clr = 240;
		for ( int q =0; q < height; q++ ){
			ImageMatrix[q][height-1] = clr<<16 | clr<<8 | clr;
			ImageMatrix[q][height-2] = clr<<16 | clr<<8 | clr;
			ImageMatrix[height-1][q] = clr<<16 | clr<<8 | clr;
			ImageMatrix[height-2][q] = clr<<16 | clr<<8 | clr;
			ImageMatrix[q][0] = clr<<16 | clr<<8 | clr;
			ImageMatrix[q][1] = clr<<16 | clr<<8 | clr;
			ImageMatrix[0][q] = clr<<16 | clr<<8 | clr;
			ImageMatrix[1][q] = clr<<16 | clr<<8 | clr;
		}
	}
	
	public static void main(String[] args) {
		new SmoothingFilter(args.length==1 ? args[0] : "baboo.png");
	}
}