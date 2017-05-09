package com.myntra.memorygame.utils;

import java.util.List;
import java.util.Random;

public class Utility {

    public static int getRandomWithExclusion(Random rnd, int start, int end, List<Integer> exclusionList) {
        int random = start + rnd.nextInt(end - start + 1 - exclusionList.size());
        for (int ex : exclusionList) {
            if (random < ex) {
                break;
            }
            random++;
        }
        return random;
    }

    public static int getRandomNumber(int start, int end) {
        Random r = new Random();
        return r.nextInt(end-start) + start;
    }

}
