package eventstore.tools.internal.samples;

import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;

import java.io.File;
import java.io.IOException;

public class GlacierDownloadArchive {
    public static String vaultName = "event-store";
    public static String archiveId = "Hma-C6bYB7OHpYLztliD47UHVhreAbiemBIrLe6AGI8h8ulUxX7Lw9yBNIAZoaDnhqLFFPag4IDRS1tHjbSnnvvxxqRdW1GNueDaSDdFkRGAtQmsOsnjJ3NkFF7EWWkGKz3B_bmUFg";
    public static String downloadFilePath  = "/Users/vach/workspace/jarvis/dist/temp";
    
    public static AmazonGlacierClient glacierClient;
    public static AmazonSQSClient sqsClient;
    public static AmazonSNSClient snsClient;
    
    public static void main(String[] args) throws IOException {
        SystemPropertiesCredentialsProvider credentials = new SystemPropertiesCredentialsProvider();
    	
        glacierClient = new AmazonGlacierClient(credentials);        
        sqsClient = new AmazonSQSClient(credentials);
        snsClient = new AmazonSNSClient(credentials);
        
        glacierClient.setEndpoint("glacier.us-west-2.amazonaws.com");
        sqsClient.setEndpoint("sqs.us-west-2.amazonaws.com");
        snsClient.setEndpoint("sns.us-west-2.amazonaws.com");

        try {
            ArchiveTransferManager atm = new ArchiveTransferManager(glacierClient, sqsClient, snsClient);
            
            atm.download(vaultName, archiveId, new File(downloadFilePath));
            
        } catch (Exception e)
        {
            System.err.println(e);
        }
    }
}