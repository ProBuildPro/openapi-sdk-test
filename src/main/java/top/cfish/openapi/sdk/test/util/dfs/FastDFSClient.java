package top.cfish.openapi.sdk.test.util.dfs;

import lombok.extern.slf4j.Slf4j;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * @author: isisiwish
 * @date: 2019/6/16
 * @time: 20:12
 */
@Slf4j
public class FastDFSClient
{
    static
    {
        try
        {
            URL url = Thread.currentThread().getContextClassLoader().getResource("");
            String filePath = url.getPath() + "config.properties";
            ClientGlobal.init(filePath);
        }
        catch (Exception e)
        {
            log.error("FastDFS Client Init Fail", e);
        }
    }
    
    public static String[] upload(FastDFSFile file)
    {
        log.info("File Name: " + file.getName() + "File Length:" + file.getContent().length);
        
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author", file.getAuthor());
        
        long startTime = System.currentTimeMillis();
        String[] uploadResults = null;
        StorageClient storageClient = null;
        try
        {
            storageClient = getStorageClient();
            uploadResults = storageClient.upload_file(file.getContent(), file.getExt(), meta_list);
        }
        catch (IOException e)
        {
            log.error("IO Exception when uploadind the file:" + file.getName(), e);
        }
        catch (Exception e)
        {
            log.error("Non IO Exception when uploadind the file:" + file.getName(), e);
        }
        log.info("upload_file time used:" + (System.currentTimeMillis() - startTime) + " ms");
        
        if (uploadResults == null && storageClient != null)
        {
            log.error("upload file fail, error code:" + storageClient.getErrorCode());
        }
        log.info("upload file successfully!!!" + "group_name:" + uploadResults[0] + ", remoteFileName:" + " " + uploadResults[1]);
        return uploadResults;
    }
    
    public static FileInfo getFile(String groupName, String remoteFileName)
    {
        try
        {
            StorageClient storageClient = getStorageClient();
            return storageClient.get_file_info(groupName, remoteFileName);
        }
        catch (IOException e)
        {
            log.error("IO Exception: Get File from Fast DFS failed", e);
        }
        catch (Exception e)
        {
            log.error("Non IO Exception: Get File from Fast DFS failed", e);
        }
        return null;
    }
    
    public static InputStream downFile(String groupName, String remoteFileName)
    {
        try
        {
            StorageClient storageClient = getStorageClient();
            byte[] fileByte = storageClient.download_file(groupName, remoteFileName);
            InputStream ins = new ByteArrayInputStream(fileByte);
            return ins;
        }
        catch (IOException e)
        {
            log.error("IO Exception: Get File from Fast DFS failed", e);
        }
        catch (Exception e)
        {
            log.error("Non IO Exception: Get File from Fast DFS failed", e);
        }
        return null;
    }
    
    public static void deleteFile(String groupName, String remoteFileName) throws Exception
    {
        StorageClient storageClient = getStorageClient();
        int i = storageClient.delete_file(groupName, remoteFileName);
        log.info("delete file successfully!!!" + i);
    }
    
    public static StorageServer[] getStoreStorages(String groupName) throws IOException
    {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getStoreStorages(trackerServer, groupName);
    }
    
    public static ServerInfo[] getFetchStorages(String groupName, String remoteFileName) throws IOException
    {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
    }
    
    public static String getTrackerUrl() throws IOException
    {
        return "http://" + getTrackerServer().getInetSocketAddress().getHostString() + ":" + ClientGlobal.getG_tracker_http_port() + "/";
    }
    
    private static StorageClient getStorageClient() throws IOException
    {
        TrackerServer trackerServer = getTrackerServer();
        StorageClient storageClient = new StorageClient(trackerServer, null);
        return storageClient;
    }
    
    private static TrackerServer getTrackerServer() throws IOException
    {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerServer;
    }
}
