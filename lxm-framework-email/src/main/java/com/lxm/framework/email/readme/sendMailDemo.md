```java
        var list = List.of(new ExcelJson().setDate("2022-01-03").setRand(RandomStringUtils.randomAlphabetic(7)),
                new ExcelJson().setDate("2022-06-18").setRand(RandomStringUtils.randomAlphanumeric(9)));
        ExcelView view = new ExcelView("test-excel", ExcelJson.class, list);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        view.getWorkbook().write(bos);
        EasyEmail.config(LxmEmail.SMTP_QQ(), "327266750@qq.com", "poqhkfutorijbhba");
        EasyEmail.subject("标题栏")
                .from("发送自")
                .cc("petrichor1989@yeah.net")
                .text("文本内容")
                .attach(bos)
                .send();
```

```java
var e = EasyEmailRecipient.config(EmailCategoryRecipient.QQ, "327266750@qq.com", "poqhkfutorijbhba")
                .connect().readThenClose("ccsvc@message.cmbchina.com");
        System.out.println(e.getFromEmail());
        System.out.println(e.getFromName());
        System.out.println(e.getSubject());
        System.out.println(e.getReceivedAt());
        System.out.println(e.getContent());
```
