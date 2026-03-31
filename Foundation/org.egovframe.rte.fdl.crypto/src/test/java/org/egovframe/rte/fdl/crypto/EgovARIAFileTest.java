package org.egovframe.rte.fdl.crypto;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.crypto.config.EgovCryptoTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EgovCryptoTestConfig.class)
public class EgovARIAFileTest {

    @Resource(name = "egov.ariaCryptoService")
    private EgovCryptoService cryptoService;

    /**
     * properties 설정 파일의 algorithmKey 값을 주입받는 password
     */
    @Resource(name = "password")
    private String password;

    @Test
    public void EgovARIAFilVerify() throws Exception {
        String sPath = Objects.requireNonNull(this.getClass().getResource("/META-INF/file")).getFile();
        cryptoService.encrypt(new File(sPath + "/test.txt"), password, new File(sPath + "/test2.txt"));
        cryptoService.decrypt(new File(sPath + "/test2.txt"), password, new File(sPath + "/test3.txt"));

        List<String> list = new ArrayList<String>();
        List<String> list3 = new ArrayList<String>();

        //파일 객체 생성
        File file = new File(sPath + "/test.txt");
        File file3 = new File(sPath + "/test3.txt");

        //입력 스트림 생성
        FileReader filereader = new FileReader(file);
        FileReader filereader3 = new FileReader(file3);

        //입력 버퍼 생성
        BufferedReader br = new BufferedReader(filereader);
        BufferedReader br3 = new BufferedReader(filereader3);

        String line = "";

        while ((line = br.readLine()) != null) {
            list.add(line);
        }

        while ((line = br3.readLine()) != null) {
            list3.add(line);
        }

        br.close();
        br3.close();

        assertEquals(list, list3);
    }

}
