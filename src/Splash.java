/**
 * Created by SriramHariharan on 5/24/18.
 */
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.RenderableImage;
import java.awt.image.renderable.RenderableImageOp;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

import static java.lang.System.out;


public class Splash {

    Color[][] pix;
    Map<Color,Integer> colormap;
    double[][] markovmat;
    BufferedImage input;
    private final String filename = "portrait";

    public static void main(String[] args) throws IOException{
        Splash splash = new Splash();
        splash.loadFromImage(new File(splash.filename+".png"));
        splash.calculatePercentages();
        splash.generateImage();
    }
    public Splash(){
        colormap = new HashMap<>();
    }

    private Color getRandomColor(){
        return pix[(int)(Math.random()*pix.length)][(int)(Math.random()*pix[0].length)];
    }
    private void generateImage(){
        BufferedImage bi = new BufferedImage(input.getWidth(),input.getHeight(),BufferedImage.TYPE_INT_RGB);
        out.println(new Color(bi.getRGB(0,0)));
        boolean[][] visited = new boolean[bi.getWidth()][bi.getHeight()];
//        for(int i = 0; i<bi.getHeight();i++){
//            generateImageHelper(bi,i,0,getRandomColor(),visited);
//
//        }
          generateImageHelper(bi, (int)(Math.random()*bi.getWidth()), (int)(Math.random()*bi.getHeight()), pix[0][0],visited);
        try {
            File outputfile = new File(filename+"_predicted.png");
            ImageIO.write(bi, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateImageHelper(BufferedImage im, int x, int y, Color color, boolean[][] visited){
        if(inBounds(x,y) && !visited[x][y]) {
            visited[x][y] = true;
            color = predictColor(color);
            im.setRGB(x, y, color.getRGB());
            generateImageHelper(im, x-1,y,color,visited);
            generateImageHelper(im, x,y+1,color,visited);
            generateImageHelper(im, x+1,y,color,visited);
            generateImageHelper(im, x,y-1,color,visited);
        }
    }
    private Color predictColor(Color c){
        int seed = colormap.get(c);
        double rand = Math.random();
        double sum = 0;
        for(int j = 1; j<markovmat[0].length;j++){
            sum+= markovmat[seed][j];
            if(sum>rand){
                return getColorFromIndex(j);
            }
        }
        return null;
    }
    private Color getColorFromIndex(int index){
        for(Color c : colormap.keySet()){
            if(colormap.get(c) == index){
                return c;
            }
        }
        return  null;
    }
    private void loadFromImage(File file) throws IOException{
        input = ImageIO.read(file);
        Set<Color> colors = new HashSet<>();
        pix = new Color[input.getWidth()][input.getHeight()];
        for (int i = 0; i < input.getWidth(); i++) {
            for (int j = 0; j < input.getHeight(); j++) {
                Color c = new Color(input.getRGB(i,j));
                colors.add(c);
                pix[i][j] = c;
            }
        }
        int index = 1;
        for(Color c: colors){
            colormap.put(c,index);
            index++;
        }
        markovmat = new double[colors.size()+1][colors.size()+1];
    }
    private void calculatePercentages(){
        for(int i = 0; i<pix.length; i++){
            for(int j = 0; j<pix[0].length;j++){
                Color currcolor = pix[i][j];
                int seed = colormap.get(currcolor);
                countUpOccurences(seed,i,j);
            }
        }
     //   printMatrix();
        for(int i = 1; i<markovmat.length;i++){
            double sum = sumRow(i);
            for(int j = 1; j<markovmat[0].length;j++){
                markovmat[i][j] = (markovmat[i][j] / sum);
            }
        }
    }

    private int sumRow(int r){
        int sum = 0;
        for(int j = 1; j<markovmat[0].length;j++){
            sum+= markovmat[r][j];
        }
        return sum;
    }

    private void countUpOccurences(int seed, int i, int j){


//        //above
//        if(inBounds(i-1,j)){
//            int transition = colormap.get(pix[i-1][j]);
//            markovmat[seed][transition]++;
//        }
        for(int r = -1; r<=1;r++){
            for(int c = -1;c<=1;c++){
                if(inBounds(i+r,j+c)){
                    Color col = pix[i+r][j+c];
                    int transition = colormap.get(col);
                    markovmat[seed][transition]++;
                }
            }
        }
    }
    private boolean inBounds(int i, int j){
        return i >= 0 && i < pix.length && j >= 0 && j < pix[0].length;
    }

    private void printMatrix() {
        out.println(colormap);
        out.println(Arrays.deepToString(markovmat).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
        out.println();

    }
}
