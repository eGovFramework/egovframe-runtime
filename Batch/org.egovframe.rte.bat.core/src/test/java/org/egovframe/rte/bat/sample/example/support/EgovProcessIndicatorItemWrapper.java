package org.egovframe.rte.bat.sample.example.support;

/**
 * 샘플을 수행하기 위해 필요한 랩퍼
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012. 07.25  배치실행개발팀     최초 생성
 *  </pre>
 * @since 2012. 07.25
 */
public class EgovProcessIndicatorItemWrapper<T> {

    private final long id;

    private final T item;

    /**
     * EgovProcessIndicatorItemWrapper 생성자
     * id와 item을 설정한다.
     *
     * @param id
     * @param item
     */
    public EgovProcessIndicatorItemWrapper(long id, T item) {
        this.id = id;
        this.item = item;
    }

    /**
     * id를 가져온다
     */
    public long getId() {
        return id;
    }

    /**
     * item을 가져온다
     */
    public T getItem() {
        return item;
    }

}
