package org.egovframe.rte.fdl.filehandling.config;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileProvider;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;
import org.apache.commons.vfs2.provider.zip.ZipFileProvider;

public class VfsFileSystemManagerFactory {

    public static FileSystemManager createFileSystemManager() throws FileSystemException {
        DefaultFileSystemManager manager = new DefaultFileSystemManager();

        // 캐시 전략 설정 (XML에서 설정되지 않았지만 일반적으로 사용)
        manager.setCacheStrategy(CacheStrategy.ON_RESOLVE);

        // 프로바이더 등록 (XML의 <provider>와 <scheme name> 대응)
        manager.addProvider("zip", new ZipFileProvider());
        manager.addProvider("ftp", new FtpFileProvider());
        manager.addProvider("file", new DefaultLocalFileProvider());
        manager.addProvider("http", new UrlFileProvider());
        manager.addProvider("https", new UrlFileProvider());

        // 기본 프로바이더 설정 (XML의 <default-provider>)
        manager.setDefaultProvider(new UrlFileProvider());

        // 초기화
        manager.init();
        return manager;
    }
}
