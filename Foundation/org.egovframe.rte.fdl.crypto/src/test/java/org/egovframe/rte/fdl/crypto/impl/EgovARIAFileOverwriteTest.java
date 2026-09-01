package org.egovframe.rte.fdl.crypto.impl;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.crypto.EgovCryptoService;
import org.egovframe.rte.fdl.crypto.config.EgovCryptoTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link EgovARIACryptoServiceImpl}의 encrypt(File, String, File)가 대상 파일을 덮어쓰는지 검증한다.
 *
 * <p>암호화 결과를 append로 기록하면 대상 파일이 이미 존재할 때 Base64 문자열이 누적된다.
 * 이 경우 복호화는 Base64 패딩 이후를 무시하므로 앞서 기록된 암호문이 복원되고, 새로 기록한
 * 암호문은 조용히 버려진다. decrypt(File, String, File)와 동일하게 덮어쓰도록 맞춘다.</p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EgovCryptoTestConfig.class)
public class EgovARIAFileOverwriteTest {

    @Resource(name = "egov.ariaCryptoService")
    private EgovCryptoService cryptoService;

    /**
     * properties 설정 파일의 algorithmKey 값을 주입받는 password
     */
    @Resource(name = "password")
    private String password;

    @TempDir
    private Path tempDir;

    @Test
    public void encryptShouldOverwriteExistingTargetFile() throws Exception {
        File firstFile = writeTempFile("first.txt", "first content");
        File secondFile = writeTempFile("second.txt", "second content");
        File encryptedFile = tempDir.resolve("encrypted.txt").toFile();
        File decryptedFile = tempDir.resolve("decrypted.txt").toFile();

        cryptoService.encrypt(firstFile, password, encryptedFile);
        cryptoService.encrypt(secondFile, password, encryptedFile);
        cryptoService.decrypt(encryptedFile, password, decryptedFile);

        assertArrayEquals(Files.readAllBytes(secondFile.toPath()), Files.readAllBytes(decryptedFile.toPath()));
    }

    @Test
    public void encryptShouldNotAccumulateTargetFileSize() throws Exception {
        File sourceFile = writeTempFile("source.txt", "first content");
        File encryptedFile = tempDir.resolve("encrypted.txt").toFile();

        cryptoService.encrypt(sourceFile, password, encryptedFile);
        long firstEncryptedLength = encryptedFile.length();
        cryptoService.encrypt(sourceFile, password, encryptedFile);

        assertEquals(firstEncryptedLength, encryptedFile.length());
    }

    @Test
    public void encryptAndDecryptShouldRoundTripFileContent() throws Exception {
        File sourceFile = writeTempFile("source.txt", "first content");
        File encryptedFile = tempDir.resolve("encrypted.txt").toFile();
        File decryptedFile = tempDir.resolve("decrypted.txt").toFile();

        cryptoService.encrypt(sourceFile, password, encryptedFile);
        cryptoService.decrypt(encryptedFile, password, decryptedFile);

        assertArrayEquals(Files.readAllBytes(sourceFile.toPath()), Files.readAllBytes(decryptedFile.toPath()));
    }

    private File writeTempFile(String fileName, String content) throws Exception {
        Path path = tempDir.resolve(fileName);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path.toFile();
    }

}
