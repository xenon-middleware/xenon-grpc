package nl.esciencecenter.xenon.grpc;

import static nl.esciencecenter.xenon.grpc.MapUtils.empty;
import static nl.esciencecenter.xenon.grpc.MapUtils.mapCredential;
import static nl.esciencecenter.xenon.grpc.MapUtils.mapPropertyDescriptions;
import static nl.esciencecenter.xenon.grpc.MapUtils.usernameOfCredential;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import nl.esciencecenter.xenon.credentials.CredentialMap;
import nl.esciencecenter.xenon.credentials.UserCredential;
import org.junit.Test;

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.credentials.CertificateCredential;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;

public class MapUtilsTest {

    @Test
    public void mapPropertyDescriptions_booltype() throws Exception {
        XenonPropertyDescription[] input = new XenonPropertyDescription[] {
            new XenonPropertyDescription("abool", XenonPropertyDescription.Type.BOOLEAN, "false", "abool desc")
        };

        List<XenonProto.PropertyDescription> result = mapPropertyDescriptions(input);

        List<XenonProto.PropertyDescription> expected = Collections.singletonList(
                XenonProto.PropertyDescription.newBuilder()
                        .setName("abool")
                        .setType(XenonProto.PropertyDescription.Type.BOOLEAN)
                        .setDefaultValue("false")
                        .setDescription("abool desc")
                        .build()
        );
        assertEquals(expected, result);
    }

    @Test
    public void test_empty() throws Exception {
        XenonProto.Empty message = empty();

        XenonProto.Empty expected = XenonProto.Empty.getDefaultInstance();
        assertEquals(expected, message);
    }

    @Test
    public void mapCredential_fileSystem_default() throws Exception {
        XenonProto.CreateSchedulerRequest request = XenonProto.CreateSchedulerRequest.newBuilder()
                .setAdaptor("file")
                .build();

        Credential result = mapCredential(request);

        DefaultCredential expected = new DefaultCredential();
        assertEquals(expected, result);
    }

    @Test
    public void mapCredential_scheduler_username() throws Exception {
        XenonProto.CreateSchedulerRequest request = XenonProto.CreateSchedulerRequest.newBuilder()
                .setAdaptor("file")
                .setDefaultCredential(XenonProto.DefaultCredential.newBuilder().setUsername("someone"))
                .build();

        Credential result = mapCredential(request);

        Credential expected = new DefaultCredential("someone");
        assertEquals(expected, result);
    }

    @Test
    public void mapCredential_scheduler_usernamePassword() throws Exception {
        XenonProto.CreateSchedulerRequest request = XenonProto.CreateSchedulerRequest.newBuilder()
                .setAdaptor("file")
                .setPasswordCredential(XenonProto.PasswordCredential.newBuilder().setUsername("someone").setPassword("mypassword"))
                .build();

        Credential result = mapCredential(request);

        Credential expected = new PasswordCredential("someone", "mypassword".toCharArray());
        assertEquals(expected, result);
    }

    @Test
    public void mapCredential_scheduler_certificate() throws Exception {
        XenonProto.CreateSchedulerRequest request = XenonProto.CreateSchedulerRequest.newBuilder()
                .setAdaptor("file")
                .setCertificateCredential(XenonProto.CertificateCredential.newBuilder()
                        .setUsername("someone")
                        .setCertfile("/home/someone/.ssh/id_rsa")
                        .setPassphrase("mypassphrase")
                )
                .build();

        Credential result = mapCredential(request);

        Credential expected = new CertificateCredential("someone","/home/someone/.ssh/id_rsa", "mypassphrase".toCharArray());
        assertEquals(expected, result);
    }

    @Test
    public void mapCredential_filesystem_default() throws Exception {
        XenonProto.CreateFileSystemRequest request = XenonProto.CreateFileSystemRequest.newBuilder()
                .setAdaptor("file")
                .build();

        Credential result = mapCredential(request);

        DefaultCredential expected = new DefaultCredential();
        assertEquals(expected, result);
    }

    @Test
    public void map_Credential_filesystem_map_minimal() {
        XenonProto.CreateFileSystemRequest request = XenonProto.CreateFileSystemRequest.newBuilder()
                .setAdaptor("file")
                .setCredentialMap(XenonProto.CredentialMap.newBuilder()
                        .build()
                )
                .build();

        Credential result = mapCredential(request);

        Credential expected = new CredentialMap();
        assertEquals(expected, result);
    }

    @Test
    public void map_Credential_filesystem_map_with_default_as_fallback() {
        XenonProto.DefaultCredential.Builder fallback = XenonProto.DefaultCredential.newBuilder();
        XenonProto.CreateSchedulerRequest request = XenonProto.CreateSchedulerRequest.newBuilder()
                .setAdaptor("file")
                .setCredentialMap(XenonProto.CredentialMap.newBuilder()
                        .setDefaultCredential(fallback)
                        .build()
                )
                .build();

        Credential result = mapCredential(request);

        Credential expected = new CredentialMap(new DefaultCredential());
        assertEquals(expected, result);
    }

    @Test
    public void map_Credential_filesystem_map_with_usernamedefault_as_fallback() {
        XenonProto.DefaultCredential.Builder fallback = XenonProto.DefaultCredential.newBuilder()
                .setUsername("someone");
        XenonProto.CreateSchedulerRequest request = XenonProto.CreateSchedulerRequest.newBuilder()
                .setAdaptor("file")
                .setCredentialMap(XenonProto.CredentialMap.newBuilder()
                        .setDefaultCredential(fallback)
                        .build()
                )
                .build();

        Credential result = mapCredential(request);

        Credential expected = new CredentialMap(new DefaultCredential("someone"));
        assertEquals(expected, result);
    }

