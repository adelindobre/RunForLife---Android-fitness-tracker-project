package utils;

/**
 * Created by AdelinGDobre on 6/7/2017.
 */
import android.graphics.Color;

public class LineColors {
    private static int[] colors = {
            Color.BLUE,
            Color.GREEN,
            Color.RED,
            Color.YELLOW,
            Color.BLACK,
            Color.CYAN,
            Color.GRAY,
            Color.MAGENTA,
            Color.LTGRAY,
            Color.DKGRAY,
    };

    public static int getColor(int index){
        return colors[index % colors.length];
    }
}
