import java.util.List;

public class ResultObj {

    private String log_id;
    private List<WordsResult> words_result;
    private int words_result_num;
    private int direction;

    public String getLog_id() {
        return log_id;
    }

    public void setLog_id(String log_id) {
        this.log_id = log_id;
    }

    public List<WordsResult> getWords_result() {
        return words_result;
    }

    public void setWords_result(List<WordsResult> words_result) {
        this.words_result = words_result;
    }

    public int getWords_result_num() {
        return words_result_num;
    }

    public void setWords_result_num(int words_result_num) {
        this.words_result_num = words_result_num;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public ResultObj(String log_id, List<WordsResult> words_result, int words_result_num, int direction) {
        this.log_id = log_id;
        this.words_result = words_result;
        this.words_result_num = words_result_num;
        this.direction = direction;
    }
}