    @Test
    public void map_Credential_filesystem_map_with_password_as_fallback() {
        XenonProto.PasswordCredential.Builder fallback = XenonProto.PasswordCredential.newBuilder()
                .setUsername("someone")
                .setPassword("mypassword");
        XenonProto.CreateSchedulerRequest request = XenonProto.CreateSchedulerRequest.newBuilder()
                .setAdaptor("file")
                .setCredentialMap(XenonProto.CredentialMap.newBuilder()
                        .setPasswordCredential(fallback)
                        .build()
                )
                .build();

        Credential result = mapCredential(request);

        Credential expected = new CredentialMap(new PasswordCredential("someone", "mypassword".toCharArray()));
        assertEquals(expected, result);
    }

    @Test
    public void map_Credential_filesystem_map_with_cert_as_fallback() {
        XenonProto.CertificateCredential.Builder fallback = XenonProto.CertificateCredential.newBuilder()
                .setUsername("someone")
                .setCertfile("/home/someone/.ssh/id_rsa")
                .setPassphrase("mypassphrase");
        XenonProto.CreateSchedulerRequest request = XenonProto.CreateSchedulerRequest.newBuilder()
                .setAdaptor("file")
                .setCredentialMap(XenonProto.CredentialMap.newBuilder()
                        .setCertificateCredential(fallback)
                        .build()
                )
                .build();

        Credential result = mapCredential(request);

        Credential expected = new CredentialMap(new CertificateCredential("someone", "/home/someone/.ssh/id_rsa", "mypassphrase".toCharArray()));
        assertEquals(expected, result);
    }

    @Test
    public void map_Credential_filesystem_map_with_default_entry() {
        XenonProto.DefaultCredential.Builder fallback = XenonProto.DefaultCredential.newBuilder()
                .setUsername("someone");
        XenonProto.UserCredential credEntry = XenonProto.UserCredential.newBuilder()
                .setDefaultCredential(
                        XenonProto.DefaultCredential.newBuilder().setUsername("someoneelse").build()
                ).build();
        XenonProto.CreateSchedulerRequest request = XenonProto.CreateSchedulerRequest.newBuilder()
                .setAdaptor("file")
                .setCredentialMap(XenonProto.CredentialMap.newBuilder()
                        .setDefaultCredential(fallback)
                        .putEntries("somehost", credEntry)
                        .build()
                )
                .build();

        Credential result = mapCredential(request);

        CredentialMap expected = new CredentialMap(new DefaultCredential("someone"));
        expected.put("somehost", new DefaultCredential("someoneelse"));
        assertEquals(expected, result);
    }

    @Test
    public void map_Credential_filesystem_map_with_userdefault_entry() {
        XenonProto.UserCredential credEntry = XenonProto.UserCredential.newBuilder()
                .setDefaultCredential(
                        XenonProto.DefaultCredential.newBuilder().setUsername("someone").build()
                ).build();
        XenonProto.CreateSchedulerRequest request = XenonProto.CreateSchedulerRequest.newBuilder()
                .setAdaptor("file")
                .setCredentialMap(XenonProto.CredentialMap.newBuilder()
                        .putEntries("somehost", credEntry)
                        .build()
                )
                .build();

        Credential result = mapCredential(request);

        CredentialMap expected = new CredentialMap();
        expected.put("somehost", new DefaultCredential("someone"));
        assertEquals(expected, result);
    }

    @Test
    public void map_Credential_filesystem_map_with_password_entry() {
        XenonProto.UserCredential credEntry = XenonProto.UserCredential.newBuilder()
                .setPasswordCredential(XenonProto.PasswordCredential.newBuilder()
                        .setUsername("someone")
                        .setPassword("mypassword")
                ).build();
        XenonProto.CreateSchedulerRequest request = XenonProto.CreateSchedulerRequest.newBuilder()
                .setAdaptor("file")
                .setCredentialMap(XenonProto.CredentialMap.newBuilder()
                        .putEntries("somehost", credEntry)
                        .build()
                )
                .build();

        Credential result = mapCredential(request);

        CredentialMap expected = new CredentialMap();
        expected.put("somehost", new PasswordCredential("someone", "mypassword".toCharArray()));
        assertEquals(expected, result);
    }

    @Test
    public void map_Credential_filesystem_map_with_cert_entry() {
        XenonProto.UserCredential credEntry = XenonProto.UserCredential.newBuilder()
                .setCertificateCredential(XenonProto.CertificateCredential.newBuilder()
                        .setUsername("someone")
                        .setCertfile("/home/someone/.ssh/id_rsa")
                        .setPassphrase("mypassphrase")
                ).build();
        XenonProto.CreateSchedulerRequest request = XenonProto.CreateSchedulerRequest.newBuilder()
                .setAdaptor("file")
                .setCredentialMap(XenonProto.CredentialMap.newBuilder()
                        .putEntries("somehost", credEntry)
                        .build()
                )
                .build();

        Credential result = mapCredential(request);

        CredentialMap expected = new CredentialMap();
        expected.put("somehost", new CertificateCredential("someone", "/home/someone/.ssh/id_rsa", "mypassphrase".toCharArray()));
        assertEquals(expected, result);
    }

    @Test
    public void usernameOfCredential_usercredential() {
        UserCredential cred = new DefaultCredential("myusername");

        String username = usernameOfCredential(cred);

        assertEquals("myusername", username);
    }

    @Test
    public void usernameOfCredential_nonusercredential() {
        Credential cred = new CredentialMap();

        String username = usernameOfCredential(cred);

        assertEquals("nousername", username);
    }
}