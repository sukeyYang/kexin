package com.web.dao;


import com.common.BaseDao;
import com.web.entity.RecordInfo;
import com.web.model.ReportProductVO;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by gaoyang on 16/2/29.
 */
@Repository
public class RecordInfoDao extends BaseDao {

    /**
     * 查询
     *
     * @param map
     * @return
     */
    public List<Map> searchRECORD_INFO(Map map) {
        StringBuffer sql = new StringBuffer();
        sql.append("select t.com_name,t.user_type,t.address from RECORD_INFO t where 1=1");

        if(map.containsKey("name") && !"".equals(map.get("name"))){
            sql.append(" and t.name like '%").append(map.get("name")).append("%'");
        }
        List<Map> list = super.findResult(sql.toString(),map);
        return list;
    }


    /**
     * 查询
     *
     * @param map
     * @return
     */
    public List<Map> searchReportProduct(Map map) {
        StringBuffer sql = new StringBuffer();
        sql.append("select t.title,t.status,to_char(t.create_time,'yyyy-MM-dd HH24:mm:ss') as create_time from report_product t where 1=1 ");
        List<Map> list = super.findResult(sql.toString(),map);
        return list;
    }
}