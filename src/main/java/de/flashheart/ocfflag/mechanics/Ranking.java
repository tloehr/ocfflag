package de.flashheart.ocfflag.mechanics;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Ranking {
    private Integer score;
    private Integer rank;

    public Ranking(Integer score, Integer rank) {
        this.score = score;
        this.rank = rank;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getScore() {

        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("score", score).
                append("rank", rank).
                toString();
    }
}