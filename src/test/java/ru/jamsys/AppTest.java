package ru.jamsys;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import ru.jamsys.component.Security;
import ru.jamsys.component.VirtualFileSystem;
import ru.jamsys.http.HttpClient;
import ru.jamsys.http.HttpClientNewImpl;
import ru.jamsys.virtual.file.system.File;
import ru.jamsys.virtual.file.system.FileLoaderFactory;
import ru.jamsys.virtual.file.system.view.FileViewKeyStore;

public class AppTest {

    @BeforeAll
    static void beforeAll() {
        String[] args = new String[]{};
        App.context = SpringApplication.run(App.class, args);
    }

    @Test
    public void appleSendNotification() throws Exception {
        Security security = App.context.getBean(Security.class);
        security.init("12345".toCharArray());

        VirtualFileSystem virtualFileSystem = App.context.getBean(VirtualFileSystem.class);
        virtualFileSystem.add(new File("/apple.p12", FileLoaderFactory.fromFileSystem("security/myTodo.notification.development.p12")));
        File appleCert = virtualFileSystem.get("/apple.p12");

        HttpClient httpClientNew = new HttpClientNewImpl();
        httpClientNew.setUrl("https://api.sandbox.push.apple.com/3/device/e81156eeb16246fd0498c53f55f870dfc5892806dde0a6e073cbf586e761382c");
        httpClientNew.setPostData("""
                {
                    "aps": {
                         "alert": "You got the new message"
                    },
                   "message" : "{\\"x1\\":1}"
                }
                """.getBytes());

        httpClientNew.setRequestHeader("apns-push-type", "alert");
        httpClientNew.setRequestHeader("apns-expiration", "0");
        httpClientNew.setRequestHeader("apns-priority", "10");
        httpClientNew.setRequestHeader("apns-topic", "ru.jamsys.myTodo");

        httpClientNew.setKeyStore(
                appleCert,
                FileViewKeyStore.prop.SECURITY_KEY.name(), "applePasswordKeyStore",
                FileViewKeyStore.prop.TYPE.name(), "PKCS12"
        );

        httpClientNew.exec();
        System.out.println(httpClientNew);
    }
}
