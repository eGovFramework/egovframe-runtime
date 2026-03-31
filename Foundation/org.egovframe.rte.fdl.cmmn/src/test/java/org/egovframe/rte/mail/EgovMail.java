package org.egovframe.rte.mail;

/**
 * EgovMail
 * <p>
 * 메일발송을 위한 메소드 정의(인터페이스-샘플용)
 *
 * @author Judd Cho (horanghi@gmail.com)
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.05.30  Judd Cho        최초 생성
 *
 * </pre>
 * @since 2009.06.01
 */
public abstract class EgovMail {

    private String host;
    private int port;
    private String username;
    private String password;
    private String[] receivers;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getReceivers() {
        return receivers;
    }

    public void setReceivers(String[] receivers) {
        this.receivers = receivers;
    }

}
