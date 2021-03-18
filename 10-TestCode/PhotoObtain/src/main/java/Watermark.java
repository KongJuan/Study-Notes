public class Watermark {

    private String registNo;

    public Watermark() {
    }

    public Watermark(String registNo) {
        this.registNo = registNo;
    }

    public String getRegistNo() {
        return registNo;
    }

    public void setRegistNo(String registNo) {
        this.registNo = registNo;
    }

    @Override
    public String toString() {
        return "WaterRemark{" +
                "registNo='" + registNo + '\'' +
                '}';
    }
}
