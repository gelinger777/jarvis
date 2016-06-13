package eventstore.tools.internal.samples;

import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class GlacierUploadArchive {

  public static String vaultName = "event-store";
  public static String archiveToUpload = "/Users/vach/workspace/jarvis/turtlerules.pdf";

  public static AmazonGlacierClient client;

  public static void main(String[] args) throws IOException {
    SystemPropertiesCredentialsProvider credProvider = new SystemPropertiesCredentialsProvider();

    client = new AmazonGlacierClient(credProvider);
    client.setEndpoint("https://glacier.us-west-2.amazonaws.com/");

    try {
      ArchiveTransferManager atm = new ArchiveTransferManager(client, credProvider);

      UploadResult result = atm.upload(vaultName, "my archive " + (new Date()), new File(archiveToUpload));
      System.out.println("Archive ID: " + result.getArchiveId());

    } catch (Exception e) {
      System.err.println(e);
    }
  }
}