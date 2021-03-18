public class WordsResult {
    private Probability probability;
    private String words;
    private Locations location;

    public Probability getProbability() {
        return probability;
    }

    public void setProbability(Probability probability) {
        this.probability = probability;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public Locations getLocation() {
        return location;
    }

    public void setLocation(Locations location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "WordsResult{" +
                "probability=" + probability +
                ", words='" + words + '\'' +
                ", location=" + location +
                '}';
    }
}
