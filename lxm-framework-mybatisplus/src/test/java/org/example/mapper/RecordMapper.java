package org.example.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.MySqlIT.TenantRecord;
@Mapper public interface RecordMapper extends BaseMapper<TenantRecord> { }
