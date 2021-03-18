public class Probability {
    private double average;
    private double min;
    private double variance;

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    @Override
    public String toString() {
        return "Probability{" +
                "average=" + average +
                ", min=" + min +
                ", variance=" + variance +
                '}';
    }
}
