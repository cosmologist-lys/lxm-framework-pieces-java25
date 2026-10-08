package com.lxm.framework.mongo.entity;

import com.mongodb.client.result.UpdateResult;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @Author: Lys
 * @Date 2022/6/17
 * @Describe
 **/
public class MongoBaseEntity extends MongoRootEntity {
    private static final long serialVersionUID = -1403104500143051687L;

    @Getter
    @Setter
    @MongoId(FieldType.OBJECT_ID)
    private ObjectId id;

    @Getter
    @Field(value = "updated_at", targetType = FieldType.DATE_TIME)
    private LocalDateTime updatedAt;

    @Getter
    @Field(value = "created_at", targetType = FieldType.DATE_TIME)
    private LocalDateTime createdAt;

    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setCreatedAt() {
        if (Objects.isNull(this.createdAt)) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @Override
    public void insert() {
        setUpdatedAt();
        setCreatedAt();
        super.insert();
    }
}
