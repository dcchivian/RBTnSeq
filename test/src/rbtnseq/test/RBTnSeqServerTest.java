package rbtnseq.test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;

import org.ini4j.Ini;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import rbtnseq.*;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.JsonServerSyslog;
import us.kbase.common.service.UObject;
import us.kbase.workspace.CreateWorkspaceParams;
import us.kbase.workspace.ObjectSaveData;
import us.kbase.workspace.SaveObjectsParams;
import us.kbase.workspace.WorkspaceClient;
import us.kbase.workspace.WorkspaceIdentity;

public class RBTnSeqServerTest {
    private static AuthToken token = null;
    private static Map<String, String> config = null;
    private static WorkspaceClient wsClient = null;
    private static String wsName = null;
    private static RBTnSeqServer impl = null;
    
    @BeforeClass
    public static void init() throws Exception {
        token = new AuthToken(System.getenv("KB_AUTH_TOKEN"));
        String configFilePath = System.getenv("KB_DEPLOYMENT_CONFIG");
        File deploy = new File(configFilePath);
        Ini ini = new Ini(deploy);
        config = ini.get("RBTnSeq");
        wsClient = new WorkspaceClient(new URL(config.get("workspace-url")), token);
        wsClient.setAuthAllowedForHttp(true);
        // These lines are necessary because we don't want to start linux syslog bridge service
        JsonServerSyslog.setStaticUseSyslog(false);
        JsonServerSyslog.setStaticMlogFile(new File(config.get("scratch"), "test.log").getAbsolutePath());
        impl = new RBTnSeqServer();
    }
    
    private static String getWsName() throws Exception {
        if (wsName == null) {
            long suffix = System.currentTimeMillis();
            wsName = "test_RBTnSeq_" + suffix;
            wsClient.createWorkspace(new CreateWorkspaceParams().withWorkspace(wsName));
        }
        return wsName;
    }
    
    @AfterClass
    public static void cleanup() {
        if (wsName != null) {
            try {
                wsClient.deleteWorkspace(new WorkspaceIdentity().withWorkspace(wsName));
                System.out.println("Test workspace was deleted");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Test
    public void testTnSeq() throws Exception {
	TnSeqInput input = new TnSeqInput();
        String rv = impl.runTnSeq(input, token);
        Assert.assertEquals("method worked!", rv);
    }

    /*    
    @Test
    public void testCountContigs() throws Exception {
        String objName = "contigset.1";
        Map<String, Object> contig = new LinkedHashMap<String, Object>();
        contig.put("id", "1");
        contig.put("length", 10);
        contig.put("md5", "md5");
        contig.put("sequence", "agcttttcat");
        Map<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("contigs", Arrays.asList(contig));
        obj.put("id", "id");
        obj.put("md5", "md5");
        obj.put("name", "name");
        obj.put("source", "source");
        obj.put("source_id", "source_id");
        obj.put("type", "type");
        wsClient.saveObjects(new SaveObjectsParams().withWorkspace(getWsName()).withObjects(Arrays.asList(
                new ObjectSaveData().withType("KBaseGenomes.ContigSet").withName(objName).withData(new UObject(obj)))));
        CountContigsResults ret = impl.countContigs(getWsName(), objName, token);
        Assert.assertEquals(1L, (long)ret.getContigCount());
    }
    */
}