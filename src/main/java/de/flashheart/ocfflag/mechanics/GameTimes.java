package de.flashheart.ocfflag.mechanics;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;

public class GameTimes extends Pair<Color, Integer> {
    Color left;
    Integer right;

    public GameTimes(Color left, Integer right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Color getLeft() {
        return left;
    }

    @Override
    public Integer getRight() {
        return right;
    }

    @Override
    public Integer setValue(Integer value) {
        right = value;
        return right;
    }


    @Override
    public int compareTo(Pair<Color, Integer> other) {
        return Integer.compare(right, other.getRight());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("left", left).
                append("right", right).
                toString();
    }
}
