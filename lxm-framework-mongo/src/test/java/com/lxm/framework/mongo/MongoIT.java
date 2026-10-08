package com.lxm.framework.mongo;
import com.lxm.framework.mongo.core.*;
import com.lxm.framework.mongo.entity.MongoBaseEntity;
import com.lxm.framework.common.page.StandardPage;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import lombok.Getter;
import lombok.Setter;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class MongoIT {
 @Getter @Setter public static class Item extends MongoBaseEntity { private String name;private int rank; }
 static class Page implements StandardPage<Item> {
  long current=1,size=1,total;List<Item> records;
  public void putRecords(List<Item> r){records=r;}public void putTotal(long t){total=t;}public void putSize(long s){size=s;}public void putCurrent(long c){current=c;}
  public long getCurrent(){return current;}public long getSize(){return size;}public String[] getAscs(){return new String[]{"rank"};}public String[] getDescs(){return new String[0];}
 }
 @Test void crudPagingRangeAndLiteralLikeAreCorrect() {
  String database="lfp_test_"+UUID.randomUUID().toString().replace("-","");
  try(var context=new AnnotationConfigApplicationContext()) {
   context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test",Map.of("lfp.mongo.enabled","true","spring.data.mongodb.uri",System.getProperty("lfp.mongo.uri","mongodb://127.0.0.1:17017"),"spring.data.mongodb.database",database)));
   context.register(MongoConfig.class);context.refresh();var service=context.getBean(MongoService.class);var template=context.getBean(MongoTemplate.class);
   try {
    var a=new Item();a.setName("literal .* 中文");a.setRank(1);var b=new Item();b.setName("another");b.setRank(2);service.saveBatch(List.of(a,b),Item.class);
    assertNotNull(a.getId());assertEquals(a.getName(),service.getById(a.getId(),Item.class).getName());
    assertEquals(1,service.getList(new MongoQuery().like("name",".*"),Item.class).size());
    var query=new MongoQuery().between("rank",1,2);var page=new Page();service.getPage(page,query,Item.class);assertEquals(2,page.total);assertEquals(1,page.records.size());
    page.current=2;service.getPage(page,query,Item.class);assertEquals(2,page.total);assertEquals(2,page.records.getFirst().getRank());assertEquals(0,query.getQuery().getSkip());
    assertEquals(1,service.edit(new MongoQuery().eq("rank",1),new Update().set("name","updated"),Item.class).getModifiedCount());assertEquals(2,service.remove(new MongoQuery().between("rank",1,2),Item.class).getDeletedCount());
   } finally {template.getDb().drop();}
  }
 }
}
