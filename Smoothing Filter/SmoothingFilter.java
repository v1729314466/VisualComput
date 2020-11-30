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

		//median filter
		if ( ((Button)e.getSource()).getLabel().equals("5x5 median") ) {
			int[][] Image = new int[height][width];
			medianFilter(Image);
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
	public void GaussianFilter(int[][] ImageM){
		int size = 5;
		int ps = (size-1)/2;
		double red_sum, green_sum, blue_sum;
		double sigma = Float.parseFloat(texSigma.getText());
		double[][] red_mask = new double[size][size];
		double[][] green_mask = new double[size][size];
		double[][] blue_mask = new double[size][size];
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				ImageM[y][x] = (new Color(source.image.getRGB(y, x))).getRed()<<16 | (new Color(source.image.getRGB(y, x))).getGreen()<<8 | (new Color(source.image.getRGB(y, x))).getBlue();
			}
		}
		for ( int q = ps; q < height - ps; q++){
			for ( int p = ps; p < width - ps; p++){
				int i = 0;
				for ( int v = -ps; v <= ps; v++){
					int j = 0;
					for ( int u = -ps; u <= ps; u++){
						red_mask[i][j] = (new Color(source.image.getRGB(q+v, p+u))).getRed();
						green_mask[i][j] = (new Color(source.image.getRGB(q+v, p+u))).getGreen();
						blue_mask[i][j] = (new Color(source.image.getRGB(q+v, p+u))).getBlue();
						j++;
					}
					i++;
				}
				GaussianFunction(red_mask, sigma);
				GaussianFunction(green_mask, sigma);
				GaussianFunction(blue_mask, sigma);
				red_sum = clamp2(sumUp(red_mask),0,255);
				green_sum = clamp2(sumUp(green_mask),0,255);
				blue_sum = clamp2(sumUp(blue_mask),0,255);
				ImageM[q][p] = (int)red_sum<<16 | (int)green_sum<<8 | (int)blue_sum;
			}
		}
		handleBoundary(ImageM);
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
			}
			x++;
		}
		for ( int i = 0; i < 5; i++ ){
			for ( int j = 0; j < 5; j++){
				mask[i][j] = mask[i][j]*temp[i][j];
			}
		}
	}
	
	public double calGaussian(double x, double y, double sigma){
		double sigma_sq = Math.pow(sigma, 2);
        double coe      = 1.0/( 2 * Math.PI * sigma_sq );
        double exp      = (-x*x -y*y)/(2.0 * sigma_sq);
        double gaussian = coe * Math.pow(Math.E, exp);

        return gaussian;
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
	public void meanFilter(int[][] ImageM){
		int size = 5;
		int ps = (size-1)/2;
		int red_sum, green_sum, blue_sum;
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
				ImageM[y][x] = (new Color(source.image.getRGB(y, x))).getRed()<<16 | (new Color(source.image.getRGB(y, x))).getGreen()<<8 | (new Color(source.image.getRGB(y, x))).getBlue();
				red[y][x] = (new Color(source.image.getRGB(y, x))).getRed();
				green[y][x] = (new Color(source.image.getRGB(y, x))).getGreen();
				blue[y][x] = (new Color(source.image.getRGB(y, x))).getBlue();
			}
		}
		for (int q = 0; q < height; q++){
			red_sum = 3*red[q][0] + red[q][1] + red[q][2];
			rtemp1[q][0] = clamp(red_sum/(2*ps+1),0,255);
			red_sum = 2*red[q][0] + red[q][1] + red[q][2] + red[q][3];
			rtemp1[q][1] = clamp(red_sum/(2*ps+1),0,255);
			red_sum = 1*red[q][0] + red[q][1] + red[q][2] + red[q][3] + red[q][4];
			rtemp1[q][2] = clamp(red_sum/(2*ps+1),0,255);
			
			green_sum = 3*green[q][0] + green[q][1] + green[q][2];
			gtemp1[q][0] = clamp(green_sum/(2*ps+1),0,255);
			green_sum = 2*green[q][0] + green[q][1] + green[q][2] + green[q][3];
			gtemp1[q][1] = clamp(green_sum/(2*ps+1),0,255);
			green_sum = 1*green[q][0] + green[q][1] + green[q][2] + green[q][3] + green[q][4];
			gtemp1[q][2] = clamp(green_sum/(2*ps+1),0,255);
			
			blue_sum = 3*blue[q][0] + blue[q][1] + blue[q][2];
			btemp1[q][0] = clamp(blue_sum/(2*ps+1),0,255);
			blue_sum = 2*blue[q][0] + blue[q][1] + blue[q][2] + blue[q][3];
			btemp1[q][1] = clamp(blue_sum/(2*ps+1),0,255);
			blue_sum = 1*blue[q][0] + blue[q][1] + blue[q][2] + blue[q][3] + blue[q][4];
			btemp1[q][2] = clamp(blue_sum/(2*ps+1),0,255);
			for ( int p = ps+1; p < width -ps; p++){
				red_sum += red[q][p+ps] - red[q][p-ps-1];
				rtemp1[q][p] = clamp(red_sum/(2*ps+1),0,255);
				green_sum += green[q][p+ps] - green[q][p-ps-1];
				gtemp1[q][p] = clamp(green_sum/(2*ps+1),0,255); 
				blue_sum += blue[q][p+ps] - blue[q][p-ps-1];
				btemp1[q][p] = clamp(blue_sum/(2*ps+1),0,255); 
			}
		}
		for (int p = 0; p < width; p++ ){
			red_sum = 3*rtemp1[0][p] + rtemp1[1][p] + rtemp1[2][p];
			rtemp2[0][p] = clamp(red_sum/(2*ps+1),0,255);
			red_sum = 2*rtemp1[0][p] + rtemp1[1][p] + rtemp1[2][p] + rtemp1[3][p];
			rtemp2[1][p] = clamp(red_sum/(2*ps+1),0,255);
			red_sum = 1*rtemp1[0][p] + rtemp1[1][p] + rtemp1[2][p] + rtemp1[3][p] + rtemp1[4][p];
			rtemp2[2][p] = clamp(red_sum/(2*ps+1),0,255);
			
			green_sum = 3*gtemp1[0][p] + gtemp1[1][p] + gtemp1[2][p];
			gtemp2[0][p] = clamp(green_sum/(2*ps+1),0,255);
			green_sum = 2*gtemp1[0][p] + gtemp1[1][p] + gtemp1[2][p] + gtemp1[3][p];
			gtemp2[1][p] = clamp(green_sum/(2*ps+1),0,255);
			green_sum = 1*gtemp1[0][p] + gtemp1[1][p] + gtemp1[2][p] + gtemp1[3][p] + gtemp1[4][p];
			gtemp2[2][p] = clamp(green_sum/(2*ps+1),0,255);
			
			blue_sum = 3*btemp1[0][p] + btemp1[1][p] + btemp1[2][p];
			btemp2[0][p] = clamp(blue_sum/(2*ps+1),0,255);
			blue_sum = 2*btemp1[0][p] + btemp1[1][p] + btemp1[2][p] + btemp1[3][p];
			btemp2[1][p] = clamp(blue_sum/(2*ps+1),0,255);
			blue_sum = 1*btemp1[0][p] + btemp1[1][p] + btemp1[2][p] + btemp1[3][p] + btemp1[4][p];
			btemp2[2][p] = clamp(blue_sum/(2*ps+1),0,255);
			ImageM[width-1][p] = rtemp2[0][p]<<16 | gtemp2[0][p]<<8 | btemp2[0][p];
			ImageM[width-2][p] = rtemp2[1][p]<<16 | gtemp2[1][p]<<8 | btemp2[1][p];
			for ( int q = ps + 1; q < height-ps; q++){
				red_sum += rtemp1[q+ps][p] - rtemp1[q-ps-1][p];
				rtemp2[q][p] = clamp(red_sum/(2*ps+1),0,255);
				green_sum += gtemp1[q+ps][p] - gtemp1[q-ps-1][p];
				gtemp2[q][p] = clamp(green_sum/(2*ps+1),0,255);
				blue_sum += btemp1[q+ps][p] - btemp1[q-ps-1][p];
				btemp2[q][p] = clamp(blue_sum/(2*ps+1),0,255);
				ImageM[q][p] = rtemp2[q][p]<<16 | gtemp2[q][p]<<8 | btemp2[q][p];
			}
		}
		handleBoundary(ImageM);

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
	//median filter
	public void  medianFilter(int[][] ImageM){
		int size = 5;
		int ps = (size -1)/2;
		int[] rmedianArray = new int [size*size];
		int[] gmedianArray = new int [size*size];
		int[] bmedianArray = new int [size*size];
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				ImageM[y][x] = (new Color(source.image.getRGB(x, y))).getRed()<<16 | (new Color(source.image.getRGB(x, y))).getGreen()<<8 | (new Color(source.image.getRGB(x, y))).getBlue();
			}
		}
		for ( int q = ps; q < height - ps; q++ ) {
			for ( int p = ps; p < width - ps; p++ ) { 
				int i = 0;
				for ( int v = -ps; v <= ps; v++ ) {
					for ( int u = -ps; u <= ps; u++ ){
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
				ImageM[q][p] = rmedian<<16 | gmedian<<8 | bmedian;
			}
		}
		handleBoundary(ImageM);
	}
	
	public int clamp(int val, int min, int max){
		if ( val < min ){ return min; }
		if ( val > max ){ return max; }
		return val;
	}
	
	public float region1 (int[] list){
		int mean;
		int vSum = 0;
		float v;
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
			vSum += (array[i]-mean)*(array[i]-mean);
		}
		v = vSum/9;
		return v;
	}
		
	public float region2 (int[] list){
		int mean;
		int vSum = 0;
		float v;
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
			vSum += (array[i]-mean)*(array[i]-mean);
		}
		v = vSum/9;
		return v;
	}

	public float region3 (int[] list){
		int mean;
		int vSum = 0;
		float v;
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
			vSum += (array[i]-mean)*(array[i]-mean);
		}
		v = vSum/9;
		return v;
	}
	
	public float region4 (int[] list){
		int mean;
		int vSum = 0;
		float v;
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
			vSum += (array[i]-mean)*(array[i]-mean);
		}
		v = vSum/9;
		return v;
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
	
	//Kuwahara filter
	public void KuwaharaFilter(int[][] ImageM) {
		int size = 5;
		int ps = (size-1)/2;
		int[] rlist = new int[size*size];
		int[] glist = new int[size*size];
		int[] blist = new int[size*size];
		float rmin, gmin, bmin; 
		int rmean = 0;
		int gmean = 0;
		int bmean = 0;
		float redv1, redv2, redv3, redv4, greenv1, greenv2, greenv3, greenv4, bluev1, bluev2, bluev3, bluev4;
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				ImageM[y][x] = (new Color(source.image.getRGB(x, y))).getRed()<<16 | (new Color(source.image.getRGB(x, y))).getGreen()<<8 | (new Color(source.image.getRGB(x, y))).getBlue();
			}
		}
		for ( int q = ps; q < height - ps; q++ ){
			for ( int p = ps; p < width - ps; p++){
				int i = 0;
				for ( int v = -ps; v <= ps; v++ ){
					for ( int u = -ps; u <= ps; u++ ){
							rlist[i] = (new Color(source.image.getRGB(q+v, p+u))).getRed();
							glist[i] = (new Color(source.image.getRGB(q+v, p+u))).getGreen();
							blist[i] = (new Color(source.image.getRGB(q+v, p+u))).getBlue();
							i++;
					}
				}
				redv1 = region1(rlist); 
				redv2 = region2(rlist); 
				redv3 = region3(rlist); 
				redv4 = region4(rlist); 
				greenv1 = region1(glist); 
				greenv2 = region2(glist); 
				greenv3 = region3(glist); 
				greenv4 = region4(glist);
				bluev1 = region1(blist); 
				bluev2 = region2(blist);
				bluev3 = region3(blist); 
				bluev4 = region4(blist);
				rmin = findmin(redv1, redv2, redv3, redv4); 
				gmin = findmin(greenv1, greenv2, greenv3, greenv4); 
				bmin = findmin(bluev1, bluev2, bluev3, bluev4);
				if ( rmin == redv1) { 
					rmean = region1mean(rlist);
				} 
				if ( rmin == redv2 ) { 
					rmean = region2mean(rlist); 
				} 
				if ( rmin == redv3 ) { 
					rmean = region3mean(rlist); 
				} 
				if ( rmin == redv4 ) { 
					rmean = region4mean(rlist); 
				}
				if ( gmin == greenv1) { 
					gmean = region1mean(glist);
				} 
				if ( gmin == greenv2 ) { 
					gmean = region2mean(glist); 
				} 
				if ( gmin == greenv3 ) {
					gmean = region3mean(glist); 
				} 
				if ( gmin == greenv4 ) { 
					gmean = region4mean(glist); 
				}
				if ( bmin == bluev1) { 
					bmean = region1mean(blist);
				} 
				if ( bmin == bluev2 ) { 
					bmean = region2mean(blist); 
				} 
				if ( bmin == bluev1 ) { 
					bmean = region3mean(blist); 
				} 
				if ( bmin == bluev1 ) { 
					bmean = region4mean(blist); 
				}
				rmean = clamp(rmean, 0, 255); gmean = clamp(gmean, 0, 255); bmean = clamp(bmean, 0, 255);
				ImageM[q][p] = rmean<<16 | gmean<<8 | bmean;
			}
		}
		
		handleBoundary(ImageM);
	}
	
	public void handleBoundary(int[][] ImageM){
		//handle boundary
		int clr = 240;
		for ( int q =0; q < height; q++ ){
			ImageM[q][height-1] = clr<<16 | clr<<8 | clr;
			ImageM[q][height-2] = clr<<16 | clr<<8 | clr;
			ImageM[height-1][q] = clr<<16 | clr<<8 | clr;
			ImageM[height-2][q] = clr<<16 | clr<<8 | clr;
			ImageM[q][0] = clr<<16 | clr<<8 | clr;
			ImageM[q][1] = clr<<16 | clr<<8 | clr;
			ImageM[0][q] = clr<<16 | clr<<8 | clr;
			ImageM[1][q] = clr<<16 | clr<<8 | clr;
		}
	}
	
	public static void main(String[] args) {
		new SmoothingFilter(args.length==1 ? args[0] : "baboo.png");
	}
}