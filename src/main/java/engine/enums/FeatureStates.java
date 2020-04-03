package engine.enums;

/**
 * @author 阿桓
 * Date: 2020/3/20
 * Time: 11:49 上午
 * Description:
 */
public enum FeatureStates {

    /**
     * 初始状态
     */
    INIT,
    /**
     * 计算状态
     */
    PROCESSING,
    /**
     * 成功
     */
    SUCCESS,
    /**
     * 失败
     */
    FAILED;

    public boolean isEndStates(){
        return this.equals(FAILED) || this.equals(SUCCESS);
    }

}
