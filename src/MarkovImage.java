/**
 * Created by SriramHariharan on 5/24/18.
 */

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

import static java.lang.System.out;


public class MarkovImage {

    Color[][] pix;
    Set<Color> colorsSet;
    Map<Color, Map<Integer, Double>> markovmap;
    Map<Color, Double> rowsum;
    BufferedImage input;
    private final String filename = "flag";
    private final String filetype = ".png";

    public static void main(String[] args) throws IOException {
        MarkovImage markovImage = new MarkovImage();
        markovImage.loadFromImage(new File(markovImage.filename + markovImage.filetype));
        markovImage.calculatePercentages();
        markovImage.generateImage();
    }

    public MarkovImage() {
        markovmap = new HashMap<>();
        rowsum = new HashMap<>();
    }

    private Color getRandomColor() {
        return pix[(int) (Math.random() * pix.length)][(int) (Math.random() * pix[0].length)];
    }

    private void generateImage() {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
        out.println(new Color(bi.getRGB(0, 0)));
        boolean[][] visited = new boolean[bi.getWidth()][bi.getHeight()];
        Color c = getRandomColor();
        for (int i = 0; i < bi.getWidth(); i++) {
            for (int j = 0; j < bi.getHeight(); j++) {
                c = generateImageHelper(bi, i, j, c, visited);
            }
        }
        try {
            File outputfile = new File(filename + "_predicted"+filetype);
            ImageIO.write(bi, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Color generateImageHelper(BufferedImage im, int x, int y, Color color, boolean[][] visited) {
        visited[x][y] = true;
        im.setRGB(x, y, color.getRGB());
        Color c = predictColor(color);
        if (inBounds(x - 1, y) && !visited[x - 1][y]) {
            visited[x - 1][y] = true;
            im.setRGB(x - 1, y, c.getRGB());
        }
        if (inBounds(x, y + 1) && !visited[x][y + 1]) {
            visited[x][y + 1] = true;
            im.setRGB(x, y + 1, c.getRGB());
        }
        if (inBounds(x + 1, y) && !visited[x + 1][y]) {
            visited[x + 1][y] = true;
            im.setRGB(x + 1, y, c.getRGB());
        }
        if (inBounds(x, y - 1) && !visited[x][y - 1]) {
            visited[x][y - 1] = true;
            im.setRGB(x, y - 1, c.getRGB());
        }
        return c;
    }

    private Color predictColor(Color c) {
        Map<Integer, Double> adjacent = markovmap.get(c);
        double rand = Math.random();
        double sum = 0;
        for (Integer output : adjacent.keySet()) {
            sum += adjacent.get(output);
            if (sum > rand) {
                return new Color(output);
            }
        }
        return null;
    }

    private void loadFromImage(File file) throws IOException {
        input = ImageIO.read(file);
        pix = new Color[input.getWidth()][input.getHeight()];
        colorsSet = new HashSet<>();
        for (int i = 0; i < input.getWidth(); i++) {
            for (int j = 0; j < input.getHeight(); j++) {
                Color c = new Color(input.getRGB(i, j));
                colorsSet.add(c);
                pix[i][j] = c;
            }
        }
    }

    private void calculatePercentages() {
        countUpOccurences();
        for (Color col : markovmap.keySet()) {
            double sum = rowsum.get(col);
            for (Integer adj : markovmap.get(col).keySet()) {
                double weight = markovmap.get(col).get(adj) / sum;
                markovmap.get(col).put(adj, weight);
            }
        }
    }


    private void countUpOccurences() {
        for (Color current : colorsSet) {
            //for each unique color in the file, make a map of adjacent colors
            Map<Integer, Double> adjacent = new TreeMap<>();
            double sum = 0;
            for (int i = 0; i < pix.length; i++) {
                for (int j = 0; j < pix[0].length; j++) {
                    Color currcolor = pix[i][j];
                    if (currcolor.equals(current)) {
                        for (int r = -1; r <= 1; r++) {
                            for (int c = -1; c <= 1; c++) {
                                if (inBounds(i + r, j + c)) {
                                    int next = pix[i + r][j + c].getRGB();
                                    Double count = adjacent.get(next);
                                    if (count != null) {
                                        adjacent.put(next, count + 1);
                                    } else {
                                        adjacent.put(next, 1.0);
                                    }
                                    sum++;
                                }
                            }
                        }
                    }
                }
            }
            //put the sum of each "row" in the map with the zero length space as its key
            rowsum.put(current, sum);
            markovmap.put(current, adjacent);
        }
    }

    private boolean inBounds(int i, int j) {
        return i >= 0 && i < pix.length && j >= 0 && j < pix[0].length;
    }

}
