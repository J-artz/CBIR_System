package CBIR_Main;

import net.semanticmetadata.lire.utils.FileUtils;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class Manhattan {
    private static int[][] originalrgb;
    private static double[][] magaryoriginal;
    private static String[] pathary;
    private static double[] accresult;
    private static double[] accmagresult;
    private static double[][][] magarydb;
    private static int col=0;
    private static int[][][] rgbdb;

    // Example: formula: E=[0,10], ManhattanSim((1,2,3,4),(4,3,2,1))=1−0.3+0.1+0.1+0.34=0.8.
    // Sum of (abs differences / 10)
    // Get rgb pixels with total of 16777216
    // Formula : rgb = 65536 * r + 256 * g + b;
    // Sobel edge detector: https://en.wikipedia.org/wiki/Sobel_operator
    // Array of grey imgs:
    // Get maximum magnitude of 1224083.9634469524; approx 1224084 rgb=(256,256,256)
    public Manhattan(String path) {
    }
    public static void indexing(String path){
        try {
            ArrayList<String> temp = FileUtils.getAllImages(new File(path), true);
            magarydb = new double[temp.size()][128][128];
            accresult = new double[temp.size()];
            accmagresult= new double[temp.size()];
            pathary = new String[temp.size()];
            rgbdb = new int[temp.size()][128][128];
            for(String al: temp){
                pathary[col]=al;
                BufferedImage before =  ImageIO.read(new File(al));
                before.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
                BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics resized = img.createGraphics();
                resized.drawImage(before, 0, 0, null);
                resized.dispose();
                double[][] ary1= convertmagnitude(img);
                magarydb[col]= ary1;
                int[][] ary=getrgb(img);
                rgbdb[col]=ary;
                col+=1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (NullPointerException nullpointerexception){ nullpointerexception.printStackTrace();}

    }
    public static void extract(BufferedImage img){
        try {
            img.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
            BufferedImage img1 = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics resized = img1.createGraphics();
            resized.drawImage(img, 0, 0, null);
            resized.dispose();
            originalrgb= getrgb(img1);
            magaryoriginal= convertmagnitude(img1);
        } catch (NullPointerException nullpointerexception){ nullpointerexception.printStackTrace();}

    }

    public static String[] retrieveall( int number){
        double result2;
        double last;
        DecimalFormat df = new DecimalFormat("0.00000000000000000");
        for (int i=0; i<col ;i++) {
            double total2=0;
            for (int j = 0; j < rgbdb[i].length ; j++) {
                for (int k = 0; k < rgbdb[i][j].length; k++) {
                    double a=Math.abs(originalrgb[j][k] - rgbdb[i][j][k]);
                    double b=a/ 16777216;
                    total2+=b;
                }
            }
            result2 = total2 / 1048576;
            last = 1 - result2;
            accresult[i] = last;
        }
        for (int i=0; i<col ;i++) {
            double total1=0;
            for (int j = 0; j < magarydb[i].length ; j++) {
                for (int k = 0; k < magarydb[i][j].length ; k++) {
                    double a=Math.abs(magaryoriginal[j][k]-magarydb[i][j][k]);
                    double b=a/ 1448.15;
                    total1+=b;
                }
            }
            result2 = total1 / 1048576;
            last = 1 - result2;
            accmagresult[i] = last;
        }
        String[] retrievepath=retrieveimg(arrange(accresult,accmagresult),pathary,number);
        System.out.println("successfully retrieved");
        return retrievepath;
    }
    public static int[][] getrgb(BufferedImage image){
        int[] pixel;
        int[][] originalpx = new int[128][128];
        for (int y=0;y<image.getWidth();y++)
        {
            for ( int x= 0; x<image.getHeight();x++)
            {
                pixel=image.getRaster().getPixel(y,x,new int[4]);
                originalpx[y][x] = (65536 * pixel[0] + 256 *pixel[1] +pixel[2]);
            }
        }
        return originalpx;
    }
    //magnitude = sqrt( magX^2 + magY^2 )
    //Sobel edge detector
    //(int)(0.2126 * r + 0.7152 * g + 0.0722 * b) to get grey image matrix.
    public static double[][] convertmagnitude (BufferedImage image) {
        int x = image.getWidth();
        int y = image.getHeight();
        int[][] magary=new int[x][y];
        for (int j = 0; j < x; j++) {
            for (int k = 0; k < y; k++) {
                int rgb=image.getRGB(j,k);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb) & 0xff;
                int gray = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);
                magary[j][k] = gray;
            }
        }
        double[][] magary1=new double[x][y];
        //1448.15
        for (int i = 0; i < x-2; i++) {
            for (int j = 0; j < y-2; j++) {

                int gx =  ((-1 * magary[i][j]) + (0 * magary[i][j+1]) + (1 * magary[i][j+2]))
                        + ((-2 * magary[i+1][j]) + (0 * magary[i+1][j+1]) + (2 * magary[i+1][j+2]))
                        + ((-1 * magary[i+2][j]) + (0 * magary[i+2][j+1]) + (1 * magary[i+2][j+2]));
                int gy =  ((-1 * magary[i][j]) + (-2 * magary[i][j+1]) + (-1 * magary[i][j+2]))
                        + ((0 * magary[i+1][j]) + (0 * magary[i+1][j+1]) + (0 * magary[i+1][j+2]))
                        + ((1 * magary[i+2][j]) + (2 * magary[i+2][j+1]) + (1 * magary[i+2][j+2]));
                double res =Math.sqrt((gx*gx)+(gy*gy));
                magary1[i][j]=res;
            }
        }


        return magary1;
    }
    public static int[] arrange(double[] ary, double[] ary1)
    {
        double[] temp = Arrays.copyOf(ary, ary.length);
        double[] temp1 = Arrays.copyOf(ary1, ary1.length);
        double a;
        int[] arranged = new int[ary.length];
        double[]temp2= new double[temp.length];
        DecimalFormat df = new DecimalFormat("0.00000000000000000");
        for (int j = 0; j < ary.length - 1; j++) {
            temp2[j]=(temp[j]+temp1[j])/2;
        }
        double[] temp3 = Arrays.copyOf(temp2, temp2.length);
        for (int j = 0; j < ary.length - 1; j++) {
            if (temp2[j] < temp2[j + 1]) {
                a = temp2[j];
                temp2[j] = temp2[j + 1];
                temp2[j + 1] = a;
                j=-1;
            }
        }
        for (int x=0 ; x<temp2.length ; x++) {
            for (int y=0 ; y<temp3.length ; y++){
                if (temp3[y] == temp2[x]){
                    arranged[x] = y;
                }
            }
        }
        return arranged;
    }
    public static String[] retrieveimg (int[] ary, String[] ary1, int num)
    {
        String[]temp = new String[num];
        for (int u = 0; u < num; u++) {
            temp[u]=ary1[ary[u]];
        }
        return temp;
    }
    /* testing
    public static void main(String[] args) throws IOException, UnsupportedLookAndFeelException
    {
        BufferedImage img = ImageIO.read(new File("C:/Users/zinhu/Desktop/cbir/validation/n0/n000.jpg"));
        indexing("C:/Users/Zinhu/desktop/cbir/validation");
        extract(img);
        retrieveall(30);
    }
    */
}
