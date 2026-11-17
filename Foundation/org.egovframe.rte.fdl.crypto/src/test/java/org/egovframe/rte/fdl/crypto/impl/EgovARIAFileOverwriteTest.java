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
 * <p>append 기록 시 기존 파일에 Base64 가 누적되어 복호화가 이전 암호문을 복원 (새 암호문 유실).
 * decrypt(File, String, File)와 동일하게 덮어쓰기.</p>
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
